$targets = @(
    @{ type = "Phone"; avd = "Pixel_8_2"; name = "Phone" },
    @{ type = "TV"; avd = "Television_1080p_2"; name = "TV" },
    @{ type = "Wear"; avd = "Wear_OS_XL_Round"; name = "Wear OS" }
)

$SdkPath = "C:\Users\Lenovo\AppData\Local\Android\Sdk"
$EmulatorCmd = "$SdkPath\emulator\emulator.exe"
$AdbCmd = "$SdkPath\platform-tools\adb.exe"

Write-Host ">>> Resolving active and launching missing emulators..." -ForegroundColor Cyan

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

    # Fallback: check model name
    $model = & $AdbCmd -s $serial shell getprop ro.product.model
    if ($model -like "*wear*" -or $model -like "*watch*") { return "Wear" }
    if ($model -like "*tv*" -or $model -like "*Television*") { return "TV" }

    return "Phone"
}

# Start missing emulators
$runningSerials = Get-RunningEmulators
$runningAvds = @{}
foreach ($serial in $runningSerials) {
    $type = Get-DeviceType $serial
    $runningAvds[$type] = $serial
    Write-Host "Found already running emulator: $serial -> $type" -ForegroundColor Yellow
}

foreach ($target in $targets) {
    $type = $target.type
    $avdName = $target.avd
    if (-not $runningAvds.ContainsKey($type)) {
        Write-Host "Starting emulator for $avdName ($type)..." -ForegroundColor Green
        Start-Process -FilePath $EmulatorCmd -ArgumentList "-avd $avdName" -WindowStyle Minimized
    }
}

# Wait for all emulators to boot and get services ready
Write-Host ">>> Waiting for all emulators to boot and system services to initialize (this can take a minute)..." -ForegroundColor Cyan
$mappedSerials = @{}

while ($mappedSerials.Count -lt $targets.Length) {
    Start-Sleep -Seconds 3
    $runningSerials = Get-RunningEmulators
    foreach ($serial in $runningSerials) {
        $type = Get-DeviceType $serial
        if (-not $mappedSerials.ContainsKey($type)) {
            $mappedSerials[$type] = $serial
            Write-Host "Detected emulator online: $serial -> $type" -ForegroundColor Green
        }
    }
    
    # Check if all targeted AVDs are booted and services are running
    $allReady = $true
    foreach ($target in $targets) {
        $type = $target.type
        if ($mappedSerials.ContainsKey($type)) {
            $serial = $mappedSerials[$type]
            
            # Check boot completion
            $bootStatus = & $AdbCmd -s $serial shell getprop sys.boot_completed
            if ($bootStatus.Trim() -ne "1") {
                $allReady = $false
                continue
            }

            # Check service availability
            $packageCheck = & $AdbCmd -s $serial shell service check package
            $activityCheck = & $AdbCmd -s $serial shell service check activity
            if ($packageCheck.Trim() -ne "Service package: found" -or $activityCheck.Trim() -ne "Service activity: found") {
                $allReady = $false
            }
        } else {
            $allReady = $false
        }
    }
    
    if ($allReady) {
        Write-Host ">>> All emulators are fully booted and ready!" -ForegroundColor Green
        break
    } else {
        Write-Host "Waiting for boot and system services..." -ForegroundColor Gray
    }
}
