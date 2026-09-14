from __future__ import annotations

import hashlib
import sys
import zipfile
from pathlib import PurePosixPath

if len(sys.argv) != 2:
    raise SystemExit("usage: verify_source_archive.py <source.zip>")

archive = sys.argv[1]
version = open("VERSION", encoding="utf-8").read().strip()
root = f"POTATO-JARVIS-V{version}"
forbidden_parts = {
    ".git", ".gradle", ".idea", ".kotlin", "build", "__pycache__", ".pytest_cache", "dist"
}
forbidden_suffixes = {".pyc", ".apk", ".aab", ".jks", ".keystore", ".p12", ".pem", ".key"}
forbidden_names = {".env", "local.properties"}
manifest_path = f"{root}/SOURCE_MANIFEST.sha256"

with zipfile.ZipFile(archive) as zf:
    bad = zf.testzip()
    if bad:
        raise SystemExit(f"ZIP integrity failed at {bad}")
    files = sorted(name for name in zf.namelist() if not name.endswith("/"))
    if not files:
        raise SystemExit("source archive is empty")
    if any(not (name == root or name.startswith(root + "/")) for name in files):
        raise SystemExit("archive contains entries outside canonical root")
    if manifest_path not in files:
        raise SystemExit("SOURCE_MANIFEST.sha256 missing")

    for name in files:
        rel = PurePosixPath(name).relative_to(root)
        if any(part in forbidden_parts for part in rel.parts):
            raise SystemExit(f"forbidden generated path in source archive: {rel}")
        if rel.name in forbidden_names:
            raise SystemExit(f"forbidden local/secret file in source archive: {rel}")
        if rel.suffix.lower() in forbidden_suffixes:
            raise SystemExit(f"forbidden binary/secret suffix in source archive: {rel}")

    manifest_lines = zf.read(manifest_path).decode("utf-8").splitlines()
    expected: dict[str, str] = {}
    for line in manifest_lines:
        digest, sep, path = line.partition("  ")
        if not sep or len(digest) != 64 or not path:
            raise SystemExit(f"invalid manifest line: {line!r}")
        if path in expected:
            raise SystemExit(f"duplicate manifest path: {path}")
        expected[path] = digest

    actual_rel = {
        str(PurePosixPath(name).relative_to(root))
        for name in files
        if name != manifest_path
    }
    if set(expected) != actual_rel:
        missing = sorted(actual_rel - set(expected))
        extra = sorted(set(expected) - actual_rel)
        raise SystemExit(f"manifest coverage mismatch missing={missing} extra={extra}")

    for rel, digest in expected.items():
        data = zf.read(f"{root}/{rel}")
        actual = hashlib.sha256(data).hexdigest()
        if actual != digest:
            raise SystemExit(f"manifest hash mismatch: {rel}")

print(f"SOURCE_ARCHIVE_OK root={root} files={len(files)} manifest_entries={len(expected)}")
