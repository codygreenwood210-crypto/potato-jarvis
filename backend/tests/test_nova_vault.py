from __future__ import annotations

import json

import pytest

from backend.nova_vault import (
    KEY_BYTES,
    NovaMemoryVault,
    RecoveryError,
    VaultIntegrityError,
    create_recovery_bundle,
    recover_master_key,
)


def test_round_trip_preserves_bytes_and_private_metadata():
    key = bytes(range(KEY_BYTES))
    vault = NovaMemoryVault(key)
    original = b"Nova memory: verified continuity."
    blob = vault.seal_bytes(
        original,
        logical_name="docs/private-memory.md",
        content_type="text/markdown",
    )

    # Logical metadata must not appear in the plaintext envelope.
    assert b"private-memory" not in blob
    assert original not in blob

    opened = vault.open_bytes(blob)
    assert opened.data == original
    assert opened.logical_name == "docs/private-memory.md"
    assert opened.content_type == "text/markdown"


def test_wrong_key_cannot_open_vault():
    blob = NovaMemoryVault(b"A" * KEY_BYTES).seal_bytes(b"secret")
    with pytest.raises(VaultIntegrityError):
        NovaMemoryVault(b"B" * KEY_BYTES).open_bytes(blob)


def test_tampering_is_detected():
    key = b"K" * KEY_BYTES
    vault = NovaMemoryVault(key)
    envelope = json.loads(vault.seal_bytes(b"secret").decode("utf-8"))
    ciphertext = envelope["ciphertext"]
    envelope["ciphertext"] = ("A" if ciphertext[0] != "A" else "B") + ciphertext[1:]
    tampered = json.dumps(envelope).encode("utf-8")

    with pytest.raises(VaultIntegrityError):
        vault.open_bytes(tampered)


def test_opaque_name_is_stable_per_key_and_name():
    key = b"K" * KEY_BYTES
    vault = NovaMemoryVault(key)
    first = vault.opaque_name("docs/memory.md")
    second = vault.opaque_name("docs/memory.md")
    other = vault.opaque_name("docs/other.md")

    assert first == second
    assert first != other
    assert first.endswith(".nvm")
    assert "memory" not in first


def test_recovery_bundle_restores_exact_master_key():
    master = b"M" * KEY_BYTES
    bundle = create_recovery_bundle(master, "correct horse battery staple")
    recovered = recover_master_key(bundle, "correct horse battery staple")
    assert recovered == master


def test_wrong_recovery_passphrase_fails():
    master = b"M" * KEY_BYTES
    bundle = create_recovery_bundle(master, "correct horse battery staple")
    with pytest.raises(RecoveryError):
        recover_master_key(bundle, "this is definitely wrong")


def test_file_round_trip_and_verify(tmp_path):
    key = b"Z" * KEY_BYTES
    vault = NovaMemoryVault(key)
    source = tmp_path / "memory.md"
    sealed = tmp_path / "memory.nvm"
    restored = tmp_path / "restored.md"
    source.write_bytes(b"durable Nova memory\n")

    vault.seal_file(source, sealed, logical_name="memory.md", content_type="text/markdown")
    verified = vault.verify_file(sealed)
    opened = vault.open_file(sealed, restored)

    assert verified.sha256 == opened.sha256
    assert restored.read_bytes() == source.read_bytes()
