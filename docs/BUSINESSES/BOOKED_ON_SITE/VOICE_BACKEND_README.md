# Booked On Site Voice Backend

Architecture: Vapi voice assistant + Twilio Australian number + Booked On Site FastAPI backend + Google Calendar + Gmail notifications.

## Evidence state
- TEST VERIFIED: deterministic policy/tool webhook tests.
- 12 pytest tests passed on hardened V0.2 source.
- S01-S20 policy matrix present and passing at the deterministic policy layer.
- Google Calendar create/delete and Gmail send permissions were live-tested through the connected Booked On Site account.
- NOT LIVE VERIFIED: Vapi account, Twilio AU number/KYC, production Google OAuth, public deployment, real phone call.

## Safety architecture
- Service and suburb eligibility are deterministic.
- Hazard language routes to human escalation.
- Availability comes only from the Calendar adapter.
- Availability results contain signed slot tokens.
- Booking requires an exact signed token and re-checks the slot before creating the event.
- The model cannot submit an arbitrary datetime and call it booked.
- Payment-card collection is prohibited.
- Audio recording is disabled by default; transcription is the production default with disclosure and minimal retention.
- Production Vapi webhook calls fail closed until real configuration is present.

## Canonical source
- GitHub source: apps/booked-on-site-voice
- Vercel root directory: apps/booked-on-site-voice
- FastAPI entrypoint: app.main:app
- Hardened V0.2 ZIP: https://drive.google.com/file/d/1Cr6wu3GMOS1hheMSSBWZHibGpdT7_gm9/view
- SHA-256: 94a3e651a883c2b2aed055fbf4ba2850c509e9bed749dfb20a7f3594778d8d5c

## Activation blockers
1. Writable Vercel project/deployment permission or a Vercel project imported from this GitHub root.
2. Vapi account/API key.
3. Twilio account with Australian KYC approved.
4. Australian number.
5. Google OAuth credentials for production Calendar/Gmail API use.
6. Production environment secrets.
7. Live real-number test + live S01-S20 conversational QA.
