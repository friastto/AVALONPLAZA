# Script de conexion dinamica y persistente para dispositivos por ADB Reverse
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"

while ($true) {
    try {
        # 1. Obtener la lista de seriales de todos los dispositivos conectados y autorizados
        $devices = & $adb devices | Where-Object { $_ -match "\tdevice$" } | ForEach-Object { ($_ -split "\t")[0] }

        # 2. Aplicar adb reverse a cada uno de ellos de forma independiente
        foreach ($dev in $devices) {
            if ($dev) {
                & $adb -s $dev reverse tcp:8900 tcp:8900 2>$null
            }
        }
    } catch {
        # Ignorar errores transitorios de conexion USB
    }
    Start-Sleep -Seconds 5
}
