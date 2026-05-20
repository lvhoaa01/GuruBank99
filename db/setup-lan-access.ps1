# Bank99 — one-time setup to let an Android device on the same LAN
# connect to PostgreSQL 18 running on this Windows machine.
#
# Run from an *elevated* PowerShell:
#     Right-click PowerShell → Run as administrator
#     cd C:\DDrive\GuruBank99
#     powershell -ExecutionPolicy Bypass -File db\setup-lan-access.ps1
#
# What it does:
#   1. Appends a pg_hba.conf rule for the 192.168.100.0/24 subnet.
#   2. Reloads PostgreSQL so the rule takes effect (no restart needed).
#   3. Adds a Windows Firewall inbound rule for TCP/5432.
# Each step is idempotent — re-running is safe.

param(
    [string]$Subnet      = "192.168.100.0/24",
    [string]$DbName      = "bank99",
    [string]$DbUser      = "bank99user",
    [string]$HbaPath     = "C:\Program Files\PostgreSQL\18\data\pg_hba.conf",
    [string]$ServiceName = "postgresql-x64-18"
)

# Bail out early if not elevated.
$principal = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
if (-not $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Error "Must run as Administrator."
    exit 1
}

# ----- 1. pg_hba.conf -----
if (-not (Test-Path $HbaPath)) {
    Write-Error "pg_hba.conf not found at $HbaPath. Adjust -HbaPath."
    exit 1
}
$ruleLine = "host    $DbName    $DbUser    $Subnet    scram-sha-256"
$existing = Get-Content $HbaPath
if ($existing -contains $ruleLine) {
    Write-Host "[skip] Rule already present in pg_hba.conf"
} else {
    $backup = "$HbaPath.bak-" + (Get-Date -Format "yyyyMMddHHmmss")
    Copy-Item $HbaPath $backup -Force
    Write-Host "[ok]   Backup written to $backup"
    Add-Content -Path $HbaPath -Value "`n# Bank99 Android LAN access`n$ruleLine" -Encoding UTF8
    Write-Host "[ok]   Appended rule: $ruleLine"
}

# ----- 2. Reload Postgres -----
$pgctl = "C:\Program Files\PostgreSQL\18\bin\pg_ctl.exe"
if (Test-Path $pgctl) {
    Write-Host "[ok]   Reloading Postgres (pg_ctl reload) ..."
    & $pgctl reload -D "C:\Program Files\PostgreSQL\18\data" | Write-Host
} else {
    Write-Host "[warn] pg_ctl not found; restarting service instead"
    Restart-Service -Name $ServiceName -Force
    Write-Host "[ok]   Service restarted"
}

# ----- 3. Windows Firewall -----
$ruleName = "Bank99 PostgreSQL LAN 5432"
$existingRule = Get-NetFirewallRule -DisplayName $ruleName -ErrorAction SilentlyContinue
if ($existingRule) {
    Write-Host "[skip] Firewall rule already present"
} else {
    New-NetFirewallRule `
        -DisplayName $ruleName `
        -Direction Inbound `
        -Action Allow `
        -Protocol TCP `
        -LocalPort 5432 `
        -Profile Private,Domain `
        -RemoteAddress $Subnet | Out-Null
    Write-Host "[ok]   Firewall rule added: TCP/5432 from $Subnet"
}

Write-Host "`nAll done. Quick connectivity test from a phone or another machine:"
Write-Host "  psql -h <DEV_IP> -U $DbUser -d $DbName"
