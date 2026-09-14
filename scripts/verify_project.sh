#!/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$ROOT"

python3 -m compileall -q backend
python3 -m pytest -q backend/tests

./scripts/verify_gradle_wrapper.sh

./gradlew :android:assembleDebug :android:lint
