param(
    [string]$Python = "python",
    [string]$RecoveryBundle = "$HOME\Nova-Recovery\nova-recovery.nvr"
)

$ErrorActionPreference = "Stop"

Write-Host "Nova Memory Vault activation" -ForegroundColor Cyan
Write-Host "This initializes a Windows DPAPI-protected master key and an encrypted recovery bundle."
Write-Host "It does NOT delete or migrate any existing plaintext memory files." -ForegroundColor Yellow

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

& $Python -c "import cryptography; print('cryptography', cryptography.__version__)"
if ($LASTEXITCODE -ne 0) {
    throw "Python 'cryptography' dependency is missing. Install backend/requirements.txt first."
}

Write-Host "Running Nova Memory Vault tests..."
& $Python -m pytest backend/tests/test_nova_vault.py -q
if ($LASTEXITCODE -ne 0) {
    throw "Vault tests failed. Activation stopped."
}

$recoveryDir = Split-Path -Parent $RecoveryBundle
if (-not (Test-Path $recoveryDir)) {
    New-Item -ItemType Directory -Path $recoveryDir -Force | Out-Null
}

Write-Host ""
Write-Host "You will now enter a recovery passphrase twice." -ForegroundColor Cyan
Write-Host "Keep that passphrase somewhere separate from the recovery bundle." -ForegroundColor Yellow

& $Python scripts/nova_memory_vault.py init --recovery-bundle $RecoveryBundle
if ($LASTEXITCODE -ne 0) {
    throw "Vault initialization failed."
}

Write-Host ""
Write-Host "Checking status..."
& $Python scripts/nova_memory_vault.py status
if ($LASTEXITCODE -ne 0) {
    throw "Vault status check failed."
}

Write-Host ""
Write-Host "Nova Memory Vault local key is initialized." -ForegroundColor Green
Write-Host "Recovery bundle: $RecoveryBundle"
Write-Host ""
Write-Host "NEXT SAFETY GATE:" -ForegroundColor Cyan
Write-Host "Do not remove plaintext canonical Nova records yet."
Write-Host "First verify a fresh Nova runtime can use an authorized local decrypt bridge."
