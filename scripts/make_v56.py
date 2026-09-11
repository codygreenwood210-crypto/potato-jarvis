from __future__ import annotations

import base64
import hashlib
import lzma
import shutil
import subprocess
import sys
import zlib
from pathlib import Path

PAYLOAD_DIR = Path(__file__).resolve().parent / "v56_patch"
STAGE2_DIR = Path(__file__).resolve().parent / "v56_stage2"
STAGE2_COMPRESSED_SHA256 = "3231fa0a0744ecb836d772e623371055ca81e1f24e2ebdb9da109424bc348693"
CACHE_NAMES = {".gradle", ".kotlin", ".pytest_cache", "build", "__pycache__", "dist"}
SECURE_REQUIREMENTS = """fastapi==0.141.1
starlette==1.6.0
uvicorn==0.48.0
httpx==0.28.1
python-dotenv==1.2.2
python-multipart==0.0.32
pypdf==6.18.0
python-docx==1.2.0
openpyxl==3.1.5
python-pptx==1.0.2
pytest==9.1.1
cryptography==50.0.1
Pillow==12.3.0
"""
DEPENDENCY_NOTE = """

## Dependency security refresh
- FastAPI 0.141.1 and Starlette 1.6.0.
- python-multipart 0.0.32.
- pypdf 6.18.0.
- pytest 9.1.1.
- `pip-audit` is a mandatory zero-known-vulnerability release gate.
"""


def fail(message: str) -> None:
    raise SystemExit(f"V5.6 transform failed: {message}")


def clean_generated(root: Path) -> None:
    targets = [p for p in root.rglob("*") if p.is_dir() and p.name in CACHE_NAMES]
    for path in sorted(targets, key=lambda p: len(p.parts), reverse=True):
        if path.exists():
            shutil.rmtree(path)
    for path in root.rglob("*"):
        if path.is_file() and path.suffix.lower() in {".pyc", ".apk", ".aab", ".jks", ".keystore", ".orig", ".rej"}:
            path.unlink()


def decode_stage2_payload() -> bytes:
    parts = sorted(STAGE2_DIR.glob("part*.txt"))
    if not parts:
        fail("missing V5.6 second-stage payload chunks")
    encoded = "".join(p.read_text(encoding="ascii").strip() for p in parts)
    try:
        compressed = base64.b64decode(encoded, validate=True)
    except Exception as exc:
        fail(f"second-stage base64 payload is invalid: {exc}")
    digest = hashlib.sha256(compressed).hexdigest()
    if digest != STAGE2_COMPRESSED_SHA256:
        fail(
            "second-stage payload SHA-256 mismatch: "
            f"expected {STAGE2_COMPRESSED_SHA256}, got {digest}"
        )
    try:
        return lzma.decompress(compressed)
    except lzma.LZMAError as exc:
        fail(f"second-stage LZMA payload is corrupt: {exc}")


def harden_dependencies(root: Path) -> None:
    requirements = root / "backend/requirements.txt"
    requirements.write_text(SECURE_REQUIREMENTS, encoding="utf-8")
    for relative in ("CHANGES_V5.6.md", "docs/RELEASE_READINESS_V5.6.md"):
        path = root / relative
        text = path.read_text(encoding="utf-8")
        if "FastAPI 0.141.1" not in text:
            path.write_text(text.rstrip() + DEPENDENCY_NOTE + "\n", encoding="utf-8")


def main() -> None:
    if len(sys.argv) != 2:
        fail("usage: make_v56.py /path/to/POTATO-JARVIS-V5.5")
    source = Path(sys.argv[1]).resolve()
    if not source.is_dir() or source.name != "POTATO-JARVIS-V5.5":
        fail(f"unexpected source root: {source}")
    dest = source.with_name("POTATO-JARVIS-V5.6")
    if dest.exists():
        shutil.rmtree(dest)
    source.rename(dest)
    clean_generated(dest)

    parts = sorted(PAYLOAD_DIR.glob("part*.txt"))
    if not parts:
        fail("missing V5.6 patch payload")
    encoded = "".join(p.read_text(encoding="ascii").strip() for p in parts)
    patch = zlib.decompress(base64.b64decode(encoded))
    completed = subprocess.run(
        ["patch", "-p1", "--batch", "--forward", "--reject-file=-"],
        cwd=dest,
        input=patch,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
    )
    if completed.returncode != 0:
        sys.stderr.write(completed.stdout.decode("utf-8", errors="replace"))
        fail(f"base patch exited {completed.returncode}")

    stage2_patch = decode_stage2_payload()
    stage2 = subprocess.run(
        ["patch", "-p1", "--batch", "--forward", "--reject-file=-"],
        cwd=dest,
        input=stage2_patch,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
    )
    if stage2.returncode != 0:
        sys.stderr.write(stage2.stdout.decode("utf-8", errors="replace"))
        fail(f"second-stage patch exited {stage2.returncode}")

    harden_dependencies(dest)
    deleted = dest / ".github/workflows/build-matrix.yml"
    if deleted.exists():
        deleted.unlink()
    clean_generated(dest)
    invariants = {
        "VERSION": "5.6",
        "backend/requirements.txt": "starlette==1.6.0",
        "android/build.gradle.kts": "versionCode = 56",
        "android/src/main/java/com/potato/jarvis/accessibility/PotatoAccessibilityService.kt": "AccessibilityConsent.isAccepted",
        "android/src/main/java/com/potato/jarvis/core/Models.kt": "foregroundPackage",
        "android/src/main/java/com/potato/jarvis/device/DeviceContext.kt": "foregroundPackage = foregroundPackage()",
        "android/src/test/java/com/potato/jarvis/DeepLinkRouterTest.kt": "class DeepLinkRouterTest",
        "android/src/test/java/com/potato/jarvis/SecurityGatewayTest.kt": "class SecurityGatewayTest",
        "android/src/main/java/com/potato/jarvis/core/SseFrameAccumulator.kt": "class SseFrameAccumulator",
        "android/src/test/java/com/potato/jarvis/SseFrameAccumulatorTest.kt": "class SseFrameAccumulatorTest",
        "scripts/package_clean_source.py": "ARCHIVE_ROOT",
        "scripts/verify_source_archive.py": "SOURCE_ARCHIVE_OK",
        "scripts/scan_secrets.py": "SECRET_PATTERN_SCAN=PASS",
        "docs/RELEASE_READINESS_V5.6.md": "POTATO-JARVIS V5.6",
    }
    for rel, needle in invariants.items():
        path = dest / rel
        if not path.is_file():
            fail(f"missing required file {rel}")
        if needle not in path.read_text(encoding="utf-8"):
            fail(f"missing invariant {needle!r} in {rel}")
    manifest = (dest / "android/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
    if "android.permission.CAMERA" in manifest:
        fail("unnecessary CAMERA permission returned")
    if '<uses-feature android:name="android.hardware.camera" android:required="false"' not in manifest:
        fail("optional camera hardware declaration missing")
    if (dest / ".github/workflows/build-matrix.yml").exists():
        fail("obsolete build matrix survived")
    print(completed.stdout.decode("utf-8", errors="replace"))
    print(stage2.stdout.decode("utf-8", errors="replace"))
    print("DEPENDENCY_SECURITY_REFRESH=PASS")
    print(f"V56_PROJECT_ROOT={dest}")
    print("V56_TRANSFORM=PASS")


if __name__ == "__main__":
    main()
