# HANDOFF

## Nota técnica: updates Android KitsuX
El sistema de actualizaciones reutiliza el updater heredado de Aniyomi/Tachiyomi y consulta GitHub Releases según build type:
- Stable: `richtunic/Kitsu-X`
- Preview: `richtunic/Kitsu-X-preview`

Contrato de publicación:
- `tag_name`: usar prefijo `v`, por ejemplo `v1.0.5`.
- `name`: usar versión limpia igual a `versionName`, por ejemplo `1.0.5`.
- Assets: subir APKs con la ABI en el nombre (`arm64-v8a`, `armeabi-v7a`, `x86_64`, `x86`) y opcionalmente un APK `universal`.

La selección del APK prioriza la ABI de la app instalada inferida desde `nativeLibraryDir`, luego recorre únicamente ABIs publicados por KitsuX (`arm64-v8a`, `armeabi-v7a`, `x86_64`, `x86`) según `Build.SUPPORTED_ABIS`, cae a `universal` si no hay match y no descarga un APK arbitrario si no existe asset compatible. No se agregaron canales configurables por usuario en esta fase; si se retoman, hacerlo como fase separada con preferencias y UI en ajustes.

El chequeo automático de app updates corre cuando la app entra o vuelve a `RESUMED`, con un intervalo corto de 1 hora para que una nueva versión en GitHub aparezca sin que el usuario tenga que buscar manualmente. El rechazo de una versión (`No ahora`) se sigue respetando por 5 días para no molestar.

Release notes:
- Publicar siempre body bilingue con secciones `## es` y `## en`.
- La app muestra solo `## es` cuando el idioma configurado es espanol.
- Para cualquier otro idioma la app muestra `## en`.
- Si falta la seccion esperada, cae a `## en` y luego al body completo.

## Nota técnica: i18n KitsuX
Las adaptaciones de KitsuX deben usar recursos `MR.strings.*`/`AYMR.strings.*` y no textos hardcodeados en Compose o ScreenModels. Las claves nuevas se agregan en `i18n/src/commonMain/moko-resources/base/strings.xml`; español se mantiene en `i18n/src/commonMain/moko-resources/es/strings.xml`; el resto de idiomas heredan base hasta que sean traducidos por el flujo normal.

## Nota UX: banner hero opcional
El banner hero del Home depende de `UiPreferences.showHeroBanner()`. Se pregunta en onboarding y también se puede cambiar en Ajustes > Experiencia. Si está desactivado, el Home no renderiza `heroBannerItems`.

## Nota UX: autocategorización opcional
La autocategorización vía Jikan es opcional y se pregunta en onboarding con `UiPreferences.autoCategorizeLibrary()`. Si está desactivada, no se consulta Jikan ni se crean categorías automáticas; el usuario debe organizar manualmente.

## Nota técnica: autocategorización idempotente
La autocategorización vía Jikan debe ejecutarse también cuando la obra ya existe en la base local o ya estaba marcada como favorita y vuelve a pasar por una ruta de agregado/cambio de categoría. Jikan sigue siendo la fuente de verdad y se crean las categorías faltantes antes de asignarlas.

## Nota UX: Hero banner local vs recomendaciones
El hero debe resolver primero si la obra existe en la biblioteca local. Si existe, el click abre detalles o continúa reproducción/lectura cuando `isStarted` es verdadero. Solo debe abrir búsqueda global cuando el item siga siendo una recomendación externa.

## Nota UX: Home continuar vs novedades
`Continuar viendo`/leyendo debe mostrar solo obras comenzadas (`hasStarted` o historial real), no obras recién añadidas a seguimiento. Las obras en seguimiento con episodios/capítulos pendientes se muestran en un carrusel separado de novedades, con etiqueta temporal tipo `Hoy`, `Ayer` o `Hace N días`.

La pulsacion larga para eliminar aplica solo a `Continuar viendo` y `Continuar leyendo`. Debe ocultar la tarjeta puntual con preferencia local, sin borrar historial ni progreso. La clave incluye tipo, obra y episodio/capitulo objetivo para permitir que contenido nuevo vuelva a aparecer.

Las filas de continuar deben poder mostrar contenido con historial aunque no este en biblioteca. Para manga, resolver la obra local con `GetManga` y buscar el siguiente capitulo no leido cuando no existan contadores de biblioteca.


## Nota técnica: autocategorización de obras
Al añadir anime o manga a la biblioteca, la autocategorización debe resolver los géneros desde Jikan (`/v4/anime?q=...&limit=1` o `/v4/manga?q=...&limit=1`) y crear/asignar solo categorías whitelisted de Jikan. No usar `anime.genre`/`manga.genre` de la extensión para categorías de biblioteca, porque algunas fuentes agregan etiquetas contaminadas o no aplicables. Si Jikan falla o no devuelve géneros válidos, se conserva el flujo normal de categoría por defecto/sin categoría.


## Nota futura: respaldo/sincronización con Google Drive

La integración de Google Drive queda diferida. No hay conexión OAuth, Drive API ni sincronización bidireccional activa. Cuando se retome, debe implementarse por fases con OAuth de Google, scope mínimo para datos propios de la app, manejo de conflictos y restauración validada de backups.

## Nota futura: temporadas unificadas

El dropdown experimental de temporadas y el resolver de temporadas vía Jikan quedan retirados por ahora. Cuando se retome, debe hacerse por fases: primero solo navegación entre temporadas detectadas por la fuente, luego resolución externa opcional, y finalmente filtrado de episodios para fuentes que publican temporadas corridas en una sola página.


## Nota técnica: Cloudflare animeonline.ninja

Se ajustó la persistencia de cookies entre WebView y OkHttp para reducir falsos fallos de bypass en fuentes protegidas por Cloudflare como `animeonline.ninja`.

Hallazgo por logs en S23 Ultra:
- `cf_clearance` existe y OkHttp la envía.
- WebView/Chromium llega a cargar recursos reales de `ww3.animeonline.ninja`.
- OkHttp sigue recibiendo `403` con `cf-mitigated: challenge`, por lo que el bloqueo restante parece estar ligado al fingerprint/cliente HTTP y no solo a persistencia de cookies.
- Se agregó override de User-Agent solo para `animeonline.ninja`/`animeninja.online`: `Brave 1.62.152, Chromium 121.0.6167.101`.

Validación:
- `./gradlew :core:common:compileDebugKotlin :app:compileDebugKotlin`


## Última tarea
MVP Phase 3: New Navigation completada con éxito.

## Archivos modificados
- [app/build.gradle.kts](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/build.gradle.kts)
- [strings.xml (base)](file:///Users/richtunic/Documents/Proyectos/KitsuX/i18n/src/commonMain/moko-resources/base/strings.xml)
- [strings.xml (es)](file:///Users/richtunic/Documents/Proyectos/KitsuX/i18n-aniyomi/src/commonMain/moko-resources/es/strings.xml)
- [TachiyomiColorScheme.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/presentation/theme/colorscheme/TachiyomiColorScheme.kt)
- [UiPreferences.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/domain/ui/UiPreferences.kt)
- [outfit.ttf](file:///Users/richtunic/Documents/Proyectos/KitsuX/presentation-core/src/main/res/font/outfit.ttf)
- [Typography.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/presentation-core/src/main/java/tachiyomi/presentation/core/theme/Typography.kt)
- [TachiyomiTheme.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/presentation/theme/TachiyomiTheme.kt)
- [androidx.versions.toml](file:///Users/richtunic/Documents/Proyectos/KitsuX/gradle/androidx.versions.toml)
- [HomeScreen.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/ui/home/HomeScreen.kt)

## Estado actual
El proyecto compila correctamente. Se han integrado `NavHost` y `NavController` para la navegación de KitsuX con 5 secciones: Home, Explore, Library, Downloads, y Profile. Se mantiene la compatibilidad con todas las vistas antiguas mediante un enfoque híbrido en el que Voyager maneja el contenido interior de cada pestaña.

## Qué funciona
- Compilación e inicialización del entorno de desarrollo Gradle.
- Cambios de strings y colores integrados.
- Tipografía global variable de Outfit enlazada.
- Preferencias del tema predeterminadas a modo oscuro.
- Sistema de navegación por pestañas de Compose Navigation instalado y verificado.

## Qué falta
- MVP Phase 4: Home Screen (Netflix UI).
- MVP Phase 5: Anime Screen.

## Riesgos pendientes
- Ninguno detectado.

## Siguiente paso recomendado
Iniciar la Fase 4 del MVP: Home Screen para diseñar y estructurar la pantalla de inicio al estilo Netflix (Hero banner carrusel rotativo, synopsis, secciones de continuar viendo, tendencias, etc.).
