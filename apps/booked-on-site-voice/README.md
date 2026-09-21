# Booked On Site Voice Backend

Production-candidate backend for Vapi + Twilio voice reception, deterministic qualification, Google Calendar booking, Gmail notifications and safe escalation.

## Evidence state
- TEST VERIFIED: local deterministic policy/tool webhook tests.
- 12 automated tests currently pass.
- NOT LIVE VERIFIED: Vapi account, Twilio Australian number/KYC, Google production OAuth, deployed public webhook, real phone call.

## Vercel
This folder is ready to import as a Vercel project with Root Directory set to `apps/booked-on-site-voice`.
The FastAPI entrypoint is `app.main:app` via `pyproject.toml`.

## Local run
```bash
python -m venv .venv
. .venv/bin/activate
pip install -r requirements.txt
cp .env.example .env
uvicorn app.main:app --reload
PYTHONPATH=. pytest -q
```

## Safety
`check_availability` returns signed slot tokens. `book_appointment` requires one exact signed token and re-checks the slot before creating the Calendar event. Production webhook calls fail closed until real secrets, Google OAuth and an escalation number are configured.
