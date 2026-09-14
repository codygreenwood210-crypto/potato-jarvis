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
