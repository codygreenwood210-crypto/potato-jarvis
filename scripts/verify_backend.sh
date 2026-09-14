#!/bin/sh
set -eu
ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$ROOT_DIR"
python3 -m py_compile backend/main.py backend/providers.py
python3 -m compileall -q backend
python3 -m pytest -q backend/tests
