<#
.SYNOPSIS
Installs the newest .msix from the shared folder the way Microsoft Store would and checks that Autogram starts.

.DESCRIPTION
Runs inside a clean Windows (dockurr/windows VM or Windows Sandbox), see docs/windows-store-testing.md.
The package is signed with a throwaway certificate whose subject is the publisher from the package manifest, the
certificate is trusted on this computer, the package is installed with Add-AppxPackage and Autogram is launched from
its Start menu entry as a regular user would. The test passes when the server for websites answers on /info.
Results are written to <SharedDir>\results\<timestamp>.

Keep this file ASCII only, Windows PowerShell 5.1 reads scripts without BOM in the ANSI code page.
#>
param(
    [string]$SharedDir = '\\host.lan\Data',
    [int]$StartupTimeoutSeconds = 90,
    [string]$InfoUrl = 'http://localhost:37200/info',
    # Registers the test to run at every sign-in of the current user (used by install.bat) and runs it now
    [switch]$Register
)

$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
$taskName = 'Autogram MSIX test'
# Differs from the certificate in docs/windows-store-testing.md, the cleanup below must not delete that one
$certFriendlyName = 'Autogram MSIX test-msix.ps1'

function Test-Administrator {
    $principal = [Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()
    return $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
}

# Trusting the certificate on the whole computer requires elevation
if (-not (Test-Administrator)) {
    $arguments = "-NoProfile -ExecutionPolicy Bypass -File `"$PSCommandPath`" -SharedDir `"$SharedDir`" " +
        "-StartupTimeoutSeconds $StartupTimeoutSeconds -InfoUrl `"$InfoUrl`""
    if ($Register) { $arguments += ' -Register' }
    Start-Process powershell.exe -Verb RunAs -ArgumentList $arguments
    return
}

if ($Register) {
    $user = "$env:USERDOMAIN\$env:USERNAME"
    $action = New-ScheduledTaskAction -Execute 'powershell.exe' `
        -Argument "-NoProfile -ExecutionPolicy Bypass -File `"$PSCommandPath`" -SharedDir `"$SharedDir`""
    $trigger = New-ScheduledTaskTrigger -AtLogOn -User $user
    $principal = New-ScheduledTaskPrincipal -UserId $user -LogonType Interactive -RunLevel Highest
    $settings = New-ScheduledTaskSettingsSet -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries `
        -ExecutionTimeLimit (New-TimeSpan -Hours 1)
    Register-ScheduledTask -TaskName $taskName -Action $action -Trigger $trigger -Principal $principal `
        -Settings $settings -Force | Out-Null
    Write-Host "Registered scheduled task '$taskName' to run at every sign-in of $user"
}

$started = Get-Date
$stamp = $started.ToString('yyyyMMdd-HHmmss')
$work = Join-Path $env:ProgramData "AutogramMsixTest\$stamp"
New-Item -ItemType Directory -Force -Path $work | Out-Null
Start-Transcript -Path (Join-Path $work 'test.log') | Out-Null

$result = 'FAIL'
$reason = $null
$details = [ordered]@{}
$resultsDir = $null
$package = $null
$appId = $null

function Write-Step([string]$message) {
    Write-Host ''
    Write-Host "=== $(Get-Date -Format 'HH:mm:ss') $message" -ForegroundColor Cyan
}

# Windows PowerShell 5.1 turns stderr of native commands into terminating errors when $ErrorActionPreference is Stop
function Invoke-Native([string]$file, [string[]]$arguments) {
    $previous = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        & $file @arguments 2>&1 | ForEach-Object { Write-Host $_ }
    } finally {
        $ErrorActionPreference = $previous
    }
    return $LASTEXITCODE
}

Add-Type -Namespace AutogramMsixTest -Name User32 -MemberDefinition @'
[DllImport("user32.dll")] public static extern IntPtr GetForegroundWindow();
[DllImport("user32.dll")] public static extern uint GetWindowThreadProcessId(IntPtr hWnd, out uint processId);
'@

# The Start menu is often open after the first sign-in and covers the Autogram window
function Close-StartMenu {
    $processId = [uint32]0
    [AutogramMsixTest.User32]::GetWindowThreadProcessId([AutogramMsixTest.User32]::GetForegroundWindow(), [ref]$processId) | Out-Null
    $foreground = Get-Process -Id $processId -ErrorAction SilentlyContinue
    if ($foreground -and $foreground.Name -in 'StartMenuExperienceHost', 'SearchHost') {
        Add-Type -AssemblyName System.Windows.Forms
        [System.Windows.Forms.SendKeys]::SendWait('{ESC}')
        Start-Sleep -Seconds 1
    }
}

function Save-Screenshot([string]$path) {
    try {
        Close-StartMenu
        Add-Type -AssemblyName System.Windows.Forms, System.Drawing
        $bounds = [System.Windows.Forms.SystemInformation]::VirtualScreen
        $bitmap = New-Object System.Drawing.Bitmap $bounds.Width, $bounds.Height
        $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
        $graphics.CopyFromScreen($bounds.Location, [System.Drawing.Point]::Empty, $bounds.Size)
        $bitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
        $graphics.Dispose()
        $bitmap.Dispose()
    } catch {
        Write-Warning "Unable to take a screenshot: $_"
    }
}

function Wait-ForInfo([int]$timeoutSeconds) {
    $deadline = (Get-Date).AddSeconds($timeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        try {
            return Invoke-WebRequest -UseBasicParsing -TimeoutSec 5 -Uri $InfoUrl
        } catch {
            Start-Sleep -Seconds 1
        }
    }
    return $null
}

function Get-PackageProcesses([string]$installLocation) {
    if (-not $installLocation) { return @() }
    return @(Get-Process | Where-Object { $_.Path -and $_.Path.StartsWith("$installLocation\", 'OrdinalIgnoreCase') })
}

function Read-PackageManifest([string]$path) {
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $zip = [System.IO.Compression.ZipFile]::OpenRead($path)
    try {
        $entry = $zip.GetEntry('AppxManifest.xml')
        if (-not $entry) { throw "$path does not contain AppxManifest.xml" }
        $reader = New-Object System.IO.StreamReader($entry.Open())
        try { [xml]$xml = $reader.ReadToEnd() } finally { $reader.Dispose() }
        $identity = $xml.Package.Identity
        return [pscustomobject]@{
            Name      = $identity.GetAttribute('Name')
            Publisher = $identity.GetAttribute('Publisher')
            Version   = $identity.GetAttribute('Version')
            AppId     = @($xml.Package.Applications.Application)[0].GetAttribute('Id')
            Signed    = $null -ne $zip.GetEntry('AppxSignature.p7x')
        }
    } finally {
        $zip.Dispose()
    }
}

function Get-SignTool([string]$directory) {
    $sdk = Get-ChildItem "${env:ProgramFiles(x86)}\Windows Kits\10\bin\*\x64\signtool.exe" -ErrorAction SilentlyContinue |
        Sort-Object FullName | Select-Object -Last 1
    if ($sdk) { return $sdk.FullName }

    [Net.ServicePointManager]::SecurityProtocol = [Net.ServicePointManager]::SecurityProtocol -bor [Net.SecurityProtocolType]::Tls12
    $feed = 'https://api.nuget.org/v3-flatcontainer/microsoft.windows.sdk.buildtools'
    $version = (Invoke-RestMethod "$feed/index.json").versions | Where-Object { $_ -notmatch '-' } | Select-Object -Last 1
    $archive = Join-Path $directory 'buildtools.zip'
    Write-Host "Downloading signtool from Microsoft.Windows.SDK.BuildTools $version"
    Invoke-WebRequest -UseBasicParsing -Uri "$feed/$version/microsoft.windows.sdk.buildtools.$version.nupkg" -OutFile $archive
    Expand-Archive -Path $archive -DestinationPath (Join-Path $directory 'buildtools') -Force
    return (Get-ChildItem (Join-Path $directory 'buildtools\bin\*\x64\signtool.exe') | Sort-Object FullName |
        Select-Object -Last 1).FullName
}

function Add-TrustedCertificate([System.Security.Cryptography.X509Certificates.X509Certificate2]$certificate) {
    $public = [System.Security.Cryptography.X509Certificates.X509Certificate2]::new($certificate.RawData)
    $public.FriendlyName = $certFriendlyName
    $store = New-Object System.Security.Cryptography.X509Certificates.X509Store('TrustedPeople', 'LocalMachine')
    $store.Open('ReadWrite')
    try { $store.Add($public) } finally { $store.Close() }
}

try {
    Write-Step "Waiting for the shared folder $SharedDir"
    $deadline = (Get-Date).AddMinutes(3)
    while (-not (Test-Path -LiteralPath $SharedDir)) {
        if ((Get-Date) -gt $deadline) { throw "Shared folder $SharedDir is not available" }
        Start-Sleep -Seconds 5
    }
    $resultsDir = Join-Path $SharedDir "results\$stamp"
    New-Item -ItemType Directory -Force -Path $resultsDir | Out-Null

    Write-Step 'Environment'
    $os = Get-ItemProperty 'HKLM:\SOFTWARE\Microsoft\Windows NT\CurrentVersion'
    # ProductName says Windows 10 also on Windows 11, which starts at build 22000
    $productName = $os.ProductName
    if ([int]$os.CurrentBuild -ge 22000) { $productName = $productName -replace 'Windows 10', 'Windows 11' }
    $details['windows'] = "$productName $($os.DisplayVersion) build $($os.CurrentBuild).$($os.UBR)"
    $details['user'] = "$env:USERDOMAIN\$env:USERNAME"
    $details['TEMP'] = $env:TEMP
    $java = Get-Command java.exe -ErrorAction SilentlyContinue
    $details['java on PATH'] = if ($java) { $java.Source } else { 'no' }
    $details['VC++ runtime in System32'] = if (Test-Path "$env:windir\System32\vcruntime140.dll") { 'yes' } else { 'no' }
    $unlock = Get-ItemProperty 'HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\AppModelUnlock' -ErrorAction SilentlyContinue
    $details['developer mode'] = if ($unlock -and $unlock.AllowDevelopmentWithoutDevLicense -eq 1) { 'on' } else { 'off' }
    $details.GetEnumerator() | ForEach-Object { Write-Host ("{0}: {1}" -f $_.Key, $_.Value) }

    $source = Get-ChildItem -LiteralPath $SharedDir -Filter '*.msix' -File | Sort-Object LastWriteTime -Descending |
        Select-Object -First 1
    if (-not $source) {
        $result = 'SKIPPED'
        $reason = "no .msix file in $SharedDir"
        return
    }
    $details['msix'] = $source.Name
    Write-Host "Testing $($source.FullName)"

    $msix = Join-Path $work $source.Name
    Copy-Item -LiteralPath $source.FullName -Destination $msix
    $manifest = Read-PackageManifest $msix
    $appId = $manifest.AppId
    $details['package'] = "$($manifest.Name) $($manifest.Version)"
    $details['publisher'] = $manifest.Publisher

    Write-Step 'Removing a previous test installation'
    foreach ($previous in @(Get-AppxPackage -Name $manifest.Name)) {
        Get-PackageProcesses $previous.InstallLocation | Stop-Process -Force -ErrorAction SilentlyContinue
        Write-Host "Removing $($previous.PackageFullName)"
        Remove-AppxPackage -Package $previous.PackageFullName
    }
    Get-ChildItem Cert:\LocalMachine\TrustedPeople, Cert:\CurrentUser\My |
        Where-Object { $_.FriendlyName -eq $certFriendlyName } | Remove-Item
    Remove-Item -LiteralPath (Join-Path $env:USERPROFILE '.autogram') -Recurse -Force -ErrorAction SilentlyContinue

    if ($manifest.Signed) {
        Write-Step 'Trusting the certificate the package is already signed with'
        $signature = Get-AuthenticodeSignature -FilePath $msix
        if (-not $signature.SignerCertificate) {
            throw "Unable to read the signature of $($source.Name): $($signature.StatusMessage)"
        }
        Add-TrustedCertificate $signature.SignerCertificate
    } else {
        Write-Step "Signing with a test certificate for $($manifest.Publisher)"
        $signtool = Get-SignTool $work
        $certificate = New-SelfSignedCertificate -Type Custom -Subject $manifest.Publisher -FriendlyName $certFriendlyName `
            -KeyUsage DigitalSignature -CertStoreLocation Cert:\CurrentUser\My -NotAfter (Get-Date).AddDays(7) `
            -TextExtension @('2.5.29.37={text}1.3.6.1.5.5.7.3.3', '2.5.29.19={text}')
        Add-TrustedCertificate $certificate
        $exitCode = Invoke-Native $signtool @('sign', '/fd', 'SHA256', '/sha1', $certificate.Thumbprint, $msix)
        if ($exitCode -ne 0) { throw "signtool failed with exit code $exitCode" }
    }

    Write-Step 'Installing the package'
    try {
        Add-AppxPackage -Path $msix
    } catch {
        $message = $_.Exception.Message
        if ($message -match '\[ActivityId\]\s+([0-9a-fA-F-]{36})') {
            Get-AppPackageLog -ActivityID $Matches[1] -ErrorAction SilentlyContinue |
                Format-List | Out-File (Join-Path $work 'appx-deployment.txt') -Width 400
        }
        throw "Add-AppxPackage failed: $message"
    }
    $package = Get-AppxPackage -Name $manifest.Name
    if (-not $package) { throw 'The package is not installed after Add-AppxPackage' }
    Write-Host "Installed $($package.PackageFullName) to $($package.InstallLocation)"

    Write-Step 'Launching Autogram from its Start menu entry'
    $deadline = (Get-Date).AddMinutes(5)
    while (-not (Get-Process explorer -ErrorAction SilentlyContinue)) {
        if ((Get-Date) -gt $deadline) { throw 'The desktop (explorer.exe) is not running' }
        Start-Sleep -Seconds 2
    }
    Start-Sleep -Seconds 10
    # explorer.exe hands the activation over to the running shell, so Autogram runs without elevation like for a user
    Start-Process explorer.exe -ArgumentList "shell:AppsFolder\$($package.PackageFamilyName)!$appId"

    Write-Step "Waiting up to $StartupTimeoutSeconds s for $InfoUrl"
    $info = Wait-ForInfo $StartupTimeoutSeconds
    Start-Sleep -Seconds 5
    Save-Screenshot (Join-Path $work 'screenshot.png')
    $processes = Get-PackageProcesses $package.InstallLocation
    $details['running processes'] = if ($processes) { ($processes | ForEach-Object { "$($_.Name) ($($_.Id))" }) -join ', ' } else { 'none' }

    if ($info -and $info.StatusCode -eq 200) {
        $info.Content | Out-File (Join-Path $work 'info.json') -Encoding utf8
        $details['info'] = $info.Content
        $result = 'PASS'
    } elseif ($processes) {
        $reason = "Autogram is running but $InfoUrl does not answer, see screenshot.png and autogram-debug.log"
    } else {
        $reason = 'Autogram is not running, see events.txt and autogram-debug.log'
    }
} catch {
    $reason = $_.Exception.Message
    Write-Host $_.ScriptStackTrace
} finally {
    if ($result -eq 'FAIL' -and $package) {
        Write-Step 'Starting Autogram again with a debug log'
        try {
            Get-PackageProcesses $package.InstallLocation | Stop-Process -Force -ErrorAction SilentlyContinue
            Start-Sleep -Seconds 2
            $log = Join-Path $work 'autogram-debug.log'
            $options = '-Dorg.slf4j.simpleLogger.log.digital.slovensko.autogram=debug ' +
                '-Dorg.slf4j.simpleLogger.showDateTime=true -Dorg.slf4j.simpleLogger.showThreadName=true'
            # -PreventBreakaway keeps the package identity for Autogram.exe started by cmd.exe
            Invoke-CommandInDesktopPackage -PackageFamilyName $package.PackageFamilyName -AppId $appId `
                -Command 'cmd.exe' -PreventBreakaway `
                -Args "/c `"set `"JAVA_TOOL_OPTIONS=$options`" && `"$($package.InstallLocation)\Autogram.exe`" > `"$log`" 2>&1`""
            Wait-ForInfo 30 | Out-Null
            Save-Screenshot (Join-Path $work 'screenshot-debug.png')
            Get-PackageProcesses $package.InstallLocation | Stop-Process -Force -ErrorAction SilentlyContinue
        } catch {
            Write-Warning "Debug run failed: $_"
        }
    }

    if ($result -ne 'SKIPPED') {
        $events = @()
        foreach ($logName in 'Application', 'Microsoft-Windows-AppXDeploymentServer/Operational') {
            $events += @(Get-WinEvent -FilterHashtable @{ LogName = $logName; StartTime = $started; Level = 1, 2, 3 } `
                -ErrorAction SilentlyContinue)
        }
        $events | Sort-Object TimeCreated | Format-List TimeCreated, LogName, ProviderName, Id, LevelDisplayName, Message |
            Out-File (Join-Path $work 'events.txt') -Width 400
    }

    $summary = @($result + $(if ($reason) { ": $reason" } else { '' }))
    $summary += $details.GetEnumerator() | ForEach-Object { "{0}: {1}" -f $_.Key, $_.Value }
    Write-Step 'Result'
    $summary | ForEach-Object { Write-Host $_ }
    Stop-Transcript | Out-Null

    $summary | Out-File (Join-Path $work 'result.txt') -Encoding utf8
    if ($resultsDir) {
        Get-ChildItem -LiteralPath $work -File | Where-Object { $_.Extension -notin '.msix', '.zip' } |
            Copy-Item -Destination $resultsDir -Force
        @("results\$stamp") + $summary | Out-File (Join-Path $SharedDir 'results\latest.txt') -Encoding utf8
        Write-Host "Results: $resultsDir"
    }
    Start-Process notepad.exe -ArgumentList "`"$(Join-Path $work 'result.txt')`""
}
