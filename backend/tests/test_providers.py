import asyncio
import json

import httpx
import pytest

from backend import providers


def _provider_with_transport(handler):
    provider = providers.OpenAIResponsesProvider("test-key", "https://api.openai.com/v1", timeout_seconds=5)

    async def install_and_run(call):
        provider._client = httpx.AsyncClient(transport=httpx.MockTransport(handler), timeout=5)
        provider._client_loop = asyncio.get_running_loop()
        try:
            return await call(provider)
        finally:
            await provider.aclose()

    return provider, install_and_run


def test_provider_reuses_one_client_within_event_loop():
    async def run():
        provider = providers.OpenAIResponsesProvider("test-key", "https://api.openai.com/v1", timeout_seconds=5)
        first = await provider._get_client()
        second = await provider._get_client()
        assert first is second
        await provider.aclose()
        assert provider._client is None
    asyncio.run(run())


def test_provider_structured_output_is_sent_in_text_format():
    seen = {}

    async def handler(request):
        seen.update(json.loads(request.content))
        return httpx.Response(200, json={"id": "r1", "output": []})

    _, runner = _provider_with_transport(handler)

    async def call(provider):
        return await provider.responses(
            "plan this",
            text={"format": {"type": "json_schema", "name": "plan", "strict": True, "schema": {"type": "object"}}},
        )

    result = asyncio.run(runner(call))
    assert result["id"] == "r1"
    assert seen["text"]["format"]["type"] == "json_schema"
    assert seen["text"]["format"]["strict"] is True


def test_provider_nonstream_response_is_bounded():
    async def handler(request):
        return httpx.Response(200, content=b"x" * (providers.MAX_PROVIDER_RESPONSE_BYTES + 1))

    _, runner = _provider_with_transport(handler)

    async def call(provider):
        with pytest.raises(RuntimeError, match="response exceeded"):
            await provider.responses("hello")

    asyncio.run(runner(call))


def test_provider_retry_after_is_honored(monkeypatch):
    attempts = 0
    sleeps = []

    async def handler(request):
        nonlocal attempts
        attempts += 1
        if attempts == 1:
            return httpx.Response(429, headers={"Retry-After": "1.25"}, text="rate limited")
        return httpx.Response(200, json={"id": "ok", "output": []})

    async def fake_sleep(seconds):
        sleeps.append(seconds)

    monkeypatch.setattr(providers.asyncio, "sleep", fake_sleep)
    _, runner = _provider_with_transport(handler)

    async def call(provider):
        return await provider.responses("hello")

    result = asyncio.run(runner(call))
    assert result["id"] == "ok"
    assert attempts == 2
    assert sleeps == [1.25]


def test_provider_stream_has_total_byte_limit(monkeypatch):
    monkeypatch.setattr(providers, "MAX_PROVIDER_STREAM_BYTES", 10)

    async def handler(request):
        return httpx.Response(200, text='data: {"type":"response.output_text.delta","delta":"way-too-long"}\n\n')

    _, runner = _provider_with_transport(handler)

    async def call(provider):
        with pytest.raises(RuntimeError, match="stream exceeded"):
            async for _ in provider.stream_responses("hello"):
                pass

    asyncio.run(runner(call))


def test_provider_timeout_configuration_fails_closed(monkeypatch):
    monkeypatch.setenv("OPENAI_API_KEY", "x")
    monkeypatch.setenv("OPENAI_TIMEOUT_SECONDS", "not-a-number")
    with pytest.raises(RuntimeError, match="must be a number"):
        providers.OpenAIResponsesProvider.from_environment()
