import base64
import hashlib
import hmac
import json
from .config import settings


def _key() -> bytes:
    return settings.booking_secret.encode('utf-8')


def sign_slot(payload: dict) -> str:
    raw = json.dumps(payload, separators=(',', ':'), sort_keys=True).encode('utf-8')
    sig = hmac.new(_key(), raw, hashlib.sha256).digest()
    return (
        base64.urlsafe_b64encode(raw).decode().rstrip('=')
        + '.'
        + base64.urlsafe_b64encode(sig).decode().rstrip('=')
    )


def verify_slot(token: str) -> dict:
    a, b = token.split('.', 1)
    raw = base64.urlsafe_b64decode(a + '=' * (-len(a) % 4))
    sig = base64.urlsafe_b64decode(b + '=' * (-len(b) % 4))
    expected = hmac.new(_key(), raw, hashlib.sha256).digest()
    if not hmac.compare_digest(sig, expected):
        raise ValueError('invalid slot token')
    return json.loads(raw)
