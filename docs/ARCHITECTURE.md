# POTATO V3.3 architecture

POTATO separates intelligence from authority.

## Request flow

1. Android sends a request.
2. Backend authenticates it unless anonymous mode is explicitly enabled.
3. Session/message state is persisted.
4. Relevant memories/preferences/history are retrieved, with conservative preference extraction from explicit user phrasing.
5. The Responses API receives the system policy and trusted context.
6. Function calls and optional web search may be requested.
7. Every executable function goes through argument normalization and the security gateway.
8. Approved tools execute asynchronously without blocking the event loop.
9. Tool results are recorded in `tool_runs` plus audit/security tables.
10. Function outputs are returned to the model through the Responses API.
11. High-risk approvals use a server challenge and Android Keystore signature before execution.
11. The conversation state is persisted.

## Planner flow

`request -> model plan -> schema validation -> dependency validation -> persistence -> security -> execution -> verified result -> recovery/report`.

Plans are limited to twelve steps. Dependencies must reference earlier steps, and stored plans are revalidated before execution. Completed steps are resumed rather than repeated after an approval interruption.

## Android flow

`Compose UI -> ViewModel StateFlow -> IO dispatcher -> JarvisApi -> backend -> persisted state -> UI state`.

The API token is encrypted with Android Keystore. High-risk approvals use a separate EC signing key gated by `BIOMETRIC_STRONG`; the server stores only the public key and verifies a single-use challenge signature. Voice and camera resources are lifecycle-scoped, and WorkManager uses the same configured backend URL/token as the foreground app.
