# VAG OEM · Volkswagen Polo 2020 · WV232 / 9070

Aplicación Android nativa Java, una sola APK, con interfaz estilo OEM Volkswagen. Versión de desarrollo **0.9.0**.

## Módulos integrados
- VAG Home: launcher OEM horizontal.
- VAG Navigation: motor de mapas vectoriales MapLibre Native 13.6.1.
- VAG Map Manager: paquetes regionales offline, progreso, biblioteca y eliminación.
- VAG Road Alerts: reportes manuales geolocalizados guardados localmente.
- Valhalla Mobile 0.6.3: motor nativo de cálculo de rutas offline integrado en la APK.
- VAG Sound Studio: perfiles locales y EQ de 10 bandas.
- Audio Lab: diagnóstico de micrófono.
- VAG Vehicle: pantallas de estado/viaje, CANBUS real aún pendiente.

## Navegación y cartografía

### MapLibre
El motor de renderizado vectorial y sus pantallas están incluidos en la APK. El estilo Liberty se sirve desde OpenFreeMap. La app configura un User-Agent identificable y limita las solicitudes HTTP simultáneas a cuatro.

El administrador permite descargar regiones desde el estéreo:
- Durango estatal (vista general).
- Coahuila estatal (vista general).
- La Laguna (detalle urbano).
- Durango capital (detalle urbano).
- Saltillo (detalle urbano).

Los paquetes descargados quedan en la base offline SQLite de MapLibre y pueden utilizarse sin conexión una vez completados. La descarga inicial requiere Wi-Fi/internet. Cada descarga se detiene al alcanzar 3,000 teselas o 50 MB y el paquete excedido se elimina. OpenFreeMap permite offline downloads de hasta 3,000 tiles o 50 MB, hasta cuatro solicitudes concurrentes y User-Agent único. <https://openfreemap.org/tos/>

**No se afirma que los mapas vectoriales estén precargados en el APK 0.9.0.** El motor está en el APK; los datos cartográficos regionales se descargan desde el administrador para no inflar la instalación y poder actualizarlos por separado.

### Rutas offline Valhalla
La APK incluye el motor nativo Valhalla Mobile 0.6.3. La pantalla ya dispone de flujo para:
- Buscar un destino cuando Android proporciona Geocoder.
- Obtener posición de origen mediante GPS.
- Calcular ruta local con Valhalla.
- Dibujar geometría y mostrar distancia, tiempo e instrucción inicial.

Para habilitar el cálculo se necesita el archivo de red vial `vag_valhalla_tiles.tar`. El administrador incluye un importador para instalarlo en el almacenamiento privado de la app. **El paquete de red vial todavía no está incluido en la APK 0.9.0.**

### Construcción del paquete de rutas
Se agregó `.github/workflows/build-offline-route-data.yml`, ejecutable manualmente desde GitHub Actions. El flujo:
1. Descarga el extracto OSM de México desde Geofabrik.
2. Recorta la región Durango-Coahuila y sus corredores de conexión.
3. Construye el grafo con la imagen oficial Valhalla.
4. Publica el artefacto `VAG-OEM-Durango-Coahuila-Valhalla-Tiles`.

Script: `tools/build-valhalla-region.sh`.
Tras ejecutar el workflow, el artefacto TAR se puede instalar desde VAG Navigation → Mapas offline → Importar paquete de rutas Valhalla. La siguiente fase será publicar un paquete regional versionado y hacer que la app lo gestione desde su propio descargador.

### Alertas de carretera
- Reportes manuales: peligro, bache, obra/cierre, inundación, accidente, vehículo detenido, retén/control y otro.
- Coordenadas GPS y nota opcional.
- Marcadores y lista persistente.
- Disponible sin conexión en el equipo donde se guardó.

**Las alertas actuales son locales; no son Waze ni se sincronizan entre vehículos.** Las alertas comunitarias y tráfico en vivo requieren backend/feed de datos y conectividad.

## Sonido
- EQ visual de 10 bandas, -6 a +6 dB.
- Perfiles OEM equilibrado, Pop, Rock, Voz y Personalizado.
- Diagnóstico de efectos Android, códecs y salidas.
- No procesa globalmente radio/CPLink: hace falta interfaz del DSP o servicio privilegiado del fabricante.

## Compatibilidad y seguridad
- Android 10 del WV232 está dentro del rango soportado.
- minSdk 24, targetSdk 32.
- CPLink original no se modifica.
- No se modifica firmware, MCU, Factory Settings ni CANBUS.
- La cámara de reversa conserva su conmutación original.
- CANBUS continúa pendiente de integración de solo lectura.

## Build
JDK 17 · Gradle 8.9 · Android Gradle Plugin 8.7.3 · SDK 35.
Artefacto de Actions: `VAG-OEM-WV232-0.9.0-debug-APK`.

## Estado de validación
La implementación está guardada en GitHub. Compilación Android y pruebas de rendimiento/compatibilidad en el WV232 siguen pendientes de confirmación. No se declara una APK compilada hasta que Actions reporte éxito.
