#!/usr/bin/env bash
set -euo pipefail

APK="app/build/outputs/apk/debug/app-debug.apk"
OUT="emulator-preview"
PKG="com.vagoem.polo.diagnostic"

mkdir -p "$OUT"
test -s "$APK" || { echo "APK no encontrada o vacía: $APK" >&2; exit 1; }

echo "== Configurando pantalla virtual =="
adb shell wm size 1024x600
adb shell wm density 160

echo "== Instalando APK =="
adb install -r "$APK"
adb shell am force-stop "$PKG"
adb shell am start -n "$PKG/.MainActivity"
sleep 7

capture() {
  local name="$1"
  sleep 2
  adb exec-out screencap -p > "$OUT/$name.png"
  test -s "$OUT/$name.png"
  echo "Captura: $OUT/$name.png"
}

tap_home() {
  adb shell input tap 43 119
  sleep 1
}

echo "== Launcher y accesos principales =="
capture 00-home
adb shell input tap 150 145
sleep 2
capture 01-radio
tap_home
adb shell input tap 635 145
sleep 2
capture 02-media
tap_home
adb shell input tap 150 248
sleep 2
capture 03-phone
tap_home
adb shell input tap 635 248
sleep 2
capture 04-app-connect

echo "== Pantallas principales =="
tap_home
adb shell input tap 150 352
sleep 2
capture 05-navigation-entry
tap_home
adb shell input tap 150 456
sleep 2
capture 06-settings
tap_home
adb shell input tap 635 456
sleep 2
capture 07-sound-entry

echo "== Tablero y pestañas del vehículo =="
tap_home
adb shell input tap 635 352
sleep 2
capture 08-vehicle-general
adb shell input tap 390 160
capture 09-vehicle-trip
adb shell input tap 630 160
capture 10-vehicle-engine
adb shell input tap 870 160
capture 11-vehicle-states

echo "== Actividades Android independientes =="
adb shell am start -n "$PKG/.SoundStudioActivity"
sleep 5
capture 12-sound-studio
adb shell am start -n "$PKG/.AudioLabActivity"
sleep 5
capture 13-audio-lab
adb shell am start -n "$PKG/.OfflineMapsActivity"
sleep 7
capture 14-offline-map-manager
adb shell am start -n "$PKG/.NavigationActivity"
sleep 25
capture 15-navigation-map

echo "== Probando búsqueda y selección de destino =="
adb shell input tap 300 95
sleep 2
adb shell input text '25.57,-103.495'
sleep 1
adb shell input tap 500 95
sleep 5
capture 16-navigation-destination

echo "== Guardando diagnóstico =="
adb logcat -d > "$OUT/logcat.txt"
adb shell dumpsys activity activities > "$OUT/activities.txt"
adb shell dumpsys package "$PKG" > "$OUT/package.txt"

cat > "$OUT/index.html" <<'HTML'
<!doctype html><html lang="es"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>VAG OEM WV232 · Android Emulator</title>
<style>body{font:16px system-ui;background:#0b111a;color:#eef2f6;margin:24px}h1{color:#ff7383}section{margin:24px 0}img{display:block;width:min(100%,1024px);height:auto;border:1px solid #465362;border-radius:8px}small{color:#aab6c4}</style>
<h1>VAG OEM · WV232 / Polo 2020</h1><p>Capturas de la APK ejecutada en Android Emulator API 35, resolución 1024×600.</p>
HTML

for f in "$OUT"/*.png; do
  name="$(basename "$f" .png)"
  printf '<section><h2>%s</h2><img src="%s" alt="%s"></section>\n' "$name" "$(basename "$f")" "$name" >> "$OUT/index.html"
done

if grep -q "FATAL EXCEPTION" "$OUT/logcat.txt"; then
  echo "::error::Android registró una excepción fatal. Revisar logcat.txt."
  exit 1
fi

echo "== Vista de emulador completada =="
