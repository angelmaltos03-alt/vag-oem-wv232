# VAG OEM · Volkswagen Polo 2020 · WV232 / 9070

Aplicación Android nativa Java, una sola APK, con interfaz estilo OEM Volkswagen. Versión objetivo **0.11.0**.

## Vista previa de la interfaz

Estas imágenes muestran vistas de referencia de las pantallas principales de VAG OEM, preparadas a partir del diseño y los componentes presentes en el código de la versión 0.11.0. **Son previews de interfaz, no capturas de una ejecución en el estéreo ni del emulador.** La validación visual en Android Emulator y en el WV232 físico se documentará por separado cuando sus pruebas terminen.

<p align="center">
  <strong>VAG Home · Launcher principal</strong><br/>
  <img src="docs/screenshots/home-preview.svg" alt="Vista previa del launcher VAG Home para Volkswagen Polo 2020" width="900"/>
</p>

<p align="center">
  <strong>VAG Vehicle · Tablero de información</strong><br/>
  <img src="docs/screenshots/vehicle-preview.svg" alt="Vista previa del panel de información del vehículo con métricas CANBUS pendientes de lectura" width="900"/>
</p>

<p align="center">
  <strong>VAG Navigation · Mapa y controles</strong><br/>
  <img src="docs/screenshots/navigation-preview.svg" alt="Vista previa de la pantalla de navegación con búsqueda y controles de mapa" width="900"/>
</p>

<p align="center">
  <strong>VAG Sound Studio · Ecualizador de 10 bandas</strong><br/>
  <img src="docs/screenshots/sound-studio-preview.svg" alt="Vista previa de Sound Studio con perfiles de audio y ecualizador de diez bandas" width="900"/>
</p>

> Nota: el tablero de vehículo muestra guiones mientras no se haya identificado e integrado un decodificador CANBUS/OBD compatible. La vista de navegación es una composición ilustrativa; el mapa real usa MapLibre con el estilo Bright de OpenFreeMap y requiere conexión o paquetes offline compatibles. La ruta solo aparece después de calcularse con datos Valhalla disponibles.

## Accesos configurables

Mantén presionado durante aproximadamente **0.6 segundos** un acceso del launcher para elegir la aplicación que debe abrir. La selección se guarda de forma local y no sustituye ni desinstala la aplicación original del equipo.

- Radio, Música, Teléfono y App-Connect: asignan la app correspondiente.
- Navegación: permite elegir un navegador externo; cuando no hay uno asignado, abre VAG Navigation.
- Vehículo: mantiene el tablero VAG Vehicle; desde el aviso inferior **APP CANBUS** se abre la aplicación CANBUS vinculada.
- Ajustes: permite elegir los ajustes OEM del fabricante.
- Sonido: permite elegir una aplicación DSP externa; si no se configura, abre VAG Sound Studio.

Para restablecer una asignación, vuelve a mantener presionado el acceso y selecciona la aplicación deseada. Los vínculos se guardan en preferencias locales.

## Inspiración y créditos

Se agradece a **Elías Salvatierra** por compartir el proyecto [InterfazRanger](https://github.com/eliassalvatierra/InterfazRanger). Su trabajo sirvió como referencia para evaluar patrones útiles de launcher automotriz, especialmente la personalización de accesos mediante pulsación prolongada, la integración de aplicaciones y la atención al consumo de recursos.

VAG OEM implementa estas ideas de forma independiente en Java y conserva su propia interfaz, recursos gráficos y arquitectura. **No se copiaron código fuente, imágenes ni protocolos del proyecto original.** El repositorio de referencia no declara una licencia de reutilización, por lo que se utiliza como inspiración y se acredita al autor, no como código incorporado.

También se identificaron como posibles mejoras futuras la tarjeta multimedia con metadatos y carátula, y el control de las tareas al pasar a segundo plano. Su integración queda sujeta a comprobar el soporte real de las aplicaciones del WV232. El protocolo de radio NWD descrito en InterfazRanger no se reutiliza: depende de paquetes y hardware específicos que no están confirmados en el WV232.

## Módulos
- VAG Home: launcher OEM horizontal.
- VAG Navigation: MapLibre Native 13.6.1.
- VAG Map Manager: regiones offline, progreso, biblioteca y eliminación.
- VAG Road Alerts: reportes manuales geolocalizados locales.
- Valhalla Mobile 0.6.3: motor nativo de rutas offline.
- VAG Sound Studio: perfiles locales y EQ de 10 bandas.
- Audio Lab: diagnóstico de micrófono.
- VAG Vehicle: tablero OEM de combustible, km/L, autonomía, viajes, motor y estados; lecturas CANBUS reales pendientes de identificar e integrar.

## Una sola APK y datos offline

### Motor de rutas regional
La APK integrada 0.11.0 se construye con el grafo Valhalla de Durango-Coahuila como asset `vag_valhalla_tiles.tar`. En el primer cálculo de ruta, la app copia el asset al almacenamiento privado y ejecuta el motor local. No necesita red para calcular una ruta cuando el paquete cubre origen y destino.

El flujo de GitHub Actions `.github/workflows/build-offline-route-data.yml`:
1. Descarga el extracto OSM de México desde Geofabrik.
2. Recorta Durango-Coahuila y los corredores viales cercanos.
3. Construye el grafo con Valhalla.
4. Publica el TAR de red vial.
5. Descarga ese TAR en el job Android y lo integra en la APK final.

El artefacto final de ese flujo se llama `VAG-OEM-WV232-0.11.0-OFFLINE-APK`. La construcción regional tarda y está pendiente de completar/validar en GitHub Actions.

### Mapas visuales
MapLibre Native y la interfaz están incluidos en la APK. Los mapas vectoriales visibles usan el estilo Bright de OpenFreeMap, con una cartografía más contrastada, jerarquía vial, etiquetas y puntos de interés. La interfaz incluye búsqueda superior, controles flotantes de GPS/3D/capas y una hoja inferior para destino y ruta. El administrador ofrece:
- Durango estatal (vista general).
- Coahuila estatal (vista general).
- La Laguna (detalle urbano).
- Durango capital (detalle urbano).
- Saltillo (detalle urbano).

Estos paquetes se descargan desde VAG OEM y quedan en la base offline SQLite de MapLibre para utilizarlos sin internet una vez completados. **Los tiles visuales no están precargados dentro del APK 0.11.0**; se descargan por región para mantener una instalación manejable y permitir actualizaciones. El motor de rutas sí se integra en la APK final del flujo de datos.

OpenFreeMap permite descargas offline de hasta 3,000 teselas o 50 MB por descarga, con un máximo de cuatro solicitudes paralelas y User-Agent identificable. VAG OEM configura User-Agent propio, cuatro solicitudes simultáneas y elimina paquetes que superan el límite. <https://openfreemap.org/tos/>

### Alertas
- Reportes manuales: peligro, bache, obra/cierre, inundación, accidente, vehículo detenido, retén/control y otro.
- Coordenadas GPS y nota opcional.
- Marcadores y lista persistente en el dispositivo.

Son reportes locales; no son alertas Waze ni se comparten en vivo. Para tráfico y alertas comunitarias hace falta un backend/feed y conectividad.

## Navegación
- Búsqueda por proveedor Geocoder cuando está disponible.
- Búsqueda offline por coordenadas `latitud,longitud`.
- GPS de origen.
- Cálculo de ruta offline Valhalla cuando está instalado el grafo.
- Dibujo de ruta y resumen de distancia/tiempo/instrucción inicial.

La guía giro a giro continua con seguimiento GPS y voz aún requiere completar y validar el módulo de navegación activa.

## Vehículo y datos CANBUS / OBD-II
La vista Vehículo presenta los campos de nivel de combustible, volumen estimado, autonomía, consumo instantáneo/promedio (km/L y L/100 km), viajes A/B, tiempo, distancia, velocidad promedio/máxima, RPM, temperatura de refrigerante, carga, acelerador, voltaje y estados de puertas/luces. Los valores se muestran como no disponibles hasta tener un decodificador CANBUS/OBD compatible y confirmar los PIDs/señales que entrega la radio. No se inventan lecturas. El volumen en litros y la autonomía requieren calibrar la capacidad del tanque y validar las señales de nivel/consumo.

La silueta roja de tres vistas se basa en la lámina del Polo proporcionada para el diseño. En Teléfono se habilitan accesos al marcador, historial, contactos y favoritos mediante las aplicaciones nativas de Android.

## Sonido
- EQ visual de 10 bandas, -6 a +6 dB.
- Perfiles OEM equilibrado, Pop, Rock, Voz y Personalizado.
- Diagnóstico de efectos Android, códecs y salidas.
- No procesa globalmente radio/CPLink: hace falta acceso al DSP o servicio privilegiado del fabricante.

## Compatibilidad y protección
- Android 10 del WV232 está dentro del rango soportado.
- minSdk 24, targetSdk 32.
- CPLink original no se modifica.
- No se modifica firmware, MCU, Factory Settings ni CANBUS.
- Cámara de reversa conserva su conmutación original.
- CANBUS de solo lectura pendiente.

## Builds
- APK base automática: `VAG-OEM-WV232-0.11.0-BASE-debug-APK` (sin grafo precargado).
- APK con grafo regional: `VAG-OEM-WV232-0.11.0-OFFLINE-APK` (flujo de construcción de datos).
- JDK 21 (CI) · Gradle 8.11.1 · Android SDK 36.

## Validación
Código y workflows guardados en GitHub. La compilación final del APK con grafo regional y pruebas en hardware WV232 siguen pendientes de confirmación. No se declara éxito de build hasta que Actions lo reporte.
