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
