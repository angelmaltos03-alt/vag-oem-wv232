# Volkswagen Polo 2020 · WV232

Launcher Android de estilo OEM inspirado en Volkswagen Discover Media / MIB2.

## Versión 0.4.0 — Audio Lab y almacenamiento

### Audio Lab
- Prueba de entrada mediante AudioRecord con fuente MIC predeterminada de Android.
- Medidor de nivel RMS aproximado y pico.
- Detección de disponibilidad de AGC, AEC y NoiseSuppressor para sesiones compatibles.
- La captura es temporal: no se guarda ni transmite audio.
- Detiene la captura al salir.
- Solicita permiso de micrófono en tiempo de ejecución.

**Limitación:** no puede elegir ni identificar con certeza la cápsula interna o externa si la MCU presenta ambas como una sola entrada. Tampoco cambia la ganancia de llamadas Bluetooth o CPLink. Las llamadas pueden usar una ruta gestionada por MCU/sistema y distinta de AudioRecord. La disponibilidad de efectos Android no demuestra que se apliquen a llamadas.

### Almacenamiento
- Muestra espacio disponible en el volumen privado de la aplicación.
- Política: no descargar paquetes de mapas con menos de 2 GB libres.
- No se incluyen mapas ni dependencias de navegación en el APK 0.4.0.

### Conservación del hardware original
- CPLink no se modifica ni sustituye.
- No se modifica firmware, MCU, Factory Settings ni parámetros CANBUS.
- La cámara conserva la conmutación original.
- CANBUS permanece como integración de solo lectura pendiente de identificar.

## Instalación y prueba
1. Instala el APK debug desde el artefacto de GitHub Actions.
2. Abre Sonido desde el menú VAG OEM.
3. Concede permiso de micrófono para ejecutar la prueba.
4. Habla a volumen normal y observa nivel RMS/pico.
5. Para comparar micrófonos por separado, el equipo debe permitir seleccionar físicamente cada entrada; la app no fuerza esa selección.
6. Prueba llamadas Bluetooth y CPLink por separado. Audio Lab no debe grabar durante las llamadas.

## Compilar APK
Abre Actions, selecciona Build VAG OEM WV232 APK y ejecuta Run workflow. Descarga el artefacto VAG-OEM-WV232-0.4.0-debug-APK.

Herramientas: JDK 17, Gradle 8.9, Android Gradle Plugin 8.7.3, SDK 35. minSdk 23, targetSdk 32.

## Estado
- Código Audio Lab incorporado; compilación y validación en hardware pendientes.
- No es firmware ni reemplaza aplicaciones originales.
- Chipset, ABI, resolución y densidad de cada variante WV232 deben verificarse.
- Integración directa CANBUS y navegación nativa offline siguen pendientes.
