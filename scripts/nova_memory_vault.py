#!/usr/bin/env python3
"""Command-line interface for Nova Memory Vault."""

from __future__ import annotations

import argparse
import getpass
import hashlib
import json
import mimetypes
import sys
from pathlib import Path

from backend.nova_vault import (
    DPAPIKeyStore,
    NovaMemoryVault,
    RecoveryError,
    VaultError,
    atomic_write,
    create_recovery_bundle,
    recover_master_key,
)


def _read_passphrase(confirm: bool = False) -> str:
    value = getpass.getpass("Recovery passphrase: ")
    if confirm:
        second = getpass.getpass("Confirm recovery passphrase: ")
        if value != second:
            raise VaultError("Passphrases did not match")
    if len(value) < 12:
        raise VaultError("Use a recovery passphrase of at least 12 characters")
    return value


def cmd_init(args: argparse.Namespace) -> int:
    store = DPAPIKeyStore(Path(args.keystore) if args.keystore else None)
    key = store.initialize(overwrite=args.overwrite)
    print(f"Local DPAPI key initialized: {store.path}")

    if args.recovery_bundle:
        passphrase = _read_passphrase(confirm=True)
        bundle = create_recovery_bundle(key, passphrase)
        path = Path(args.recovery_bundle)
        atomic_write(path, bundle, private=True)
        print(f"Recovery bundle written: {path}")
        print("Keep the recovery passphrase separate from the bundle.")
    else:
        print("WARNING: no recovery bundle was created.")
    return 0


def cmd_status(args: argparse.Namespace) -> int:
    store = DPAPIKeyStore(Path(args.keystore) if args.keystore else None)
    result = {
        "keystore": str(store.path),
        "platform_supported": store.is_supported,
        "initialized": store.is_initialized,
        "cipher": "AES-256-GCM",
        "recovery": "passphrase-wrapped bundle supported",
    }
    print(json.dumps(result, indent=2))
    return 0 if store.is_initialized else 2


def _vault(args: argparse.Namespace) -> NovaMemoryVault:
    store = DPAPIKeyStore(Path(args.keystore) if args.keystore else None)
    return NovaMemoryVault(store.load_key())


def cmd_seal(args: argparse.Namespace) -> int:
    vault = _vault(args)
    source = Path(args.source)
    if args.destination:
        destination = Path(args.destination)
    elif args.opaque_name:
        destination = source.with_name(vault.opaque_name(str(source)))
    else:
        destination = Path(str(source) + ".nvm")
    content_type = mimetypes.guess_type(source.name)[0] or "application/octet-stream"
    vault.seal_file(
        source,
        destination,
        logical_name=args.logical_name or str(source),
        content_type=content_type,
    )
    opened = vault.verify_file(destination)
    print(f"SEALED+VERIFIED {source} -> {destination} sha256={opened.sha256}")
    return 0


def cmd_open(args: argparse.Namespace) -> int:
    vault = _vault(args)
    opened = vault.open_file(Path(args.source), Path(args.destination))
    print(
        f"OPENED {args.source} -> {args.destination} "
        f"logical_name={opened.logical_name!r} sha256={opened.sha256}"
    )
    return 0


def cmd_verify(args: argparse.Namespace) -> int:
    vault = _vault(args)
    opened = vault.verify_file(Path(args.source))
    print(
        json.dumps(
            {
                "verified": True,
                "logical_name": opened.logical_name,
                "content_type": opened.content_type,
                "sha256": opened.sha256,
            },
            indent=2,
        )
    )
    return 0


def cmd_recover(args: argparse.Namespace) -> int:
    store = DPAPIKeyStore(Path(args.keystore) if args.keystore else None)
    passphrase = _read_passphrase(confirm=False)
    key = recover_master_key(Path(args.recovery_bundle).read_bytes(), passphrase)
    store.save_key(key, overwrite=args.overwrite)
    print(f"Recovered local DPAPI-protected key: {store.path}")
    return 0


def _collect_sources(values: list[str]) -> list[Path]:
    out: list[Path] = []
    for value in values:
        p = Path(value)
        if p.is_dir():
            out.extend(x for x in p.rglob("*") if x.is_file())
        elif p.is_file():
            out.append(p)
        else:
            raise VaultError(f"Source does not exist: {p}")
    return sorted(set(out))


def cmd_migrate(args: argparse.Namespace) -> int:
    vault = _vault(args)
    output = Path(args.output)
    sources = _collect_sources(args.source)
    plan = []

    for src in sources:
        logical_name = str(src)
        dst_name = vault.opaque_name(logical_name) if args.opaque_names else src.name + ".nvm"
        dst = output / dst_name
        plan.append((src, dst, logical_name))

    print(f"Migration plan: {len(plan)} file(s)")
    for src, dst, _ in plan:
        print(f"  {src} -> {dst}")

    if not args.apply:
        print("DRY RUN ONLY. Re-run with --apply to create encrypted copies.")
        return 0

    verified = []
    for src, dst, logical_name in plan:
        content_type = mimetypes.guess_type(src.name)[0] or "application/octet-stream"
        vault.seal_file(src, dst, logical_name=logical_name, content_type=content_type)
        opened = vault.verify_file(dst)
        source_hash = hashlib.sha256(src.read_bytes()).hexdigest()
        if opened.sha256 != source_hash:
            raise VaultError(f"Verification failed for {src}")
        verified.append((src, dst, opened.sha256))
        print(f"  VERIFIED {src} sha256={opened.sha256}")

    # The mapping itself can reveal filenames, so store it encrypted.
    manifest = {
        "files": [
            {"source": str(src), "vault_file": dst.name, "sha256": sha}
            for src, dst, sha in verified
        ]
    }
    manifest_blob = vault.seal_bytes(
        json.dumps(manifest, indent=2).encode("utf-8"),
        logical_name="nova-memory-migration-index.json",
        content_type="application/json",
    )
    index_path = output / ".index.nvm"
    atomic_write(index_path, manifest_blob)
    vault.verify_file(index_path)
    print(f"Encrypted migration index: {index_path}")

    if args.remove_plaintext:
        if args.confirm != "REMOVE_PLAINTEXT":
            raise VaultError(
                "--remove-plaintext requires --confirm REMOVE_PLAINTEXT"
            )
        for src, _, _ in verified:
            src.unlink()
            print(f"  REMOVED PLAINTEXT {src}")

    print("Migration completed and encrypted copies verified.")
    return 0


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Nova Memory Vault")
    parser.add_argument("--keystore", help="Override local DPAPI key path")
    sub = parser.add_subparsers(dest="command", required=True)

    p = sub.add_parser("init", help="Initialize local DPAPI-protected master key")
    p.add_argument("--recovery-bundle", help="Path for encrypted recovery bundle")
    p.add_argument("--overwrite", action="store_true")
    p.set_defaults(func=cmd_init)

    p = sub.add_parser("status", help="Show vault status")
    p.set_defaults(func=cmd_status)

    p = sub.add_parser("seal", help="Encrypt one file")
    p.add_argument("source")
    p.add_argument("destination", nargs="?")
    p.add_argument("--logical-name")
    p.add_argument("--opaque-name", action="store_true")
    p.set_defaults(func=cmd_seal)

    p = sub.add_parser("open", help="Decrypt one file")
    p.add_argument("source")
    p.add_argument("destination")
    p.set_defaults(func=cmd_open)

    p = sub.add_parser("verify", help="Authenticate/decrypt without writing plaintext")
    p.add_argument("source")
    p.set_defaults(func=cmd_verify)

    p = sub.add_parser("recover", help="Restore master key from recovery bundle")
    p.add_argument("recovery_bundle")
    p.add_argument("--overwrite", action="store_true")
    p.set_defaults(func=cmd_recover)

    p = sub.add_parser("migrate", help="Create verified encrypted copies of memory files")
    p.add_argument("--source", action="append", required=True)
    p.add_argument("--output", required=True)
    p.add_argument("--opaque-names", action="store_true")
    p.add_argument("--apply", action="store_true")
    p.add_argument("--remove-plaintext", action="store_true")
    p.add_argument("--confirm")
    p.set_defaults(func=cmd_migrate)
    return parser


def main() -> int:
    parser = build_parser()
    args = parser.parse_args()
    try:
        return int(args.func(args))
    except (VaultError, RecoveryError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
