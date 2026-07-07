# CHANGELOG_AI

Fecha: 2026-07-07
Tarea: Preparar release KitsuX 1.0.5 y corregir enlaces del README
Cambios:
- Se corrigieron los enlaces de descarga del README para apuntar a assets reales `Kitsu-X-v1.0.5-*.apk`, evitando los 404 provocados por nombres `app-*-release.apk`.
- Se actualizo el script generador de bloques de descarga para mantener el mismo formato de assets en futuras releases.
- Se compilaron APKs release firmados para universal, arm64-v8a, armeabi-v7a, x86 y x86_64.
- Se agregaron notas de release bilingues para GitHub con checksums SHA-256.
Archivos:
- [README.md](file:///Users/richtunic/Documents/Proyectos/KitsuX/README.md)
- [update_readme_downloads.py](file:///Users/richtunic/Documents/Proyectos/KitsuX/.github/scripts/update_readme_downloads.py)
- [release-notes-v1.0.5.md](file:///Users/richtunic/Documents/Proyectos/KitsuX/docs/release-notes-v1.0.5.md)
Validacion:
- `./gradlew :app:assembleRelease -Penable-updater` ejecutado correctamente.

---

Fecha: 2026-07-07
Tarea: Corregir confianza persistente de extensiones
Cambios:
- La validacion de extensiones confiables en anime y manga ahora compara la entrada guardada contra cualquier huella SHA-256 actual del APK, no solo contra la ultima huella de la lista.
- Esto evita que una extension marcada como confiable vuelva a aparecer como no confiable cuando Android devuelve historial o multiples firmantes en distinto orden.
Archivos:
- [TrustAnimeExtension.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/domain/extension/anime/interactor/TrustAnimeExtension.kt)
- [TrustMangaExtension.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/domain/extension/manga/interactor/TrustMangaExtension.kt)
Validacion:
- `./gradlew :app:compileDebugKotlin` ejecutado correctamente.

---

Fecha: 2026-07-07
Tarea: Compatibilidad manga con repos actuales y bypass Cloudflare alineado con Mihon
Cambios:
- Se clono Mihon en `/private/tmp/mihon` para comparar el flujo real sin sobrescribir KitsuX.
- Se corrigio `CloudflareInterceptor` para detectar errores HTTP del WebView con `onReceivedHttpError`, conservar la cookie `cf_clearance` anterior y desbloquear cuando no aparece challenge, siguiendo el comportamiento de Mihon.
- Se agrego `FlexibleLongSerializer` para aceptar IDs de fuentes de extensiones como numero o string en indices legacy actuales.
- Se aplico el parser flexible tanto a extensiones de Manga como de Anime, preservando compatibilidad con Aniyomi.
- Se hizo mas robusto el update manual de extensiones: si el paquete instalado no existe en el mapa disponible local, se refresca el repo antes de fallar y se emite estado `Error` en vez de completar silenciosamente.
- Se agrego timeout de 2 minutos a la migracion de Anime y Manga para evitar que una fuente colgada deje el overlay de carga indefinidamente.
- Se optimizo el tap de `Continuar viendo/leyendo`: las cards ahora guardan el episodio/capitulo objetivo y el handler evita taps duplicados mientras abre el player/lector.
- Se separaron las capas de Home: `Continuar viendo` queda solo para Anime y `Continuar leyendo` solo para Manga; ambas se ocultan cuando no hay contenido con historial/progreso real.
- Se normalizo la entrada a fuentes: al abrir una fuente se muestra `Latest` por defecto cuando la extension lo soporta, con fallback a `Popular`.
Archivos:
- [CloudflareInterceptor.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/core/common/src/main/java/eu/kanade/tachiyomi/network/interceptor/CloudflareInterceptor.kt)
- [NetworkHelper.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/core/common/src/main/java/eu/kanade/tachiyomi/network/NetworkHelper.kt)
- [FlexibleLongSerializer.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/extension/api/FlexibleLongSerializer.kt)
- [MangaExtensionApi.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/extension/manga/api/MangaExtensionApi.kt)
- [AnimeExtensionApi.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/extension/anime/api/AnimeExtensionApi.kt)
- [MangaExtensionManager.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/extension/manga/MangaExtensionManager.kt)
- [AnimeExtensionManager.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/extension/anime/AnimeExtensionManager.kt)
- [MigrateMangaDialog.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/ui/browse/manga/migration/search/MigrateMangaDialog.kt)
- [MigrateAnimeDialog.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/ui/browse/anime/migration/search/MigrateAnimeDialog.kt)
- [KitsuXHomeScreenModel.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/ui/home/KitsuXHomeScreenModel.kt)
- [HomeScreenContent.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/presentation/home/HomeScreenContent.kt)
- [MangaSourcesScreen.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/presentation/browse/manga/MangaSourcesScreen.kt)
- [AnimeSourcesScreen.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/presentation/browse/anime/AnimeSourcesScreen.kt)
- [BrowseMangaSourceScreenModel.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/ui/browse/manga/source/browse/BrowseMangaSourceScreenModel.kt)
- [BrowseAnimeSourceScreenModel.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/ui/browse/anime/source/browse/BrowseAnimeSourceScreenModel.kt)
Validacion:
- `./gradlew :core:common:compileDebugKotlin :app:compileDebugKotlin` ejecutado correctamente.
- `./gradlew :app:compileDebugKotlin` ejecutado correctamente tras los fixes de updates/migracion.
- `./gradlew :app:compileDebugKotlin` ejecutado correctamente tras optimizar el tap de continuar.
Notas:
- No se porto todo Mihon porque KitsuX depende de Aniyomi y un reemplazo completo del motor de fuentes/extensiones seria alto riesgo. El fix se limito a diferencias concretas que afectan repos y Cloudflare.
- En Olympus, los logs mostraron `HTTP 525` al pedir capitulos en `dashboard.olympusxyz.com`; se confirmo que era una extension desactualizada y se dejo que futuras correcciones dependan de updates de la extension.

---

Fecha: 2026-07-05
Tarea: Release KitsuX 1.0.5 con continuar leyendo separado y fixes de extensiones
Cambios:
- Bump de versión estable a `versionName = 1.0.5` y `versionCode = 6`.
- Separación de "Continuar viendo" (Anime) y "Continuar leyendo" (Manga) en dos filas distintas en la pantalla de inicio con reglas de filtrado específicas para manga.
- Fix de deserialización (MissingFieldException) en metadatos de repositorios de extensiones (como Keiyoushi's repo.json) haciendo `shortName` y `sources` opcionales.
- Fix en la generación de User-Agent por defecto para la opción de Brave, adaptándolo a un formato móvil de Android que coincide con la huella TLS (TLS fingerprint) del dispositivo, evitando errores HTTP 500 y 525 de Cloudflare.
Archivos:
- [app/build.gradle.kts](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/build.gradle.kts)
- [ExtensionRepoDto.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/domain/src/main/java/mihon/domain/extensionrepo/service/ExtensionRepoDto.kt)
- [MangaExtensionApi.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/extension/manga/api/MangaExtensionApi.kt)
- [AnimeExtensionApi.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/extension/anime/api/AnimeExtensionApi.kt)
- [NetworkHelper.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/core/common/src/main/java/eu/kanade/tachiyomi/network/NetworkHelper.kt)
- [KitsuXHomeScreenModel.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/ui/home/KitsuXHomeScreenModel.kt)
- [HomeScreenContent.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/presentation/home/HomeScreenContent.kt)
- [base strings.xml](file:///Users/richtunic/Documents/Proyectos/KitsuX/i18n/src/commonMain/moko-resources/base/strings.xml)
- [es strings.xml](file:///Users/richtunic/Documents/Proyectos/KitsuX/i18n/src/commonMain/moko-resources/es/strings.xml)
Validacion:
- `./gradlew compileDebugKotlin` completado exitosamente.

---

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
