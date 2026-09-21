# Booked On Site QA Status

## PASS — automated backend layer
- 12 pytest tests passed on the hardened V0.2 source.
- S01-S20 policy matrix exists and passes at deterministic policy layer.
- Hazard escalation passes.
- Unsupported service and out-of-area rejection pass.
- HMAC signed booking slot generation/validation passes.
- Tampered booking token rejection passes.
- Vapi tool-call response structure passes.
- Unknown tool rejection passes.
- Production configuration fails closed: the webhook returns 503 until real secrets, Google OAuth and a human escalation number are configured.
- Health output exposes missing configuration keys without exposing placeholder secret values.
- Gmail operational sending and Google Calendar create/delete permissions were live-tested through the connected account.

## DEPLOYMENT SOURCE
- Vercel-ready source: apps/booked-on-site-voice
- Entry point: app.main:app
- Vercel root directory: apps/booked-on-site-voice
- Hardened Drive package: https://drive.google.com/file/d/1Cr6wu3GMOS1hheMSSBWZHibGpdT7_gm9/view
- Package SHA-256: 94a3e651a883c2b2aed055fbf4ba2850c509e9bed749dfb20a7f3594778d8d5c

## NOT YET VERIFIED — live layer
- Vercel public deployment; connected Vercel MCP currently exposes no writable project/deploy action.
- Natural-language/voice behavior through live Vapi.
- Australian PSTN inbound call through Twilio.
- Vapi-to-backend webhook authentication in production.
- Production Google OAuth Calendar booking from the deployed backend.
- Production Gmail notification from the deployed backend.
- Real client call forwarding.

Production Judge certification remains withheld until these live gates pass.
