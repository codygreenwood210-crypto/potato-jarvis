from datetime import datetime, timedelta
from zoneinfo import ZoneInfo
from dateutil import parser
from .config import settings
from .security import sign_slot, verify_slot

HAZARD_TERMS = (
    'gas smell','live wire','sparking','fire','smoke','electrocution',
    'carbon monoxide','major flooding','burst main'
)


def normalise(s: str) -> str:
    return ' '.join((s or '').strip().lower().split())


def qualify_job(job_type: str, suburb: str, description: str = '') -> dict:
    jt, sub, desc = normalise(job_type), normalise(suburb), normalise(description)
    hazard = any(term in f'{jt} {desc}' for term in HAZARD_TERMS)
    service_ok = any(s in jt or jt in s for s in settings.allowed_service_list) if jt else False
    area_ok = sub in settings.service_area_list if sub else False
    if hazard:
        return {'eligible': False, 'action': 'human_escalation', 'reason': 'hazard_or_emergency_language'}
    if not area_ok:
        return {'eligible': False, 'action': 'callback_or_decline', 'reason': 'outside_service_area'}
    if not service_ok:
        return {'eligible': False, 'action': 'callback_or_decline', 'reason': 'unsupported_service'}
    return {'eligible': True, 'action': 'check_availability', 'reason': 'eligible'}


def build_slots(window_start: str, window_end: str, busy: list, duration_minutes: int = 60) -> list:
    start = parser.isoparse(window_start)
    end = parser.isoparse(window_end)
    tz = ZoneInfo(settings.timezone)
    if start.tzinfo is None:
        start = start.replace(tzinfo=tz)
    if end.tzinfo is None:
        end = end.replace(tzinfo=tz)
    busy_pairs = [(parser.isoparse(x['start']), parser.isoparse(x['end'])) for x in busy]
    slots = []
    cur = start
    step = timedelta(minutes=30)
    dur = timedelta(minutes=duration_minutes)
    while cur + dur <= end and len(slots) < 6:
        local = cur.astimezone(tz)
        if local.weekday() < 5 and 8 <= local.hour and (local + dur).hour <= 18:
            slot_end = cur + dur
            overlap = any(cur < b_end and slot_end > b_start for b_start, b_end in busy_pairs)
            if not overlap:
                payload = {'start': cur.isoformat(), 'end': slot_end.isoformat(), 'duration': duration_minutes}
                slots.append({'start': payload['start'], 'end': payload['end'], 'slot_token': sign_slot(payload)})
        cur += step
    return slots


def validate_booking_token(token: str) -> dict:
    payload = verify_slot(token)
    start = parser.isoparse(payload['start'])
    if start < datetime.now(start.tzinfo) - timedelta(minutes=5):
        raise ValueError('slot token expired')
    return payload
