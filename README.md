# VAG OEM · Volkswagen Polo 2020 · WV232 / 9070

Proyecto Android nativo Java para convertir el estéreo aftermarket en una experiencia integrada estilo OEM Volkswagen. Una sola APK, con módulos internos.

## Versión 0.6.0 — VAG Navigation integrado

### Aplicación única
La aplicación incluye Home, Sound Studio, Audio Lab, Navigation y pantallas de vehículo/sistema. No se divide en APK independientes. CPLink y las apps originales del fabricante se conservan y se abren sin modificarlas.

### VAG Navigation — primera integración
- Motor de renderizado vectorial MapLibre Native Android 13.6.1 incluido como dependencia de la APK.
- Estilo Liberty de OpenFreeMap para cartografía vectorial.
- Interfaz horizontal optimizada para pantalla de automóvil: buscador, estado, ubicación GPS, perspectiva inclinada y capas.
- Búsqueda de destinos mediante Geocoder Android cuando exista proveedor disponible.
- Centrado inicial en la región de La Laguna (Durango/Coahuila).
- Permisos de ubicación solicitados al utilizar la función.
- Ciclo de vida de MapView conectado a la Activity.
- Atribución visible de OpenStreetMap/OpenFreeMap.

**Estado funcional:** el mapa base y la interfaz están integrados. La primera carga del estilo requiere conexión a internet. La búsqueda de destinos también depende de que el dispositivo tenga un proveedor de geocodificación. El botón 3D inclina la cámara; la extrusión de edificios depende de las capas disponibles en el estilo.

**Todavía pendiente:** paquetes offline regionales instalables, motor de cálculo de rutas local, navegación giro a giro, indicaciones de voz, reruteo offline, tráfico en vivo y alertas comunitarias. No se simulan como funciones terminadas. La navegación giro a giro requiere un motor de rutas y datos OSM regionales preparados; Valhalla es candidato para la fase offline.

### Mapas y almacenamiento
- El motor MapLibre está integrado en la APK; los datos de mapas no se incrustan en el APK.
- Los paquetes regionales se instalarán desde el administrador de mapas en una fase posterior.
- Se reservará espacio para Android, CPLink y aplicaciones OEM.
- No se iniciará una descarga cartográfica masiva sin comprobar el espacio disponible.
- Los paquetes de mapas se diseñarán por región y niveles de zoom para limitar almacenamiento.

### VAG Sound Studio
- Perfiles locales OEM equilibrado, Pop, Rock, Voz y Personalizado.
- Ecualizador gráfico de 10 bandas con ajustes de -6 a +6 dB y persistencia local.
- Diagnóstico de disponibilidad de Equalizer, LoudnessEnhancer y DynamicsProcessing.
- Inventario de códecs decodificadores y salidas de audio expuestas por Android.
- Los perfiles se guardan, pero todavía no procesan la radio, CPLink ni el audio global. Para eso hace falta acceso al DSP/servicio de audio del fabricante o integración privilegiada.

### Audio Lab
- Prueba temporal de micrófono mediante AudioRecord.
- Medidor RMS aproximado y pico.
- Detección de AGC, AEC y NoiseSuppressor para sesiones compatibles.
- No guarda ni transmite audio.
- No cambia la ganancia de llamadas Bluetooth o CPLink.

### Conservación del hardware
- CPLink no se modifica ni sustituye.
- No se modifica firmware, MCU, Factory Settings ni parámetros CANBUS.
- La cámara conserva la conmutación original.
- CANBUS permanece como integración de solo lectura pendiente de identificar.

## Datos de hardware
La pantalla Sound Studio reporta versión Android/API, fabricante, modelo, placa, hardware, producto, ABI, resolución lógica, densidad, RAM disponible, almacenamiento /data, efectos de audio, códecs y salidas. Esos datos permiten identificar la revisión real del WV232/9070.

## Compilación
GitHub Actions compila el APK debug con JDK 17, Gradle 8.9, Android Gradle Plugin 8.7.3 y SDK 35. minSdk 23, targetSdk 32.
Artefacto: `VAG-OEM-WV232-0.6.0-debug-APK`.

## Validación
- Código de VAG Navigation integrado; compilación y prueba en hardware pendientes.
- Confirmar que la versión del WV232 puede renderizar el estilo vectorial con fluidez.
- Medir consumo de RAM, almacenamiento y temperatura en uso prolongado.
- Probar GPS y permisos en Android 10 real.
