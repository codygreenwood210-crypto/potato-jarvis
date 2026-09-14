from __future__ import annotations

import re
from typing import Any
from urllib.parse import urlparse


def sanitize_web_answer(text: str) -> str:
    """Remove invisible Responses citation control markers before Android/UI rendering."""
    cleaned = re.sub(r"\ue200.*?\ue201", "", str(text or ""))
    cleaned = re.sub(r"\ue200.*?\ue202", "", cleaned)
    cleaned = re.sub(r"[\ue200-\ue202]", "", cleaned)
    return cleaned.strip()[:30_000]


def extract_web_citations(response: dict[str, Any]) -> list[dict[str, str]]:
    """Extract only structured URL citations from Responses output annotations."""
    citations: list[dict[str, str]] = []
    seen: set[str] = set()
    for item in response.get("output", []):
        if item.get("type") != "message":
            continue
        for part in item.get("content", []):
            if part.get("type") != "output_text":
                continue
            for annotation in part.get("annotations", []) or []:
                if annotation.get("type") != "url_citation":
                    continue
                url = str(annotation.get("url", "")).strip()
                title = str(annotation.get("title", "")).strip()
                if not url or url in seen:
                    continue
                parsed = urlparse(url)
                if parsed.scheme not in {"http", "https"} or not parsed.netloc:
                    continue
                seen.add(url)
                citations.append({"url": url[:2048], "title": title[:500] or parsed.netloc})
                if len(citations) >= 20:
                    return citations
    return citations
