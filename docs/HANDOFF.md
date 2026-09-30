# HANDOFF

## Gran actualización 2026-09-29: fase técnica 0

El PRD `Kitsu_X_PRD_Gran_Actualizacion.md` se inició en el worktree aislado `codex/gran-actualizacion` desde `b829a5838`. Se completó el inventario y el plan en `docs/GRAN_ACTUALIZACION.md`. El checkout principal tiene modificaciones locales previas y no se tocó. No hay cambios de app ni DB en esta fase.

Siguiente acción: implementar PR 1 / Foundations en esta rama, empezando por acceso seguro a Historial y Actualizaciones desde Más, luego cinco destinos y adaptación de barra/rail al ancho actual de ventana. Compilar y probar móvil/tablet antes de avanzar a PR 2. La validación en dispositivo sigue pendiente.

## Nota técnica: bandeja de novedades y actualización de extensiones en lote
Home deriva `newReleaseGroups` del estado local de anime y manga. Los límites se calculan con el inicio del día en la zona horaria del dispositivo: `Hoy`, `Ayer` y los seis días recientes restantes como `Esta semana`. Los grupos vacíos no se dibujan y la bandeja completa se oculta cuando no hay elementos válidos. Cada tarjeta conserva `onContinueClick`, que abre directamente el siguiente episodio o capítulo pendiente.

Las pantallas de extensiones de anime y manga solicitan confirmación antes de `Actualizar todas`. Los ScreenModels toman una instantánea de todas las extensiones instaladas con `hasUpdate`, sin depender del filtro visible, las ordenan por nombre y esperan a que cada flujo termine antes de iniciar el siguiente. Un `AtomicBoolean` impide lotes simultáneos y `isUpdatingAll` deshabilita el botón. Cada actualización pasa por `extensionManager.updateExtension()`: Package Installer conserva el consentimiento y la validación de certificado de Android, y el instalador privado compara versión y firmas con la extensión instalada.

## Nota técnica: actualización automática persistente de biblioteca
Las bibliotecas de anime y manga usan trabajos periódicos únicos de WorkManager (`AnimeLibraryUpdate-auto` y `LibraryUpdate-auto`). El intervalo predeterminado es de 12 horas, con red Wi-Fi y batería no baja como restricciones iniciales. En Ajustes > Biblioteca se puede elegir `Nunca`, 6, 8, 12, 24, 48, 72 horas o semanal. Los trabajos quedan persistidos por WorkManager, sobreviven cierres de la app y reinicios del dispositivo, y se reprograman cuando el usuario cambia el intervalo o las restricciones. Abrir Home no inicia una actualización automática, mientras que el gesto de refrescar conserva la ejecución manual.

Al finalizar, cada worker publica su resumen de anime o manga con el total de episodios/capitulos nuevos. La notificacion expandida muestra en este orden: obras con contenido nuevo y su cantidad, obras omitidas por las restricciones de actualizacion y obras con error. Si hay errores, `Mostrar errores` abre el registro detallado desde el mismo resumen. Las notificaciones individuales de cada obra con contenido nuevo se conservan; el resumen solo se agrupa cuando esas notificaciones hijas existen para que los resultados con cero novedades sigan visibles. `hideNotificationContent` oculta los titulos y deja solo los contadores.

## Nota técnica: repositorios de extensiones unificados
La UI muestra un solo apartado de repositorios de extensiones. Al añadir una URL, KitsuX la registra en los motores internos de anime y manga, conserva sus bases separadas por compatibilidad y refresca ambos catálogos. Al abrir el gestor también sincroniza repositorios antiguos que solo existan en uno de los dos motores, permitiendo que las extensiones de manga aparezcan para instalar sin volver a añadir la URL.

Desde las pantallas de extensiones, el gestor se presenta en el menú overflow como `Listados de extensiones`. El botón `Añadir listado de extensiones` solo aparece cuando no hay ningún repositorio configurado; el menú permanece disponible para administrar o agregar más. Un listado solo aporta las extensiones compatibles que realmente publique: por ejemplo, Yūzōnō es de anime y no llena el catálogo de manga.

El motor de manga sigue el campo `index_v2` de `repo.json`, descomprime GZIP cuando el `index.pb` lo requiere y decodifica el catálogo Protobuf, con fallback al `index.min.json` legado. Esto es necesario para Keiyoushi: su índice JSON antiguo solo contiene los avisos `Outdated App` y `Update to Mihon 0.20.1+`, mientras el catálogo vigente está en `https://github.com/keiyoushi/extensions/raw/repo/index.pb`. Las URLs absolutas de APK e icono publicadas por el índice v2 se usan directamente. El cargador acepta extension-lib hasta `1.6`, lee los metadatos `tachiyomix.*` publicados por las extensiones modernas y normaliza su valor `Float` antes de compararlo para conservar extensiones compartidas 1.4 instaladas desde Mihon u otras apps compatibles.

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
