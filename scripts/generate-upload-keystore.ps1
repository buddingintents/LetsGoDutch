[CmdletBinding()]
param(
    [string]$KeystorePath = "keystore/letsgodutch-upload.jks",
    [string]$Alias = "upload",
    [string]$DName = "CN=LetsGoDutch Upload, O=LetsGoDutch, C=IN",
    [string]$KeystorePropertiesPath = "keystore.properties",
    [string]$KeytoolPath,
    [string]$StorePassword,
    [string]$KeyPassword,
    [switch]$Overwrite,
    [switch]$SkipPropertiesWrite
)

$ErrorActionPreference = "Stop"

function Resolve-KeytoolPath {
    param([string]$ExplicitPath)

    if ($ExplicitPath) {
        if (-not (Test-Path $ExplicitPath)) {
            throw "keytool.exe was not found at '$ExplicitPath'."
        }
        return (Resolve-Path $ExplicitPath).Path
    }

    $candidates = @()

    if ($env:JAVA_HOME) {
        $candidates += (Join-Path $env:JAVA_HOME "bin\keytool.exe")
    }

    $command = Get-Command keytool.exe -ErrorAction SilentlyContinue
    if ($command) {
        $candidates += $command.Source
    }

    $candidates += "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe"

    foreach ($candidate in $candidates | Select-Object -Unique) {
        if ($candidate -and (Test-Path $candidate)) {
            return (Resolve-Path $candidate).Path
        }
    }

    throw "Unable to find keytool.exe. Pass -KeytoolPath explicitly or install a JDK."
}

function Read-Secret {
    param([string]$Prompt)

    $secureValue = Read-Host -Prompt $Prompt -AsSecureString
    $bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureValue)
    try {
        return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr)
    }
    finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
    }
}

function Get-RelativeProjectPath {
    param(
        [string]$ProjectRoot,
        [string]$TargetPath
    )

    $projectUri = [Uri]((Resolve-Path $ProjectRoot).Path.TrimEnd('\') + '\')
    $targetUri = [Uri](Resolve-Path $TargetPath).Path
    $relativeUri = $projectUri.MakeRelativeUri($targetUri)
    return [Uri]::UnescapeDataString($relativeUri.ToString()).Replace('/', '\')
}

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$resolvedKeytoolPath = Resolve-KeytoolPath -ExplicitPath $KeytoolPath
$resolvedKeystorePath = Join-Path $projectRoot $KeystorePath
$resolvedPropertiesPath = Join-Path $projectRoot $KeystorePropertiesPath

if (-not $StorePassword) {
    $StorePassword = Read-Secret -Prompt "Enter keystore password"
}

if (-not $KeyPassword) {
    $KeyPassword = Read-Secret -Prompt "Enter key password"
}

if ([string]::IsNullOrWhiteSpace($StorePassword) -or [string]::IsNullOrWhiteSpace($KeyPassword)) {
    throw "Store password and key password are required."
}

$keystoreDirectory = Split-Path -Parent $resolvedKeystorePath
if ($keystoreDirectory -and -not (Test-Path $keystoreDirectory)) {
    New-Item -ItemType Directory -Path $keystoreDirectory | Out-Null
}

if ((Test-Path $resolvedKeystorePath) -and -not $Overwrite) {
    throw "Keystore already exists at '$resolvedKeystorePath'. Re-run with -Overwrite to replace it."
}

if ((Test-Path $resolvedKeystorePath) -and $Overwrite) {
    Remove-Item $resolvedKeystorePath -Force
}

$keytoolArguments = @(
    "-genkeypair",
    "-v",
    "-storetype", "PKCS12",
    "-keystore", $resolvedKeystorePath,
    "-alias", $Alias,
    "-keyalg", "RSA",
    "-keysize", "2048",
    "-validity", "10000",
    "-dname", $DName,
    "-storepass", $StorePassword,
    "-keypass", $KeyPassword
)

& $resolvedKeytoolPath @keytoolArguments
if ($LASTEXITCODE -ne 0) {
    throw "keytool failed while generating the upload keystore."
}

if (-not $SkipPropertiesWrite) {
    $relativeKeystorePath = Get-RelativeProjectPath -ProjectRoot $projectRoot -TargetPath $resolvedKeystorePath
    @"
storeFile=$relativeKeystorePath
storePassword=$StorePassword
keyAlias=$Alias
keyPassword=$KeyPassword
"@ | Set-Content -Path $resolvedPropertiesPath -Encoding ASCII

    Write-Host "Wrote signing properties to $resolvedPropertiesPath"
}

$fingerprintOutput = & $resolvedKeytoolPath `
    -list `
    -v `
    -keystore $resolvedKeystorePath `
    -alias $Alias `
    -storepass $StorePassword `
    -keypass $KeyPassword

if ($LASTEXITCODE -ne 0) {
    throw "keytool failed while reading the generated certificate fingerprints."
}

$sha1Line = $fingerprintOutput | Where-Object { $_ -match "^\s*SHA1:" }
$sha256Line = $fingerprintOutput | Where-Object { $_ -match "^\s*SHA-?256:" }

Write-Host ""
Write-Host "Upload keystore created at: $resolvedKeystorePath"
Write-Host $sha1Line
Write-Host $sha256Line
Write-Host ""
Write-Host "Next:"
Write-Host "1. Add the SHA1 and SHA-256 above to Firebase for the Android app."
Write-Host "2. In Play Console, enable Play App Signing and register this as your upload key."
Write-Host "3. Build the release bundle with .\gradlew.bat :app:bundleRelease"
