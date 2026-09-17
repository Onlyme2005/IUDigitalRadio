# IU Digital Radio — guía de implementación y entrega

## 1. Alcance

Este proyecto resuelve la **Evidencia de aprendizaje 3 – Taller práctico** en modalidad individual. La aplicación usa Jetpack Compose para toda la interfaz visible, gestiona estado con `rememberSaveable`, solicita el permiso de cámara en tiempo de ejecución, captura una fotografía con `ActivityResultContracts.TakePicturePreview`, genera vibración corta en los controles, consulta emisoras reales desde Radio Browser y reproduce sus streams con Media3 ExoPlayer.

La API de Radio Browser es pública y no requiere una clave. La disponibilidad de una emisora depende del servidor de terceros; por ello, la app filtra estaciones marcadas como no rotas y muestra un mensaje si temporalmente no hay conectividad.

## 2. Requisitos previos

Instala Android Studio Ladybug o una versión posterior, Android SDK Platform 35, Android SDK Build-Tools, un JDK 17 y un emulador con cámara virtual o un teléfono Android físico. Para probar audio se requiere conexión a Internet y el volumen multimedia habilitado.

## 3. Crear o abrir el proyecto

1. Descomprime o copia la carpeta `IUDigitalRadio` en una ubicación local.

1. En Android Studio selecciona **File > Open** y elige la carpeta del proyecto.

1. Espera la sincronización de Gradle. Si Android Studio solicita instalar SDK Platform 35, acepta.

1. Comprueba que el módulo seleccionado sea `app` y que el dispositivo de ejecución tenga Android 7.0/API 24 o superior.

1. Si Android Studio crea archivos de configuración diferentes, conserva los archivos del proyecto entregado, especialmente `app/build.gradle.kts`, `AndroidManifest.xml` y `MainActivity.kt`.

## 4. Estructura relevante

| Archivo | Responsabilidad |
| --- | --- |
| `app/src/main/java/com/iudigital/radio/MainActivity.kt` | Actividad, UI Compose, estados, cámara, vibración, consulta de emisoras y ExoPlayer |
| `app/src/main/AndroidManifest.xml` | Permisos de Internet, cámara y vibración |
| `app/build.gradle.kts` | Compose, Media3 ExoPlayer, SDK y configuración Kotlin |
| `app/src/main/res/values/themes.xml` | Tema mínimo requerido por la actividad |

No se usa un XML de layout: la interfaz se construye con `Column`, `Row`, `Card`, `LazyColumn`, `Modifier` y componentes Material 3. El XML existente solo define recursos que Android exige para el tema y el nombre de la aplicación.

## 5. Ejecutar la aplicación

1. Conecta el teléfono con **Depuración USB** habilitada o inicia un AVD.

1. Presiona el botón **Run ▶**.

1. En el primer inicio concede el permiso de cámara cuando se solicite.

1. Presiona **Foto**, toma una imagen y verifica que aparezca en el círculo del perfil.

1. Espera la carga de “Emisoras destacadas”. Selecciona una estación y presiona el botón central de reproducción.

1. Comprueba que el texto cambie entre “Listo para reproducir” y “Reproduciendo en vivo”.

1. Presiona el icono de volumen para silenciar/restaurar el audio. Cada control produce una vibración háptica corta si el dispositivo la soporta.

1. Gira el dispositivo. La emisora seleccionada y los estados de reproducción/silencio se preservan mediante `rememberSaveable`.

## 6. Generar el APK

En Android Studio selecciona **Build > Build Bundle(s) / APK(s) > Build APK(s)**. Al finalizar, usa el enlace **locate** de la notificación o busca el archivo en:

```
app/build/outputs/apk/debug/app-debug.apk
```

Para un APK de distribución firmado selecciona **Build > Generate Signed App Bundle or APK**, crea o elige un keystore y guarda las credenciales en un lugar seguro. Para la evidencia académica normalmente basta el APK de depuración, salvo que el docente indique lo contrario.

## 7. Prueba funcional sugerida para el video de 2–4 minutos

1. Inicia la app desde cero y presenta la cabecera.

1. Presiona **Foto**, muestra el diálogo de permiso y acepta.

1. Toma la fotografía y regresa a la aplicación.

1. Explica que la lista proviene de una API pública de estaciones reales.

1. Selecciona una emisora, presiona Play y muestra el cambio de estado y el audio.

1. Presiona Pause y Mute, mencionando la respuesta háptica.

1. Selecciona otra emisora y muestra el cambio de la tarjeta principal.

1. Finaliza enseñando el APK generado en `app/build/outputs/apk/debug/`.

## 8. Lista de chequeo

- [ ] Modalidad individual indicada en el documento técnico.

- [ ] Proyecto creado/abierto correctamente en Android Studio.

- [ ] Interfaz implementada únicamente con Jetpack Compose.

- [ ] Se observan `Column`, `Row`, `Card`, `LazyColumn` y `Modifier` en el código.

- [ ] `CAMERA`, `VIBRATE` e `INTERNET` declarados en el manifiesto.

- [ ] Permiso de cámara solicitado en tiempo de ejecución.

- [ ] Captura realizada con `TakePicturePreview`.

- [ ] Fotografía mostrada en el perfil.

- [ ] `isPlaying`, `isMuted` y `selectedStationId` gestionados con estado Compose.

- [ ] Estados principales preservados con `rememberSaveable`.

- [ ] Vibración ejecutada en Play/Pause, Mute y selección de estación.

- [ ] Lista dinámica cargada desde Radio Browser.

- [ ] Emisoras seleccionables en `LazyColumn`.

- [ ] Audio reproducido mediante Media3 ExoPlayer.

- [ ] APK compilado y probado en un dispositivo/emulador.

- [ ] Capturas de UI, permiso, cámara y APK guardadas para el PDF.

- [ ] Repositorio público GitHub/GitLab enlazado en el PDF.

- [ ] Video de 2–4 minutos subido a Google Drive y enlace incluido en el PDF.

## 9. Problemas frecuentes

**La lista aparece vacía.** Comprueba Internet y pulsa el botón de actualización de la barra superior. Radio Browser es un servicio comunitario y puede presentar interrupciones.

**No aparece el permiso de cámara.** Desinstala la app del emulador/teléfono y vuelve a instalarla; Android recuerda decisiones anteriores. También verifica que el dispositivo tenga una aplicación de cámara disponible.

**La emisora no reproduce.** Algunas estaciones pueden apagarse, rechazar conexiones o entregar un códec no compatible. Selecciona otra estación; la fuente es externa y cambia con el tiempo.

**No se siente vibración.** Verifica que la vibración del dispositivo esté habilitada. `VIBRATE` es un permiso normal de Android y no muestra un diálogo de runtime como `CAMERA`; queda declarado en el manifiesto.

## 10. Fuentes técnicas

- [Radio Browser API](https://docs.radio-browser.info/): endpoint de búsqueda, campos de estación y recomendaciones de User-Agent.

- [Guía oficial de Media3 ExoPlayer](https://developer.android.com/media/implement/playback-app): creación, preparación, control y liberación del reproductor.

- [TakePicturePreview](https://developer.android.com/reference/androidx/activity/result/contract/ActivityResultContracts.TakePicturePreview): contrato que devuelve un `Bitmap` de la fotografía capturada.

- [Permisos de Android](https://developer.android.com/training/permissions/requesting): solicitud de permisos en tiempo de ejecución.

