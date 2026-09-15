# Building and verifying POTATO-JARVIS V5.8

## Prerequisites

Use JDK 17, Python 3.13 (or a compatible supported Python), and Android SDK platform 36. The project pins Android Gradle Plugin 8.12.2 and Gradle 8.13.

## Backend

```bash
python3 -m venv .venv
. .venv/bin/activate
python -m pip install --upgrade pip
python -m pip install -r backend/requirements.txt
python -m compileall -q backend
python -m pytest -q backend/tests
```

For a deployment, copy `backend/.env.example` to an untracked `backend/.env` and set server-side credentials. Never put `OPENAI_API_KEY`, signing material, or production bearer tokens into the Android source or an artifact.

## Android automated gates

```bash
./scripts/verify_gradle_wrapper.sh
./gradlew :android:processDebugResources --no-daemon --stacktrace
./gradlew :android:testDebugUnitTest --no-daemon --stacktrace
./gradlew :android:assembleDebug --no-daemon --stacktrace
./gradlew :android:lint --no-daemon --max-workers=1 -Dorg.gradle.jvmargs='-Xmx3g -Dfile.encoding=UTF-8'
./gradlew :android:bundleRelease --no-daemon --stacktrace
```

The CI workflow additionally rejects a `testDebugUnitTest` run that produces zero actual JUnit test cases. This prevents a `NO-SOURCE` Gradle task from being mistaken for meaningful test coverage.

## Dependency/security audit

CI installs `pip-audit` and audits `backend/requirements.txt` against known Python package advisories. Android lint remains a blocking gate; there is no blanket lint suppression or `continue-on-error` release path.

## Clean source package

```bash
python scripts/package_clean_source.py /tmp/POTATO-JARVIS-FINAL-V5.8.zip
python scripts/verify_source_archive.py /tmp/POTATO-JARVIS-FINAL-V5.8.zip
sha256sum /tmp/POTATO-JARVIS-FINAL-V5.8.zip
unzip -t /tmp/POTATO-JARVIS-FINAL-V5.8.zip
```

The source verifier checks the canonical archive root, ZIP integrity, forbidden generated/secret/binary content, complete manifest coverage, duplicate manifest entries, and SHA-256 correctness for every packaged source file.

## Debug APK versus production AAB

`assembleDebug` creates a debug APK intended for development/device verification. `bundleRelease` creates the release AAB build output, but a production upload must use developer-controlled signing / Play App Signing. Do not create or commit a public throwaway production keystore just to make a release gate green.

## Manual release gates

After automated verification, use a signed AAB in a Google Play internal testing track and test on physical supported Android hardware. Verify install/launch, backend connection over production HTTPS, chat/AI, voice, camera/OCR, files, optional location/calendar/contacts, notifications/deep links, Accessibility disclosure/accept/decline/revoke behavior, automations/background work, and risk-3 biometric approvals. Record Play policy declarations and any Play Protect outcome separately from CI.
