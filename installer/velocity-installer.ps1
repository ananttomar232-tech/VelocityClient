# Velocity Client installer for Windows.
# Sets up Fabric + Velocity + FPS mods as a "Velocity Client" profile in the official Minecraft Launcher.
# Uses its own game folder (%APPDATA%\.velocity) so your normal Minecraft stays untouched.

$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$McVersion   = '1.21.1'
$Repo        = 'ananttomar232-tech/VelocityClient'
$ProfileKey  = 'velocity-client'
$McDir       = Join-Path $env:APPDATA '.minecraft'
$GameDir     = Join-Path $env:APPDATA '.velocity'
$ModsDir     = Join-Path $GameDir 'mods'
$Managed     = Join-Path $ModsDir '.velocity-managed.txt'
$ScriptDir   = Split-Path -Parent $MyInvocation.MyCommand.Path
$UserAgent   = 'VelocityClient-Installer/1.0 (github.com/' + $Repo + ')'

# Tuned for a 16 GB RAM / 4-core PC: 4 GB heap with low-pause G1 garbage collection.
$JavaArgs = '-Xms4G -Xmx4G -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 ' +
            '-XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch ' +
            '-XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M ' +
            '-XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 ' +
            '-XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 ' +
            '-XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1'

# Mods from Modrinth: slug -> why it is included
$Mods = [ordered]@{
    'fabric-api'      = 'Fabric API (required)'
    'sodium'          = 'Sodium - big FPS boost'
    'lithium'         = 'Lithium - faster game logic'
    'ferrite-core'    = 'FerriteCore - less RAM'
    'immediatelyfast' = 'ImmediatelyFast - faster HUD/text'
    'entityculling'   = 'EntityCulling - skip hidden entities'
    'modmenu'         = 'Mod Menu - settings for other mods'
    'modernfix'       = 'ModernFix - faster startup, less RAM'
    'krypton'         = 'Krypton - lighter networking'
    'noisium'         = 'Noisium - faster world generation'
    'badoptimizations'= 'BadOptimizations - small CPU savings'
}

# Optional extras (asked during install)
$ShaderMods = [ordered]@{ 'iris' = 'Iris Shaders' }
$ShaderPack = 'makeup-ultra-fast-shaders'      # the lightest good-looking shader pack
$AnimationMods = [ordered]@{
    'entitytexturefeatures'  = 'Entity Texture Features'
    'entity-model-features'  = 'Entity Model Features'
}
$AnimationPack = 'fresh-animations'

function Write-Step($text) { Write-Host ''; Write-Host "  > $text" -ForegroundColor Magenta }
function Write-Ok($text)   { Write-Host "    + $text" -ForegroundColor Green }
function Write-Warn2($text){ Write-Host "    ! $text" -ForegroundColor Yellow }

function Get-Json($url) {
    Invoke-RestMethod -Uri $url -Headers @{ 'User-Agent' = $UserAgent } -UseBasicParsing
}

# PowerShell 5's -Encoding UTF8 adds a BOM, which the launcher's JSON parser rejects.
function Write-Utf8($path, $text) {
    [IO.File]::WriteAllText($path, $text, (New-Object System.Text.UTF8Encoding $false))
}

function Ask-YesNo($question, $default) {
    $hint = if ($default) { '[Y/n]' } else { '[y/N]' }
    $answer = Read-Host "  $question $hint"
    if ([string]::IsNullOrWhiteSpace($answer)) { return $default }
    return $answer.Trim().ToLower().StartsWith('y')
}

function Save-File($url, $path) {
    Invoke-WebRequest -Uri $url -OutFile $path -Headers @{ 'User-Agent' = $UserAgent } -UseBasicParsing
}

Clear-Host
Write-Host ''
Write-Host '   __     __   _            _ _         ' -ForegroundColor Magenta
Write-Host '   \ \   / /__| | ___   ___(_) |_ _   _ ' -ForegroundColor Magenta
Write-Host '    \ \ / / _ \ |/ _ \ / __| | __| | | |' -ForegroundColor Magenta
Write-Host '     \ V /  __/ | (_) | (__| | |_| |_| |' -ForegroundColor Magenta
Write-Host '      \_/ \___|_|\___/ \___|_|\__|\__, |' -ForegroundColor Magenta
Write-Host '                    CLIENT INSTALLER |___/ ' -ForegroundColor Magenta
Write-Host ''

if (-not (Test-Path $McDir)) {
    Write-Host 'Could not find the official Minecraft Launcher folder:' -ForegroundColor Red
    Write-Host "  $McDir"
    Write-Host 'Install the Minecraft Launcher, open it once, then run this installer again.'
    exit 1
}

if (Get-Process -Name 'MinecraftLauncher', 'Minecraft' -ErrorAction SilentlyContinue) {
    Write-Warn2 'The Minecraft Launcher is open. Please close it now, then press Enter to continue.'
    [void](Read-Host)
}

New-Item -ItemType Directory -Force -Path $ModsDir | Out-Null

# --- 1. Fabric loader ------------------------------------------------------
Write-Step "Installing Fabric for Minecraft $McVersion"
$loaders = Get-Json "https://meta.fabricmc.net/v2/versions/loader/$McVersion"
$loader = ($loaders | Where-Object { $_.loader.stable } | Select-Object -First 1).loader.version
if (-not $loader) { $loader = $loaders[0].loader.version }
$profileJson = Get-Json "https://meta.fabricmc.net/v2/versions/loader/$McVersion/$loader/profile/json"
$versionId = $profileJson.id
$versionDir = Join-Path $McDir "versions\$versionId"
New-Item -ItemType Directory -Force -Path $versionDir | Out-Null
Write-Utf8 (Join-Path $versionDir "$versionId.json") ($profileJson | ConvertTo-Json -Depth 32)
$dummyJar = Join-Path $versionDir "$versionId.jar"
if (-not (Test-Path $dummyJar)) { New-Item -ItemType File -Path $dummyJar | Out-Null }
Write-Ok "Fabric Loader $loader"

# --- 2. Choose extras ------------------------------------------------------
Write-Step 'Optional extras'
Write-Host '    Shaders look amazing but cost a lot of FPS on integrated graphics.' -ForegroundColor DarkGray
$wantShaders = Ask-YesNo 'Install Iris + MakeUp Ultra Fast shaders? (turn on with O in Video Settings > Shader Packs)' $false
$wantAnimations = Ask-YesNo 'Install Fresh Animations (smooth mob animations, small FPS cost)?' $true

# --- 3. Remove files this installer added last time (so updates are clean) --
$ShaderDir = Join-Path $GameDir 'shaderpacks'
$PackDir = Join-Path $GameDir 'resourcepacks'
New-Item -ItemType Directory -Force -Path $ShaderDir, $PackDir | Out-Null
if (Test-Path $Managed) {
    foreach ($old in Get-Content $Managed) {
        if (-not $old) { continue }
        $oldPath = if ($old.Contains('\')) { Join-Path $GameDir $old } else { Join-Path $ModsDir $old }
        if (Test-Path $oldPath) { Remove-Item $oldPath -Force }
    }
}
$installed = New-Object System.Collections.Generic.List[string]

# Downloads the newest $McVersion release of a Modrinth project into $dir. Returns the file name or $null.
function Install-Modrinth($slug, $label, $loader, $dir, $prefix) {
    $query = '?loaders=%5B%22' + $loader + '%22%5D&game_versions=%5B%22' + $McVersion + '%22%5D'
    try {
        $versions = Get-Json ("https://api.modrinth.com/v2/project/$slug/version" + $query)
        if (-not $versions -and $loader -ne 'fabric') {
            # Shader / resource packs often don't list every game version, so accept any version for their loader.
            $versions = Get-Json ("https://api.modrinth.com/v2/project/$slug/version" + '?loaders=%5B%22' + $loader + '%22%5D')
        }
        $pick = $versions | Where-Object { $_.version_type -eq 'release' } | Select-Object -First 1
        if (-not $pick) { $pick = $versions | Select-Object -First 1 }
        if (-not $pick) { Write-Warn2 "${label}: no $McVersion version found, skipped"; return $null }
        $file = $pick.files | Where-Object { $_.primary } | Select-Object -First 1
        if (-not $file) { $file = $pick.files | Select-Object -First 1 }
        Save-File $file.url (Join-Path $dir $file.filename)
        $installed.Add($prefix + $file.filename)
        Write-Ok "$label  ($($pick.version_number))"
        return $file.filename
    } catch {
        Write-Warn2 "${label}: download failed ($($_.Exception.Message))"
        return $null
    }
}

# --- 4. Performance mods from Modrinth ------------------------------------
Write-Step 'Downloading performance mods from Modrinth'
foreach ($slug in $Mods.Keys) { [void](Install-Modrinth $slug $Mods[$slug] 'fabric' $ModsDir '') }

if ($wantShaders) {
    Write-Step 'Downloading shaders'
    foreach ($slug in $ShaderMods.Keys) { [void](Install-Modrinth $slug $ShaderMods[$slug] 'fabric' $ModsDir '') }
    [void](Install-Modrinth $ShaderPack 'MakeUp Ultra Fast shader pack' 'iris' $ShaderDir 'shaderpacks\')
}

$animationPackFile = $null
if ($wantAnimations) {
    Write-Step 'Downloading Fresh Animations'
    foreach ($slug in $AnimationMods.Keys) { [void](Install-Modrinth $slug $AnimationMods[$slug] 'fabric' $ModsDir '') }
    $animationPackFile = Install-Modrinth $AnimationPack 'Fresh Animations resource pack' 'minecraft' $PackDir 'resourcepacks\'
}

# --- 5. Velocity itself -----------------------------------------------------
Write-Step 'Installing Velocity Client'
$localJar = Get-ChildItem -Path $ScriptDir, (Join-Path $ScriptDir '..\build\libs') -Filter 'velocity-client-*.jar' -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notmatch 'sources|dev' } | Sort-Object LastWriteTime -Descending | Select-Object -First 1
if ($localJar) {
    Copy-Item $localJar.FullName (Join-Path $ModsDir $localJar.Name) -Force
    $installed.Add($localJar.Name)
    Write-Ok "Velocity ($($localJar.Name))"
} else {
    try {
        $release = Get-Json "https://api.github.com/repos/$Repo/releases/latest"
        $asset = $release.assets | Where-Object { $_.name -like 'velocity-client-*.jar' } | Select-Object -First 1
        Save-File $asset.browser_download_url (Join-Path $ModsDir $asset.name)
        $installed.Add($asset.name)
        Write-Ok "Velocity $($release.tag_name)"
    } catch {
        Write-Warn2 'Could not find a Velocity jar. Put velocity-client-x.y.z.jar next to this installer and run it again.'
    }
}
Write-Utf8 $Managed ($installed -join [Environment]::NewLine)

# Keep your keybinds and settings from normal Minecraft on the first install.
$opts = Join-Path $GameDir 'options.txt'
if (-not (Test-Path $opts) -and (Test-Path (Join-Path $McDir 'options.txt'))) {
    Copy-Item (Join-Path $McDir 'options.txt') $opts
}

# Switch Fresh Animations on so it works straight away.
if ($animationPackFile) {
    $entry = '"file/' + $animationPackFile + '"'
    $lines = if (Test-Path $opts) { @(Get-Content $opts) } else { @() }
    $found = $false
    for ($i = 0; $i -lt $lines.Count; $i++) {
        if ($lines[$i] -match '^resourcePacks:\[(.*)\]$') {
            $found = $true
            $list = $Matches[1]
            # Drop older Fresh Animations versions, then add the new one at the end (highest priority).
            $items = @($list -split ',' | Where-Object { $_ -and ($_ -notmatch 'FreshAnimations|Fresh Animations|fresh-animations') })
            $items += $entry
            $lines[$i] = 'resourcePacks:[' + ($items -join ',') + ']'
        }
    }
    if (-not $found) { $lines += 'resourcePacks:["vanilla","fabric",' + $entry + ']' }
    Write-Utf8 $opts ($lines -join [Environment]::NewLine)
    Write-Ok 'Fresh Animations enabled'
}

# --- 6. Launcher profile ----------------------------------------------------
Write-Step 'Adding the "Velocity Client" profile to the Minecraft Launcher'
$iconPath = Join-Path $ScriptDir 'velocity.png'
$icon = 'Grass'
if (Test-Path $iconPath) { $icon = 'data:image/png;base64,' + [Convert]::ToBase64String([IO.File]::ReadAllBytes($iconPath)) }
$now = (Get-Date).ToUniversalTime().ToString('yyyy-MM-ddTHH:mm:ss.fffZ')

$profileFiles = @('launcher_profiles.json', 'launcher_profiles_microsoft_store.json') |
        ForEach-Object { Join-Path $McDir $_ } | Where-Object { Test-Path $_ }
if (-not $profileFiles) {
    $profileFiles = @(Join-Path $McDir 'launcher_profiles.json')
    Write-Utf8 $profileFiles[0] '{ "profiles": {}, "settings": {}, "version": 3 }'
}

foreach ($pf in $profileFiles) {
    Copy-Item $pf "$pf.velocity-backup" -Force
    $data = Get-Content $pf -Raw | ConvertFrom-Json
    if (-not $data.profiles) { $data | Add-Member -NotePropertyName profiles -NotePropertyValue ([pscustomobject]@{}) -Force }
    $velocityProfile = [pscustomobject]@{
        name          = 'Velocity Client'
        type          = 'custom'
        created       = $now
        lastUsed      = $now
        lastVersionId = $versionId
        gameDir       = $GameDir
        javaArgs      = $JavaArgs
        icon          = $icon
    }
    $data.profiles | Add-Member -NotePropertyName $ProfileKey -NotePropertyValue $velocityProfile -Force
    Write-Utf8 $pf ($data | ConvertTo-Json -Depth 32)
    Write-Ok (Split-Path $pf -Leaf)
}

Write-Host ''
Write-Host '  Velocity Client is installed!' -ForegroundColor Green
Write-Host ''
Write-Host '  1. Open the Minecraft Launcher'
Write-Host '  2. Pick "Velocity Client" next to the Play button'
Write-Host '  3. Press Play  -  in game, press RIGHT SHIFT to open the Velocity menu'
Write-Host ''
Write-Host '  Extra FPS tip for laptops: plug in the charger and set Windows power mode to "Best performance".'
Write-Host "  Your Velocity mods folder: $ModsDir"
Write-Host ''
