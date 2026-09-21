# Booked On Site — Vercel Activation

## One-click import
https://vercel.com/new/clone?repository-url=https%3A%2F%2Fgithub.com%2Fcodygreenwood210-crypto%2Fpotato-jarvis%2Ftree%2Fpotato-v5.8-final%2Fapps%2Fbooked-on-site-voice&project-name=booked-on-site-voice

The source is already Vercel-ready.

- Repository: codygreenwood210-crypto/potato-jarvis
- Branch: potato-v5.8-final
- Source/root: apps/booked-on-site-voice
- FastAPI entrypoint: app.main:app

## Required production environment variables
Do not commit secret values.

- ENVIRONMENT=production
- PUBLIC_BASE_URL=<assigned Vercel production URL>
- WEBHOOK_BEARER_TOKEN=<long random secret>
- BOOKING_SECRET=<long random secret>
- BUSINESS_NAME=<client business name>
- TIMEZONE=Australia/Melbourne
- SERVICE_AREAS=<comma separated approved service areas>
- ALLOWED_SERVICES=<comma separated approved service categories>
- HUMAN_ESCALATION_PHONE=<approved callback/escalation phone>
- GOOGLE_CLIENT_ID=<production OAuth client>
- GOOGLE_CLIENT_SECRET=<production OAuth secret>
- GOOGLE_REFRESH_TOKEN=<authorised refresh token>
- GOOGLE_CALENDAR_ID=primary
- NOTIFICATION_EMAIL=bookedonsite@gmail.com
- GMAIL_SENDER=bookedonsite@gmail.com

## Safe initial deployment behavior
Until the required secrets/OAuth/escalation number are present:
- GET /health returns production_ready=false and lists missing configuration keys.
- POST /vapi/webhook returns HTTP 503.
- Placeholder secrets cannot make the service live.

## After deployment
1. Set PUBLIC_BASE_URL to the final deployment URL.
2. Create a Vapi saved Bearer Token credential using the same WEBHOOK_BEARER_TOKEN.
3. Run scripts/provision_vapi.py with VAPI_API_KEY, VAPI_CREDENTIAL_ID and PUBLIC_BASE_URL.
4. Verify /health reports production_ready=true.
5. Complete Twilio Australian KYC and number provisioning.
6. Import/attach the Twilio number to the Vapi assistant.
7. Run the real PSTN end-to-end call and live S01-S20 conversational QA.

Current runtime note (2026-09-21): the connected Vercel app in ChatGPT exposed read-only project state but its documented deploy write action was not available, so project creation still requires the Vercel import authorisation above or an equivalent Vercel token/project write path.
