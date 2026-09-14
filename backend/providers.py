from __future__ import annotations

import asyncio
import os
from typing import Any, AsyncIterator, Protocol
from urllib.parse import urlparse

import httpx
import json


class AIProvider(Protocol):
    async def responses(
        self,
        input_items: Any,
        *,
        tools: list[dict[str, Any]] | None = None,
        model: str | None = None,
        previous_response_id: str | None = None,
    ) -> dict[str, Any]: ...

    async def stream_responses(
        self,
        input_items: Any,
        *,
        tools: list[dict[str, Any]] | None = None,
        model: str | None = None,
        previous_response_id: str | None = None,
    ) -> AsyncIterator[dict[str, Any]]: ...


def validate_provider_response(payload: Any) -> dict[str, Any]:
    if not isinstance(payload, dict):
        raise RuntimeError("Provider returned a non-object response")
    output = payload.get("output")
    if output is not None and not isinstance(output, list):
        raise RuntimeError("Provider output must be a list")
    return payload


def decode_stream_payload(payload: str, event_type: str | None = None) -> dict[str, Any]:
    try:
        item = json.loads(payload)
    except json.JSONDecodeError:
        return {"type": event_type or "unknown", "raw": payload[:2000]}
    if not isinstance(item, dict):
        return {"type": event_type or "unknown", "raw": payload[:2000]}
    if event_type and not item.get("type"):
        item["type"] = event_type
    return item


class OpenAIResponsesProvider:
    def __init__(self, api_key: str, base_url: str, timeout_seconds: float = 120.0) -> None:
        if not api_key:
            raise RuntimeError("OPENAI_API_KEY is not configured")
        normalized_base = base_url.strip().rstrip("/")
        parsed = urlparse(normalized_base)
        if parsed.scheme not in {"http", "https"} or not parsed.hostname:
            raise RuntimeError("OPENAI_BASE must be an absolute HTTP(S) URL")
        if parsed.username or parsed.password or parsed.query or parsed.fragment:
            raise RuntimeError("OPENAI_BASE must not contain credentials, a query, or a fragment")
        environment = os.getenv("POTATO_ENV", "development").strip().lower()
        if environment == "production" and parsed.scheme != "https":
            raise RuntimeError("OPENAI_BASE must use HTTPS in production")
        self.api_key = api_key
        self.base_url = normalized_base
        self.timeout_seconds = timeout_seconds

    @classmethod
    def from_environment(cls) -> "OpenAIResponsesProvider":
        return cls(
            api_key=os.getenv("OPENAI_API_KEY", "").strip(),
            base_url=os.getenv("OPENAI_BASE", "https://api.openai.com/v1"),
            timeout_seconds=float(os.getenv("OPENAI_TIMEOUT_SECONDS", "120")),
        )

    async def stream_responses(
        self,
        input_items: Any,
        *,
        tools: list[dict[str, Any]] | None = None,
        model: str | None = None,
        previous_response_id: str | None = None,
    ) -> AsyncIterator[dict[str, Any]]:
        body: dict[str, Any] = {
            "model": model or os.getenv("OPENAI_MODEL", "gpt-5.6-luna"),
            "input": input_items,
            "stream": True,
        }
        if tools:
            body["tools"] = tools
        if previous_response_id:
            body["previous_response_id"] = previous_response_id
        timeout = httpx.Timeout(timeout=self.timeout_seconds, connect=min(self.timeout_seconds, 20.0), read=self.timeout_seconds, write=min(self.timeout_seconds, 30.0), pool=min(self.timeout_seconds, 20.0))
        async with httpx.AsyncClient(timeout=timeout) as client:
            async with client.stream(
                "POST",
                f"{self.base_url}/responses",
                headers={"Authorization": f"Bearer {self.api_key}", "Content-Type": "application/json", "Accept": "text/event-stream"},
                json=body,
            ) as response:
                if response.status_code >= 400:
                    detail = (await response.aread()).decode("utf-8", errors="replace")[:1500].replace("\n", " ")
                    raise RuntimeError(f"OpenAI HTTP {response.status_code}: {detail}")
                event_type = None
                data_lines: list[str] = []
                async for line in response.aiter_lines():
                    if line.startswith("event:"):
                        event_type = line[6:].strip()
                    elif line.startswith("data:"):
                        data_lines.append(line[5:].lstrip())
                    elif not line.strip() and data_lines:
                        payload = "\n".join(data_lines)
                        data_lines = []
                        if payload == "[DONE]":
                            yield {"type": "done"}
                            break
                        yield decode_stream_payload(payload, event_type)
                        event_type = None
                if data_lines:
                    payload = "\n".join(data_lines)
                    if payload and payload != "[DONE]":
                        yield decode_stream_payload(payload, event_type)

    async def responses(
        self,
        input_items: Any,
        *,
        tools: list[dict[str, Any]] | None = None,
        model: str | None = None,
        previous_response_id: str | None = None,
    ) -> dict[str, Any]:
        body: dict[str, Any] = {
            "model": model or os.getenv("OPENAI_MODEL", "gpt-5.6-luna"),
            "input": input_items,
        }
        if tools:
            body["tools"] = tools
        if previous_response_id:
            body["previous_response_id"] = previous_response_id
        last_error: Exception | None = None
        for attempt in range(3):
            try:
                timeout = httpx.Timeout(timeout=self.timeout_seconds, connect=min(self.timeout_seconds, 20.0), read=self.timeout_seconds, write=min(self.timeout_seconds, 30.0), pool=min(self.timeout_seconds, 20.0))
                async with httpx.AsyncClient(timeout=timeout) as client:
                    response = await client.post(
                        f"{self.base_url}/responses",
                        headers={"Authorization": f"Bearer {self.api_key}", "Content-Type": "application/json"},
                        json=body,
                    )
                if response.status_code in {408, 429} or response.status_code >= 500:
                    detail = response.text[:1_500].replace("\n", " ")
                    last_error = RuntimeError(f"OpenAI HTTP {response.status_code}: {detail}")
                    if attempt < 2:
                        await asyncio.sleep(0.5 * (2 ** attempt))
                        continue
                    raise last_error
                if response.status_code >= 400:
                    detail = response.text[:1_500].replace("\n", " ")
                    raise RuntimeError(f"OpenAI HTTP {response.status_code}: {detail}")
                return validate_provider_response(response.json())
            except (httpx.TimeoutException, httpx.NetworkError) as exc:
                last_error = exc
                if attempt < 2:
                    await asyncio.sleep(0.5 * (2 ** attempt))
                    continue
                raise
        raise last_error or RuntimeError("OpenAI request failed")


def provider_from_environment() -> AIProvider:
    return OpenAIResponsesProvider.from_environment()
