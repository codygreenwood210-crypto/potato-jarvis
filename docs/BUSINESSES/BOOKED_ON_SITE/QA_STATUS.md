# Booked On Site QA Status

## PASS — automated backend layer
- 9 pytest tests passed.
- S01-S20 policy matrix exists and passes at deterministic policy layer.
- Hazard escalation passes.
- Unsupported service and out-of-area rejection pass.
- HMAC signed booking slot token generation/validation passes.
- Tampered booking token rejection passes.
- Vapi tool-call response structure passes.
- Unknown tool rejection passes.

## NOT YET VERIFIED — live layer
- Natural-language/voice behavior through live Vapi.
- Australian PSTN inbound call through Twilio.
- Vapi-to-backend webhook authentication in production.
- Live Google OAuth Calendar booking from deployed backend.
- Live Gmail notification from deployed backend.
- Real client call forwarding.

Production Judge certification remains withheld until these live gates pass.
