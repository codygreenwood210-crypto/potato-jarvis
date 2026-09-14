#!/bin/sh
set -eu
ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$ROOT_DIR"
exec python3 -m uvicorn backend.main:app --host "${HOST:-0.0.0.0}" --port "${PORT:-8000}"
