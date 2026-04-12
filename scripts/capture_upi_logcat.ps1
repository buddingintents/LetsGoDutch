param(
    [string]$OutputPath = "",
    [switch]$NoClear
)

$ErrorActionPreference = "Stop"

$adb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path $adb)) {
    throw "adb not found at $adb"
}

$repoRoot = Split-Path -Parent $PSScriptRoot
$outputDir = Join-Path $repoRoot "qa_artifacts\upi-logcat"
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

if ([string]::IsNullOrWhiteSpace($OutputPath)) {
    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $OutputPath = Join-Path $outputDir "upi_logcat_$timestamp.txt"
}

if (-not $NoClear) {
    & $adb logcat -c
}

Write-Host "Capturing logcat to: $OutputPath"
Write-Host "Reproduce the UPI flow, then press Ctrl+C to stop."
Write-Host "Suggested follow-up filters:"
Write-Host "  Select-String -Path `"$OutputPath`" -Pattern 'LGD_UPI_TRACE','yono','upi','npci','sbi'"

& $adb logcat -v time | Tee-Object -FilePath $OutputPath
