from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SKIP_PARTS = {".git", ".gradle", ".idea", ".kotlin", "build", "__pycache__", ".pytest_cache", "dist"}
SKIP_SUFFIXES = {".zip", ".jar", ".apk", ".aab", ".png", ".jpg", ".jpeg", ".webp", ".gif", ".pdf"}
PLACEHOLDERS = {
    "", "change-me", "changeme", "replace-me", "example", "example-token",
    "your-token", "your_api_key", "your-api-key", "test", "dummy",
}
OPENAI_KEY = re.compile(r"\bsk-[A-Za-z0-9_-]{20,}\b")
ASSIGNMENT = re.compile(r"\b(OPENAI_API_KEY|POTATO_API_TOKEN)[ \t]*=[ \t]*([^\s#\"']+)")

hits: list[str] = []
for path in ROOT.rglob("*"):
    if not path.is_file() or path.resolve() == Path(__file__).resolve():
        continue
    rel = path.relative_to(ROOT)
    if any(part in SKIP_PARTS for part in rel.parts) or path.suffix.lower() in SKIP_SUFFIXES:
        continue
    try:
        text = path.read_text(encoding="utf-8")
    except (UnicodeDecodeError, OSError):
        continue
    if OPENAI_KEY.search(text):
        hits.append(f"{rel}: OpenAI-style secret")
    for match in ASSIGNMENT.finditer(text):
        value = match.group(2).strip()
        normalized = value.lower()
        if normalized in PLACEHOLDERS or value.startswith("${") or value.startswith("<"):
            continue
        hits.append(f"{rel}: non-placeholder {match.group(1)} assignment")

if hits:
    raise SystemExit("Potential committed secrets:\n" + "\n".join(hits[:50]))
print("SECRET_PATTERN_SCAN=PASS")
