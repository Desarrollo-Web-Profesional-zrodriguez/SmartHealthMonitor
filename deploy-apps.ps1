$targets = @(
    @{
        type     = "Phone"
        apk      = "app/build/outputs/apk/debug/app-debug.apk"
        activity = "mx.utng.smarthealthmonitor/.MainActivity"
        name     = "Phone/App"
    },
    @{
        type     = "TV"
        apk      = "tv/build/outputs/apk/debug/tv-debug.apk"
        activity = "mx.utng.smarthealthmonitor/mx.utng.smarthealthmonitor.tv.MainActivity"
        name     = "TV"
    },
    @{
        type     = "Wear"
        apk      = "wear/build/outputs/apk/debug/wear-debug.apk"
        activity = "mx.utng.smarthealthmonitor/mx.utng.smarthealthmonitor.wear.presentation.WearMainActivity"
        name     = "Wear OS"
    }
)

$SdkPath = "C:\Users\Lenovo\AppData\Local\Android\Sdk"
$AdbCmd = "$SdkPath\platform-tools\adb.exe"

# 1. Set environment variables to fix Gradle/jlink build environment
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"

Write-Host ">>> Building the debug APKs for all modules..." -ForegroundColor Cyan
& .\gradlew.bat assembleDebug "-Dorg.gradle.java.home=C:\Program Files\Android\Android Studio\jbr"

if ($LASTEXITCODE -ne 0) {
    Write-Error "Gradle build failed!"
    exit 1
}
Write-Host ">>> Build completed successfully!" -ForegroundColor Green

# 2. Get active emulators
Write-Host ">>> Resolving active emulators..." -ForegroundColor Cyan

function Get-RunningEmulators {
    $devices = & $AdbCmd devices
    $serials = @()
    foreach ($line in $devices) {
        if ($line -match "^(emulator-\d+)\s+device") {
            $serials += $Matches[1]
        }
    }
    return $serials
}

function Get-DeviceType ($serial) {
    # Check if the device is online
    $stateCheck = & $AdbCmd devices
    $isOffline = $true
    foreach ($line in $stateCheck) {
        if ($line -match "^$serial\s+device") {
            $isOffline = $false
            break
        }
    }
    if ($isOffline) {
        return "Offline"
    }

    $avd = & $AdbCmd -s $serial emu avd name
    $avdName = ""
    if ($avd -match "([^\r\n]+)\r?\nOK") {
        $avdName = $Matches[1].Trim()
    }

    # Match exact AVD names
    if ($avdName -eq "Pixel_8_2") { return "Phone" }
    if ($avdName -eq "Television_1080p_2") { return "TV" }
    if ($avdName -eq "Wear_OS_XL_Round") { return "Wear" }

    # Fallback: check characteristics
    $characteristics = & $AdbCmd -s $serial shell getprop ro.build.characteristics
    if ($characteristics -like "*watch*" -or $avdName -like "*wear*") { return "Wear" }
    if ($characteristics -like "*tv*" -or $avdName -like "*tv*") { return "TV" }

    # Fallback: check model
    $model = & $AdbCmd -s $serial shell getprop ro.product.model
    if ($model -like "*wear*" -or $model -like "*watch*") { return "Wear" }
    if ($model -like "*tv*" -or $model -like "*Television*") { return "TV" }

    return "Phone"
}

$runningSerials = Get-RunningEmulators
$mappedSerials = @{}
foreach ($serial in $runningSerials) {
    $type = Get-DeviceType $serial
    $mappedSerials[$type] = $serial
    Write-Host "Found emulator: $serial -> $type" -ForegroundColor Yellow
}

# 3. Wait helper
function Wait-For-Services ($serial) {
    Write-Host "Checking android services readiness on $serial..." -ForegroundColor Gray
    for ($i = 0; $i -lt 15; $i++) {
        $packageCheck = & $AdbCmd -s $serial shell service check package
        $activityCheck = & $AdbCmd -s $serial shell service check activity
        if ($packageCheck.Trim() -eq "Service package: found" -and $activityCheck.Trim() -eq "Service activity: found") {
            return $true
        }
        Start-Sleep -Seconds 2
    }
    return $false
}

# 4. Deploy APKs
foreach ($target in $targets) {
    $type = $target.type
    $serial = $mappedSerials[$type]
    $apkPath = $target.apk
    $activity = $target.activity
    $name = $target.name

    if ($serial) {
        $ready = Wait-For-Services $serial
        if ($ready) {
            Write-Host ">>> Installing and running $name on $type ($serial)..." -ForegroundColor Cyan
            & $AdbCmd -s $serial install -r $apkPath
            & $AdbCmd -s $serial shell am start -n $activity
        } else {
            Write-Warning "Android services were not ready on $type ($serial). Skipping install."
        }
    } else {
        Write-Warning "No active emulator found for $name ($type)! Make sure to run .\start-emulators.ps1 first."
    }
}

Write-Host ">>> All apps successfully deployed!" -ForegroundColor Green
