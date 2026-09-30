# Gran actualización: fase técnica 0

Fuente de producto: `Kitsu_X_PRD_Gran_Actualizacion.md` (29 de septiembre de 2026). El código de este checkout prevalece ante diferencias con el PRD.

## Estado y alcance

- Rama aislada: `codex/gran-actualizacion`, creada desde `b829a5838`. El checkout principal en `main` contiene trabajo local sin confirmar y no se modificó.
- Esta fase es inventario y decisión técnica. No cambia la app, las bases de datos ni las preferencias.
- Viabilidad: sí, por etapas. Riesgo alto en navegación heredada y migración de categorías; medio en Home y responsive.

## Inventario

| Área | Implementación actual | Consecuencia para el rediseño |
| --- | --- | --- |
| Navegación | `HomeScreen.kt` usa Voyager `TabNavigator`, barra inferior y rail Material 3. `NavStyle` elige qué destino antiguo mover a «Más»; Home se agrega aparte. | La lista actual puede superar los cinco destinos del PRD. Conservar Historial y Actualizaciones accesibles desde «Más» antes de fijar la nueva lista. Reutilizar Voyager y el `Scaffold` existente. |
| Responsive | `isTabletUi()` depende de `smallestScreenWidthDp` y del contexto adaptado por `tabletUiMode`. | El rail debe responder al ancho **actual** de la ventana (compacto `<600dp`, mediano `600–839dp`, expandido `>=840dp`), incluida pantalla dividida. Comprobar la interacción con la preferencia heredada. |
| Tema y componentes | `TachiyomiTheme.kt` y `BaseColorScheme` ya centralizan colores; existen temas y tipografía KitsuX. Hay cards, chips, carga y estados reutilizables en `presentation` y `presentation-core`. | Inventariar y adaptar antes de crear tokens o componentes paralelos. Preservar temas elegidos por usuarios hasta decidir una migración visual explícita. |
| Inicio | `KitsuXHomeTab`, `HomeScreenContent` y `KitsuXHomeScreenModel` son propios. El modelo combina biblioteca local, progreso y recomendaciones de `KitsuXIntelSystem`/Jikan. | Separar hero local de descubrimiento externo. No asumir que cada item del hero pertenece a la biblioteca. |
| Biblioteca | `AnimeLibraryTab`/`MangaLibraryTab`, pantallas y ScreenModels conservan filtros, categorías y acciones heredadas. | Cambiar presentación por etapas, conservando consultas y estado antes de sustituir grids. |
| Organización | `autoCategorizeLibrary()` existe y es opcional. `AnimeScreenModel` y `MangaScreenModel` consultan Jikan, normalizan géneros y crean/asignan categorías. | El nuevo modelo de categoría principal y etiquetas requiere una propuesta de migración y pruebas con datos reales; no pertenece a Foundations. |
| Datos | Anime: `data/src/main/sqldelightanime/dataanime/categories.sq`, `animes_categories.sq`, `animelibView.sq`. Manga: equivalentes en `sqldelight/data` y `libraryView.sq`. | Ambas relaciones obra-categoría admiten múltiples filas; las vistas de biblioteca hacen `LEFT JOIN` y exponen una fila por categoría. «Todo» debe deduplicar por ID de obra sin destruir pertenencias. No cambiar esquemas aún. |
| Upstream | Voyager, `Scaffold`, `BrowseTab`, `MoreTab`, bibliotecas, player, reader, fuentes y extensiones conservan estructura Aniyomi/Mihon. | Evitar reescrituras de motores y revisar consumidores antes de retirar una pantalla o ruta. |

## Secuencia de trabajo

1. **PR 1 / Foundations:** fijar navegación de Inicio, Anime, Manga, Explorar y Más; habilitar Historial y Actualizaciones desde Más; adaptar barra/rail al ancho de ventana; reutilizar tema y componentes. Validar compilación y navegación en móvil/tablet antes de seguir.
2. **PR 2:** card de progreso, grid y skeleton compartidos, con datos de prueba y sin cambiar consultas.
3. **PR 3:** Inicio local, hero solo de biblioteca y secciones de progreso/novedades; recomendaciones externas pasan a Explorar.
4. **PR 4–5:** bibliotecas, filtros, categorías y acciones contextuales, manteniendo estado de retorno.
5. **PR 6:** documentar primero el modelo y rollback de categorías/etiquetas; migrar solo después de pruebas de backups y datos de ejemplo.
6. **PR 7–11:** Explorar, fichas, herramientas, pulido de player/reader y QA.

## Validación pendiente antes de cerrar Foundations

- Build de `:app:compileDebugKotlin` y `:app:assembleDebug`.
- Navegación, Back, scroll y accesos secundarios en teléfono compacto, teléfono grande, tablet y pantalla dividida.
- Prueba en dispositivo de bibliotecas, reproducción, lectura y extensiones. Una compilación no demuestra estas rutas.

## Avance PR 1: navegación

- La lista principal se reduce a Inicio, Anime, Manga, Explorar y Más. Anime/Manga siguen respetando la preferencia existente de ocultarlos.
- Historial y Actualizaciones están en Más y las rutas programáticas a esas pantallas se abren sobre Más. El contador de novedades aparece en el icono de Más.
- Barra inferior si el ancho actual es menor que `600dp`; rail desde `600dp`. Se conserva el `Scaffold` y Voyager actuales. La preferencia antigua de estilo de navegación se oculta en Ajustes, pero su dato se conserva para compatibilidad.
- La etiqueta inglesa `Browse` pasa a `Explore`; la pantalla todavía conserva sus secciones actuales de fuentes y extensiones hasta PR 7.
- Se copió a esta rama la corrección ya validada en el checkout principal de FlexibleAdapter desde Maven Central, porque el commit base apuntaba a un AAR de JitPack inexistente.
- `:app:compileDebugKotlin --offline` y `:app:assembleDebug --offline` pasaron. `assembleDebug` emitió avisos D8 de reescritura de metadata Kotlin en clases heredadas; terminó con `BUILD SUCCESSFUL`.
- `adb devices` no mostró equipos. Falta verificar Back, scroll, accesos y ancho de ventana en dispositivos; por eso Foundations sigue abierta. También faltan tokens visuales, cards y skeletons del PRD, que deben adaptar los componentes existentes.

## Avance PR 1: componentes de Foundations

- `KitsuXLayoutTokens` centraliza los breakpoints compact/medium/expanded, el gutter, el radio de tarjeta y el ancho máximo del hero. Home usa el breakpoint compartido y limita el hero sin tocar consultas.
- `HomeLoadingSkeleton` sustituye el indicador aislado durante la carga de Inicio. Usa superficies del tema y deja un único texto de carga para accesibilidad.
- `MediaProgressCard` se usa en Continuar viendo y leyendo. Mantiene artwork, progreso y novedades, limita el progreso a `0..1`, muestra reproducción solo en anime y abre un menú contextual con continuar/quitar. Quitar conserva la confirmación previa.
- Se reutilizan `TachiyomiTheme`, `ItemCover`, los grids y estados compartidos ya presentes. No se añadieron dependencias ni cambios de DB.
- `:app:compileDebugKotlin --offline --quiet` y `:app:assembleDebug --offline --quiet` pasaron; el APK arm64 debug se generó. `adb devices -l` no encontró equipo y `emulator -list-avds` no mostró AVD. Foundations aún requiere revisión visual/táctil en móvil y tablet.
- `:app:spotlessKotlinCheck --offline` no pudo ejecutar ktlint porque `com.pinterest.ktlint:ktlint-cli:1.5.0` no está en la caché local; se revisaron manualmente imports, formato y `git diff --check`.
- Pendiente en Foundations: evaluar la tarjeta y skeleton en dispositivo, Back y split-screen; adaptar densidad de grids y menús de biblioteca por etapas, sin cambiar aún lógica de negocio.

## Avance PR 3: Inicio local

- Hero: solo obras de biblioteca, ordenadas por progreso/novedades, uso reciente y fecha de incorporación. Paginación manual, artwork de fondo opcional y ancho máximo responsive.
- Continuar viendo/leyendo: historial local con progreso confirmado; para anime se conserva la corrección del siguiente episodio ya existente en el checkout principal.
- Novedades: obras con episodios/capítulos pendientes de los últimos siete días, deduplicadas por tipo e ID, con enlace a detalles. Añadidos recientemente aparece después si hay contenido.
- Inicio ya no consume recomendaciones Jikan ni inicializa consultas externas al arranque. Jikan queda para Explorar en una fase posterior.
- Kotlin y APK debug compilados offline. Falta revisar UI e interacción real en teléfono/tablet antes de dar por validado el criterio de salida responsive.

## Avance PR 4: Biblioteca

- «Todo» es una categoría virtual de lectura con ID reservado `-1`; las relaciones de categorías reales se conservan y el agregado se deduplica por obra. Refrescar «Todo» equivale al refresco global; ordenar usa la preferencia global.
- Se desplazan una sola vez los índices guardados mayores que cero para mantener la categoría seleccionada. La opción de grid/lista y las columnas explícitas permanecen; el grid automático responde al ancho visible.
- Chips rápidos de estado filtran la vista por progreso real de episodios/capítulos. El filtro avanzado, selección múltiple y acciones existentes siguen operativos.
- Kotlin y APK debug compilaron offline. La verificación en teléfono/tablet y de backups queda pendiente.

## Avance PR 6: Organización 2.0

- Para nuevas altas, Jikan solo puede sugerir una categoría existente; no crea una por género. Se eliminó la recategorización al abrir detalles y al guardar una elección manual.
- El switch de Ajustes > Biblioteca controla la sugerencia. Las categorías predeterminadas y el selector manual existentes siguen como fallback.
- Ajustes también ofrece Automática, Preguntar siempre, Categoría predeterminada y Sin categoría. La confirmación manual vincula tracking después del alta; cancelar no lo abre.
- Migración pendiente: las asociaciones antiguas no indican si una categoría provino de Jikan o del usuario. La conversión a etiquetas deberá ser opcional, respaldar relaciones, preservar ambiguas y probar rollback antes de activarse.
- El contrato aditivo y el plan de rollback están en `docs/ORGANIZACION_2_MIGRACION.md`.

## Avance PR 7: Explorar

- Descubrir es la primera pestaña de Explorar y consulta Jikan solo al entrar. Incluye temporada actual, próximos estrenos, tendencias, géneros y estado de error con reintento.
- Tocar metadatos externos abre la búsqueda global en fuentes instaladas. No se ofrece «Añadir» directo hasta resolver una fuente real; la UI lo explica.
- Las pestañas previas siguen accesibles y los accesos programáticos a Extensiones se calculan según tipos visibles. Se eliminó el supuesto de paridad entre índice de pestaña y Anime/Manga para la búsqueda.
- Kotlin y APK debug compilaron offline. La API Jikan y la UI real no están verificadas en este entorno.

## Avance PR 8: fichas de obra

- Se conservan cabeceras, acciones, listas y dos paneles existentes; los CTA usan textos localizados y Manga usa icono de libro.
- Los episodios parcialmente vistos muestran una barra pequeña basada en el progreso y duración reales.
- Kotlin compiló offline. Las pestañas de Información/Relacionado y el ajuste táctil en tablet quedan pendientes.

## Avance PR 9: herramientas

- Más enlaza a Fuentes, Extensiones y Tracking, además de las herramientas ya presentes. Las rutas de Fuentes y Extensiones cambian a las pestañas existentes de Explorar según los tipos visibles.
- Se mantienen instaladores, descarga, historial, actualizaciones, tracking y ajustes existentes sin cambios de contrato. Kotlin compiló offline; falta recorrido táctil de los destinos.
