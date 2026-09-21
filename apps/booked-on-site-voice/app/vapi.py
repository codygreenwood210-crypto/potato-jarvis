import json
from typing import Any, Dict
from .policy import qualify_job, build_slots, validate_booking_token
from .google_api import calendar, notifier
from .config import settings


def one_line(obj: Any) -> str:
    return json.dumps(obj, separators=(',', ':'), ensure_ascii=False).replace('\n', ' ')


def handle_tool(name: str, args: dict) -> dict:
    if name == 'qualify_job':
        return qualify_job(args.get('job_type', ''), args.get('suburb', ''), args.get('description', ''))
    if name == 'check_availability':
        busy = calendar.freebusy(args['window_start'], args['window_end'])
        return {'slots': build_slots(
            args['window_start'],
            args['window_end'],
            busy,
            int(args.get('duration_minutes', 60)),
        )}
    if name == 'book_appointment':
        payload = validate_booking_token(args['slot_token'])
        busy = calendar.freebusy(payload['start'], payload['end'])
        if busy:
            return {'booked': False, 'reason': 'slot_no_longer_available'}
        summary = f"{args.get('job_type', 'Service')} - {args.get('caller_name', 'Caller')}"
        desc = (
            f"Phone: {args.get('caller_phone', '')}\n"
            f"Address/Suburb: {args.get('address', '')}\n"
            f"Notes: {args.get('notes', '')}"
        )
        ev = calendar.create_event(payload['start'], payload['end'], summary, desc)
        return {'booked': True, 'event_id': ev.get('id'), 'start': payload['start'], 'end': payload['end']}
    if name == 'notify_business':
        subject = f"Booked On Site lead: {args.get('caller_name', 'Caller')}"
        body = args.get('summary', 'No summary supplied')
        notifier.send(settings.notification_email, subject, body)
        return {'sent': True}
    if name == 'human_escalation':
        return {
            'escalate': True,
            'phone': settings.human_escalation_phone or None,
            'instruction': 'Collect callback details and tell the caller a person will handle this. Do not provide repair advice.',
        }
    raise ValueError(f'unknown tool: {name}')


def process_vapi_message(body: Dict[str, Any]) -> Dict[str, Any]:
    msg = body.get('message') or {}
    mtype = msg.get('type')
    if mtype != 'tool-calls':
        return {'received': True}
    calls = msg.get('toolCallList') or []
    if not calls and msg.get('toolWithToolCallList'):
        calls = [x.get('toolCall', {}) | {'name': x.get('name')} for x in msg['toolWithToolCallList']]
    results = []
    for call in calls:
        call_id = call.get('id', '')
        name = call.get('name') or (call.get('function') or {}).get('name')
        args = call.get('parameters') or call.get('arguments') or (call.get('function') or {}).get('arguments') or {}
        if isinstance(args, str):
            try:
                args = json.loads(args)
            except Exception:
                args = {}
        try:
            result = handle_tool(name, args)
            results.append({'toolCallId': call_id, 'result': one_line(result)})
        except Exception as e:
            results.append({'toolCallId': call_id, 'error': str(e).replace('\n', ' ')[:400]})
    return {'results': results}
