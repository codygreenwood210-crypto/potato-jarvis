from __future__ import annotations

import hashlib
import os
import xml.etree.ElementTree as ET
import shutil
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
VERSION = (ROOT / "VERSION").read_text().strip()
OUT = (
    Path(sys.argv[1]).resolve()
    if len(sys.argv) > 1
    else ROOT.parent / f"POTATO-JARVIS-FINAL-V{VERSION}.zip"
)
ARCHIVE_ROOT = f"POTATO-JARVIS-V{VERSION}"
EXCLUDE_PARTS = {
    ".git",
    ".gradle",
    ".idea",
    "build",
    "__pycache__",
    ".pytest_cache",
    ".kotlin",
    "dist",
}
EXCLUDE_SUFFIXES = {".pyc", ".apk", ".aab", ".jks", ".keystore", ".p12", ".pem", ".key"}
EXCLUDE_NAMES = {".env", "local.properties"}




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
        raise SystemExit(f"Cannot finalize CI evidence: tests={tests} failures={failures} errors={errors}")

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
    if os.getenv("POTATO_EMULATOR_SMOKE") != "PASS":
        raise SystemExit("Cannot finalize CI evidence: Android emulator smoke verification did not pass")

    build_text = (ROOT / "android/build.gradle.kts").read_text(encoding="utf-8")
    match = __import__("re").search(r"versionCode\s*=\s*(\d+)", build_text)
    version_code = match.group(1) if match else "unknown"
    run_id = os.getenv("GITHUB_RUN_ID", "unknown")
    commit = os.getenv("GITHUB_SHA", "unknown")
    run_url = f"https://github.com/{os.getenv('GITHUB_REPOSITORY', 'unknown')}/actions/runs/{run_id}"
    report = f'''# POTATO-JARVIS V{VERSION} Release Verification Report

**Release:** {VERSION} (`versionCode {version_code}`)  \n**Verification run:** {run_id}  \n**Source commit:** `{commit}`  \n**Run URL:** {run_url}  \n**Automated working-copy verification:** **PASS**

## Automated evidence

This clean source archive is created only after every mandatory V{VERSION} CI gate succeeds.

- Source/version integrity and secret-pattern scan: PASS.
- Official Gradle-wrapper integrity verification: PASS.
- Python dependency vulnerability audit: PASS.
- Backend Python compile and regression tests: PASS.
- Android JVM tests: PASS - {tests} executed, {failures} failures, {errors} errors, {skipped} skipped.
- Debug APK assembly: PASS.
- Android lint: PASS - 0 errors; {lint_warnings} warnings and {lint_hints} hints retained for maintenance review.
- Release AAB assembly: PASS.
- Android emulator boot, APK installation, MainActivity launch, first-use disclosure, setup connection to a real local POTATO backend, and chat-screen UI smoke checks: PASS.
- Green/black theme source invariant: PASS.
- Clean-source packaging and source-manifest verification: executed immediately after this report is written.

## Scope

This evidence establishes the V{VERSION} downloadable debug working copy produced by this CI run. The unsigned release AAB is a build candidate, not a Play-distribution claim.

## External production gates

Play App Signing/developer release signing, Play Console policy review, production backend/provider/device credentials, Play internal-track testing, Play Protect classification, and physical-device hardware-specific checks remain external deployment gates and are not represented as CI PASS results.
'''
    (ROOT / "RELEASE_REPORT.md").write_text(report, encoding="utf-8")


finalize_ci_evidence()

OUT.parent.mkdir(parents=True, exist_ok=True)

with tempfile.TemporaryDirectory(
    prefix=f".potato-v{VERSION}-clean-source-",
    dir=OUT.parent,
) as temp_dir:
    stage = Path(temp_dir) / ARCHIVE_ROOT
    stage.mkdir(parents=True)

    for src in ROOT.rglob("*"):
        rel = src.relative_to(ROOT)
        if any(part in EXCLUDE_PARTS for part in rel.parts):
            continue
        if src.is_dir() or src.is_symlink():
            continue
        if rel.as_posix() == "SOURCE_MANIFEST.sha256":
            continue
        if src.name in EXCLUDE_NAMES:
            continue
        if src.suffix.lower() in EXCLUDE_SUFFIXES:
            continue
        dst = stage / rel
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst)

    manifest: list[str] = []
    for file in sorted(p for p in stage.rglob("*") if p.is_file()):
        digest = hashlib.sha256(file.read_bytes()).hexdigest()
        manifest.append(f"{digest}  {file.relative_to(stage).as_posix()}")
    (stage / "SOURCE_MANIFEST.sha256").write_text("\n".join(manifest) + "\n")

    base = OUT.with_suffix("")
    made = Path(shutil.make_archive(str(base), "zip", stage.parent, stage.name))
    if made != OUT:
        made.replace(OUT)

print(f"{hashlib.sha256(OUT.read_bytes()).hexdigest()}  {OUT}")
