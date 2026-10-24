<#
.SYNOPSIS
Tests the MSIX package in Windows Sandbox, a disposable clean copy of the Windows installed on this computer.

.DESCRIPTION
Requires Windows 10/11 Pro or Enterprise with Windows Sandbox enabled. The newest .msix in the shared folder is
tested by oem\test-msix.ps1, results are written to shared\results, see docs/windows-store-testing.md.

.EXAMPLE
.\packaging\msix-test\sandbox.ps1 -Msix target\Autogram-2.8.3.0.msix
#>
param(
    # Copied to the shared folder, otherwise the newest .msix already in the shared folder is tested
    [string]$Msix
)

$ErrorActionPreference = 'Stop'

if (-not (Test-Path "$env:windir\System32\WindowsSandbox.exe")) {
    throw 'Windows Sandbox is not enabled. Enable it in an elevated PowerShell and restart the computer: ' +
        'Enable-WindowsOptionalFeature -Online -FeatureName Containers-DisposableClientVM -All'
}

$oem = Join-Path $PSScriptRoot 'oem'
$shared = Join-Path $PSScriptRoot 'shared'
New-Item -ItemType Directory -Force -Path $shared | Out-Null

if ($Msix) {
    $copy = Copy-Item -LiteralPath $Msix -Destination $shared -PassThru
    $copy.LastWriteTime = Get-Date
}

if (-not (Get-ChildItem -LiteralPath $shared -Filter '*.msix' -File)) {
    throw "No .msix file in $shared, use -Msix"
}

$configuration = @"
<Configuration>
  <MappedFolders>
    <MappedFolder>
      <HostFolder>$([Security.SecurityElement]::Escape($oem))</HostFolder>
      <SandboxFolder>C:\OEM</SandboxFolder>
      <ReadOnly>true</ReadOnly>
    </MappedFolder>
    <MappedFolder>
      <HostFolder>$([Security.SecurityElement]::Escape($shared))</HostFolder>
      <SandboxFolder>C:\Shared</SandboxFolder>
      <ReadOnly>false</ReadOnly>
    </MappedFolder>
  </MappedFolders>
  <LogonCommand>
    <Command>powershell.exe -NoProfile -ExecutionPolicy Bypass -File C:\OEM\test-msix.ps1 -SharedDir C:\Shared</Command>
  </LogonCommand>
</Configuration>
"@

$wsb = Join-Path $env:TEMP 'autogram-msix-test.wsb'
Set-Content -LiteralPath $wsb -Value $configuration -Encoding UTF8
Write-Host "Starting Windows Sandbox, results will be in $shared\results"
Invoke-Item -LiteralPath $wsb
