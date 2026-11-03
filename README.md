# Volkswagen Polo 2020 · WV232

Launcher Android de estilo OEM inspirado en Volkswagen Discover Media / MIB2.

## Versión 0.5.0 — VAG Sound Studio + diagnóstico de hardware

### VAG Sound Studio
- Perfiles locales OEM equilibrado, Pop, Rock, Voz y Personalizado.
- Ecualizador gráfico de 10 bandas con ajuste de -6 a +6 dB y persistencia local.
- Diagnóstico de disponibilidad de Equalizer, LoudnessEnhancer y DynamicsProcessing.
- Inventario de códecs decodificadores de audio expuestos por Android.
- Enumeración de salidas de audio que Android reporta.
- Enlace al Audio Lab para medir el micrófono y a los ajustes de sonido Android.

**Límite técnico:** los perfiles se guardan en VAG OEM, pero todavía no procesan la radio, CPLink ni el audio global del sistema. Android 10 no garantiza control global desde una app normal. Para aplicar EQ/Loudness de forma global necesitamos una interfaz del fabricante, DSP accesible, servicio privilegiado o integración de firmware. No se fuerza ganancia artificial: eso puede producir clipping y distorsión, no potencia real.

### Diagnóstico del hardware
La pantalla informa versión Android/API, fabricante, modelo, placa, hardware, producto, ABI, resolución lógica, densidad, RAM disponible, almacenamiento /data, efectos de audio, códecs y salidas de audio reportadas.
Los datos obtenidos en el propio estéreo son la fuente de verdad para identificar cada revisión de WV232/9070.

### Audio Lab
- Prueba de entrada mediante AudioRecord con fuente MIC predeterminada.
- Medidor RMS aproximado y pico.
- Detección AGC, AEC y NoiseSuppressor para sesiones compatibles.
- Captura temporal: no se guarda ni transmite audio.
- Detiene la captura al salir.
- Solicita permiso de micrófono en tiempo de ejecución.

**Limitación:** no puede elegir la cápsula interna/externa si la MCU las presenta como una sola entrada. Tampoco cambia la ganancia de llamadas Bluetooth o CPLink.

### Almacenamiento y navegación
- Se informa espacio libre de /data.
- No descargar mapas con menos de 2 GB libres.
- Los paquetes de mapas se instalarán por separado del APK para administrar espacio.

### Conservación del hardware original
- CPLink no se modifica ni sustituye.
- No se modifica firmware, MCU, Factory Settings ni parámetros CANBUS.
- La cámara conserva la conmutación original.
- CANBUS permanece como integración de solo lectura pendiente de identificar.

## Instalación y pruebas
1. Descarga el artefacto de GitHub Actions.
2. Instala el APK debug.
3. Abre Sonido para entrar a VAG Sound Studio.
4. Revisa el bloque DIAGNÓSTICO DEL EQUIPO y comparte una captura para identificar la revisión exacta.
5. Los perfiles de EQ se guardan localmente; todavía no afectan apps externas.
6. Prueba CPLink, radio y Bluetooth por separado.

## Compilar APK
Abre Actions y descarga el artefacto `VAG-OEM-WV232-0.5.0-debug-APK`.

Herramientas: JDK 17, Gradle 8.9, Android Gradle Plugin 8.7.3, SDK 35. minSdk 23, targetSdk 32.

## Estado
- VAG Sound Studio y diagnóstico incorporados al código.
- Compilación y validación en el WV232 pendientes.
- No es firmware ni reemplaza aplicaciones originales.
- Navegación offline nativa y CANBUS de solo lectura siguen pendientes.
