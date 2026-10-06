# HANDOFF

## 2026-10-06: preparación 1.1.0 y distribución ARM64

Usuario autoriza subir a 1.1.0, actualizar README y notas con funcionalidades/correcciones actuales, y distribuir exclusivamente ARM de 64 bits. Evaluación: viable; se excluyen explícitamente sistemas ARM32 y x86/x86_64. Fases: 1 versionado/documentación/ABI; 2 release firmado y prueba de actualización; 3 preparación en GitHub. Solo worktree codex/gran-actualizacion; no mezclar checkout principal antiguo.

app/build.gradle.kts: versionName 1.1.0, versionCode 9; splits solo arm64-v8a, sin universal. AGP rechaza duplicar filtros en ndk y splits, por lo que se conserva únicamente splits. Firma lee KITSUX_KEYSTORE_PATH, KITSUX_STORE_PASSWORD, KITSUX_KEY_ALIAS y KITSUX_KEY_PASSWORD; se retiran credenciales embebidas. Para build local se reutiliza el keystore ignorado existente del checkout principal, sin copiar/commitear secretos. Comparar certificado con APK oficial v1.0.7 antes de instalar.

README español/inglés renovado: Inicio/progreso, Descubrir/caché, Buscar/Extensiones/Migraciones, Recientes/modos/búsqueda, descargas/errores/retry, paleta y bienvenida. Requisitos Android 8+ con sistema ARM64; un procesador 64-bit con sistema 32-bit no basta. Se retiran promesas universales sobre bloqueos/Cloudflare y funciones exclusivas antiguas del texto. docs/releases/1.1.0.md contiene notas públicas sin menciones de generación. Script de enlaces conserva solo asset ARM64. Workflow de tags apunta a richtunic/Kitsu-X, un solo asset y notas versionadas; mantiene draft=true. CI usa Java 21 para ejecutar dependencia FlexibleAdapter classfile 65, sin cambiar target JVM 17 ni minSdk.

Validación hasta ahora: 27 pruebas dirigidas de app en release y 11 de red pasan, XML previo/formato dirigido/diff, sintaxis Python y YAML correctos. Logs /tmp/kitsux-110-release-tests.log y kitsux-110-release-build.log. Release minificado firmado pasó, APK 63442509 bytes; aapt confirma io.kitsux.app / versionCode 9 / 1.1.0 / minSdk 26 y ZIP solo lib/arm64-v8a. Certificado SHA256 b18fa525fe4279c08d7f633c70ba3b1296a0a08c962e54387069d6140b1e04dd coincide con APK oficial v1.0.7 descargado. Se recompila desde commit de código dd457e542 para trazabilidad final. S23 tiene producción io.kitsux.app 1.0.7 versionCode 8 y debug separado; antes de actualizar se registró biblioteca/categorías Original/Romance y títulos Youjo Senki Movie, Slime Movie, Overlord IV y Mushoku Part 2. Captura release-107-library.png. No borrar datos ni desinstalar. Actualización física 1.0.7→1.1.0 completada con adb install --no-streaming -r sin desinstalar: S23 conserva Original/Romance y los seis títulos visibles de la biblioteca. Captura release-110-library.png; firstInstallTime conserva 2026-06-22. La preferencia previa de Manga deshabilitado se conserva, así que el navbar de producción tiene cinco destinos; debug con Manga habilitado tiene seis. PR de preparación https://github.com/richtunic/Kitsu-X/pull/2 en borrador. CI inicial falló antes de build porque Dependency Review no está disponible en el repo; ese paso se activa con variable DEPENDENCY_REVIEW_ENABLED=true cuando el servicio esté configurado. No se omiten compilación/formato/pruebas. GitGuardian pasó. No hay secretos de firma configurados en GitHub; build firmado localmente usando clave existente, la automatización de tags requiere configurar sus secretos. Queda adjuntar APK final al borrador release; no dar por validada descarga completa de Anime ni aislamiento simultáneo de retry por estas pruebas.


Cierre de formato: la CI detectó infracciones heredadas en LibraryPreferences y tres reglas no autoformateables de tokens/layout y condiciones de categorías. Se corrigen nombres de dos constantes locales y saltos/espacios en seis archivos, sin cambios de lógica. spotlessApply/spotlessCheck globales pasan en /tmp/kitsux-110-full-format-final.log. Borrador release https://github.com/richtunic/Kitsu-X/releases/tag/untagged-1dda75b175e45a6d30f4 ya creado con un único APK firmado; se sustituirá por el artefacto recompilado del último commit de código. Borrador PR #2 incluye README; no fusionado/publicado como estable. APK previo SHA 8824208cfe2d233e82e0062ea0f8fc08e3919a106167946df47571461caed2fc instalado y cotejado; pantalla restaurada a 120000 ms. Pendientes de estabilidad siguen Anime completo y varias descargas simultáneas. No afirmar CI final verde hasta concluir el nuevo run.

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

## 2026-10-05: Migraciones y navegación flotante sin franja

Explorar incorpora una cuarta página Migraciones, con filtro de fuentes y grupos plegables Anime/Manga usando los controles existentes de selección, orden y ayuda. Se retira el acceso duplicado del menú y la pantalla separada de migración. La guía español/inglés refleja cuatro páginas. Las operaciones de migración y sus confirmaciones no cambian.

Home deja de recortar el contenido por el alto de la barra inferior; mantiene únicamente el padding superior/lateral y distribuye su reserva inferior mediante `LocalFloatingNavigationPadding` a los Scaffold hijos. Las listas conservan espacio al final para alcanzar el último elemento, pero dibujan bajo la barra. Descubrir pasa su reserva inferior al contentPadding del LazyColumn, en vez de aplicar un recorte exterior. Inicio recibe la reserva como contentPadding del LazyColumn; Más también aplica el padding a la lista, en lugar de recortar el contenedor. La píldora mantiene color sólido y se retira su sombra negra de 10 dp. El rail conserva el espacio lateral y la reserva inferior es cero sin barra.

Kotlin/APK, formato dirigido, firma y diff pasaron. APK final instalado sin borrar datos en S23 Ultra. Pestaña y desplegable Manga comprobados con ManhwaWeb y cuatro obras; capturas `migraciones-verified.png` y `migraciones-manga-verified.png` en `/tmp/kitsux-issue1-device`. No se ejecutó una migración real. La comprobación final del contenido detrás de la barra queda pendiente: el móvil cambió de app durante las interacciones y se detuvieron los toques para no afectar otras aplicaciones. Revisar Inicio/Descubrir con portadas desplazándose bajo los márgenes de la píldora y el último elemento alcanzable; rail/rotación no probados físicamente. Sin dependencias, secretos, permisos ni esquemas nuevos.

## 2026-10-05: Explorar en tres páginas deslizables

Por instrucción del usuario, Explorar reúne Descubrir, Buscar y Extensiones. Descubrir conserva filas de temporada, estrenos y tendencias sin formulario. Buscar concentra consulta global con selector Anime/Manga y grupos desplegables de fuentes instaladas; abre directamente cada catálogo y conserva novedades, pin, opciones y filtros de fuentes. Extensiones comparte filtro por nombre, acceso visible a repositorios y dos grupos desplegables con los controles existentes de instalación, actualización, confianza y permisos. Migración permanece como herramienta desde el menú de Extensiones. Más > Fuentes/Extensiones lleva a las páginas correspondientes; los enlaces anteriores siguen funcionando. Reseleccionar Explorar vuelve a Descubrir, sin abrir búsqueda de anime.

Se retiraron los prototipos de diálogo y pantalla separada de herramientas de esta tarea. La guía en español/inglés explica las tres páginas. Sin dependencias, cambios de base de datos, credenciales ni extractores. Compilación Kotlin/APK, Spotless de archivos afectados, XML/claves únicas, firma y diff pasaron. En S23 Ultra se verificaron las tres pestañas, grupos plegables y acceso directo a AnimeFLV e InManga; este último cargó títulos. Capturas en `/tmp/kitsux-issue1-device/buscar-tres-paginas.png` y `search-expanded-manga.png`. Durante búsqueda global Manga se reprodujo un cierre por `NoClassDefFoundError: okhttp3.CompressionInterceptor` en una extensión instalada. Ambos buscadores ahora capturan `LinkageError` por fuente para conservar resultados de las demás; no se cambia la dependencia HTTP ni se actualizan paquetes automáticamente. Tras instalar la corrección, buscar Naruto en Manga devolvió resultados de varias fuentes y la actividad permaneció activa sin cierre. El filtro de extensiones encontró InManga; repositorios abrió la lista con Añadir. No se instalaron/actualizaron extensiones ni se añadieron repositorios durante la revisión. El estado vacío del gestor se compactó y se comprobó en el APK final instalado, con InManga filtrado (`extensiones-filtro-final.png`). Swipe de Descubrir a Buscar comprobado; las filas de portadas conservan su scroll horizontal. No se comprobó render inglés ni instalaciones reales. Probar swipe, selección, desplegables, catálogo, búsqueda, filtro de extensiones, repositorios y Back sin instalar/actualizar extensiones durante la revisión.

## 2026-10-05: bienvenida alineada con la gran actualización

El paso `RecommendationsStep` se sustituye por `DiscoveryStep`, informativo: Inicio reúne biblioteca/progreso y Explorar > Descubrir ofrece temporada, estrenos y tendencias con búsqueda en fuentes instaladas. Se quita de la guía el interruptor de recomendaciones personalizadas; no se modifica su preferencia heredada ni se fuerza una elección. El banner se describe como contenido de la biblioteca, y la organización como sugerencia de categorías existentes en modo Automática. La guía explica Más > Extensiones y conserva configuración de contenido, banner y permisos. Los botones Omitir/Comenzar usan recursos traducidos.

Textos actualizados en base inglés y español. XML válido con claves únicas; no hay referencias Kotlin a los recursos de recomendaciones retirados. Spotless de los Kotlin afectados, `:app:compileDebugKotlin :app:assembleDebug --offline`, firma APK y `git diff --check` pasaron. APK debug instalado con `adb install -r` en S23 Ultra sin borrar datos. La revisión visual está pendiente: el dispositivo está bloqueado y se solicitó al usuario desbloquearlo. Revisar desde Más > Ajustes > Avanzado > Guía para principiantes sin reiniciar el onboarding ni cambiar preferencias. El render inglés tampoco se ha comprobado en teléfono.

## 2026-10-05: Descubrir, carga progresiva y caché de ambos proveedores

`ExploreScreenModel.refresh` publica cada fila al terminar, en vez de esperar las tres consultas secuenciales. Las consultas Jikan se inician a 0/1.2/2.4 s y pueden solaparse; cada llamada tiene límite total de 4 s antes del respaldo AniList (límite de 10 s). Se consulta primero el caché válido de Jikan y AniList, ambos de 6 horas, y un reintento forzado los omite. Se conserva el contenido previo si una sección falla, se impiden refrescos simultáneos desde la UI y se propaga cancelación. Las respuestas Jikan se validan antes de guardarse. No cambian los filtros de temporada ni las consultas de los proveedores.

`ExploreDiscover` usa claves estables en sus filas para conservar estado/desplazamiento al publicarlas por separado; muestra una barra discreta mientras quedan peticiones, sin bloquear el contenido disponible. Se mantienen las búsquedas en fuentes y los errores con Reintentar.

Validación: tres pruebas de ScreenModel pasaron (caché AniList sin red, publicación parcial con sección lenta y separación de peticiones, reintento fallido conserva filas); Spotless de los archivos afectados, build Kotlin/APK, firma y diff pasaron. Se instaló el APK debug del worktree correcto en S23 Ultra / Android 16. Primera lectura de UI a 3.03 s tras tocar Explorar ya tenía Temporada actual con títulos y Próximos estrenos; al desplazarse se confirmaron títulos de estrenos y tendencias. Capturas en `/tmp/kitsux-issue1-device/discovery-fast.png` y `discovery-upcoming-trending.png`. Este muestreo incluye el coste de UIAutomator y no demuestra carga en frío sin caché ni un tiempo universal. Desde el Mac, dos consultas Jikan no respondieron en 8 s; AniList dio HTTP 200 en 1.50 s. La red puede variar. No se borraron datos del teléfono.

## 2026-10-05: barra de progreso integrada en las portadas de Continuar

`MediaProgressCard` dibuja el progreso dentro de la portada, con margen de 6 dp, grosor de 6 dp y extremos redondeados. La portada usa radio de 12 dp en las cuatro esquinas; la etiqueta del episodio/capítulo se levanta para no invadir la barra. El título conserva su área inferior. Se elimina la franja rectangular externa de 3 dp. Se mantiene el progreso y los callbacks existentes, sin tocar datos, red ni navegación. Validación: Spotless del archivo, `:app:compileDebugKotlin`, `:app:assembleDebug`, firma APK y `git diff --check` pasaron. APK 1.0.7-44 reinstalado; captura física del S23 Ultra confirma barras dentro de las portadas, redondeadas y más gruesas (`/tmp/kitsux-issue1-device/progreso-integrado.png`). No se añadieron pruebas para este cambio exclusivamente visual.

## 2026-10-05: issue #1 sobre la base de diseño correcta

El APK `1.0.7-27` instalado inicialmente para probar el issue #1 se compiló desde el checkout principal `main` y reemplazó el APK debug `1.0.7-43` de la gran actualización. Por eso el teléfono mostró el diseño anterior; los commits de diseño no se borraron. La base visual vigente es ESTE worktree, `/Users/richtunic/.codex/worktrees/gran-actualizacion/KitsuX`, rama `codex/gran-actualizacion`, HEAD `cc264d6aa` antes del parche.

Aquí se aplica únicamente la retirada de `IgnoreGzipInterceptor` y `BrotliInterceptor` del cliente compartido y se copia `NetworkHelperTest` con las dos pruebas de contrato Keiyoushi/gzip HTTP. No copiar Home ni las pantallas de biblioteca desde `main`: reemplazarían componentes del rediseño. Los cambios locales del checkout principal se conservan por separado.

Validación: ambas pruebas, `:app:compileDebugKotlin`, `:app:assembleDebug` y firma APK pasaron. Se reinstaló con éxito `io.kitsux.app.dev` 1.0.7-44 sin borrar datos. Captura del S23 Ultra confirma Inicio con portadas compactas, Continuar leyendo, Añadidos recientemente y barra flotante de cinco destinos. Captura local: `/tmp/kitsux-issue1-device/diseno-restaurado-inicio.png`. InManga volvió a cargar populares con títulos y portadas en el APK de diseño nuevo, sin cierre registrado por AndroidRuntime; captura `/tmp/kitsux-issue1-device/inmanga-diseno-nuevo.png`. La pestaña Descubrir está presente, pero la carga de sus filas remotas no quedó verificada. Las pruebas más amplias previas del issue usaban el APK de main. Producción `io.kitsux.app` no se reemplaza.

## Explorar: leyenda retirada, 2026-09-29

Por petición del usuario, Descubrir ya no muestra la leyenda de procedencia Jikan/AniList encima de las filas. Se eliminó el estado `usedAniListFallback` que solo alimentaba ese texto. Permanecen las consultas, el respaldo AniList, la búsqueda en fuentes instaladas y el error con Reintentar cuando una sección realmente falla. Kotlin y APK debug compilaron offline; se instaló `io.kitsux.app.dev` 1.0.7-43 sin borrar datos. Una captura del S23 Ultra confirmó las filas de temporada y próximos estrenos con portadas y sin leyenda.


## Explorar: respaldo AniList para Jikan, 2026-09-29

`ExploreScreenModel` mantiene las tres consultas Jikan y, ante errores de conexión, HTTP 429 o 5xx, consulta AniList GraphQL para temporada actual (`RELEASING`), próximo trimestre (`NOT_YET_RELEASED`) y tendencias (`TRENDING_DESC`). El trimestre se calcula en la zona del dispositivo y el caché se separa por proveedor, sección y temporada; se guarda solo tras decodificar la respuesta. Las tarjetas siguen abriendo búsqueda en fuentes locales. Se verificaron respuestas HTTP 200 directas para las tres consultas AniList, con títulos e imágenes; `:app:compileDebugKotlin` y `:app:assembleDebug --offline --quiet` pasaron. Se instaló `io.kitsux.app.dev` 1.0.7-42 en S23 Ultra sin borrar datos y una captura de Explorar confirmó temporada actual y próximos estrenos con imágenes; otra captura de Inicio confirmó imagen en hero y Continuar viendo/leyendo. La integración no usa tokens nuevos. Pendiente: recorrido táctil de Tendencias, Añadidos recientemente, reintento y cambio de temporada.


## Inicio: portadas grises, 2026-09-29

El usuario reportó portadas grises en Continuar leyendo, Añadidos recientemente y hero mientras Biblioteca Anime/Manga sí las mostraba. Inicio entregaba `thumbnailUrl` directamente a Coil; Biblioteca entrega `AnimeCover`/`MangaCover` a sus fetchers, que resuelven caché, portada personalizada y cabeceras de la fuente. Las filas de Inicio ahora usan esos modelos mediante `KitsuXMediaItem.coverData`. El hero conserva `backgroundUrl` cuando carga y cambia a `coverData` si falta o falla. `:app:compileDebugKotlin` y `:app:assembleDebug --offline --quiet` pasaron; APK debug 1.0.7-41 generado. No hubo dispositivo ADB ni servicio mDNS disponible, así que no se instaló ni se comprobó visualmente esta corrección. Siguiente acción: conectar S23 Ultra, instalar APK sin borrar datos y revisar las obras reportadas en Inicio y Biblioteca.


## Diagnóstico Jikan, 2026-09-29

Se verificó el contrato oficial de `jikan-me/jikan-rest`: `routes/web.v4.php` y `storage/api-docs/api-docs.json` todavía definen `/v4/seasons/now` y `/v4/seasons/upcoming`. La implementación de `ExploreScreenModel` usa estas rutas correctas. En la consulta directa, `/v4/anime/1` y `/v4/top/anime?limit=1` devolvieron HTTP 200; `/v4/seasons/now?limit=1`, `/v4/seasons/upcoming?limit=1` y `/v4/top/anime?filter=airing&limit=15` devolvieron HTTP 504 con `BadResponseException` indicando que Jikan no pudo conectar con MyAnimeList. El repositorio oficial mantiene abierto el issue #612 sobre errores 504. No hay evidencia de cambio de API ni de un defecto de serialización en KitsuX; la causa observable está en la instancia pública o su conexión ascendente. El S23 Ultra ya no figuró en ADB durante esta revisión, por lo que la comprobación actual fue desde el equipo. No sustituir «Temporada actual» por el ranking general: clasificaría obras incorrectamente. Siguiente verificación: repetir `/seasons/now` cuando Jikan se recupere y comprobar la lista en el teléfono.


## Gran actualización 2026-09-29: Jikan temporada actual

La app sí consulta `https://api.jikan.moe/v4/seasons/now?limit=15`. El 2026-09-29, `curl` local y en S23 Ultra recibieron HTTP 504 con el mensaje de Jikan de que no pudo conectar con MyAnimeList. Explorar ahora muestra «Jikan no está disponible temporalmente» ante respuestas 5xx y conserva Reintentar. Kotlin y APK debug pasaron offline; `io.kitsux.app.dev` 1.0.7-39 se instaló y el mensaje se comprobó visualmente. Pendiente: comprobar que los títulos de temporada aparecen cuando Jikan vuelva a responder; no confundir esta indisponibilidad con falta de obras en las fuentes instaladas.


## Gran actualización 2026-09-29: navegación flotante

La barra inferior de Home ahora es una píldora con margen de 12 dp, separación inferior de 10 dp y esquinas de 24 dp. Conserva los cinco destinos y sus badges; el rail no cambia. Kotlin y APK debug compilaron offline. Se instaló `io.kitsux.app.dev` 1.0.7-38 en S23 Ultra y una captura confirmó forma, margen y etiquetas. Falta tocar cada destino, probar Back y rotación; la captura mostró un error de carga de Jikan en Explorar, ajeno a la barra.


## Gran actualización 2026-09-29: densidad visual de portadas

La referencia del usuario pide portadas verticales tipo Netflix y más obras visibles. Inicio usa 108 dp en Continuar, Novedades y Añadidos; la biblioteca calcula columnas automáticas con ancho mínimo de 100 dp en móvil y 132 dp en ventanas amplias. Los valores manuales de columnas no cambian. Kotlin compiló offline. Pendiente: revisar visualmente y tocar Inicio, Anime y Manga en el S23 Ultra; verificar títulos largos, badges, barras, menús y accesibilidad antes de dar por cerrado el ajuste.


## Gran actualización 2026-09-29: Fase 7, herramientas

Más ofrece accesos a Fuentes, Extensiones y Tracking además de Descargas, Historial, Actualizaciones, Categorías, almacenamiento y Ajustes. Fuentes/Extensiones cambian a la pestaña visible correspondiente de Explorar; el índice se calcula con los tipos Anime/Manga habilitados. Se conservan los gestores, instaladores, estados y búsquedas existentes. Kotlin compiló offline. Pendiente: recorrido táctil de cada acceso, Back, filtros y estados de instalación en teléfono.


## Gran actualización 2026-09-29: Fase 6, ficha (avance)

Las fichas de Anime y Manga reutilizan sus cabeceras, acciones, listas y `TwoPanelBox` existente. Los CTA principales se localizan en base/español; Manga usa icono de libro en móvil y tablet. La lista de episodios dibuja una barra pequeña cuando existe progreso positivo y duración conocida, manteniendo el texto de progreso. `:app:compileDebugKotlin --offline` pasó. Pendiente: diseño de tabs Información/Relacionado y validación visual/táctil de ficha, progreso y dos paneles en teléfono/tablet.


## Gran actualización 2026-09-29: Fase 5, Explorar (avance)

Explorar tiene una pestaña inicial Descubrir que consulta `/v4/seasons/now`, `/v4/seasons/upcoming` y `/v4/top/anime` de Jikan al abrirse. Muestra filas de temporada, próximos estrenos y tendencias, además de géneros filtrables. El buscador distingue Anime y Manga; tocar una obra externa abre búsqueda global de anime en las fuentes instaladas. La UI aclara que los metadatos no garantizan disponibilidad. Hay error y reintento para secciones fallidas; las consultas se espacian 1.2 s.

Se mantienen las pestañas de Fuentes, Extensiones y migración. El destino de Extensiones se calcula según pestañas visibles y el buscador de cada pestaña usa su tipo explícito. Kotlin y APK debug compilaron offline. Falta prueba de Jikan real, disponibilidad de fuentes, tablet y teléfono. No se añadió acción «Añadir» directa porque una obra de Jikan todavía no tiene fuente local resuelta.


## Gran actualización 2026-09-29: Organización 2.0, avance seguro

El alta de anime/manga ya no crea categorías por cada género de Jikan. Si la preferencia está activa, el modelo busca como máximo una categoría existente con nombre coincidente; en otro caso conserva el flujo de categoría predeterminada o selección manual. Abrir detalles de una obra favorita ya no modifica sus categorías. Guardar una selección manual espera primero al alta de la obra y luego escribe la asociación, sin aplicar Jikan encima. La preferencia se muestra en Ajustes > Biblioteca; onboarding y textos describen la conducta actual.

El selector de Ajustes > Biblioteca ofrece Automática, Preguntar siempre, Categoría predeterminada y Sin categoría. La selección manual usa el diálogo actual; al confirmar, el tracking se vincula después de guardar la obra y sus categorías, y cancelar no abre tracking. En modo Automática, el switch Jikan controla si se busca coincidencia externa; se conserva el fallback de categoría predeterminada. Kotlin compiló offline.
Si todavía no hay categorías, el diálogo ofrece añadir la obra sin categoría o abrir el editor de categorías.

Pendiente para completar Fase 4: categoría principal y etiquetas persistentes, una hoja de alta renovada, y migración de asociaciones antiguas con backup/rollback probado sobre datos de ejemplo. No ejecutar una conversión automática de las categorías actuales: no hay procedencia fiable para distinguir las creadas por Jikan de las personales. Falta dispositivo.
El contrato y los casos de rollback están en `docs/ORGANIZACION_2_MIGRACION.md`.


## Gran actualización 2026-09-29: Fase 3, Biblioteca

Anime y Manga tienen una pestaña virtual «Todo» que toma las asociaciones existentes y deduplica por ID. No se modifican tablas ni categorías. Una bandera de preferencia desplaza una vez los índices guardados mayores que cero para conservar la pestaña previa. «Todo» refresca la biblioteca completa y usa el orden global. Ambas pantallas muestran chips rápidos Todo/Viendo o Leyendo/Pendientes/Completados basados en recuentos reales; los filtros avanzados y la elección persistida de cuadrícula/lista se conservan. La cuadrícula automática calcula 2–8 columnas según el ancho visible, y la preferencia explícita de columnas sigue mandando.

`app:compileDebugKotlin` y `app:assembleDebug` pasaron offline. Faltan revisión táctil y de rotación en teléfono/tablet. El siguiente trabajo es Organización 2.0, empezando por impedir que Jikan cree múltiples categorías al añadir una obra y por preservar la elección manual.


## Gran actualización 2026-09-29: Fase 2, Inicio

En el worktree `codex/gran-actualizacion`, Inicio obtiene hero, continuar, novedades y añadidos recientemente desde biblioteca e historial locales. El hero se pagina manualmente, usa artwork de fondo cuando existe y muestra un indicador breve. Las novedades muestran obras pendientes de los últimos siete días y abren detalles. Si no hay biblioteca, el estado vacío ofrece Explorar; si los filtros ocultan todo, lo explica. Se incorporó aquí la corrección de progreso de anime presente en el checkout principal, sin tocar ese checkout.

Se quitó la inicialización de Jikan al arrancar la app y sus recomendaciones de Inicio. Los archivos de Jikan siguen disponibles para la futura fase de Explorar. `:app:compileDebugKotlin` y `:app:assembleDebug` pasaron en modo offline; `git diff --check` pasó. No hay prueba en teléfono ni tablet todavía, por petición del usuario. Siguiente paso: revisar Inicio, navegación, Back, hero, novedades, filtros y continuación real en teléfono; después abordar Fase 3 Biblioteca.


## Gran actualización 2026-09-29: componentes de Foundations

En el worktree `codex/gran-actualizacion`, Home usa `KitsuXLayoutTokens` para breakpoints y ancho del hero. `HomeLoadingSkeleton` reemplaza la carga con spinner; `MediaProgressCard` se usa en Continuar viendo y leyendo, con menú de continuar/quitar y sin icono de reproducción para manga. Las acciones existentes y los datos no cambiaron.

`./gradlew :app:compileDebugKotlin --offline --quiet` y `:app:assembleDebug --offline --quiet` pasaron. `:app:spotlessKotlinCheck --offline` no corrió por faltar `ktlint-cli:1.5.0` en caché. `adb devices -l` no mostró equipos y no hay AVD. Antes de cerrar Foundations hay que revisar en móvil/tablet el menú, las barras de progreso, rotación, split-screen y Back; luego continuar con densidad de grid y componentes de biblioteca sin migración de datos. Ver `docs/GRAN_ACTUALIZACION.md`.


## Gran actualización 2026-09-29: Foundations en progreso

En `codex/gran-actualizacion`, la navegación principal ahora presenta Inicio, Anime, Manga, Explorar y Más; Historial/Actualizaciones viven en Más, con contador en ese icono. Home usa rail desde `600dp` de ancho actual de ventana. La antigua preferencia de navegación queda oculta, no borrada. Se añadió la coordenada Maven Central de FlexibleAdapter en un commit separado para desbloquear el build del commit base.

Validación: `:app:compileDebugKotlin --offline` pasó tras el ajuste final del badge y `:app:assembleDebug --offline` pasó antes de ese ajuste. ADB no listó dispositivos. No dar por cerrada Foundations ni avanzar a Home nuevo hasta probar navegación/Back en móvil y tablet y completar tokens, cards y skeletons. El mapa detallado está en `docs/GRAN_ACTUALIZACION.md`.


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
