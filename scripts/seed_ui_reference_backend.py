import json
import urllib.request

BASE = "http://127.0.0.1:8000"


def request(path: str, method: str = "GET", body: dict | None = None) -> dict:
    data = None if body is None else json.dumps(body).encode("utf-8")
    req = urllib.request.Request(
        BASE + path,
        data=data,
        method=method,
        headers={"Content-Type": "application/json", "Accept": "application/json"},
    )
    with urllib.request.urlopen(req, timeout=10) as response:
        raw = response.read().decode("utf-8")
        return json.loads(raw or "{}")


def create(description: str, due_at: str) -> dict:
    return request(
        "/v1/tasks",
        "POST",
        {
            "description": description,
            "priority": "normal",
            "due_at": due_at,
            "recurrence": "none",
            "dependencies": [],
            "notes": "UI reference fixture",
        },
    )


created = [
    create("Review project proposal", "2026-09-16T09:00:00+00:00"),
    create("Call Mom", "2026-09-16T11:00:00+00:00"),
    create("Work on POTATO V5.8 ideas", "2026-09-16T14:30:00+00:00"),
    create("Go for a walk", "2026-09-16T18:00:00+00:00"),
]

first_id = created[0].get("id", "")
if first_id:
    request(f"/v1/tasks/{first_id}", "PATCH", {"status": "completed"})

listed = request("/v1/tasks?limit=100").get("tasks", [])
if len(listed) < 4:
    raise SystemExit(f"Expected at least four UI fixture tasks, got {len(listed)}")
print("POTATO_UI_REFERENCE_TASK_FIXTURE=PASS")
