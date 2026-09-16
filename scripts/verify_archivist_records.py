#!/usr/bin/env python3
"""Verify POTATO's repository-backed Archivist continuity contract.

This intentionally checks structure and cross-references only. It does not claim to
verify application behavior, CI results, device evidence, or the truth of a human-
authored historical statement.
"""

from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ARCHIVE = ROOT / "docs" / "ARCHIVIST"

REQUIRED = {
    ROOT / "POTATO_NEXT_CHAT_START_HERE.md": [
        "docs/ARCHIVIST/README.md",
        "docs/ARCHIVIST/CURRENT_STATE.md",
        "docs/ARCHIVIST/HANDOFF_LEDGER.md",
    ],
    ARCHIVE / "README.md": [
        "# POTATO Archivist",
        "Mandatory startup sequence",
        "Mandatory closeout sequence",
        "Scout investigates -> Specialists implement/repair -> Judge verifies -> Archivist records",
    ],
    ARCHIVE / "CURRENT_STATE.md": [
        "# POTATO Archivist — Current State",
        "potato-v5.8-final",
        "6cc4f2076d46bf11e9bd3c7d0cbdabff4d9d232a",
    ],
    ARCHIVE / "TEAM_ROSTER.md": [
        "The Archivist",
        "Judge",
        "Scout",
    ],
    ARCHIVE / "WORK_LEDGER.md": ["## Record format", "WORK-"],
    ARCHIVE / "DECISION_LEDGER.md": ["## Record format", "DEC-"],
    ARCHIVE / "VERIFICATION_LEDGER.md": ["## Record format", "VER-"],
    ARCHIVE / "ACHIEVEMENTS_AND_REWARDS.md": ["## Record format", "ACH-", "## Rewards"],
    ARCHIVE / "HANDOFF_LEDGER.md": ["## Required session capsule fields", "HANDOFF-"],
    ARCHIVE / "RECORD_TEMPLATE.md": ["## Work entry", "## Handoff capsule"],
}

ID_PATTERN = re.compile(r"^###\s+((?:WORK|DEC|VER|ACH|HANDOFF)-\d{4}-\d{3})\b", re.MULTILINE)
FORBIDDEN_SECRET_VALUE_PATTERNS = [
    re.compile(r"\bsk-[A-Za-z0-9_-]{20,}\b"),
    re.compile(r"-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----"),
]


def fail(message: str) -> None:
    raise SystemExit(f"ARCHIVIST_VERIFY_FAIL: {message}")


def main() -> None:
    texts: dict[Path, str] = {}
    for path, markers in REQUIRED.items():
        if not path.is_file():
            fail(f"missing required file: {path.relative_to(ROOT)}")
        text = path.read_text(encoding="utf-8")
        texts[path] = text
        for marker in markers:
            if marker not in text:
                fail(f"missing marker {marker!r} in {path.relative_to(ROOT)}")

    all_ids: dict[str, Path] = {}
    for path in [
        ARCHIVE / "WORK_LEDGER.md",
        ARCHIVE / "DECISION_LEDGER.md",
        ARCHIVE / "VERIFICATION_LEDGER.md",
        ARCHIVE / "ACHIEVEMENTS_AND_REWARDS.md",
        ARCHIVE / "HANDOFF_LEDGER.md",
    ]:
        for record_id in ID_PATTERN.findall(texts[path]):
            if record_id in all_ids:
                fail(
                    f"duplicate record id {record_id} in "
                    f"{all_ids[record_id].relative_to(ROOT)} and {path.relative_to(ROOT)}"
                )
            all_ids[record_id] = path

    required_prefixes = {"WORK-", "DEC-", "VER-", "ACH-", "HANDOFF-"}
    present_prefixes = {next(p for p in required_prefixes if rid.startswith(p)) for rid in all_ids}
    if present_prefixes != required_prefixes:
        fail(f"record classes incomplete: present={sorted(present_prefixes)}")

    combined = "\n".join(texts.values())
    for pattern in FORBIDDEN_SECRET_VALUE_PATTERNS:
        if pattern.search(combined):
            fail(f"possible secret material matched {pattern.pattern!r}")

    print(f"ARCHIVIST_VERIFY=PASS files={len(REQUIRED)} records={len(all_ids)}")


if __name__ == "__main__":
    main()
