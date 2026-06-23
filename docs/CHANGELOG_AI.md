# CHANGELOG_AI

Fecha: 2026-06-23
Tarea: Release KitsuX 1.0.4 con changelog bilingue localizado
Cambios:
- Bump de version estable a `versionName = 1.0.4` y `versionCode = 5`.
- Se agrego selector de release notes por idioma: `## es` para usuarios con app en espanol y `## en` para cualquier otro idioma.
- La pantalla de nueva version y el dialogo post-update reutilizan el mismo selector de changelog.
Archivos:
- [app/build.gradle.kts](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/build.gradle.kts)
- [ReleaseNotes.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/data/updater/ReleaseNotes.kt)
- [MainActivity.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/ui/main/MainActivity.kt)
- [NewUpdateScreen.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/ui/more/NewUpdateScreen.kt)
Validacion:
- `./gradlew :app:compileDebugKotlin` ejecutado correctamente.
- `./gradlew :app:assembleRelease` ejecutado correctamente.
Notas:
- Las notas de GitHub Release deben mantener secciones markdown `## es` y `## en`.

---

Fecha: 2026-06-23
Tarea: Adaptar sistema de actualizaciones Android por GitHub Releases para KitsuX
Cambios:
- El updater existente ahora usa el `name` del GitHub Release como versión limpia de la app, con fallback al `tag_name` sin prefijo `v`.
- La selección del APK de actualización ahora recorre todas las ABIs soportadas por el dispositivo.
- Se agregó fallback a APK `universal` y, como último recurso, al primer asset `.apk` disponible.
Archivos:
- [GithubRelease.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/data/src/main/java/tachiyomi/data/release/GithubRelease.kt)
- [ReleaseServiceImpl.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/data/src/main/java/tachiyomi/data/release/ReleaseServiceImpl.kt)
Validación:
- `./gradlew :data:compileDebugKotlin` ejecutado correctamente.
Notas:
- No se agregaron dependencias ni nueva UI. Se reutiliza el updater heredado de Aniyomi/Tachiyomi ya integrado con WorkManager, notificaciones y `FileProvider`.

---

Fecha: 2026-06-20
Tarea: KitsuX MVP Fase 1: Build & Rebrand
Cambios:
- Configuración inicial y fork de Aniyomi verificado.
- Rebranding del ID de aplicación (applicationId) a "io.kitsux.app" en app/build.gradle.kts.
- Rebranding del nombre de aplicación a "KitsuX" en strings.xml base y traducciones al español.
- Aplicación de paleta de colores de marca premium dark (Netflix Red #E50914, Secondary #141414, Accent #FF4D4D, Background #000000, Surface #111111) en el TachiyomiColorScheme por defecto.
Archivos:
- [app/build.gradle.kts](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/build.gradle.kts)
- [strings.xml (base)](file:///Users/richtunic/Documents/Proyectos/KitsuX/i18n/src/commonMain/moko-resources/base/strings.xml)
- [strings.xml (es)](file:///Users/richtunic/Documents/Proyectos/KitsuX/i18n-aniyomi/src/commonMain/moko-resources/es/strings.xml)
- [TachiyomiColorScheme.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/presentation/theme/colorscheme/TachiyomiColorScheme.kt)
Validación:
- Compilación e inicialización exitosa de Gradle en entorno local.
- Build de verificación de APK rebranded ejecutado con éxito.
Notas:
- Se preserva el namespace de código ("eu.kanade.tachiyomi") intacto para compatibilidad total con extensiones existentes de Aniyomi.

---

Fecha: 2026-06-20
Tarea: KitsuX MVP Fase 2: Design System & Theme Engine
Cambios:
- Se forzó el modo oscuro por defecto (ThemeMode.DARK) y el tema de la marca KitsuX (AppTheme.DEFAULT) como predeterminados en UiPreferences, independientemente del estado de Monet (colores dinámicos) en el dispositivo.
- Descarga e integración de la fuente tipográfica premium "Outfit" desde Google Fonts en el directorio de recursos de `presentation-core` (`presentation-core/src/main/res/font/outfit.ttf`).
- Definición de `kitsuXTypography` asignando la tipografía Outfit a todos los estilos de texto de Material 3 en `Typography.kt`.
- Integración global de la tipografía de marca en la base de `TachiyomiTheme.kt`.
Archivos:
- [UiPreferences.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/domain/ui/UiPreferences.kt)
- [outfit.ttf](file:///Users/richtunic/Documents/Proyectos/KitsuX/presentation-core/src/main/res/font/outfit.ttf)
- [Typography.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/presentation-core/src/main/java/tachiyomi/presentation/core/theme/Typography.kt)
- [TachiyomiTheme.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/presentation/theme/TachiyomiTheme.kt)
Validación:
- Compilación incremental con Gradle completada con éxito (16 segundos).
Notas:
- El uso de la fuente variable Outfit garantiza una carga tipográfica eficiente con soporte nativo de múltiples grosores (Normal, Medium, SemiBold, Bold).

---

Fecha: 2026-06-20
Tarea: KitsuX MVP Fase 3: New Navigation
Cambios:
- Añadida la dependencia de `navigation-compose` en el catálogo de versiones y su implementación en el build de la aplicación.
- Rediseño de la navegación principal del usuario en `HomeScreen.kt` utilizando `NavHost` y `NavController` de Compose Navigation.
- Creación de las 5 pestañas de destino en `KitsuXDestination`: Home, Explore, Library, Downloads, y Profile.
- Implementación de la vista consolidada de la biblioteca `KitsuXLibraryTabScreen` combinando los apartados de Anime y Manga mediante una barra de pestañas en un único destino.
- Mapeado y redirección de los eventos globales de redirección de pestañas a Compose Navigation para conservar la funcionalidad general de la app.
Archivos:
- [androidx.versions.toml](file:///Users/richtunic/Documents/Proyectos/KitsuX/gradle/androidx.versions.toml)
- [app/build.gradle.kts](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/build.gradle.kts)
- [HomeScreen.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/ui/home/HomeScreen.kt)
Validación:
- Compilación incremental completa con Gradle ejecutada correctamente (1 minuto 51 segundos).
- Comprobación exitosa de las firmas y enlazados de dependencias.
Notas:
- La arquitectura híbrida elegida mantiene la compatibilidad de Voyager internamente para evitar reescribir todos los screen models y preservar las integraciones existentes.
