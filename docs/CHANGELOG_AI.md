# CHANGELOG_AI

## 2026-10-06: preparación 1.1.0 y distribución ARM64

Usuario autoriza subir a 1.1.0, actualizar README y notas con funcionalidades/correcciones actuales, y distribuir exclusivamente ARM de 64 bits. Evaluación: viable; se excluyen explícitamente sistemas ARM32 y x86/x86_64. Fases: 1 versionado/documentación/ABI; 2 release firmado y prueba de actualización; 3 preparación en GitHub. Solo worktree codex/gran-actualizacion; no mezclar checkout principal antiguo.

app/build.gradle.kts: versionName 1.1.0, versionCode 9; splits solo arm64-v8a, sin universal. AGP rechaza duplicar filtros en ndk y splits, por lo que se conserva únicamente splits. Firma lee KITSUX_KEYSTORE_PATH, KITSUX_STORE_PASSWORD, KITSUX_KEY_ALIAS y KITSUX_KEY_PASSWORD; se retiran credenciales embebidas. Para build local se reutiliza el keystore ignorado existente del checkout principal, sin copiar/commitear secretos. Comparar certificado con APK oficial v1.0.7 antes de instalar.

README español/inglés renovado: Inicio/progreso, Descubrir/caché, Buscar/Extensiones/Migraciones, Recientes/modos/búsqueda, descargas/errores/retry, paleta y bienvenida. Requisitos Android 8+ con sistema ARM64; un procesador 64-bit con sistema 32-bit no basta. Se retiran promesas universales sobre bloqueos/Cloudflare y funciones exclusivas antiguas del texto. docs/releases/1.1.0.md contiene notas públicas sin menciones de generación. Script de enlaces conserva solo asset ARM64. Workflow de tags apunta a richtunic/Kitsu-X, un solo asset y notas versionadas; mantiene draft=true. CI usa Java 21 para ejecutar dependencia FlexibleAdapter classfile 65, sin cambiar target JVM 17 ni minSdk.

Validación hasta ahora: 27 pruebas dirigidas de app en release y 11 de red pasan, XML previo/formato dirigido/diff, sintaxis Python y YAML correctos. Logs /tmp/kitsux-110-release-tests.log y kitsux-110-release-build.log. Release minificado en compilación. S23 tiene producción io.kitsux.app 1.0.7 versionCode 8 y debug separado; antes de actualizar se registró biblioteca/categorías Original/Romance y títulos Youjo Senki Movie, Slime Movie, Overlord IV y Mushoku Part 2. Captura release-107-library.png. No borrar datos ni desinstalar. Queda comprobar actualización/firma/única ABI/manifest y preparar borrador GitHub; no dar por validada descarga completa de Anime ni aislamiento simultáneo de retry por estas pruebas.


## 2026-10-06: decimal limpio en Continuar leyendo

Reporte: Kanojo no Tomodachi (JYURA) muestra capítulo 0.01 como 0.009999999776482582 en la portada de Inicio. Reproducción física capturada en /tmp/kitsux-issue1-device/chapter-decimal-before.png. Ese valor coincide con 0.01f convertido a Double; el motor fuente/DB conserva ese número, pero Inicio usaba Double.toString().removeSuffix(".0") y exponía los dígitos binarios. Ficha, historial y notificaciones ya usan formatChapterNumber con hasta tres decimales.

Corrección mínima: KitsuXHomeScreenModel reutiliza formatChapterNumber para la etiqueta Cap. Se sincroniza el DecimalFormat compartido para evitar acceso concurrente desde procesamiento IO de Inicio y UI/notificaciones; no se añaden formateadores/dependencias ni se altera política existente de decimales. No cambiar DB, orden, reconocimiento, identificadores, fuente ni avance de lectura. Tres pruebas nuevas cubren artefacto Float→Double, enteros/0.001/1.5/48.25 y uso paralelo de capítulos/episodios. Tres regresiones de Inicio pasan: seis pruebas, cero fallos. Formato dirigido, diff, build Kotlin/APK y firma pasan. Logs /tmp/kitsux-chapter-number-final.log y kitsux-chapter-number-format.log. APK SHA 2941d579c39893a9ed5651b1fb489faf6cf5a9910ebda30dd67d93cb5ed318a3. APK instalado con --no-streaming -r sin borrar datos y SHA instalado cotejado. Captura /tmp/kitsux-issue1-device/chapter-decimal-after.png confirma Kanojo con Cap. 0.01 en una sola línea, Lucky Mia Cap. 30 e Instructor Cap. 164; portadas, barra y diseño conservados. No se inició lector ni se modificaron números/progreso/datos para esta corrección.

Seguridad: solo presentación y protección del formateador mutable; sin secretos, permisos, red, extractores o schemas nuevos. No se modifican datos del manga para ocultar el error. Mantener todos los cambios de diseño del worktree codex/gran-actualizacion.


## 2026-10-06: validación S23 y causa real de descarga

Prueba física autorizada por el usuario. Solo checkout /Users/richtunic/.codex/worktrees/gran-actualizacion/KitsuX, rama codex/gran-actualizacion. APK de fluidez instalado sin borrar datos, SHA 3d7c35e90f7ce7924d57acb55162f0c662e36479ec248dc7c4706f2d6bd9e379 cotejado. S23 Ultra Android 16, ManhwaWeb 1.4.13. Descubrir muestra contenido guardado al abrir; Buscar mantiene consulta Lucky, selector Manga, grupo desplegado y posición tras ir a Inicio y volver. Tres arranques fríos debug: 1994/1845/1834 ms, mediana 1845 ms; mide am start -W, no carga completa remota ni FPS.

Causa confirmada del fallo Lucky Mia!: cliente Android recibe HTTP 200 image/webp, pero RawFile no puede crear 003.tmp y otras páginas: EPERM (Operation not permitted). MANAGE_EXTERNAL_STORAGE estaba denegado. Se habilitó desde Ajustes de Android, sin desactivar WiFi, borrar datos ni cambiar extensión. Diálogo de error se abre y Copiar detalles funciona; reintento individual del capítulo 33 termina y sale de cola. CBZ 26487131 bytes, integridad ZIP correcta, páginas originales 001..013 completas; 34 imágenes por división de páginas largas y ComicInfo.xml. Lector muestra Capítulo 33 y 34 imágenes. Se conservó archivo y biblioteca. La primera apertura de Continuar fue capítulo 30 y no prueba el 33; captura fluid-lucky-33-reader.png sí identifica capítulo 33. No se apagó red inalámbrica: lectura de archivo local comprobada, aislamiento sin conexión no probado.

Corrección mínima: creación fallida de archivos/directorios/CBZ pasa de !! y motivo nulo a IOException localizada que orienta a revisar permisos y carpeta. Se aplica al flujo de Manga y Anime, sin cambiar extractores, esquemas, dependencias ni permisos declarados. Respuesta de imagen se cierra si no puede crearse archivo temporal. Build/Kotlin y siete pruebas de descargas pasan, XML/claves únicas, formato dirigido y diff pasan; logs /tmp/kitsux-storage-tests.log y /tmp/kitsux-storage-device-final-build.log. APK final SHA 18777a1e0963bb3d076099d96d06f8c73ea0a1d57e9d6d7fc1ac3a1de6d04b5d reinstalado y cotejado tras reconectar ADB. Prueba negativa física completada con capítulo 32: al desactivar temporalmente acceso a archivos, la fila y el diálogo muestran el mensaje localizado de permisos/carpeta. Captura fluid-storage-error-large.png verifica texto al 130 % y acciones completas. Tras restaurar el permiso y tocar Reintentar en el diálogo, capítulo 32 completa (9 páginas originales, CBZ 26 MB) y cola queda vacía. Se conservan capítulos de prueba 32/33; no se borran descargas. Permiso confirmado allow, font_scale=1.0 y animator_duration_scale=1.0 restaurados; no se cambió tema ni rotación. Captura fluid-queue-complete.png confirma cola vacía. No hubo otros fallos/descargas activas para probar aislamiento simultáneo; esa propiedad queda cubierta por pruebas unitarias. Aperturas del lector pueden añadir historial/progreso normal.

Revisión de seguridad: sin secretos, envío de diagnósticos ni cambios de autenticación; acceso a todos los archivos es permiso existente y se habilitó para la carpeta raw elegida. No se asume causa HTTP global ni cobertura universal de fuentes. Pendientes: dos fallos simultáneos para aislamiento real de retry, Anime/video completo, light/rotación/TalkBack y medición de memoria; pruebas unitarias cubren selección individual.


## 2026-10-06: fluidez, continuidad y diagnóstico autorizados

Usuario aprobó cuatro recomendaciones: conservar posición/búsqueda/filtros/grupos, reutilizar contenido guardado, reducir trabajo/memoria y diagnóstico con reintento individual. Evaluación: viable, riesgo bajo en estado/presentación, medio en caché y coordinación de retry. Fases: 1 continuidad/caché con pruebas; 2 selección acotada de historial; 3 detalle de descargas, retry aislado y ciclo de vida de vistas. Se mantiene checkout codex/gran-actualizacion, diseño/paleta/navbar y cambios anteriores.

Fase 1: selector Anime/Manga de Explorar y género de Descubrir pasan a rememberSaveable. Consultas/grupos/listState ya guardables se conservan; no reemplazar navegación. Descubrir sigue usando caché fresco de 6 h sin llamadas; cuando solo hay datos vencidos, muestra hasta 7 días de contenido guardado antes de consultar Jikan/AniList. Las tres secciones se publican por separado y el estado marca cuáles siguen guardadas, con etiqueta discreta. Actualización exitosa sustituye datos y quita marca; error conserva filas. JSON corrupto no bloquea red y una respuesta vacía fresca no dispara peticiones adicionales. Siete pruebas de Explore pasan.

Fase 2: selectContinueAnimeHistory conserva orden/selección por grupo y exige reproducción real, pero detiene consultas de episodios al encontrar 15 obras. Tres pruebas, incluyendo 500 entradas con 15 consultas y grupos sin reproducción/duplicados. No afirmar mejora adicional en milisegundos/FPS sin dispositivo. AsyncImage/Coil ya resuelve tamaño según límites y carga diferida; no añadir precarga masiva ni otro caché de imágenes.

Fase 3: tocar fila con ERROR abre AlertDialog Material3 de 22 dp, desplazable y texto seleccionable; incluye obra, capítulo/episodio, fuente y razón. Copiar detalles retira URLs y credenciales comunes, sin compartir/envío automático. Mensajes Anime ahora se guardan en memoria igual que Manga. Retry recibe ID individual vía WorkManager y permite preparar una entrada restaurada; otros ERROR permanecen intactos y una entrada activa/completada/retirada no se recrea desde el diálogo. Cola normal conserva Reanudar todo; cola activa se despierta por señal y finalización de jobs sin cancelar las otras descargas activas. Sin cambio de extractores, video/lector, schemas ni dependencias. Estado de error en memoria no se conserva al morir proceso, conforme al almacén heredado.

Se corrige ciclo de vida AndroidView de Anime/Manga: observadores viven en DisposableEffect y se cancelan al salir. AndroidView.onRelease libera adapter/binding y jobs de progreso; comprueba identidad de vista para que liberar una vista antigua no borre una nueva. No retener actividad/vistas de páginas descartadas.

Validación final: 24 pruebas dirigidas app pasan (7 Explore, 3 Inicio, 3 preparación de retry, 1 selección/diálogo retirado, 2 sanitización, 1 contador colas y 7 fuentes). Compilación Kotlin/APK, formato dirigido, XML/claves únicas, firma y git diff --check pasan. Logs /tmp/kitsux-fluid-delivery.log y kitsux-fluid-final-apk.log. ADB vacío: APK final no instalado, no nuevas capturas/rendimiento S23. Pendiente instalar con --no-streaming -r y cotejar SHA, reintentar Lucky Mia! capítulo 33 y verificar 13/13/lectura offline; probar diálogo/copiar/retry mientras otra descarga corre; volver de ficha y alternar páginas; escala 1.3/tema claro/rotación y restauración; medir arranque y memoria. Pausa/reanudación/WorkManager y cierre real del error ManhwaWeb siguen pendientes de prueba Android. No se borraron datos/colas ni se actualizó ninguna extensión durante este trabajo.

## 2026-10-06: recuperación general de Referer para imágenes

Usuario pide extender prevención a otras extensiones. Evaluación: viable con condiciones; no agregar cabecera global a toda petición ni tratar todo 403 como prueba de Referer ausente. Solución mínima por fases: detectar rechazo de imagen, reintento acotado con origen de la fuente, validar aislamiento y transporte HTTP antes de entregar APK.

HttpSource.getImage usa fetchImageWithRefererFallback con su imageRequest original y baseUrl. El helper primero conserva solicitud original; únicamente ante HttpException 403, método GET y Referer ausente/vacío, reintenta una vez con origen normalizado de la fuente (sin ruta/query/fragmento ni credenciales en URL). Respeta cabeceras, URL, auth y cancelación; no reintenta otros códigos, IOException ni origen inválido/con credenciales. Si el reintento falla, propaga ese error. No se modifica el formato/API binaria de extensiones, compresión, cookies, DB ni dependencias. Se conserva compatibilidad específica CDN ManhwaWeb ya validada.

Alcance: descarga estándar de imágenes de extensiones de manga que heredan HttpSource.getImage, incluidas imageRequest personalizadas. Extensiones que reemplazan getImage sin delegar al método base conservan su implementación; no se promete cobertura universal ni se fuerza recuperación de video/anime, autenticación o desafíos Cloudflare. La capa Cloudflare puede envolver un 403 en IOException: en ese caso no se inventa un código ni se añade retry de cabecera. El problema exacto en el S23 sigue sin confirmar porque no hay ADB y la versión actual Keiyoushi ManhwaWeb ya configura Referer.

Validación: seis pruebas generales (recuperación, éxito sin mutación, aislamiento, otros errores/cancelación, máximo un retry y socket HTTP real 403→200 mediante OkHttp.awaitSuccess), tres del CDN específico y dos de cliente/gzip pasan: 11 pruebas de red, cero fallos. Formato dirigido, diff, Kotlin/APK y firma pasan. No instalado ni descarga Android/offline comprobada; pendiente instalar APK worktree y leer error real del S23. Evidencia /tmp/kitsux-generic-image-final.log; conservar diagnóstico y pendiente de Lucky Mia! capítulo 33 del apartado anterior.

## 2026-10-06: descarga Lucky Mia! / ManhwaWeb

Reporte del usuario: falla descarga de Lucky Mia! en ManhwaWeb; no proporcionó aún mensaje exacto. Evaluación: viable con condición de validar la extensión instalada/dispositivo, riesgo bajo en fallback de cabecera estrictamente acotado. Fase 1: diagnóstico remoto sin dispositivo; Fase 2: compatibilidad y error visible; Fase 3: prueba instalada cuando haya ADB. No cambiar lector, esquemas, fuentes instaladas ni colas existentes.

Diagnóstico remoto: backend de ManhwaWeb devuelve 13 URLs para Lucky Mia! capítulo 33. Imagen 001 sin Referer devuelve HTTP 403 y HTML Cloudflare; con Referer https://manhwaweb.com/ devuelve HTTP 200 image/webp. Solo cambiar User-Agent no resuelve el 403. Las 13 páginas completas responden 200 image/webp con Referer (prueba desde Mac, dos solicitudes simultáneas, /tmp/kitsux-lucky-download-probe.json). Esto verifica acceso CDN, no guardado Android. El código actual de la extensión Keiyoushi ya envía Referer; versión/cabeceras de la extensión instalada no verificables sin teléfono, por lo que esta evidencia no confirma todavía la causa exacta del reporte.

ManhwaWebImageInterceptor añade Referer solo cuando falta/está vacío, únicamente para HTTPS img2mw.xyz /manhwas/. Conserva cabeceras existentes y no reintenta/cambia hosts ni resuelve desafíos Cloudflare. Integrado en cliente compartido antes de Cloudflare para extensiones antiguas que omiten la cabecera. Sin dependencias ni sustitución de compresión. MangaDownload guarda en memoria el motivo del último error, reinicia al reintentar y la fila lo muestra limitado a primera línea/200 caracteres. Fallos de directorio/preparación ahora marcan la descarga ERROR y cancelación al resolver URL de página se propaga como cancelación, no fallo de página.

Validación: tres pruebas específicas de cabecera/aislamiento + dos pruebas de cliente/gzip + ocho regresiones de búsquedas/colas pasan (13 total). Formato dirigido, diff, compilación debug y firma pasan. ADB sin dispositivos: no se instaló este APK, no se probó descarga completa ni lectura offline en S23, y no se borró la descarga de prueba del capítulo 33. Pendiente instalar APK del worktree con --no-streaming -r, leer motivo exacto/versión de extensión, reintentar solo Lucky Mia! y confirmar 13/13 + lectura offline. Si falla con Referer ya presente, investigar ese error real antes de ampliar compatibilidad.

## 2026-10-06: cierre local de optimización y pendientes físicos

Continuación solicitada sin teléfono disponible. Checkout válido: /Users/richtunic/.codex/worktrees/gran-actualizacion/KitsuX, rama codex/gran-actualizacion; no compilar/instalar el checkout principal con diseño antiguo.

Implementación terminada: arranque sin espera artificial, procesamiento de Inicio en IO y búsquedas de biblioteca por ID; Descargas Anime/Manga con tarjetas, estados y errores reactivos; retry por fuente sin perder otras respuestas; filtros activos y limpieza; Continuar con fuente/título de dos líneas/Ver detalles; flecha de grupos con animación Compose de 150 ms y semántica de progreso. Sin dependencias, esquemas, permisos ni motores nuevos.

Revisión adicional detectó que Anime no cancelaba búsqueda/retry anteriores al cambiar consulta. Una prueba falló esperando esa cancelación; tras corregir searchJob y retryJobs, ocho pruebas dirigidas pasan para ambas búsquedas, errores y colas. También se evita spinner en anime pausado con progreso desconocido y se oculta el progreso numérico desconocido. Formato dirigido, diff, XML y compilación APK pasan. Pruebas usan launcher temporal Java 21 por FlexibleAdapter, sin cambios de configuración del proyecto.

Evidencia S23 previa a desconexión: medianas de arranque frío debug 6529→1809 ms (72.3 % menos, tres muestras por versión); no mide carga remota de todas las imágenes. Capturas /tmp/kitsux-issue1-device/continue-final.png, filters-active-final.png y continue-details-final.png verifican fuente visible, limpiar filtros y ficha Oni no Hanayome desde pulsación larga. download-populated-final.png verifica Lucky Mia! capítulo 33, 13 páginas, fila redondeada, estado Descargando y conteo Manga 1/Anime 0. Se añadió solo ese capítulo para prueba; estaba 0/13 en la última lectura. No se confirmó descarga completa, pausa/reanudación ni se limpió esa entrada tras perder conexión. No se borraron descargas del usuario.

ADB sin dispositivos ni servicios mDNS al continuar el 6 de octubre. Última instalación comprobada incluye pulido visual y filtros, pero precede a correcciones finales de cancelación Anime/spinner. No afirmar que el APK definitivo esté instalado ni SHA remoto final cotejado. Pendientes al reconectar: instalar app/build/outputs/apk/debug/app-arm64-v8a-debug.apk con adb install --no-streaming -r, cotejar SHA local/instalado, repetir tres arranques fríos, revisar pause/resume y estado del capítulo 33 de prueba, verificar filas Anime, escala de texto 1.3 y animator_duration_scale=0 con restauración de originales. No desactivar Wi-Fi: ADB es inalámbrico. No tocar descargas existentes ni resetear datos. No se comprobó TalkBack completo, FPS universal, tablet ni playback decodificado en esta fase. Teléfono se conservó oscuro, font_scale=1.0 y rotación 0; no se llegó a modificar escala de animación.

## 2026-10-05: arranque y pulido de uso autorizados

Evaluación: viable, riesgo medio al tocar arranque/cancelación, moderado en presentación. Fase 1: medir arranque frío, retirar espera artificial y sacar el procesamiento de Inicio del hilo UI; validar APK/S23 antes de continuar. Fase 2: Descargas con estilo actual, conservar cola/reordenación/acciones; errores de fuentes claros con reintento. Fase 3: filtros activos visibles, información Continuar, animaciones/accesibilidad y revisión física. No añadir destinos, dependencias, cambios de esquema ni extractores. Usuario autoriza pruebas con teléfono desbloqueado y ejecución continua sin nuevas confirmaciones. Baseline inicial 6592 ms; repetir tres muestras. Launcher ACTION_MAIN sale de handleIntentAction sin marcar ready, reteniendo splash hasta el máximo de 5 s. Corrección aplicada tras atender el intent, manteniendo el límite de respaldo; eliminado mínimo artificial de 500 ms. Estado Inicio procesado en IO y lookup de biblioteca por ID. Fase 1 Kotlin/APK/firma e instalación con SHA cotejado pasaron. Tres arranques fríos antes: 6605/6493/6529 ms; después: 1809/1838/1766 ms. Mediana 6529→1809 ms, 72.3 % menos; medidas am start -W del mismo debug/S23, no tiempo universal ni descarga de portadas remotas. Captura startup-fast.png muestra Inicio conservado con portadas y progreso. Fase 2 implementada: tarjetas nativas de descargas mantienen FlexibleAdapter, estados y resumen reactivos, selección Anime/Manga; mensajes de error traducidos y retry individual en búsqueda global, dispatcher IO compartido limitado a cinco por modelo y publicación atómica de resultados.


Fase 2 terminada: seis pruebas dirigidas pasan (clasificación de errores, retry aislado/cancelado, conteos de errores en ambas colas). Kotlin/APK y formato dirigido pasan. S23: búsqueda Naruto en Anime/Manga conserva resultados por fuente; AnimeFenix sin resultados, AnimeID requiere verificación y AnimeFLV muestra fallo con reintento. Reintentar AnimeFLV mantiene AnimeID y resultados de otras fuentes. Preferencia «Con resultados» restaurada a true tras inspección. Descargas Anime/Manga tienen estados, resumen de errores y vacío estilizado; corregida etiqueta Manga usando label_manga. Pruebas de cola usan launcher Java 21 temporal /tmp/kitsux-test-java21.gradle por FlexibleAdapter classfile 65, sin alterar configuración del proyecto ni agregar dependencias.

Fase 3 implementada: filtro rápido guardable, indicador de filtros activos y limpiar por medio; conserva consulta, categoría, orden y modo global solo descargados. filterIntervalCustom sigue siendo preferencia compartida heredada. Continuar amplía tarjetas a 120 dp, título de dos líneas, nombre de fuente local y acción Ver detalles por pulsación larga; capítulo localizado sin truncar decimales. Semántica expone progreso textual sin prometer porcentaje preciso de manga. Flecha de grupos animada 150 ms con Compose, sin loops ni animaciones propias que ignoren escala de duración del sistema. Ver cierre del 6 de octubre para evidencia física y pendientes tras desconexión.


## 2026-10-05: pulido integral autorizado, implementación por fases

Evaluación: viable; riesgo bajo en presentación y medio en preservar selección/retorno/scroll, complejidad moderada. Fase 1: Agrupados con una portada por obra, grupos plegables y filas compactas; búsqueda despliega coincidencias y selección muestra todos los resultados para no ocultarlos. Recientes retira el texto explicativo fijo y reduce padding del buscador, conservando dos selectores y objetivos táctiles. Fase 2: fichas Anime/Manga, portadas, metadatos, CTA y filas con identidad consistente; conservar callbacks, fuentes y motores. Fase 3: bienvenida actualizada, accesibilidad y recorrido físico con teclado, títulos largos, retorno y scroll. No añadir destinos al navbar ni dependencias. Fase 1 validada con formato, Kotlin, pruebas, APK y S23: grupos plegables y búsqueda por capítulo, una portada por obra, retorno desde ficha conserva consulta/modo/expansión. Fase 2 implementada con cabecera compartida, metadatos localizados, CTA adaptable, descripción/tags y filas redondeadas. La prueba física detectó palabras partidas en cinco acciones: se usan tres columnas normales y dos con fontScale > 1.1. Fase 3 actualiza bienvenida base/es y añade regresión de 500 actualizaciones por tipo. Cinco pruebas pasan, Kotlin/APK/firma pasan. S23: ficha Lucky Mia y Oni no Hanayome, tema claro legible, escala 1.3 comprobada y restaurada a 1.0; bienvenida desde Ajustes > Avanzado confirma los modos nuevos, sin resetear preferencias. Reanudar lectura carga la página de Lucky Mia y Back vuelve a ficha/Recientes. El degradado de sinopsis ahora termina en surfaceContainer para evitar una franja de otro color. APK final instalado sin borrar datos; firma y SHA256 remoto/local verificados. Ficha de Mushoku Tensei III desde Historial confirma título completo en dos líneas y tres con escala 1.3, acciones en dos columnas sin palabras partidas. Historial conserva exactamente la posición de Hope tras abrir su ficha y Back (bounds [291,848][388,905]). Teléfono devuelto a oscuro, portrait/user_rotation=0 y font_scale=1.0. Capturas reales: /tmp/kitsux-issue1-device/polish-phase1-{collapsed,expanded}.png, polish-return-query.png, polish-manga-final.png, polish-manga-light.png, polish-reader.png, polish-onboarding.png, polish-detail-long-final.png, polish-detail-long-large-final.png, polish-history-scroll-return.png. Las capturas polish-anime-long-* corresponden a reproductor/Inicio, no a ficha, y no sirven como evidencia de título largo. Abrir Continuar lanzó el reproductor de Mushoku episodio 2 y alcanzó carga; no se verificó video decodificado. La prueba añade una entrada reciente al historial normal de reproducción; no se borraron entradas. La prueba de 500 entradas verifica filtrado/identidades, no FPS de una biblioteca física con 500 obras; no se sembraron datos ni se midió rendimiento universal. No se probó render inglés, TalkBack completo ni tablet. Las tres fases de presentación quedan implementadas y validadas dentro de este alcance. Sin dependencias, DB, permisos ni secretos nuevos.


## 2026-10-05: modos y búsqueda integrada en Recientes

Recientes reúne selector Anime/Manga, chips Nuevos/Historial/Todos/Agrupados y buscador permanente de la vista activa. Nuevos filtra capítulos no leídos/episodios no vistos (incluye progreso parcial); Todos conserva todas las actualizaciones de la consulta existente; Agrupados las reúne por ID de obra, manteniendo distintas fuentes aunque compartan título. Historial reutiliza los modelos/acciones/confirmaciones existentes de lectura y reproducción, agrupado por fecha, con tarjetas y portadas redondeadas. La búsqueda de actualizaciones encuentra título o capítulo/episodio; Historial conserva su consulta por título. Cambiar tipo/modo limpia búsqueda y selección. Seleccionar todos/invertir se limita a resultados visibles; la pulsación larga selecciona una fila sin rangos ocultos. Mantener límites heredados de actualizaciones (3 meses, hasta 500 por tipo). No cambian consultas DB, motor, permisos ni dependencias. Cuatro pruebas de regresión pasan: búsqueda dentro de Nuevos/Todos y agrupación sin fusionar fuentes homónimas en Anime/Manga. Formato dirigido, XML/claves únicas, diff, compilación Kotlin/APK y firma pasaron. APK de codex/gran-actualizacion instalado en S23 Ultra Android 16, conservando datos; SHA256 cotejado con el artefacto local. Inspección física: selector rojo legible, Todos por fecha, Agrupados con Lucky Mia y nueve capítulos; búsqueda por número reduce a una actualización. Selección de todos sobre búsqueda 33 mantiene contador 1, inversión devuelve la barra de navegación; no se marcaron, descargaron ni borraron entradas. Nuevos con búsqueda inexistente muestra Sin resultados; al cambiar de modo se restablece el buscador. Historial Manga encuentra Baek y Anime encuentra Oni; las tarjetas y los encabezados de fecha siguen el diseño actual. Se elimina el inset superior duplicado del contenido y se conserva reserva inferior para scroll bajo la píldora. Capturas en /tmp/kitsux-issue1-device/recents-final-*.png; se descartaron dos capturas iniciales con nombres de Manga que correspondían a Anime. APK final con corrección singular/plural instalado y SHA256 cotejado de nuevo; filtro 32 confirma Lucky Mia · 1 actualización (recents-final-group-single.png). Portada del historial de Instructor Estelar Maestro Baek abre su ficha de Manga, con título y capítulos correctos (recents-final-history-detail.png); no se inició lector ni reproductor. No se probó reproducción/lectura real ni tablet/rotación en este ajuste.


## 2026-10-05: reloj animado de Recientes, referencia TachiJ2K

Se sustituye Schedule estático por `anim_recents_enter.xml`, reloj con manecillas que giran y transición de contorno a relleno al seleccionar Recientes; vuelve en sentido inverso al salir mediante rememberAnimatedVectorPainter, igual que las demás pestañas. Recurso de Jays2Kings/tachiyomiJ2K, revisión b83defe42b2ac9f460667bda3a42fdd9d12e767e, Apache-2.0, atribuido en el XML. Solo cambian UpdatesTab.kt y el recurso, sin dependencias, datos, permisos, red ni paleta. Formato dirigido, XML, compilación Kotlin/APK, firma y diff verificados. APK instalado en S23 Ultra con --no-streaming -r, conservando datos; SHA256 instalado coincide con el APK final del worktree codex/gran-actualizacion. Grabación /tmp/kitsux-issue1-device/animated-clock.mp4 y secuencia animated-clock-frames.png confirman giro de manecillas y contorno→relleno al entrar, transición inversa al salir y repetición correcta al volver desde Más. Prueba enfocada en el navbar, sin cierre de la app; no se hicieron operaciones de extensiones ni migraciones.

## 2026-10-05: reloj en Recientes y paleta fija KitsuX

Recientes usa Schedule (reloj), sólido al seleccionarlo y contorno al salir. Apariencia conserva Claro/Oscuro/Sistema y AMOLED, pero retira la galería de paletas. Los consumidores de tema runtime (Compose, ThemingDelegate y contexto del lector) usan DEFAULT, sin leer el antiguo pref_app_theme; la clave heredada se conserva para compatibilidad de preferencias/backups. Claro pasa al rojo KitsuX y los recursos nativos oscuros se alinean con la paleta Compose existente para evitar azul en lector/diálogos. No se obliga a modo oscuro ni se cambian los fondos específicos del lector.

Archivos: UpdatesTab.kt, SettingsAppearanceScreen.kt, TachiyomiTheme.kt, TachiyomiColorScheme.kt, ThemingDelegate.kt, ContextExtensions.kt y colors_tachiyomi.xml de values/values-night; strings base/es corrigen el resumen de Apariencia. Sin dependencias, permisos, secretos, cambios de red ni esquemas. Formato dirigido, XML, compilación Kotlin/APK y diff pasaron. Colores de marca Compose/nativos coinciden en claro/oscuro; contraste blanco/rojo 4.79:1. APK final firmado e instalado con `adb install --no-streaming -r`, sin borrar datos; SHA256 instalado coincide con el APK del worktree. El primer intento de instalación por streaming se canceló al no terminar; la transferencia directa completó en 18 s. S23 Ultra: reloj seleccionado y sin seleccionar comprobados (`clock-recent-selected.png`, `clock-more.png`); Apariencia sin galería y modo Claro con acento rojo comprobados en el APK final (`clock-final-appearance-dark.png`, `clock-final-appearance-light.png`). Primera prueba de modos no fue válida porque cambió la pantalla; las capturas incorrectas se descartaron y se repitió con selección por texto, validando la pantalla antes de tocar. Retorno a Oscuro confirmado (`clock-final-appearance-dark-restored.png`); se dejó el móvil con ese modo original. Recursos nativos del lector comprobados por contrato de color; no se abrió un lector en esta revisión. No se hicieron operaciones de extensiones, migraciones ni cambios de biblioteca. Mantener seis destinos como límite práctico de la barra móvil.

## 2026-10-05: Recientes, categorías y listas completas de Explorar

Fases de desplazamiento y presentación compiladas por separado. Extensiones/Migraciones comparten un LazyColumn por página; se reutilizan sus filas y callbacks mediante un slot de lista, con claves separadas Anime/Manga. Reserva inferior en contentPadding, sin recorte del contenedor. Extensiones conserva confianza, permisos, confirmaciones y refresco de ambos gestores. Recientes se añade a la barra con etiquetas compactas; distingue uso como tab de pantalla abierta desde Más. Bibliotecas usan chips de categorías; actualizaciones conservan modelos/acciones y reciben tarjetas, portadas verticales y encabezados Hoy/Ayer/fecha. Se conserva la ventana existente de actualizaciones (3 meses, hasta 500 por tipo), sin modificar consultas ni datos. Las cabeceras de Biblioteca/Explorar/Recientes usan el fondo de la página, incluso al desplazarse; se retira el divisor de tabs y los filtros rápidos comparten forma/color de chips. Enlaces de actualizaciones abren el nuevo destino inferior. Compilación Kotlin/APK final, formato dirigido, firma, XML/claves únicas y diff pasaron. Primer APK verificado físicamente en S23: seis etiquetas legibles, categorías, actualizaciones Manga agrupadas por fecha y scroll de Extensiones que retira buscador/grupo de pantalla. Descubrir muestra imágenes por debajo y a los márgenes de la píldora (`refine-discover.png`), sin franja rectangular. APK final instalado sin borrar datos. Capturas finales `polish-manga.png`, `polish-recent-top.png`, `polish-recent-anime.png` y `polish-extensions.png` en `/tmp/kitsux-issue1-device` confirman fondo continuo, chips, tarjetas por fecha, selector Anime/Manga y extensión Anime con lista hasta la barra. Anime no tiene actualizaciones locales; Manga mostró Lucky Mia con fechas de agosto/julio. No había grupos Hoy/Ayer en los datos de la captura, por lo que esos textos se verificaron por código, sin inventar entradas. El gesto para volver arriba activó el refresco de biblioteca existente: se comprobó su snackbar y timestamp; no se instalaron/actualizaron extensiones ni se migraron obras. El intento final de filtrar se detuvo al cambiar el foco del móvil; filtro de ambos grupos queda sin comprobación final física. Rail/rotación e instalaciones reales no probados. Sin dependencias, credenciales, permisos nuevos ni esquemas.

## 2026-10-05: Migraciones como pestaña y barra flotante sobre el contenido

- Cuarta página deslizante con grupos Anime/Manga, filtro de fuentes y controles de migración existentes; bienvenida actualizada.
- Eliminada la franja causada por el recorte inferior del contenido. Reserva para scroll/FAB/snackbar conservada en Scaffold hijos; píldora sólida sin sombra negra.
- Kotlin/APK, formato, XML, firma y diff pasaron; instalado en S23 Ultra. Migraciones y desplegable Manga con cuatro obras comprobados. No se migraron datos; prueba visual final de contenido detrás de la barra pendiente porque el teléfono cambió de app. Sin cambios al motor de migración, datos, permisos o dependencias.

## 2026-10-05: unificar Explorar sin ocultar fuentes

- Tres páginas deslizables: Descubrir, Buscar y Extensiones; se retiran búsquedas duplicadas y la hilera de siete pestañas.
- Buscador con selector Anime/Manga y fuentes instaladas desplegables, acceso a catálogo, novedades, pin, filtros y opciones existentes.
- Gestión de extensiones agrupada por tipo, filtro común y repositorios visibles; se reutilizan instalador, actualización y confirmaciones existentes. Migración disponible en menú.
- Bienvenida actualizada para explicar esta estructura. Sin nuevas dependencias, esquemas, secretos ni permisos.
- Kotlin/APK, formato, XML, firma y diff pasaron. S23 Ultra confirmó páginas/grupos y acceso a AnimeFLV/InManga con títulos en este último. Detectado cierre por clase ausente en extensión Manga: se aísla `LinkageError` por fuente en ambos buscadores. Nueva búsqueda Manga devolvió resultados sin cierre. Filtro de gestión y repositorios con Añadir comprobados; estado vacío compacto. Sin instalaciones/actualizaciones reales durante pruebas.

## 2026-10-05: actualizar la guía de bienvenida

- Sustituido el paso y switch de recomendaciones personalizadas por una explicación de Inicio, Descubrir y fuentes instaladas.
- Corregidas las descripciones de banner, categorías existentes, repositorios y pantalla final para reflejar la experiencia actual; español e inglés.
- Botones de omitir/finalizar localizados con los recursos existentes. Se conservan preferencias y permisos, sin nuevas dependencias ni migraciones.
- XML de ambos idiomas válido y sin claves duplicadas; Spotless, compilación Kotlin/APK offline, firma y diff pasaron. Instalado en S23 Ultra sin borrar datos; revisión visual pendiente de desbloquear el dispositivo. Sin cambios de seguridad, permisos ni backend.

## 2026-10-05: Descubrir más rápido y progresivo

- Filas publicadas por separado; peticiones Jikan solapadas con inicios espaciados 1.2 s y límite por llamada de 4 s. AniList conserva su respaldo con límite de 10 s.
- Caché de cualquiera de los dos proveedores leído antes de llamar a la red; se conserva contenido ante errores. Respuestas Jikan decodificadas antes de guardarse.
- Claves estables de filas e indicador discreto durante carga parcial, conservando búsquedas/filtros y diseño actual.
- Tres pruebas, formato de archivos afectados, compilación/APK, firma y diff pasaron. APK instalado en S23 Ultra: contenido ya visible en el primer muestreo a 3.03 s; las tres filas se comprobaron con títulos y portadas. No se midió arranque sin caché ni se garantiza ese tiempo en todas las redes.
- Sin dependencias, secretos, permisos, autenticación ni cambios de esquema.

## 2026-10-05: ajuste visual de progreso en Inicio

- Barra de progreso dentro de las portadas de Continuar viendo/leyendo, con el doble de grosor (6 dp), margen interior y extremos redondeados.
- Portada redondeada en las cuatro esquinas y etiqueta del episodio/capítulo separada de la barra. Se conservan diseño de la gran actualización, datos y acciones existentes.
- Sin dependencias, permisos, secretos ni cambios de esquema. Spotless del archivo, compilación Kotlin, APK, firma y diff pasaron. Se reinstaló y comprobó visualmente en S23 Ultra / Android 16; InManga también cargó populares con este APK.

## 2026-10-05: corrección del issue #1 conservando la gran actualización

- Se detectó que el APK anterior de diagnóstico procedía de `main`, que no contiene los 17 commits del rediseño; su instalación había sustituido la app debug de la gran actualización en el teléfono.
- Se aplica en `codex/gran-actualizacion` el mismo parche mínimo de compresión: retirar los dos interceptores de red que Keiyoushi rechaza. Se añaden las dos pruebas de regresión ya verificadas.
- Se conservan los componentes de Inicio, navegación flotante, bibliotecas y Descubrir de esta rama, sin importar archivos UI del checkout principal.
- Ambas pruebas, compilación Kotlin, APK y firma pasaron. Se instaló 1.0.7-44 sin borrar datos; una captura confirma Inicio, portadas compactas y barra flotante restaurados. InManga cargó populares con este APK; Descubrir conserva su pestaña, aunque las filas remotas no se comprobaron. Sin nuevas dependencias, permisos, secretos ni cambios de esquema.

Fecha: 2026-09-29
Tarea: Retirar la leyenda de proveedores en Explorar
Cambios:
- Descubrir deja de mostrar la leyenda de Jikan/AniList sobre las filas de temporada, próximos estrenos y tendencias. El respaldo AniList y los mensajes de error reales siguen funcionando.
Validación:
- Kotlin y APK debug compilaron offline. `io.kitsux.app.dev` 1.0.7-43 se instaló en S23 Ultra y una captura de Descubrir confirmó las filas de temporada y próximos estrenos sin la leyenda.

---

Fecha: 2026-09-29
Tarea: Respaldo de Explorar durante fallos de Jikan
Cambios:
- Temporada actual, próximos estrenos y tendencias usan AniList como respaldo cuando Jikan responde 429/5xx o falla la conexión.
- Explorar conserva la búsqueda en fuentes instaladas y cachea respuestas válidas durante seis horas.
Validación:
- Consultas GraphQL directas de las tres secciones devolvieron HTTP 200 con títulos e imágenes. Kotlin y APK debug compilaron offline. Se instaló `io.kitsux.app.dev` 1.0.7-42 en S23 Ultra; Explorar mostró temporada actual y próximos estrenos con portadas de AniList. Inicio mostró hero y Continuar con imágenes. Falta recorrido de Tendencias y Añadidos recientemente.

---

Fecha: 2026-09-29
Tarea: Recuperar portadas grises en Inicio
Cambios:
- Continuar viendo/leyendo, Novedades y Añadidos recientemente usan `AnimeCover`/`MangaCover`, igual que las bibliotecas, para acceder a caché, portadas personalizadas y cabeceras de fuente.
- El hero usa la portada de la obra cuando no hay banner o falla su carga.
Validación:
- `:app:compileDebugKotlin` y `:app:assembleDebug --offline --quiet` pasaron; APK debug `io.kitsux.app.dev` 1.0.7-41 generado. El teléfono no apareció en ADB y falta la prueba visual en dispositivo.

---

Fecha: 2026-09-29
Tarea: Diagnosticar la carga de temporada de Jikan
Cambios:
- Explorar distingue errores HTTP 5xx de Jikan de otros fallos y muestra un mensaje de indisponibilidad temporal con Reintentar.
Validación:
- `/v4/seasons/now?limit=15` devolvió HTTP 504 tanto desde el equipo como desde el S23 Ultra, con un mensaje de Jikan sobre su conexión a MyAnimeList.
- Kotlin y APK debug compilaron offline. Se instaló `io.kitsux.app.dev` 1.0.7-39 y se comprobó el mensaje en pantalla.

---

Fecha: 2026-09-29
Tarea: Barra de navegación inferior flotante
Cambios:
- En teléfonos, la barra de cinco destinos tiene esquinas redondeadas, sombra y márgenes laterales e inferior, respetando el área de gestos del sistema.
- Se conserva el espacio reservado por Scaffold para que el contenido no quede bajo la barra y el rail para ventanas amplias.
Validación:
- `:app:compileDebugKotlin` y `:app:assembleDebug --offline --quiet` pasaron. APK `io.kitsux.app.dev` 1.0.7-38 instalado y revisado visualmente en S23 Ultra.

---

Fecha: 2026-09-29
Tarea: Compactar portadas de Anime y Manga según la referencia visual
Cambios:
- Inicio muestra portadas verticales de 108 dp en Continuar, Novedades y Añadidos recientemente; Continuar conserva el avance sobre la portada y su barra de progreso.
- La cuadrícula automática de las bibliotecas usa portadas de ancho mínimo 100 dp en móvil y 132 dp en ventanas amplias; los ajustes manuales de columnas se respetan.
Validación:
- `:app:compileDebugKotlin --offline --quiet` pasó; falta revisar en teléfono la legibilidad y el tacto de las tarjetas.

---

Fecha: 2026-08-01
Tarea: Preparar release KitsuX 1.0.7
Cambios:
- Se incrementó la aplicación a `versionName 1.0.7` y `versionCode 8`.
- Se prepararon notas bilingües para GitHub que resumen repositorios unificados, compatibilidad moderna de manga, actualización persistente de biblioteca, notificaciones detalladas, bandeja de novedades y actualización de extensiones en lote.
- Las notas incluyen el comentario solicitado: `Se añadieron nuevos bugs para solucionar en versiones posteriores` y su equivalente en inglés.
Archivos:
- `app/build.gradle.kts`
- `docs/release-notes-v1.0.7.md`
Validación:
- `./gradlew spotlessCheck --rerun-tasks` ejecutado correctamente.
- `./gradlew :app:testReleaseUnitTest :app:assembleRelease -Penable-updater` ejecutado correctamente.
- Los cinco APK muestran `versionName 1.0.7`, `versionCode 8`, firma APK v2 válida y checksums SHA-256 documentados.

---

Fecha: 2026-08-01
Tarea: Añadir bandeja de novedades y actualización de extensiones en lote
Cambios:
- Home agrupa los estrenos recientes de la biblioteca en `Hoy`, `Ayer` y `Esta semana` y conserva el acceso directo al siguiente episodio o capítulo pendiente.
- Las obras sin fecha válida o con más de seis días no aparecen en la bandeja, pero permanecen disponibles en Biblioteca.
- Anime y manga muestran `Actualizar todas` cuando existen extensiones pendientes y solicitan una confirmación global antes de comenzar.
- El lote toma todas las extensiones instaladas con actualización, aunque haya una búsqueda activa, las procesa una por una y bloquea ejecuciones simultáneas.
- Cada elemento reutiliza el instalador configurado: Package Installer conserva la confirmación del sistema y el instalador privado rechaza downgrades, APK sin firma y firmas diferentes a la instalada.
Archivos:
- `app/src/main/java/eu/kanade/tachiyomi/ui/home/KitsuXHomeScreenModel.kt`
- `app/src/main/java/eu/kanade/presentation/home/HomeScreenContent.kt`
- `app/src/main/java/eu/kanade/tachiyomi/ui/browse/anime/extension/AnimeExtensionsScreenModel.kt`
- `app/src/main/java/eu/kanade/tachiyomi/ui/browse/manga/extension/MangaExtensionsScreenModel.kt`
- `app/src/main/java/eu/kanade/tachiyomi/ui/browse/anime/extension/AnimeExtensionsTab.kt`
- `app/src/main/java/eu/kanade/tachiyomi/ui/browse/manga/extension/MangaExtensionsTab.kt`
- `app/src/main/java/eu/kanade/presentation/browse/anime/AnimeExtensionsScreen.kt`
- `app/src/main/java/eu/kanade/presentation/browse/manga/MangaExtensionsScreen.kt`
- `i18n/src/commonMain/moko-resources/base/strings.xml`
- `i18n/src/commonMain/moko-resources/es/strings.xml`
Validación:
- `./gradlew spotlessCheck` ejecutado correctamente.
- `./gradlew :app:compileDebugKotlin` ejecutado correctamente.
- `./gradlew :app:assembleDebug` ejecutado correctamente; se conservan advertencias D8/Kotlin metadata preexistentes.
- APK arm64 instalado con `adb install -r` en Galaxy S23 Ultra.
- Home abrió con la biblioteca existente y mantuvo los accesos de continuación. El dispositivo no tenía estrenos fechados en los últimos seis días, por lo que la bandeja permaneció oculta según el estado vacío previsto.
- La validación interactiva del diálogo de extensiones quedó limitada para no interrumpir el uso activo del teléfono; el diálogo, el bloqueo del lote y el flujo de firma se validaron en código y compilación.

---

Fecha: 2026-08-01
Tarea: Resumir resultados de actualizacion de biblioteca en notificaciones
Cambios:
- Las actualizaciones de anime y manga notifican la cantidad total de episodios o capitulos nuevos, incluso cuando el resultado es cero.
- La vista expandida ordena las obras con contenido nuevo y su cantidad, despues las omitidas y finalmente las que fallaron.
- Los errores ya no generan un segundo resumen duplicado; el registro completo queda disponible mediante la accion `Mostrar errores` del mismo resumen.
- El resumen solo se marca como notificacion de grupo cuando existen obras con contenido nuevo, evitando que Android oculte resultados con cero novedades.
- Se conserva la preferencia de privacidad: al ocultar contenido de notificaciones solo se muestran los contadores.
Archivos:
- [AnimeLibraryUpdateJob.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/data/library/anime/AnimeLibraryUpdateJob.kt)
- [AnimeLibraryUpdateNotifier.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/data/library/anime/AnimeLibraryUpdateNotifier.kt)
- [MangaLibraryUpdateJob.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/data/library/manga/MangaLibraryUpdateJob.kt)
- [MangaLibraryUpdateNotifier.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/data/library/manga/MangaLibraryUpdateNotifier.kt)
- [strings.xml base](file:///Users/richtunic/Documents/Proyectos/KitsuX/i18n/src/commonMain/moko-resources/base/strings.xml)
- [strings.xml es](file:///Users/richtunic/Documents/Proyectos/KitsuX/i18n/src/commonMain/moko-resources/es/strings.xml)
Validacion:
- `git diff --check` ejecutado correctamente.
- `./gradlew :app:compileDebugKotlin` ejecutado correctamente.
- `./gradlew :app:assembleDebug` ejecutado correctamente; se conservan advertencias D8/Kotlin metadata preexistentes.
- APK arm64 instalado con `adb install -r` en Galaxy S23 Ultra. Una actualizacion manual publico `0 nuevos capitulos`, `1 omitidos`, `0 con error` y el detalle expandido `OMITIDOS · 1` seguido de `Lucky Mia!`.

---

Fecha: 2026-07-07
Tarea: Preparar release KitsuX 1.0.6
Cambios:
- Bump de version estable a `versionName = 1.0.6` y `versionCode = 7`.
- Se actualizaron enlaces de descarga del README a `v1.0.6`.
- Se compilaron APKs release firmados para universal, arm64-v8a, armeabi-v7a, x86 y x86_64.
- Se agregaron notas de release bilingues sin mencionar fuentes especificas afectadas; el fix de reproduccion queda documentado como bug menor de compatibilidad de la version anterior.
Archivos:
- [app/build.gradle.kts](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/build.gradle.kts)
- [README.md](file:///Users/richtunic/Documents/Proyectos/KitsuX/README.md)
- [release-notes-v1.0.6.md](file:///Users/richtunic/Documents/Proyectos/KitsuX/docs/release-notes-v1.0.6.md)
Validacion:
- `./gradlew :domain:testDebugUnitTest --tests tachiyomi.domain.release.interactor.GetApplicationReleaseTest` ejecutado correctamente.
- `./gradlew :app:assembleRelease -Penable-updater` ejecutado correctamente.

---

Fecha: 2026-07-07
Tarea: Ocultar tarjetas individuales de continuar viendo/leyendo
Cambios:
- La pulsacion larga en `Continuar viendo` y `Continuar leyendo` ahora oculta solo la tarjeta seleccionada mediante una preferencia local, sin borrar historial ni progreso.
- La clave de ocultamiento incluye tipo, obra y episodio/capitulo objetivo; si aparece contenido nuevo con otro objetivo, la obra puede volver a mostrarse.
- El carrusel de novedades ya no reutiliza el dialogo de eliminar, dejando el gesto largo limitado a continuar viendo/leyendo.
- `Continuar leyendo` ahora puede resolver manga fuera de biblioteca usando la entrada local y el siguiente capitulo no leido, no solo los contadores de biblioteca.
Archivos:
- [KitsuXHomeScreenModel.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/ui/home/KitsuXHomeScreenModel.kt)
- [KitsuXHomeTab.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/ui/home/KitsuXHomeTab.kt)
- [HomeScreenContent.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/presentation/home/HomeScreenContent.kt)
Validacion:
- `./gradlew :app:compileDebugKotlin` ejecutado correctamente.

---

Fecha: 2026-07-07
Tarea: Mejorar deteccion automatica de updates y seleccion de APK compatible
Cambios:
- Se redujo el intervalo del chequeo automatico de app updates de 3 dias a 1 hora para que una nueva version publicada en GitHub se muestre al abrir la app sin buscar manualmente.
- El chequeo de app update ahora se ejecuta al entrar o volver a estado `RESUMED`, protegido por el intervalo automatico para no consultar GitHub en exceso ni apilar pantallas duplicadas.
- Se conserva el respeto a `No ahora`: una version rechazada no vuelve a molestar durante 5 dias.
- La seleccion de APK ahora prioriza la ABI instalada inferida por `nativeLibraryDir`, luego las ABIs soportadas por el dispositivo y finalmente `universal`.
- Se retiro el fallback inseguro al primer `.apk` del release; si GitHub no tiene asset compatible, no se ofrece descarga arbitraria.
Archivos:
- [GetApplicationRelease.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/domain/src/main/java/tachiyomi/domain/release/interactor/GetApplicationRelease.kt)
- [MainActivity.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/ui/main/MainActivity.kt)
- [AppUpdateChecker.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/data/updater/AppUpdateChecker.kt)
- [ReleaseServiceImpl.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/data/src/main/java/tachiyomi/data/release/ReleaseServiceImpl.kt)
- [GetApplicationReleaseTest.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/domain/src/test/java/tachiyomi/domain/release/interactor/GetApplicationReleaseTest.kt)
- [HANDOFF.md](file:///Users/richtunic/Documents/Proyectos/KitsuX/docs/HANDOFF.md)
Validacion:
- `./gradlew :domain:testDebugUnitTest --tests tachiyomi.domain.release.interactor.GetApplicationReleaseTest` ejecutado correctamente.
- `./gradlew :app:compileDebugKotlin` ejecutado correctamente.

---

Fecha: 2026-07-07
Tarea: Reducir fallos al reproducir Anime por requests duplicados de recomendaciones
Cambios:
- Se revisaron logs de Legion Anime: la fuente entrega contenido y abre el `PlayerActivity`; el ruido critico viene de rafagas de Jikan con `429 Too Many Requests` y `504 Gateway Time-out`.
- Se evito que `KitsuXIntelSystem` registre multiples observers para "similar a lo ultimo visto" cada vez que se refrescan recomendaciones.
- Se deduplicaron busquedas simultaneas del mismo `mal_id` por titulo para no saturar Jikan al abrir/reproducir el mismo contenido.
- Se retiro el fallback experimental de `Latest` a `Popular` porque no corresponde con el problema observado en Legion Anime.
- Se corrigio la reproduccion de hosters que devuelven URLs protocol-relative (`//host/ruta`): ahora se normalizan a `https://...` antes de enviarlas a mpv o reproductores externos. Los logs de Legion Anime mostraban mpv intentando abrir `//www.mediafire.com/...` como archivo local.
- Se agrego fallback automatico de hoster cuando mpv rechaza el enlace con `loading failed` o `unrecognized file format`; el video fallido queda marcado como error y se intenta el siguiente hoster disponible.
- Se restauro el User-Agent legacy de Brave como default real en vez de reemplazarlo por Android Chrome/WebView inferido. La tablet con KitsuX 1.0.4 que si reproduce Legion Anime usa ese UA y llega a `video/avc`.
- Se elimino la conversion del User-Agent Brave a Android Chrome/WebView en requests a hosters externos, evitando que Legion Anime reciba paginas HTML donde espera streams reproducibles. Los overrides especificos de AnimeOnline siguen limitados a sus dominios.
Archivos:
- [KitsuXIntelSystem.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/ui/home/intelligence/KitsuXIntelSystem.kt)
- [PlayerUtils.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/ui/player/PlayerUtils.kt)
- [PlayerActivity.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/ui/player/PlayerActivity.kt)
- [PlayerObserver.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/ui/player/PlayerObserver.kt)
- [PlayerViewModel.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/ui/player/PlayerViewModel.kt)
- [ExternalIntents.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/app/src/main/java/eu/kanade/tachiyomi/ui/player/ExternalIntents.kt)
- [NetworkHelper.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/core/common/src/main/java/eu/kanade/tachiyomi/network/NetworkHelper.kt)
- [UserAgentInterceptor.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/core/common/src/main/java/eu/kanade/tachiyomi/network/interceptor/UserAgentInterceptor.kt)
- [AnimeOnlineCloudflareCompat.kt](file:///Users/richtunic/Documents/Proyectos/KitsuX/core/common/src/main/java/eu/kanade/tachiyomi/network/interceptor/AnimeOnlineCloudflareCompat.kt)
Validacion:
- `./gradlew :app:compileDebugKotlin` ejecutado correctamente tras el fix de Jikan.
- `./gradlew :app:compileDebugKotlin` ejecutado correctamente tras restaurar el User-Agent legacy.
- `./gradlew :app:installDebug` ejecutado correctamente; APK debug `1.0.5-20` instalado en los dispositivos conectados.
- Pendiente prueba manual de Legion Anime en el movil con logs limpios para confirmar si el hoster ya devuelve stream reproducible en vez de HTML.

---

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

# 2026-09-29 — Gran actualización, fase técnica 0

- Inventariados navegación, responsive, tema, Inicio, biblioteca, Jikan y esquemas de categorías para el nuevo PRD.
- Creada la rama aislada `codex/gran-actualizacion` desde `b829a5838`; el checkout principal y sus cambios locales quedaron intactos.
- Registrado el plan por fases y los riesgos de compatibilidad en `docs/GRAN_ACTUALIZACION.md`. No se cambió código ni base de datos.

# 2026-09-29 — Foundations, navegación (avance)

- Navegación principal de hasta cinco destinos y rail desde `600dp` de ancho visible; Historial y Actualizaciones pasan a Más con indicador de novedades.
- Se retiró de Ajustes el selector de navegación anterior, conservando el valor almacenado. La etiqueta inglesa de Explorar se actualizó.
- Se trasladó el arreglo de FlexibleAdapter a esta rama para compilar el commit base. `:app:compileDebugKotlin` y `:app:assembleDebug` pasaron sin red; falta prueba en dispositivo.

# 2026-09-29 — Foundations, componentes compartidos (avance)

- Se centralizaron breakpoints y tamaños del layout, se limitó el ancho del hero y se añadió un skeleton accesible para la carga de Inicio.
- La tarjeta de progreso de Continuar viendo/leyendo se extrajo como componente reutilizable con menú contextual; manga deja de mostrar el icono de reproducción.
- Compilación Kotlin y APK debug pasaron sin red. No hay dispositivo ni AVD para validar la interacción.

# 2026-09-29 — Fase 2, Inicio

- El hero usa solo biblioteca local, con paginación manual, indicador contextual y acción coherente para continuar o abrir detalles.
- Continuar viendo/leyendo conserva historial confirmado; novedades y añadidos recientemente usan datos locales y eliminan duplicados por ID.
- Se retiraron las recomendaciones Jikan de Inicio y la inicialización de Jikan al arranque. Se añadieron estados vacíos y textos base/español.
- `:app:compileDebugKotlin` y `:app:assembleDebug` pasaron offline; `git diff --check` pasó. Pruebas en teléfono y tablet pendientes.

# 2026-09-29 — Fase 3, Biblioteca

- Anime y Manga agregan «Todo» virtual sin duplicados ni cambios de DB; se conserva la selección previa de categoría al desplazar índices guardados.
- Chips rápidos filtran por progreso y pendientes reales; la cuadrícula automática se adapta al ancho actual de ventana con límite de ocho columnas.
- Kotlin y APK debug compilaron offline. Pruebas táctiles y responsive en dispositivo pendientes.

# 2026-09-29 — Organización 2.0, avance seguro

- Jikan dejó de crear una categoría por género al añadir anime/manga y dejó de modificar asociaciones al abrir detalles.
- La elección manual de categorías se conserva y se escribe tras confirmar el alta. La preferencia de sugerir una categoría existente se expone en Ajustes de Biblioteca.
- No se migraron asociaciones históricas ni se añadieron tablas; la conversión a etiquetas requiere backup y rollback validados.

# 2026-09-29 — Fase 5, Explorar (avance)

- Descubrir muestra temporada actual, próximos estrenos, tendencias y filtros locales de género desde Jikan.
- Búsqueda de Anime/Manga y tarjetas externas abren las búsquedas existentes en fuentes instaladas; se aclara que Jikan no verifica disponibilidad.
- Fuentes y Extensiones conservan acceso y búsqueda correcta tras añadir la pestaña. Kotlin y APK debug compilaron offline; falta prueba de red y dispositivo.

# 2026-09-29 — Fase 6, fichas (avance)

- CTA de Anime/Manga localizados; Manga usa icono de lectura en lugar de reproducción.
- Episodios con progreso parcial muestran barra pequeña además del texto existente. Kotlin compiló offline; falta inspección en dispositivo.

# 2026-09-29 — Fase 7, herramientas

- Más enlaza directamente a Fuentes, Extensiones y Tracking usando las pantallas actuales. El destino en Explorar se calcula con las pestañas disponibles.
- Kotlin compiló offline. Los recorridos táctiles y estados reales de instalación quedan pendientes.

# 2026-09-29 — Ajustes de QA del rediseño

- «Todo» usa el orden global guardado y el estado vacío de filtros rápidos explica que no hay coincidencias.
- Descubrir reutiliza la caché Jikan existente durante seis horas y conserva resultados visibles si un reintento falla.
- Ajustes de Biblioteca ofrece cuatro modos de organización al añadir; el diálogo manual guarda la obra antes de vincular tracking, y cancelarlo no abre tracking.
