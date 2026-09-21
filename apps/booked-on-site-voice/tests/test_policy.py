import os
os.environ['BOOKING_SECRET']='unit-test-secret'
os.environ['SERVICE_AREAS']='Richmond,Carlton'
os.environ['ALLOWED_SERVICES']='blocked drain,leaking tap,hot water,burst pipe,toilet repair'
from app.policy import qualify_job, build_slots, validate_booking_token
from datetime import datetime, timedelta
from zoneinfo import ZoneInfo


def test_s01_routine_eligible():
    r=qualify_job('blocked drain','Richmond','kitchen sink blocked')
    assert r['eligible'] and r['action']=='check_availability'


def test_s02_hazard_escalates():
    r=qualify_job('burst pipe','Richmond','major flooding near switchboard')
    assert not r['eligible'] and r['action']=='human_escalation'


def test_s03_out_of_area():
    r=qualify_job('blocked drain','Geelong','')
    assert r['reason']=='outside_service_area'


def test_s04_unsupported_service():
    r=qualify_job('roof repair','Richmond','')
    assert r['reason']=='unsupported_service'


def test_s07_no_slots_when_busy():
    tz=ZoneInfo('Australia/Melbourne')
    now=datetime.now(tz)
    d=now
    while d.weekday()>=5:
        d += timedelta(days=1)
    start=d.replace(hour=9,minute=0,second=0,microsecond=0)
    end=start+timedelta(hours=2)
    busy=[{'start':start.isoformat(),'end':end.isoformat()}]
    assert build_slots(start.isoformat(),end.isoformat(),busy,60)==[]


def test_slot_token_tamper_resistant():
    tz=ZoneInfo('Australia/Melbourne')
    d=datetime.now(tz)+timedelta(days=1)
    while d.weekday()>=5:
        d += timedelta(days=1)
    start=d.replace(hour=9,minute=0,second=0,microsecond=0)
    end=start+timedelta(hours=2)
    slots=build_slots(start.isoformat(),end.isoformat(),[],60)
    token=slots[0]['slot_token']
    assert validate_booking_token(token)['start']==slots[0]['start']
    try:
        validate_booking_token(token[:-1]+'A')
        assert False, 'tamper should fail'
    except Exception:
        pass
