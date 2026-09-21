# Booked On Site Voice Backend

Architecture: Vapi voice assistant + Twilio Australian number + Booked On Site FastAPI backend + Google Calendar + Gmail notifications.

## Evidence state
- TEST VERIFIED: deterministic policy/tool webhook tests.
- 9 pytest tests passed.
- S01-S20 policy matrix present and passing at the deterministic policy layer.
- NOT LIVE VERIFIED: Vapi account, Twilio AU number/KYC, Google production OAuth, deployed public webhook, real phone call.

## Safety architecture
- Service and suburb eligibility are deterministic.
- Hazard language routes to human escalation.
- Availability comes only from the Calendar adapter.
- Availability results contain signed slot tokens.
- Booking requires an exact signed token and re-checks the slot before creating the event.
- The model cannot submit an arbitrary datetime and call it booked.
- Payment-card collection is prohibited.
- Audio recording is disabled by default; transcription is the production default with disclosure and minimal retention.

## Canonical code artifact
Google Drive: https://drive.google.com/file/d/1WVRpljDjm9LKBYFdIUiwcIwQgS4ql4Y8/view

The ZIP contains the FastAPI backend, Google Calendar/Gmail adapters, Vapi webhook handler, tool schemas, assistant prompt, environment template and automated tests.

## Activation blockers
1. Vapi account/API key.
2. Twilio account with Australian KYC approved.
3. Australian number.
4. Google OAuth credentials for production Calendar/Gmail API use.
5. Public deployment URL and Vapi webhook credential.
6. Live real-number test + live S01-S20 conversational QA.
