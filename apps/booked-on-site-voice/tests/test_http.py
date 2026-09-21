from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_health_discloses_only_missing_keys_not_values():
    r = client.get('/health')
    assert r.status_code == 200
    body = r.json()
    assert body['ok'] is True
    assert body['production_ready'] is False
    assert 'GOOGLE_CLIENT_ID' in body['missing_configuration']
    assert 'dev-token' not in r.text
    assert 'replace-me' not in r.text


def test_unconfigured_webhook_fails_closed():
    r = client.post(
        '/vapi/webhook',
        headers={'Authorization': 'Bearer dev-token'},
        json={'message': {'type': 'tool-calls', 'toolCallList': []}},
    )
    assert r.status_code == 503
