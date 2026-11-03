# Bienvenido · Volkswagen Polo 2020 · WV232

Launcher Android horizontal inspirado en Volkswagen Discover Media, con tema grafito y acentos rojo Volkswagen.

## Funciones
- Inicio con emblema VW, POLO 2020, BIENVENIDO, reloj y conectividad.
- Accesos a Radio, Media, Teléfono, App-Connect, Navegación, Vehículo, Ajustes y Sonido.
- App-Connect busca CPLink por nombre/paquete y permite seleccionarlo una vez si no lo detecta; guarda el vínculo en preferencias.
- Navegación admite Organic Maps (`app.organicmaps`) o un navegador elegido.
- Paneles de vehículo y viaje no inventan datos CANBUS: la integración de lectura queda pendiente de identificar el proveedor.
- No intercepta la entrada RCA ni la señal original de cámara de reversa.

## Compilación en GitHub Actions
El workflow en `.github/workflows/build-apk.yml` compila un APK debug con JDK 17, Android SDK 35 y Gradle 8.9.
En la pestaña Actions selecciona **Build VAG OEM WV232 APK** y pulsa **Run workflow**. El artefacto `VAG-OEM-WV232-0.3.1-debug-APK` contendrá el APK.

## Requisitos y límites
- Android minSdk 23, targetSdk 32.
- No incluye modificaciones de firmware, MCU, Factory Settings ni escritura CAN.
- La compilación debe probarse en el estéreo físico WV232; chipset, API Android, ABI y resolución reales aún no se han verificado.
- APK debug para pruebas; aún no es un paquete de producción firmado con clave estable.

## Estructura
- `app/`: aplicación Android Java y recursos.
- `preview/`: prototipo visual HTML y capturas.
- `.github/workflows/build-apk.yml`: compilación automática.
