# IU Digital Radio

Aplicación móvil nativa para Android desarrollada como solución a la **Evidencia de Aprendizaje 3** de la asignatura **Programación de Dispositivos Móviles**.

IU Digital Radio permite consultar emisoras reales, seleccionar una estación y reproducir su transmisión en vivo mediante Internet. También integra cámara, permisos de Android, vibración háptica y una interfaz moderna desarrollada completamente con Jetpack Compose.

## Características principales

- Interfaz declarativa desarrollada con Jetpack Compose.
- Diseño moderno basado en Material 3.
- Perfil de usuario con captura de fotografía mediante la cámara del dispositivo.
- Solicitud del permiso `CAMERA` en tiempo de ejecución.
- Lista dinámica de emisoras mediante `LazyColumn`.
- Consulta de emisoras reales mediante Radio Browser API.
- Reproducción de transmisiones en vivo con Media3 ExoPlayer.
- Controles de reproducción, pausa y silencio.
- Vibración háptica al presionar los controles principales.
- Estado dinámico para la emisora seleccionada, reproducción y silencio.
- Conservación de estados principales mediante `rememberSaveable`.
- Catálogo alternativo de respaldo si la API no está disponible.
- Compatibilidad con teléfonos físicos y emuladores Android.

## Tecnologías utilizadas

- Kotlin
- Android Studio
- Jetpack Compose
- Material 3
- Media3 ExoPlayer
- Radio Browser API
- Android Activity Result API
- Gradle Kotlin DSL
- Android SDK 35
- Java 17

## Requisitos

Para ejecutar el proyecto se necesita:

- Android Studio.
- Android SDK Platform 35.
- JDK 17.
- Un teléfono Android o un emulador.
- Conexión a Internet para cargar y reproducir las emisoras.
- Depuración USB o depuración inalámbrica si se utiliza un teléfono físico.

## Permisos utilizados

El proyecto declara los siguientes permisos en `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.VIBRATE" />
```

El permiso de cámara se solicita al usuario en tiempo de ejecución antes de iniciar la captura fotográfica.

## Estructura del proyecto

```text
IUDigitalRadio/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── AndroidManifest.xml
│   │       ├── java/com/iudigital/radio/
│   │       │   └── MainActivity.kt
│   │       └── res/
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
├── README.md
└── .gitignore
```

## Ejecución del proyecto

### 1. Clonar el repositorio

```bash
git clone https://github.com/Onlyme2005/IUDigitalRadio.git
```

### 2. Abrir el proyecto

Abre la carpeta clonada desde Android Studio y espera a que finalice la sincronización de Gradle.

### 3. Seleccionar un dispositivo

Selecciona un teléfono Android conectado mediante USB o Wi-Fi ADB, o inicia un emulador Android.

### 4. Ejecutar la aplicación

Desde Android Studio presiona:

```text
Run ▶
```

También puedes compilar la aplicación desde PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

## Generar el APK

Desde Android Studio selecciona:

```text
Build > Build Bundle(s) / APK(s) > Build APK(s)
```

El APK de depuración se genera en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

También puedes generarlo desde PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

## Uso de la aplicación

1. Abre la aplicación.
2. Presiona el botón **Foto**.
3. Acepta el permiso de cámara.
4. Toma una fotografía.
5. Verifica que la fotografía aparezca en el perfil.
6. Espera la carga del catálogo de emisoras.
7. Selecciona una estación.
8. Presiona **Play** para comenzar la transmisión.
9. Utiliza **Pause** para detener temporalmente el audio.
10. Utiliza **Mute** para silenciar o restaurar el volumen.
11. Selecciona otra emisora para cambiar la transmisión activa.

## Fuente de emisoras

Las emisoras se consultan mediante la API pública de Radio Browser:

[Radio Browser API](https://docs.radio-browser.info/)

Las emisoras son servicios externos. Su disponibilidad puede variar dependiendo del servidor, la conexión a Internet o el formato de transmisión.

## Solución de problemas

### No se cargan las emisoras

Verifica que el teléfono tenga conexión a Internet y vuelve a cargar la aplicación. Si la API no responde, la aplicación utiliza una emisora de respaldo configurada localmente.

### No se escucha el audio

Comprueba el volumen multimedia del teléfono, verifica que el botón **Mute** esté desactivado y prueba otra emisora. Algunas estaciones pueden estar temporalmente fuera de servicio o utilizar un formato no compatible.

### No aparece la cámara

Comprueba que el permiso de cámara haya sido aceptado en:

```text
Ajustes > Aplicaciones > IU Digital Radio > Permisos
```

### No aparece el teléfono en Android Studio

Verifica que la depuración USB o la depuración inalámbrica estén activadas y que el teléfono aparezca como `device` mediante ADB.

## Estado del proyecto

Proyecto funcional para demostración académica de los siguientes conceptos:

- Maquetación declarativa.
- Gestión de estado.
- Integración de cámara.
- Gestión de permisos.
- Vibración del dispositivo.
- Consumo de una API externa.
- Reproducción de streaming.
- Generación de un APK instalable.

## Autor

**Brislleily Carmona**

Proyecto académico individual para la asignatura de Programación de Dispositivos Móviles.

## Licencia

Proyecto desarrollado con fines académicos.
