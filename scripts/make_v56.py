from __future__ import annotations

import base64
import shutil
import subprocess
import sys
import zlib
from pathlib import Path

PAYLOAD_DIR = Path(__file__).resolve().parent / "v56_patch"
CACHE_NAMES = {".gradle", ".kotlin", ".pytest_cache", "build", "__pycache__", "dist"}


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
        fail(f"patch exited {completed.returncode}")

    deleted = dest / ".github/workflows/build-matrix.yml"
    if deleted.exists():
        deleted.unlink()
    clean_generated(dest)
    invariants = {
        "VERSION": "5.6",
        "android/build.gradle.kts": "versionCode = 56",
        "android/src/main/java/com/potato/jarvis/accessibility/PotatoAccessibilityService.kt": "AccessibilityConsent.isAccepted",
        "android/src/main/java/com/potato/jarvis/core/Models.kt": "foregroundPackage",
        "android/src/main/java/com/potato/jarvis/device/DeviceContext.kt": "foregroundPackage = foregroundPackage()",
        "android/src/test/java/com/potato/jarvis/DeepLinkRouterTest.kt": "class DeepLinkRouterTest",
        "android/src/test/java/com/potato/jarvis/SecurityGatewayTest.kt": "class SecurityGatewayTest",
        "scripts/package_clean_source.py": "ARCHIVE_ROOT=",
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
    print(f"V56_PROJECT_ROOT={dest}")
    print("V56_TRANSFORM=PASS")


if __name__ == "__main__":
    main()
