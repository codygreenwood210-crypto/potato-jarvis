"""Nova Memory Vault.

Local-first authenticated encryption for Nova-controlled durable memory.

Design goals:
- AES-256-GCM authenticated encryption.
- Master key is never stored in plaintext.
- On Windows, the master key is wrapped with user-scope DPAPI.
- Optional passphrase-wrapped recovery bundle using scrypt + AES-GCM.
- Encrypted memory files contain no plaintext logical filename/metadata.
- Opaque file names can be derived with HMAC-SHA256.
- No dependency on PNOS.

This module does not claim that cryptography can recognize an abstract identity.
"Nova-only" access means an authorized Nova runtime is the component granted
access to the locally protected key material.
"""

from __future__ import annotations

import base64
import ctypes
import hashlib
import hmac
import json
import os
import secrets
import tempfile
from ctypes import wintypes
from dataclasses import dataclass
from pathlib import Path
from typing import Any

from cryptography.exceptions import InvalidTag
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from cryptography.hazmat.primitives.kdf.scrypt import Scrypt

VAULT_FORMAT = "nova-memory-vault"
VAULT_VERSION = 1
VAULT_ALGORITHM = "AES-256-GCM"
RECOVERY_FORMAT = "nova-memory-recovery"
RECOVERY_VERSION = 1
RECOVERY_AAD = b"NOVA-MEMORY-RECOVERY-V1"
VAULT_AAD = b"NOVA-MEMORY-VAULT-V1"
KEY_BYTES = 32
NONCE_BYTES = 12
SALT_BYTES = 16


class VaultError(RuntimeError):
    """Base Nova Memory Vault error."""


class VaultNotInitializedError(VaultError):
    """Raised when no local master key has been provisioned."""


class VaultIntegrityError(VaultError):
    """Raised when encrypted data fails authenticated decryption."""


class RecoveryError(VaultError):
    """Raised when recovery data cannot be unlocked."""


@dataclass(frozen=True)
class OpenedMemory:
    data: bytes
    logical_name: str | None
    content_type: str
    sha256: str


class _DataBlob(ctypes.Structure):
    _fields_ = [
        ("cbData", wintypes.DWORD),
        ("pbData", ctypes.POINTER(ctypes.c_byte)),
    ]


def _b64e(data: bytes) -> str:
    return base64.b64encode(data).decode("ascii")


def _b64d(value: str) -> bytes:
    try:
        return base64.b64decode(value.encode("ascii"), validate=True)
    except Exception as exc:
        raise VaultError("Invalid base64 in vault envelope") from exc


def _canonical_json(value: Any) -> bytes:
    return json.dumps(
        value,
        sort_keys=True,
        separators=(",", ":"),
        ensure_ascii=False,
    ).encode("utf-8")


def _best_effort_private_permissions(path: Path) -> None:
    try:
        path.chmod(0o600)
    except OSError:
        pass


def atomic_write(path: Path, data: bytes, *, private: bool = False) -> None:
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    fd, tmp_name = tempfile.mkstemp(prefix=f".{path.name}.", dir=str(path.parent))
    tmp_path = Path(tmp_name)
    try:
        with os.fdopen(fd, "wb") as handle:
            handle.write(data)
            handle.flush()
            os.fsync(handle.fileno())
        if private:
            _best_effort_private_permissions(tmp_path)
        os.replace(tmp_path, path)
        if private:
            _best_effort_private_permissions(path)
    finally:
        if tmp_path.exists():
            try:
                tmp_path.unlink()
            except OSError:
                pass


def _dpapi_protect(data: bytes, description: str = "Nova Memory Vault") -> bytes:
    if os.name != "nt":
        raise VaultError("Windows DPAPI is only available on Windows")

    in_buffer = ctypes.create_string_buffer(data)
    in_blob = _DataBlob(
        len(data),
        ctypes.cast(in_buffer, ctypes.POINTER(ctypes.c_byte)),
    )
    out_blob = _DataBlob()

    crypt32 = ctypes.windll.crypt32
    kernel32 = ctypes.windll.kernel32
    cryptprotect_ui_forbidden = 0x01

    ok = crypt32.CryptProtectData(
        ctypes.byref(in_blob),
        description,
        None,
        None,
        None,
        cryptprotect_ui_forbidden,
        ctypes.byref(out_blob),
    )
    if not ok:
        raise ctypes.WinError()

    try:
        return ctypes.string_at(out_blob.pbData, out_blob.cbData)
    finally:
        kernel32.LocalFree(out_blob.pbData)


def _dpapi_unprotect(data: bytes) -> bytes:
    if os.name != "nt":
        raise VaultError("Windows DPAPI is only available on Windows")

    in_buffer = ctypes.create_string_buffer(data)
    in_blob = _DataBlob(
        len(data),
        ctypes.cast(in_buffer, ctypes.POINTER(ctypes.c_byte)),
    )
    out_blob = _DataBlob()

    crypt32 = ctypes.windll.crypt32
    kernel32 = ctypes.windll.kernel32
    cryptprotect_ui_forbidden = 0x01

    ok = crypt32.CryptUnprotectData(
        ctypes.byref(in_blob),
        None,
        None,
        None,
        None,
        cryptprotect_ui_forbidden,
        ctypes.byref(out_blob),
    )
    if not ok:
        raise ctypes.WinError()

    try:
        return ctypes.string_at(out_blob.pbData, out_blob.cbData)
    finally:
        kernel32.LocalFree(out_blob.pbData)


def default_keystore_path() -> Path:
    if os.name == "nt":
        root = os.environ.get("LOCALAPPDATA")
        if root:
            return Path(root) / "Nova" / "MemoryVault" / "master_key.dpapi"
        return Path.home() / "AppData" / "Local" / "Nova" / "MemoryVault" / "master_key.dpapi"
    return Path.home() / ".local" / "share" / "nova" / "memory-vault" / "master_key.unsupported"


class DPAPIKeyStore:
    """Store a 256-bit vault key protected by Windows user-scope DPAPI."""

    def __init__(self, path: Path | None = None) -> None:
        self.path = Path(path) if path is not None else default_keystore_path()

    @property
    def is_supported(self) -> bool:
        return os.name == "nt"

    @property
    def is_initialized(self) -> bool:
        return self.path.exists()

    def initialize(self, *, overwrite: bool = False) -> bytes:
        if not self.is_supported:
            raise VaultError("DPAPI keystore initialization requires Windows")
        if self.path.exists() and not overwrite:
            raise VaultError(f"Vault key already exists: {self.path}")
        key = secrets.token_bytes(KEY_BYTES)
        self.save_key(key, overwrite=overwrite)
        return key

    def save_key(self, key: bytes, *, overwrite: bool = False) -> None:
        if len(key) != KEY_BYTES:
            raise VaultError("Master key must be exactly 32 bytes")
        if not self.is_supported:
            raise VaultError("DPAPI keystore requires Windows")
        if self.path.exists() and not overwrite:
            raise VaultError(f"Vault key already exists: {self.path}")

        wrapped = _dpapi_protect(key)
        envelope = {
            "format": "nova-memory-dpapi-key",
            "version": 1,
            "scope": "windows-current-user",
            "wrapped_key": _b64e(wrapped),
        }
        atomic_write(self.path, _canonical_json(envelope) + b"\n", private=True)

    def load_key(self) -> bytes:
        if not self.path.exists():
            raise VaultNotInitializedError(
                f"Nova Memory Vault is not initialized: {self.path}"
            )
        try:
            envelope = json.loads(self.path.read_text(encoding="utf-8"))
            if envelope.get("format") != "nova-memory-dpapi-key":
                raise VaultError("Unexpected local key format")
            wrapped = _b64d(envelope["wrapped_key"])
            key = _dpapi_unprotect(wrapped)
        except VaultError:
            raise
        except Exception as exc:
            raise VaultError("Unable to load DPAPI-protected Nova master key") from exc

        if len(key) != KEY_BYTES:
            raise VaultError("Recovered master key has invalid length")
        return key


def _derive_recovery_key(passphrase: str, salt: bytes, *, n: int, r: int, p: int) -> bytes:
    if not passphrase:
        raise RecoveryError("Recovery passphrase cannot be empty")
    kdf = Scrypt(salt=salt, length=KEY_BYTES, n=n, r=r, p=p)
    return kdf.derive(passphrase.encode("utf-8"))


def create_recovery_bundle(master_key: bytes, passphrase: str) -> bytes:
    if len(master_key) != KEY_BYTES:
        raise RecoveryError("Master key must be exactly 32 bytes")

    salt = secrets.token_bytes(SALT_BYTES)
    nonce = secrets.token_bytes(NONCE_BYTES)
    # Deliberately moderate parameters for broad Windows compatibility.
    # They are stored in the bundle so they can be raised in future versions.
    n, r, p = 2**15, 8, 1
    recovery_key = _derive_recovery_key(passphrase, salt, n=n, r=r, p=p)
    ciphertext = AESGCM(recovery_key).encrypt(nonce, master_key, RECOVERY_AAD)

    envelope = {
        "format": RECOVERY_FORMAT,
        "version": RECOVERY_VERSION,
        "algorithm": VAULT_ALGORITHM,
        "kdf": "scrypt",
        "kdf_params": {"n": n, "r": r, "p": p},
        "salt": _b64e(salt),
        "nonce": _b64e(nonce),
        "ciphertext": _b64e(ciphertext),
    }
    return _canonical_json(envelope) + b"\n"


def recover_master_key(bundle: bytes, passphrase: str) -> bytes:
    try:
        envelope = json.loads(bundle.decode("utf-8"))
        if envelope.get("format") != RECOVERY_FORMAT:
            raise RecoveryError("Unexpected recovery bundle format")
        if envelope.get("version") != RECOVERY_VERSION:
            raise RecoveryError("Unsupported recovery bundle version")
        if envelope.get("kdf") != "scrypt":
            raise RecoveryError("Unsupported recovery KDF")
        params = envelope["kdf_params"]
        salt = _b64d(envelope["salt"])
        nonce = _b64d(envelope["nonce"])
        ciphertext = _b64d(envelope["ciphertext"])
        recovery_key = _derive_recovery_key(
            passphrase,
            salt,
            n=int(params["n"]),
            r=int(params["r"]),
            p=int(params["p"]),
        )
        key = AESGCM(recovery_key).decrypt(nonce, ciphertext, RECOVERY_AAD)
    except RecoveryError:
        raise
    except (InvalidTag, KeyError, ValueError, TypeError, json.JSONDecodeError) as exc:
        raise RecoveryError("Recovery failed: wrong passphrase or damaged bundle") from exc

    if len(key) != KEY_BYTES:
        raise RecoveryError("Recovered master key has invalid length")
    return key


class NovaMemoryVault:
    """Authenticated memory encryption using a caller-supplied 256-bit key."""

    def __init__(self, master_key: bytes) -> None:
        if len(master_key) != KEY_BYTES:
            raise VaultError("Master key must be exactly 32 bytes")
        self._key = bytes(master_key)

    def opaque_name(self, logical_name: str, suffix: str = ".nvm") -> str:
        digest = hmac.new(
            self._key,
            logical_name.encode("utf-8"),
            hashlib.sha256,
        ).hexdigest()
        return f"{digest}{suffix}"

    def seal_bytes(
        self,
        data: bytes,
        *,
        logical_name: str | None = None,
        content_type: str = "application/octet-stream",
    ) -> bytes:
        inner = {
            "logical_name": logical_name,
            "content_type": content_type,
            "sha256": hashlib.sha256(data).hexdigest(),
            "data": _b64e(data),
        }
        plaintext = _canonical_json(inner)
        nonce = secrets.token_bytes(NONCE_BYTES)
        ciphertext = AESGCM(self._key).encrypt(nonce, plaintext, VAULT_AAD)
        envelope = {
            "format": VAULT_FORMAT,
            "version": VAULT_VERSION,
            "algorithm": VAULT_ALGORITHM,
            "nonce": _b64e(nonce),
            "ciphertext": _b64e(ciphertext),
        }
        return _canonical_json(envelope) + b"\n"

    def open_bytes(self, blob: bytes) -> OpenedMemory:
        try:
            envelope = json.loads(blob.decode("utf-8"))
            if envelope.get("format") != VAULT_FORMAT:
                raise VaultError("Unexpected vault format")
            if envelope.get("version") != VAULT_VERSION:
                raise VaultError("Unsupported vault version")
            if envelope.get("algorithm") != VAULT_ALGORITHM:
                raise VaultError("Unsupported vault algorithm")
            nonce = _b64d(envelope["nonce"])
            ciphertext = _b64d(envelope["ciphertext"])
            plaintext = AESGCM(self._key).decrypt(nonce, ciphertext, VAULT_AAD)
            inner = json.loads(plaintext.decode("utf-8"))
            data = _b64d(inner["data"])
            actual_hash = hashlib.sha256(data).hexdigest()
            expected_hash = inner["sha256"]
            if not hmac.compare_digest(actual_hash, expected_hash):
                raise VaultIntegrityError("Plaintext hash does not match encrypted metadata")
            return OpenedMemory(
                data=data,
                logical_name=inner.get("logical_name"),
                content_type=inner.get("content_type", "application/octet-stream"),
                sha256=actual_hash,
            )
        except VaultIntegrityError:
            raise
        except InvalidTag as exc:
            raise VaultIntegrityError(
                "Authenticated decryption failed: wrong key or tampered ciphertext"
            ) from exc
        except VaultError:
            raise
        except Exception as exc:
            raise VaultError("Invalid Nova Memory Vault envelope") from exc

    def seal_file(
        self,
        source: Path,
        destination: Path,
        *,
        logical_name: str | None = None,
        content_type: str = "application/octet-stream",
    ) -> Path:
        source = Path(source)
        destination = Path(destination)
        data = source.read_bytes()
        blob = self.seal_bytes(
            data,
            logical_name=logical_name or source.name,
            content_type=content_type,
        )
        atomic_write(destination, blob)
        return destination

    def open_file(self, source: Path, destination: Path) -> OpenedMemory:
        source = Path(source)
        destination = Path(destination)
        opened = self.open_bytes(source.read_bytes())
        atomic_write(destination, opened.data, private=True)
        return opened

    def verify_file(self, source: Path) -> OpenedMemory:
        return self.open_bytes(Path(source).read_bytes())


def load_default_vault() -> NovaMemoryVault:
    return NovaMemoryVault(DPAPIKeyStore().load_key())
