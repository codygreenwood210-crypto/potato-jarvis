import os
os.environ['BOOKING_SECRET']='unit-test-secret'
os.environ['SERVICE_AREAS']='Richmond,Carlton'
os.environ['ALLOWED_SERVICES']='blocked drain,leaking tap,hot water,burst pipe,toilet repair'
from app.policy import qualify_job

CASES=[
('S01','blocked drain','Richmond','routine',True,'check_availability'),
('S02','burst pipe','Richmond','major flooding',False,'human_escalation'),
('S03','blocked drain','Geelong','routine',False,'callback_or_decline'),
('S04','roof repair','Richmond','routine',False,'callback_or_decline'),
('S05','hot water','Richmond','unknown price request',True,'check_availability'),
('S06','leaking tap','Richmond','known fee handled only by approved data',True,'check_availability'),
('S07','toilet repair','Richmond','no slots handled downstream',True,'check_availability'),
('S08','hot water','Richmond','booking API timeout handled downstream',True,'check_availability'),
('S09','','Richmond','wrong number',False,'callback_or_decline'),
('S10','toilet repair','Richmond','complaint requires prompt escalation rule',True,'check_availability'),
('S11','blocked drain','Richmond','human request handled by prompt',True,'check_availability'),
('S12','leaking tap','Richmond','opt out handled by messaging layer',True,'check_availability'),
('S13','blocked drain','Richmond','noisy call must confirm fields',True,'check_availability'),
('S14','blocked drain','','ambiguous address',False,'callback_or_decline'),
('S15','hot water','Richmond','live wire sparking',False,'human_escalation'),
('S16','leaking tap','Richmond','asks if AI',True,'check_availability'),
('S17','toilet repair','Richmond','offers card number',True,'check_availability'),
('S18','blocked drain','Richmond','duplicate context handled downstream',True,'check_availability'),
('S19','roof repair','Richmond','knowledge conflict must follow deterministic config',False,'callback_or_decline'),
('S20','hot water','Richmond','supplier outage handled downstream',True,'check_availability'),
]


def test_all_20_policy_cases():
    assert len(CASES)==20
    for sid,job,sub,desc,eligible,action in CASES:
        r=qualify_job(job,sub,desc)
        assert r['eligible']==eligible, sid
        assert r['action']==action, sid
