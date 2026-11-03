# VAG OEM · Volkswagen Polo 2020 · WV232 / 9070

## Versión 0.7.0 — Mapas offline y alertas locales

Aplicación Android nativa Java en una sola APK. Incluye Home OEM, VAG Sound Studio, Audio Lab, VAG Navigation, administrador de mapas y alertas locales.

### Navegación
- MapLibre Native Android 13.6.1 integrado.
- Estilo vectorial Liberty de OpenFreeMap.
- Búsqueda de destinos mediante Geocoder Android cuando hay proveedor disponible.
- GPS, perspectiva inclinada, capas y navegación regional.
- Administrador de paquetes offline integrado.
- Paquetes: Durango estatal (vista general), Coahuila estatal (vista general), La Laguna (detalle), Durango capital (detalle) y Saltillo (detalle).
- Descarga iniciada explícitamente por el usuario; una vez completa, las regiones se guardan en la base SQLite privada de MapLibre y se pueden consultar sin internet.
- Límite configurado de 3,000 tiles y parada al superar 50 MB por paquete.
- Atribución visible: OpenFreeMap © OpenMapTiles · Datos de OpenStreetMap (ODbL).

**Contenido de la APK:** incluye el motor, pantallas y administrador. Los paquetes cartográficos no están precargados dentro del APK 0.7.0; se descargan desde la propia app y quedan almacenados en el estéreo. Una APK autosuficiente con mapas precargados requiere generar y validar una base offline de MapLibre e incorporarla como asset en una siguiente fase.

### Alertas VAG
- Reportes manuales geolocalizados de peligros, baches, obras/cierres, inundaciones, accidentes, vehículos detenidos y otros.
- Guardado persistente local.
- Marcadores en el mapa y lista de reportes.
- Los reportes locales siguen disponibles sin conexión.

**Límite:** no son alertas Waze ni se comparten en vivo. Alertas comunitarias sincronizadas requieren un servicio de datos y conectividad; no se inventan incidencias.

### Condiciones cartográficas
OpenFreeMap permite descargas offline iniciadas por el usuario hasta 3,000 tiles o 50 MB por descarga, con un máximo de 4 solicitudes concurrentes y User-Agent identificable. Se limita cada paquete y se solicita acción explícita. No usar el servidor estándar de teselas raster de OpenStreetMap para descargas offline masivas.

### Sound Studio
- Perfiles locales OEM, Pop, Rock, Voz y Personalizado.
- EQ de 10 bandas, -6 a +6 dB.
- Diagnóstico de efectos Android, códecs y salidas de audio.
- Los perfiles se guardan, pero no procesan todavía el audio global, radio o CPLink. Requiere acceso al DSP o servicio privilegiado del fabricante.

### Audio Lab
- Prueba temporal de micrófono y medidor RMS/pico.
- Detección de AGC, AEC y reducción de ruido.
- No guarda ni transmite audio.

### Protección de equipo original
- CPLink no se modifica.
- No se modifica firmware, MCU, Factory Settings ni parámetros CANBUS.
- La cámara conserva la conmutación original.
- CANBUS queda pendiente de integración de solo lectura.

### Build
JDK 17 · Gradle 8.9 · Android Gradle Plugin 8.7.3 · SDK 35 · minSdk 23 · targetSdk 32.
Artefacto: `VAG-OEM-WV232-0.7.0-debug-APK`.

### Validación pendiente
Compilación y prueba en hardware Android 10; verificar descarga completa y uso offline; probar GPS. Navegación giro a giro offline y alertas comunitarias sincronizadas siguen pendientes.
