# POTATO V3.3 security model

## Trust boundaries

- User input is intent, but every field is still validated as data.
- Model output is untrusted and cannot grant itself permissions.
- Web pages, documents, and other external content are untrusted data.
- Tool arguments are normalized and validated before execution.
- Private files are constrained to application storage roots.
- Destructive/device actions require exact-argument approvals.
- Production API authentication is enabled by default and requires a 32+ character bearer token; anonymous mode is rejected when `POTATO_ENV=production`.

## Risk policy

| Risk | Policy |
|---|---|
| 0 | automatic read-only |
| 1 | automatic low-risk |
| 2 | explicit approval |
| 3 | explicit approval + Android biometric/device authentication |
| 4 | always denied |

Approvals are bound to the tool name and SHA-256 hash of the exact normalized JSON arguments and expire after fifteen minutes. Risk-3 approvals require a short-lived challenge signed by an Android Keystore EC key whose use is gated by strong biometric authentication. Approval challenges and one-shot consumption are server-side controls. Plan approvals are scoped to the exact plan step rather than reusable across unrelated requests.

## Network security

Smart-device HTTP URLs are validated. Private/loopback/link-local/reserved device addresses are blocked by default and can be intentionally enabled with `POTATO_ALLOW_PRIVATE_DEVICE_NETWORKS=true`. Device action paths must be explicitly configured and redirects are not followed.

## Production requirements

Use HTTPS, a strong `POTATO_API_TOKEN`, restricted network exposure, protected server storage, OS-level permissions, regular dependency updates, and backups appropriate to the data sensitivity. Never put the OpenAI key in the APK.
