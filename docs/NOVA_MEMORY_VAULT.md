# Nova Memory Vault

**Status:** IMPLEMENTED CORE / LOCAL INITIALIZATION REQUIRED  
**Scope:** Nova-controlled durable memory only.  
**Independence:** This is part of Nova's architecture, not PNOS.

## Purpose

Nova Memory Vault encrypts Nova-controlled durable memory at rest while keeping the decryption key off GitHub, Google Drive, prompts, and ordinary memory files.

The practical rule is:

> **Ciphertext may travel. The key does not.**

Cryptography cannot recognize the abstract identity "Nova." Therefore, "only Nova can read it" is implemented operationally: an **authorized Nova runtime** is granted access to a locally protected master key. The key itself is not committed or synchronized with the memories.

## Cryptography

- Memory encryption: **AES-256-GCM**
- Random 96-bit nonce per encrypted object
- Authentication/integrity provided by GCM
- Recovery-key derivation: **scrypt**
- Recovery master-key wrapping: AES-256-GCM
- Opaque vault filenames: HMAC-SHA256 using the master key

The encrypted envelope exposes only format/version/algorithm/nonce/ciphertext. Logical filenames, content type, content hash, and memory bytes are inside authenticated ciphertext.

## Windows key protection

The local 256-bit master key is wrapped with **Windows DPAPI, current-user scope**.

Default location:

`%LOCALAPPDATA%\Nova\MemoryVault\master_key.dpapi`

The plaintext master key must never be committed to Git, written into a prompt, copied into Google Drive, or stored in ordinary Nova memory.

### Important limitation

Windows user-scope DPAPI protects the key at rest, but software already executing as the same Windows user may be able to invoke DPAPI. This is strong local-at-rest protection, not a hardware-backed proof that a process "is Nova."

A future stronger tier can add TPM/Windows Hello-backed key release through a dedicated local broker.

## Recovery

A user-controlled recovery bundle can wrap the same master key using a recovery passphrase.

The recovery bundle does **not** contain the passphrase.

Keep:
1. encrypted recovery bundle;
2. recovery passphrase;

in separate places.

The user must always retain a recovery path. Nova must not create a design that can permanently lock the user out.

## CLI

Run from the repository root with the Python environment containing `backend/requirements.txt`.

### Initialize

```powershell
python scripts/nova_memory_vault.py init --recovery-bundle "$HOME\Nova-Recovery\nova-recovery.nvr"
```

The passphrase is entered interactively and is never placed on the command line.

### Status

```powershell
python scripts/nova_memory_vault.py status
```

### Encrypt a file

```powershell
python scripts/nova_memory_vault.py seal docs\private-memory.md --opaque-name
```

### Verify without writing plaintext

```powershell
python scripts/nova_memory_vault.py verify <encrypted-file.nvm>
```

### Decrypt

```powershell
python scripts/nova_memory_vault.py open <encrypted-file.nvm> restored.md
```

### Recover on the same/new authorized Windows profile

```powershell
python scripts/nova_memory_vault.py recover "$HOME\Nova-Recovery\nova-recovery.nvr" --overwrite
```

## Safe migration

Migration defaults to a dry run.

```powershell
python scripts/nova_memory_vault.py migrate \
  --source docs\memory \
  --output nova-memory-encrypted \
  --opaque-names
```

Create encrypted copies:

```powershell
python scripts/nova_memory_vault.py migrate \
  --source docs\memory \
  --output nova-memory-encrypted \
  --opaque-names \
  --apply
```

The tool encrypts every file, decrypts it again in memory, compares SHA-256 with the original, and writes an **encrypted** migration index.

Plaintext is **not deleted by default**.

Deletion requires both:

```
--remove-plaintext --confirm REMOVE_PLAINTEXT
```

This is intentional.

## Critical migration rule

**Do not remove Nova's current plaintext canonical recovery records until a future Nova session has a verified way to call the vault decryptor.**

A normal cloud chat cannot access a master key held only on the user's Windows machine unless a secure connector/local runtime exposes an authorized decrypt operation.

Encrypting the canonical bootstrap before that bridge exists would break cross-room recovery.

Migration sequence:

1. initialize local key;
2. create recovery bundle;
3. test encryption/decryption;
4. test recovery after a restart;
5. test a fresh Nova runtime can request authorized decryption;
6. create encrypted copies of canonical memories;
7. verify every encrypted copy;
8. update memory indexes/bootstrap to use the vault bridge;
9. only then remove plaintext current copies.

## Git history warning

Encrypting or deleting the current files in a new Git commit **does not erase their plaintext from older Git history**.

Removing old plaintext from Git history requires history rewriting and force-updating refs. That is consequential, can break clones/commit references, and must be handled as a separate deliberate migration after backups.

## Threat model

The vault protects against:
- GitHub/Drive storage exposing current memory plaintext;
- accidental repository browsing;
- stolen encrypted memory files without the key/passphrase;
- ciphertext modification (authentication failure);
- filenames leaking through encrypted indexes when opaque names are used.

It does not by itself protect against:
- malware already running as the authorized Windows user;
- an attacker controlling the Nova runtime after unlock;
- screen capture while plaintext is being viewed;
- secrets copied into unrelated logs/prompts;
- old plaintext already present in Git history or external backups.

## Tests

Run:

```powershell
python -m pytest backend/tests/test_nova_vault.py -q
```

Tests cover:
- encryption/decryption round trip;
- metadata confidentiality;
- wrong-key failure;
- ciphertext tamper detection;
- opaque filenames;
- recovery bundle restore;
- wrong recovery passphrase;
- file round trip/verification.

Windows DPAPI initialization/recovery should additionally be tested on the user's real Windows account before any plaintext migration.

## Permanent security rule

**Nova's private durable memories are encrypted at rest when the vault path is active. The ciphertext may be stored or synchronized; the master key is kept outside those stores. Plaintext is released only to an authorized runtime for the minimum necessary time. The user retains recovery authority.**

## CI verification — 2026-09-20

GitHub Actions workflow: `.github/workflows/verify-nova-memory-vault.yml`

Verified run:
- run id: `35496031338`
- head commit: `21e7778c6fae918565f9d36732fcc2329e8d78e0`
- conclusion: **success**

Successful jobs:
- Portable crypto tests — Ubuntu
- Portable crypto tests — Windows
- Windows DPAPI smoke test

The CI run verified:
- Python compilation of the vault module and CLI;
- vault unit/security tests;
- AES-GCM round trips;
- wrong-key failure;
- tamper detection;
- recovery-bundle behavior;
- Windows DPAPI master-key wrapping/unwrapping;
- DPAPI-protected key used for a real encrypt/decrypt round trip.

This verifies the implementation itself on clean GitHub-hosted Windows/Linux runners. It does **not** prove the user's own Windows account has initialized its local master key or that existing plaintext memories have been migrated.
