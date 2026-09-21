"""Provision Booked On Site Vapi tools + assistant.

Required for live provisioning:
  VAPI_API_KEY
  PUBLIC_BASE_URL
  VAPI_CREDENTIAL_ID

Optional:
  BUSINESS_NAME
  VAPI_MODEL_PROVIDER (default: openai)
  VAPI_MODEL          (default: gpt-4o)

Use --dry-run to validate/render configuration without calling Vapi.
"""
from __future__ import annotations

import argparse
import json
import os
from pathlib import Path
from typing import Any

import requests

ROOT = Path(__file__).resolve().parents[1]
VAPI = "https://api.vapi.ai"


def require(name: str, *, live: bool = True) -> str:
    value = os.getenv(name, "").strip()
    if live and not value:
        raise SystemExit(f"Missing required environment variable: {name}")
    return value


def load_json(path: Path) -> Any:
    return json.loads(path.read_text(encoding="utf-8"))


def api_post(path: str, payload: dict, api_key: str) -> dict:
    r = requests.post(
        f"{VAPI}{path}",
        headers={
            "Authorization": f"Bearer {api_key}",
            "Content-Type": "application/json",
        },
        json=payload,
        timeout=30,
    )
    if not r.ok:
        raise SystemExit(f"Vapi {path} failed: HTTP {r.status_code}: {r.text[:800]}")
    return r.json()


def build_tool_payloads(public_base_url: str, credential_id: str) -> list[dict]:
    tools = load_json(ROOT / "app" / "vapi_tools.json")
    webhook = public_base_url.rstrip("/") + "/vapi/webhook"
    rendered = []
    for item in tools:
        tool = dict(item)
        tool["server"] = {
            "url": webhook,
            "credentialId": credential_id,
        }
        rendered.append(tool)
    return rendered


def build_assistant_payload(tool_ids: list[str], public_base_url: str, credential_id: str) -> dict:
    business_name = os.getenv("BUSINESS_NAME", "Booked On Site Demo Plumbing")
    provider = os.getenv("VAPI_MODEL_PROVIDER", "openai")
    model = os.getenv("VAPI_MODEL", "gpt-4o")
    prompt = (ROOT / "app" / "assistant_prompt.txt").read_text(encoding="utf-8")
    prompt = prompt.replace("{{business_name}}", business_name)
    return {
        "name": "Booked On Site Receptionist",
        "firstMessage": (
            f"Thanks for calling {business_name}. I'm the automated receptionist. "
            "This call may be transcribed to handle your enquiry and booking. How can I help today?"
        ),
        "firstMessageMode": "assistant-speaks-first",
        "maxDurationSeconds": 600,
        "artifactPlan": {
            "recordingEnabled": False,
            "loggingEnabled": True,
            "pcapEnabled": False,
            "transcriptPlan": {
                "enabled": True,
                "assistantName": "Receptionist",
                "userName": "Caller",
            },
        },
        "serverMessages": ["tool-calls", "end-of-call-report", "status-update", "hang"],
        "server": {
            "url": public_base_url.rstrip("/") + "/vapi/webhook",
            "credentialId": credential_id,
        },
        "model": {
            "provider": provider,
            "model": model,
            "temperature": 0.2,
            "messages": [{"role": "system", "content": prompt}],
            "toolIds": tool_ids,
        },
        "metadata": {"company": "Booked On Site", "environment": "production"},
    }


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()

    live = not args.dry_run
    api_key = require("VAPI_API_KEY", live=live)
    public_base_url = require("PUBLIC_BASE_URL", live=live) or "https://example.vercel.app"
    credential_id = require("VAPI_CREDENTIAL_ID", live=live) or "cred_REPLACE_ME"

    tool_payloads = build_tool_payloads(public_base_url, credential_id)
    if args.dry_run:
        assistant = build_assistant_payload(
            [f"tool_dry_{i+1}" for i in range(len(tool_payloads))],
            public_base_url,
            credential_id,
        )
        print(json.dumps({"tools": tool_payloads, "assistant": assistant}, indent=2))
        return

    tool_ids: list[str] = []
    for payload in tool_payloads:
        created = api_post("/tool", payload, api_key)
        tool_id = created.get("id")
        if not tool_id:
            raise SystemExit("Vapi tool creation response did not include id")
        tool_ids.append(tool_id)

    assistant_payload = build_assistant_payload(tool_ids, public_base_url, credential_id)
    assistant = api_post("/assistant", assistant_payload, api_key)
    print(json.dumps({
        "assistant_id": assistant.get("id"),
        "tool_ids": tool_ids,
        "webhook": public_base_url.rstrip("/") + "/vapi/webhook",
    }, indent=2))


if __name__ == "__main__":
    main()
