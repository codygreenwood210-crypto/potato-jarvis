import os
os.environ['BOOKING_SECRET']='unit-test-secret'
os.environ['SERVICE_AREAS']='Richmond,Carlton'
os.environ['ALLOWED_SERVICES']='blocked drain,leaking tap,hot water,burst pipe,toilet repair'
from app.vapi import process_vapi_message


def call(name,args):
    return {'message':{'type':'tool-calls','toolCallList':[{'id':'call_1','name':name,'parameters':args}]}}


def test_vapi_success_format():
    r=process_vapi_message(call('qualify_job',{'job_type':'hot water','suburb':'Richmond','description':'no hot water'}))
    assert list(r)==['results']
    assert r['results'][0]['toolCallId']=='call_1'
    assert isinstance(r['results'][0]['result'],str)
    assert '\n' not in r['results'][0]['result']


def test_unknown_tool_returns_error_but_response_shape_ok():
    r=process_vapi_message(call('make_up_a_booking',{}))
    assert r['results'][0]['toolCallId']=='call_1'
    assert 'error' in r['results'][0]
