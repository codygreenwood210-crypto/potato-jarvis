# Booked On Site — START HERE

**Status:** PRE-REVENUE / LAUNCH BLUEPRINT  
**Date:** 2026-09-21  
**Mission:** Build a low-touch, recurring-revenue service for Australian trade/local-service businesses that recovers missed inbound opportunities by answering calls, qualifying jobs, booking only verified availability, escalating exceptions, and logging outcomes.

## Canonical Drive records

- Company Operating System: https://docs.google.com/document/d/11p84O1Ef3_wQx8r7JI8QWtPx2P2gqX1lTb5oox47dMk/edit
- Team Training & Certification Manual: https://docs.google.com/document/d/1OcDV5P-0hk1VRsuD9QJyZHdaKlW-AwV2vMgB-2bHyTk/edit

## Current commercial hypothesis

Launch niche: Australian plumbers, electricians, HVAC/refrigeration contractors, locksmiths, roofers, pest-control operators, landscapers, cleaners and similar field-service businesses.

Working launch pricing:
- Founding Pilot: A$299 setup + A$199/month, first five suitable customers.
- Core: A$499 setup + A$349/month.
- Pro: A$799 setup + A$599/month.
- Usage caps/overages must be explicit; no unsupported "unlimited" promise.

Pricing, brand, provider stack, conversion assumptions and margin targets are PROVISIONAL until tested with real supplier accounts and customers.

## Product promise

Sell recovered opportunity, not "AI." The service should:
1. Accept eligible inbound calls forwarded from the client's existing number.
2. Use only client-approved information and deterministic service rules.
3. Capture required caller/job details.
4. Check real calendar/tool availability.
5. Call a booking confirmed only after tool confirmation.
6. Escalate urgent, uncertain or human-requested calls.
7. Log and notify the client.
8. Follow up only where expected/consented and legally appropriate.

## Hard safety rules

Never invent prices, discounts, licences, insurance, warranties, service areas, staff, arrival times or availability.
Never give hazardous repair instructions.
Never claim booking success after a failed/unknown tool result.
Never ignore opt-outs.
Never claim compliance, revenue, profit, customer results, production readiness or passive income without evidence.
Keep secrets and credentials out of canonical records.

## Initial architecture

- Voice-agent layer: Retell AI or Vapi after live AU testing.
- Telephony: provider-native or Twilio/SIP/BYOC as client requirements dictate.
- Orchestration: n8n.
- Data: Supabase/Postgres or equivalent auditable store.
- Calendar: Google Calendar initially, later justified job-management integrations.
- Billing: PayPal Australia after user-controlled account setup/authorization.
- Monitoring: webhook retries, daily health checks, usage alerts, supplier incident paths.
- Mandatory provider adapters so suppliers can be swapped.

## Company pod

**SRO:** Venture.

Supporting operators:
- Nova / Atlas — mission control and user-facing synthesis.
- Margin / Vault — pricing, cash controls, margin.
- Forge — product delivery.
- Cortex / Agent — voice-agent behavior and tool discipline.
- Automate / Toolsmith — integrations and workflow reliability.
- Beacon / Radar / Hook / Closer — positioning, acquisition and sales.
- Guard / Privacy / Trust — privacy, security and communications governance.
- QA-11 — reproducible tests and regression evidence.
- Metric / Lab — KPI truth and experiments.
- Judge — independent acceptance.
- Archivist — continuity and provenance.

The Team Training & Certification Manual contains shared source material, S01-S20 synthetic scenarios, role-specific drills, pass criteria and client-specific training requirements.

## Judge state

**PROVISIONAL PASS — PRE-REVENUE OPERATING DESIGN ONLY.**

Production certification is withheld until:
- supplier/payment accounts exist;
- a production-capable AU number/voice path exists;
- QA-11 runs the live integration matrix;
- a real-number end-to-end client test passes;
- at least one real customer onboarding is tested;
- variable costs and support burden are measured.

## First real success gate

Do not call the company successful merely because documents, prompts, demos or automations exist.

First meaningful commercial proof requires:
1. one real Australian trade/local-service business pays;
2. its actual number routes eligible calls into the production system;
3. the agent safely handles real inbound traffic under that client's approved rules;
4. at least one meaningful business outcome is recorded;
5. variable cost and support burden are measured.

Until then, Booked On Site remains PRE-REVENUE.

## Next execution sequence

1. Final brand/domain/business-name/trademark check.
2. User-controlled business/ABN/GST and banking/payment decisions as applicable.
3. Create/authorize chosen voice, telephony and billing supplier accounts.
4. Build one production-like trade demo with a test calendar and audit logging.
5. QA S01-S20 plus provider/network failure cases.
6. Build simple landing/demo/checkout/onboarding flow.
7. Recruit no more than five founding pilots through compliant channels.
8. Onboard first customer manually while logging friction.
9. Automate only repeated proven steps.
10. Judge re-evaluates production readiness and economics.

**Trust rule:** JARVIS, NEVER ULTRON. Evidence over confidence. Never fake capability or victory.
