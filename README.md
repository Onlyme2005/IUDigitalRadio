# IU Digital Radio

Aplicación Android nativa para la Evidencia de aprendizaje 3, desarrollada con Kotlin, Jetpack Compose, Media3 ExoPlayer, cámara nativa y vibración háptica.

## Funcionalidades

- Interfaz declarativa moderna en Compose Material 3.
- Perfil con captura usando `ActivityResultContracts.TakePicturePreview`.
- Permiso de cámara en runtime.
- Vibración en controles de reproducción y selección.
- Catálogo real de emisoras desde Radio Browser.
- Streaming con Media3 ExoPlayer.
- Estado de emisora, reproducción y silencio con `rememberSaveable`.

## Abrir

Abre esta carpeta desde Android Studio, instala SDK Platform 35 y ejecuta el módulo `app`. La guía completa está en [GUIA_ENTREGA.md](GUIA_ENTREGA.md). La plantilla del documento técnico está en [DOCUMENTO_TECNICO.md](DOCUMENTO_TECNICO.md) y su PDF en [DOCUMENTO_TECNICO.pdf](DOCUMENTO_TECNICO.pdf).

## APK

Tras ejecutar **Build > Build Bundle(s) / APK(s) > Build APK(s)**, el archivo se encontrará en `app/build/outputs/apk/debug/app-debug.apk`.
