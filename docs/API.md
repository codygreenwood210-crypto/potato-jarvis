# POTATO V5.6 API surface

Authentication is required by default for every endpoint except `GET /v1/health`. Local development can explicitly opt into anonymous mode with `POTATO_ALLOW_ANONYMOUS=true`.

## Core and unified agent

- `GET /v1/health`
- `GET /v1/diagnostics`
- `GET/POST /v1/sessions`
- `GET /v1/sessions/{session_id}/messages`
- `DELETE /v1/sessions/{session_id}`
- `POST /v1/agent/run` — unified chat/plan JARVIS Core entry point
- `POST /v1/chat` — focused conversation compatibility endpoint

## Memory and preferences

- `GET/POST /v1/memory`
- `DELETE /v1/memory/{memory_id}`
- `GET /v1/preferences`
- `PUT /v1/preferences`

## Tools and validation

- `GET /v1/tools`
- `GET /v1/tools/{tool_name}`
- `POST /v1/tools/validate`

## Planning and durable tasks

- `POST /v1/plan`
- `POST /v1/plan/execute`
- `GET /v1/tasks/{task_id}`
- `POST /v1/tasks/{task_id}/cancel`

## Security and tracing

- `GET /v1/approvals`
- `GET /v1/approvals/{approval_id}/challenge`
- `POST /v1/approvals/{approval_id}`
- `POST /v1/security/device-key`
- `GET /v1/trace`
- `GET /v1/trace/{trace_id}`

## Files and vision

- `POST /v1/files`
- `GET /v1/files`
- `GET /v1/files/{file_id}`
- `DELETE /v1/files/{file_id}`
- `POST /v1/vision`

## Agents, automation, devices, and notes

- `POST /v1/agents`
- `GET/POST/DELETE /v1/automations`
- `GET/POST /v1/devices`
- `POST /v1/devices/action`
- `GET /v1/notes`

Device credentials are never returned by `GET /v1/devices`; clients receive only a `token_configured` boolean. Device action paths are allowlisted and reject traversal, query strings, fragments, backslashes, and ambiguous double-leading slashes.

The Android accessibility service is reserved and does not expose arbitrary cross-app clicking, text injection, or screen scraping.

The canonical implementation is `backend.main`. Launch from the repository root with `./backend/run.sh` or `python3 -m uvicorn backend.main:app ...` so the same package import path is used by development, tests, and deployment.
