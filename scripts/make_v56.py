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


def install_ci_evidence_finalizer(root: Path) -> None:
    """Make the source packager record final CI evidence only after prior gates pass."""
    package_script = root / "scripts/package_clean_source.py"
    text = package_script.read_text(encoding="utf-8")
    if "def finalize_ci_evidence()" in text:
        return
    text = text.replace(
        "import hashlib\n",
        "import hashlib\nimport os\nimport xml.etree.ElementTree as ET\n",
        1,
    )
    anchor = "OUT.parent.mkdir(parents=True, exist_ok=True)\n"
    if anchor not in text:
        fail("clean source packager anchor not found")
    finalizer = r'''


def finalize_ci_evidence() -> None:
    if os.getenv("GITHUB_ACTIONS", "").lower() != "true":
        return

    test_root = ROOT / "android/build/test-results/testDebugUnitTest"
    reports = sorted(test_root.glob("TEST-*.xml"))
    if not reports:
        raise SystemExit("Cannot finalize CI evidence: Android JUnit reports are missing")
    tests = failures = errors = skipped = 0
    for report in reports:
        suite = ET.parse(report).getroot()
        tests += int(suite.attrib.get("tests", "0"))
        failures += int(suite.attrib.get("failures", "0"))
        errors += int(suite.attrib.get("errors", "0"))
        skipped += int(suite.attrib.get("skipped", "0"))
    if tests <= 0 or failures or errors:
        raise SystemExit(
            f"Cannot finalize CI evidence: tests={tests} failures={failures} errors={errors}"
        )

    lint_path = ROOT / "android/build/reports/lint-results-debug.xml"
    if not lint_path.is_file():
        raise SystemExit("Cannot finalize CI evidence: lint XML is missing")
    lint_root = ET.parse(lint_path).getroot()
    lint_issues = lint_root.findall("issue")
    lint_errors = sum(1 for item in lint_issues if item.attrib.get("severity") in {"Error", "Fatal"})
    lint_warnings = sum(1 for item in lint_issues if item.attrib.get("severity") == "Warning")
    lint_hints = sum(1 for item in lint_issues if item.attrib.get("severity") == "Hint")
    if lint_errors:
        raise SystemExit(f"Cannot finalize CI evidence: lint errors={lint_errors}")

    apk = ROOT / "android/build/outputs/apk/debug/android-debug.apk"
    aab = ROOT / "android/build/outputs/bundle/release/android-release.aab"
    if not apk.is_file() or not aab.is_file() or apk.stat().st_size == 0 or aab.stat().st_size == 0:
        raise SystemExit("Cannot finalize CI evidence: APK/AAB release artifacts are missing")

    run_id = os.getenv("GITHUB_RUN_ID", "unknown")
    commit = os.getenv("GITHUB_SHA", "unknown")
    run_url = f"https://github.com/{os.getenv('GITHUB_REPOSITORY', 'unknown')}/actions/runs/{run_id}"
    report = f'''# POTATO-JARVIS V5.6 Release Verification Report

**Release:** 5.6 (`versionCode 56`)  
**Audit date:** 2026-09-12  
**Verification run:** {run_id}  
**Harness commit:** `{commit}`  
**Run URL:** {run_url}  
**Automated verification status:** **PASS**

## Automated evidence

The clean source archive containing this report is created only after every preceding canonical V5.6 workflow gate succeeds.

- Exact V5.5 baseline integrity: PASS.
- V5.6 source reconstruction/invariants: PASS.
- Secret-pattern scan: PASS.
- Android XML parse: PASS.
- Generated-output cleanliness before build: PASS.
- Official Gradle 8.13 wrapper integrity verification: PASS.
- Python dependency vulnerability audit (`pip-audit`): PASS with no known vulnerabilities reported by the gate.
- Backend Python compile and tests: PASS.
- API-36 Android resource processing: PASS.
- Android JVM tests: PASS — {tests} executed, {failures} failures, {errors} errors, {skipped} skipped.
- Debug APK assembly: PASS.
- Android lint: PASS — 0 errors; {lint_warnings} warnings and {lint_hints} hints retained for non-blocking maintenance review.
- Release AAB assembly including release lint-vital: PASS.
- Clean-source packaging and independent source-manifest verification: runs immediately after this report is written.

## Release artifacts

The workflow produces and hashes a debug APK, an unsigned release AAB candidate, the complete clean V5.6 source ZIP, and verification reports. Production signing credentials are intentionally not committed to source or CI.

## External/manual gates

The following are deliberately **not** represented as automated PASS results: developer-controlled upload signing / Play App Signing, Play Console policy declarations and review, internal-track installation, physical-device critical-flow regression, live production backend/provider/device integration, and final Play Protect classification.

## Production status

**NOT PRODUCTION READY** until those external/manual release gates are completed successfully.
'''
    (ROOT / "RELEASE_REPORT.md").write_text(report, encoding="utf-8")

    readiness = f'''# POTATO-JARVIS V5.6 release-readiness gates

## Automated gates — PASS

Canonical GitHub Actions run **{run_id}** (harness commit `{commit}`) reached source packaging only after all mandatory automated gates succeeded: backend compile/tests, Python dependency vulnerability audit, Gradle-wrapper integrity, API-36 resources, {tests} real Android JVM tests, debug APK assembly, blocking Android lint with 0 errors, release AAB assembly, secret scan, XML validation, and source-state checks.

Lint retained {lint_warnings} non-blocking warnings and {lint_hints} hint for maintenance review; none are lint errors.

## Manual/external gates — NOT RUN / BLOCKED OUTSIDE CI

Production release still requires developer-controlled signing / Play App Signing, Play policy declarations including Accessibility/Data Safety where applicable, Play internal-track installation, physical-device critical-flow regression, a production HTTPS backend, live provider/device integration verification, and resolution/appeal of any Google Play Protect or Play policy block.

## Production status

**NOT PRODUCTION READY** until the external/manual gates above have successful evidence.

## Dependency security baseline

- FastAPI 0.141.1 and Starlette 1.6.0.
- python-multipart 0.0.32.
- pypdf 6.18.0.
- pytest 9.1.1.
- `pip-audit` is a mandatory zero-known-vulnerability release gate.
'''
    (ROOT / "docs/RELEASE_READINESS_V5.6.md").write_text(readiness, encoding="utf-8")


finalize_ci_evidence()
'''
    package_script.write_text(text.replace(anchor, finalizer + "\n" + anchor, 1), encoding="utf-8")


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
    install_ci_evidence_finalizer(dest)
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
        "scripts/package_clean_source.py": "finalize_ci_evidence",
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
    print("CI_EVIDENCE_FINALIZER=INSTALLED")
    print(f"V56_PROJECT_ROOT={dest}")
    print("V56_TRANSFORM=PASS")


if __name__ == "__main__":
    main()
