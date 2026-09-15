from __future__ import annotations

import asyncio
from email.utils import parsedate_to_datetime
import json
import os
import random
from datetime import datetime, timezone
from typing import Any, AsyncIterator, Protocol
from urllib.parse import urlparse

import httpx


MAX_PROVIDER_RESPONSE_BYTES = 2_000_000
MAX_PROVIDER_ERROR_BYTES = 64_000
MAX_PROVIDER_STREAM_BYTES = 4_000_000
MAX_PROVIDER_SSE_EVENT_BYTES = 512_000
MAX_RETRY_AFTER_SECONDS = 30.0


class AIProvider(Protocol):
    async def responses(
        self,
        input_items: Any,
        *,
        tools: list[dict[str, Any]] | None = None,
        text: dict[str, Any] | None = None,
        model: str | None = None,
        previous_response_id: str | None = None,
    ) -> dict[str, Any]: ...

    async def stream_responses(
        self,
        input_items: Any,
        *,
        tools: list[dict[str, Any]] | None = None,
        text: dict[str, Any] | None = None,
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


def _validated_timeout(value: str | float) -> float:
    try:
        timeout = float(value)
    except (TypeError, ValueError) as exc:
        raise RuntimeError("OPENAI_TIMEOUT_SECONDS must be a number") from exc
    if not 1.0 <= timeout <= 600.0:
        raise RuntimeError("OPENAI_TIMEOUT_SECONDS must be between 1 and 600 seconds")
    return timeout


def _retry_after_seconds(value: str | None) -> float | None:
    if not value:
        return None
    raw = value.strip()
    try:
        seconds = float(raw)
    except ValueError:
        try:
            target = parsedate_to_datetime(raw)
            if target.tzinfo is None:
                target = target.replace(tzinfo=timezone.utc)
            seconds = (target - datetime.now(timezone.utc)).total_seconds()
        except (TypeError, ValueError, OverflowError):
            return None
    return max(0.0, min(seconds, MAX_RETRY_AFTER_SECONDS))


async def _read_bounded_async(response: httpx.Response, limit: int) -> bytes:
    chunks: list[bytes] = []
    total = 0
    async for chunk in response.aiter_bytes():
        total += len(chunk)
        if total > limit:
            raise RuntimeError(f"Provider response exceeded {limit} bytes")
        chunks.append(chunk)
    return b"".join(chunks)


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
        self.timeout_seconds = _validated_timeout(timeout_seconds)
        self._client: httpx.AsyncClient | None = None
        self._client_loop: asyncio.AbstractEventLoop | None = None

    @classmethod
    def from_environment(cls) -> "OpenAIResponsesProvider":
        return cls(
            api_key=os.getenv("OPENAI_API_KEY", "").strip(),
            base_url=os.getenv("OPENAI_BASE", "https://api.openai.com/v1"),
            timeout_seconds=_validated_timeout(os.getenv("OPENAI_TIMEOUT_SECONDS", "120")),
        )

    def _timeout(self) -> httpx.Timeout:
        return httpx.Timeout(
            timeout=self.timeout_seconds,
            connect=min(self.timeout_seconds, 20.0),
            read=self.timeout_seconds,
            write=min(self.timeout_seconds, 30.0),
            pool=min(self.timeout_seconds, 20.0),
        )

    async def _get_client(self) -> httpx.AsyncClient:
        loop = asyncio.get_running_loop()
        if self._client is not None and self._client_loop is not loop:
            await self._client.aclose()
            self._client = None
            self._client_loop = None
        if self._client is None:
            self._client = httpx.AsyncClient(timeout=self._timeout(), follow_redirects=False)
            self._client_loop = loop
        return self._client

    async def aclose(self) -> None:
        if self._client is not None:
            await self._client.aclose()
            self._client = None
            self._client_loop = None

    def _body(
        self,
        input_items: Any,
        *,
        tools: list[dict[str, Any]] | None,
        text: dict[str, Any] | None,
        model: str | None,
        previous_response_id: str | None,
        stream: bool,
    ) -> dict[str, Any]:
        body: dict[str, Any] = {
            "model": model or os.getenv("OPENAI_MODEL", "gpt-5.6-luna"),
            "input": input_items,
        }
        if stream:
            body["stream"] = True
        if tools:
            body["tools"] = tools
        if text:
            body["text"] = text
        if previous_response_id:
            body["previous_response_id"] = previous_response_id
        return body

    async def stream_responses(
        self,
        input_items: Any,
        *,
        tools: list[dict[str, Any]] | None = None,
        text: dict[str, Any] | None = None,
        model: str | None = None,
        previous_response_id: str | None = None,
    ) -> AsyncIterator[dict[str, Any]]:
        body = self._body(
            input_items,
            tools=tools,
            text=text,
            model=model,
            previous_response_id=previous_response_id,
            stream=True,
        )
        client = await self._get_client()
        async with client.stream(
            "POST",
            f"{self.base_url}/responses",
            headers={"Authorization": f"Bearer {self.api_key}", "Content-Type": "application/json", "Accept": "text/event-stream"},
            json=body,
        ) as response:
            if response.status_code >= 400:
                raw = await _read_bounded_async(response, MAX_PROVIDER_ERROR_BYTES)
                detail = raw.decode("utf-8", errors="replace")[:1500].replace("\n", " ")
                raise RuntimeError(f"OpenAI HTTP {response.status_code}: {detail}")

            event_type: str | None = None
            data_lines: list[str] = []
            event_bytes = 0
            total_bytes = 0
            async for line in response.aiter_lines():
                encoded_size = len(line.encode("utf-8", errors="replace")) + 1
                total_bytes += encoded_size
                if total_bytes > MAX_PROVIDER_STREAM_BYTES:
                    raise RuntimeError("OpenAI stream exceeded the configured response limit")
                if line.startswith("event:"):
                    event_type = line[6:].strip()
                elif line.startswith("data:"):
                    data = line[5:].lstrip()
                    event_bytes += len(data.encode("utf-8", errors="replace"))
                    if event_bytes > MAX_PROVIDER_SSE_EVENT_BYTES:
                        raise RuntimeError("OpenAI SSE event exceeded the configured event limit")
                    data_lines.append(data)
                elif not line.strip() and data_lines:
                    payload = "\n".join(data_lines)
                    data_lines = []
                    event_bytes = 0
                    if payload == "[DONE]":
                        yield {"type": "done"}
                        return
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
        text: dict[str, Any] | None = None,
        model: str | None = None,
        previous_response_id: str | None = None,
    ) -> dict[str, Any]:
        body = self._body(
            input_items,
            tools=tools,
            text=text,
            model=model,
            previous_response_id=previous_response_id,
            stream=False,
        )
        last_error: Exception | None = None
        for attempt in range(3):
            try:
                client = await self._get_client()
                async with client.stream(
                    "POST",
                    f"{self.base_url}/responses",
                    headers={"Authorization": f"Bearer {self.api_key}", "Content-Type": "application/json"},
                    json=body,
                ) as response:
                    raw = await _read_bounded_async(
                        response,
                        MAX_PROVIDER_ERROR_BYTES if response.status_code >= 400 else MAX_PROVIDER_RESPONSE_BYTES,
                    )
                    detail = raw.decode("utf-8", errors="replace")[:1500].replace("\n", " ")
                    if response.status_code in {408, 429} or response.status_code >= 500:
                        last_error = RuntimeError(f"OpenAI HTTP {response.status_code}: {detail}")
                        if attempt < 2:
                            retry_after = _retry_after_seconds(response.headers.get("Retry-After"))
                            backoff = 0.5 * (2**attempt)
                            delay = retry_after if retry_after is not None else backoff + random.uniform(0.0, 0.2)
                            await asyncio.sleep(delay)
                            continue
                        raise last_error
                    if response.status_code >= 400:
                        raise RuntimeError(f"OpenAI HTTP {response.status_code}: {detail}")
                    try:
                        payload = json.loads(raw)
                    except json.JSONDecodeError as exc:
                        raise RuntimeError("Provider returned invalid JSON") from exc
                    return validate_provider_response(payload)
            except (httpx.TimeoutException, httpx.NetworkError) as exc:
                last_error = exc
                if attempt < 2:
                    await asyncio.sleep(0.5 * (2**attempt) + random.uniform(0.0, 0.2))
                    continue
                raise
        raise last_error or RuntimeError("OpenAI request failed")


_PROVIDER_CACHE_KEY: tuple[str, str, float] | None = None
_PROVIDER_CACHE: OpenAIResponsesProvider | None = None


def provider_from_environment() -> AIProvider:
    global _PROVIDER_CACHE_KEY, _PROVIDER_CACHE
    api_key = os.getenv("OPENAI_API_KEY", "").strip()
    base_url = os.getenv("OPENAI_BASE", "https://api.openai.com/v1")
    timeout = _validated_timeout(os.getenv("OPENAI_TIMEOUT_SECONDS", "120"))
    key = (api_key, base_url.strip().rstrip("/"), timeout)
    if _PROVIDER_CACHE is not None and _PROVIDER_CACHE_KEY == key:
        return _PROVIDER_CACHE
    provider = OpenAIResponsesProvider(api_key=api_key, base_url=base_url, timeout_seconds=timeout)
    _PROVIDER_CACHE_KEY = key
    _PROVIDER_CACHE = provider
    return provider


async def close_cached_provider() -> None:
    global _PROVIDER_CACHE_KEY, _PROVIDER_CACHE
    provider = _PROVIDER_CACHE
    _PROVIDER_CACHE = None
    _PROVIDER_CACHE_KEY = None
    if provider is not None:
        await provider.aclose()
