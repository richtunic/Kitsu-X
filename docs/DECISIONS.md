# DECISIONS

## 2026-10-06: preparación 1.1.0 y distribución ARM64

Usuario autoriza subir a 1.1.0, actualizar README y notas con funcionalidades/correcciones actuales, y distribuir exclusivamente ARM de 64 bits. Evaluación: viable; se excluyen explícitamente sistemas ARM32 y x86/x86_64. Fases: 1 versionado/documentación/ABI; 2 release firmado y prueba de actualización; 3 preparación en GitHub. Solo worktree codex/gran-actualizacion; no mezclar checkout principal antiguo.

app/build.gradle.kts: versionName 1.1.0, versionCode 9; splits solo arm64-v8a, sin universal. AGP rechaza duplicar filtros en ndk y splits, por lo que se conserva únicamente splits. Firma lee KITSUX_KEYSTORE_PATH, KITSUX_STORE_PASSWORD, KITSUX_KEY_ALIAS y KITSUX_KEY_PASSWORD; se retiran credenciales embebidas. Para build local se reutiliza el keystore ignorado existente del checkout principal, sin copiar/commitear secretos. Comparar certificado con APK oficial v1.0.7 antes de instalar.

README español/inglés renovado: Inicio/progreso, Descubrir/caché, Buscar/Extensiones/Migraciones, Recientes/modos/búsqueda, descargas/errores/retry, paleta y bienvenida. Requisitos Android 8+ con sistema ARM64; un procesador 64-bit con sistema 32-bit no basta. Se retiran promesas universales sobre bloqueos/Cloudflare y funciones exclusivas antiguas del texto. docs/releases/1.1.0.md contiene notas públicas sin menciones de generación. Script de enlaces conserva solo asset ARM64. Workflow de tags apunta a richtunic/Kitsu-X, un solo asset y notas versionadas; mantiene draft=true. CI usa Java 21 para ejecutar dependencia FlexibleAdapter classfile 65, sin cambiar target JVM 17 ni minSdk.

Validación hasta ahora: 27 pruebas dirigidas de app en release y 11 de red pasan, XML previo/formato dirigido/diff, sintaxis Python y YAML correctos. Logs /tmp/kitsux-110-release-tests.log y kitsux-110-release-build.log. Release minificado firmado pasó, APK 63442509 bytes; aapt confirma io.kitsux.app / versionCode 9 / 1.1.0 / minSdk 26 y ZIP solo lib/arm64-v8a. Certificado SHA256 b18fa525fe4279c08d7f633c70ba3b1296a0a08c962e54387069d6140b1e04dd coincide con APK oficial v1.0.7 descargado. Se recompila desde commit de código dd457e542 para trazabilidad final. S23 tiene producción io.kitsux.app 1.0.7 versionCode 8 y debug separado; antes de actualizar se registró biblioteca/categorías Original/Romance y títulos Youjo Senki Movie, Slime Movie, Overlord IV y Mushoku Part 2. Captura release-107-library.png. No borrar datos ni desinstalar. Actualización física 1.0.7→1.1.0 completada con adb install --no-streaming -r sin desinstalar: S23 conserva Original/Romance y los seis títulos visibles de la biblioteca. Captura release-110-library.png; firstInstallTime conserva 2026-06-22. La preferencia previa de Manga deshabilitado se conserva, así que el navbar de producción tiene cinco destinos; debug con Manga habilitado tiene seis. PR de preparación https://github.com/richtunic/Kitsu-X/pull/2 en borrador. CI inicial falló antes de build porque Dependency Review no está disponible en el repo; ese paso se activa con variable DEPENDENCY_REVIEW_ENABLED=true cuando el servicio esté configurado. No se omiten compilación/formato/pruebas. GitGuardian pasó. No hay secretos de firma configurados en GitHub; build firmado localmente usando clave existente, la automatización de tags requiere configurar sus secretos. Queda adjuntar APK final al borrador release; no dar por validada descarga completa de Anime ni aislamiento simultáneo de retry por estas pruebas.


Cierre de formato: la CI detectó infracciones heredadas en LibraryPreferences y tres reglas no autoformateables de tokens/layout y condiciones de categorías. Se corrigen nombres de dos constantes locales y saltos/espacios en seis archivos, sin cambios de lógica. spotlessApply/spotlessCheck globales pasan en /tmp/kitsux-110-full-format-final.log. Borrador release https://github.com/richtunic/Kitsu-X/releases/tag/untagged-1dda75b175e45a6d30f4 ya creado con un único APK firmado; se sustituirá por el artefacto recompilado del último commit de código. Borrador PR #2 incluye README; no fusionado/publicado como estable. APK previo SHA 8824208cfe2d233e82e0062ea0f8fc08e3919a106167946df47571461caed2fc instalado y cotejado; pantalla restaurada a 120000 ms. Pendientes de estabilidad siguen Anime completo y varias descargas simultáneas. No afirmar CI final verde hasta concluir el nuevo run.

Entrega final: APK release 1.1.0 / versionCode 9 / solo arm64-v8a, 63442509 bytes, certificado de 1.0.7 conservado. Compilado desde commit de código 943e7d229; SHA256 1543d84fb9871171a57ffa5785df1b32fd3049753bdd255998ae3e45dfe3d3dc. Instalado en io.kitsux.app y SHA remoto/local cotejado; timeout 120000 ms restaurado. Archivo local releases/1.1.0/Kitsu-X-v1.1.0-arm64-v8a.apk y mapping.txt preservados fuera de Git. Borrador release ya contiene un único APK con digest coincidente y notas con su SHA. README y cambios se revisan en PR #2, aún borrador; no publicar estable hasta cerrar pendientes de Anime/simultaneidad. Se limita ignore /releases/ al directorio raíz para versionar docs/releases/1.1.0.md; el patrón anterior ocultaba también las notas. El commit final de documentación no altera código compilado. CI de 943e7d229 superó formato y continúa build/pruebas: no afirmar éxito final remoto aún.

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

Fecha: 2026-10-05
Decisión: Exponer Migraciones como cuarta página de Explorar y superponer la píldora inferior al contenido, manteniendo reserva de scroll.
Motivo: El usuario pidió el mismo formato agrupado para migración y retirar la capa negra detrás de la barra flotante. La reserva aplicada al contenedor raíz impedía dibujar debajo de la barra.
Impacto: La migración conserva selección/orden/confirmaciones, con un filtro de presentación. La reserva inferior se comunica a los Scaffold hijos mediante un local cuyo valor por defecto es cero; el rail no cambia. La superficie sólida de la barra se conserva y no se utiliza una barra transparente.

---

Fecha: 2026-10-05
Decisión: Mantener fuentes accesibles dentro de Explorar y consolidar navegación en Descubrir, Buscar y Extensiones.
Motivo: Ocultar fuentes únicamente en Más dificultó el acceso a catálogos. El usuario pidió explícitamente páginas laterales con búsqueda, selector y grupos desplegables para explorar fuentes y administrar extensiones.
Impacto: Se reutilizan modelos de fuentes y contenido de gestión de extensiones, sin modificar su motor ni los instaladores. Las fuentes son entradas de catálogo, y las extensiones son paquetes administrables; sus acciones se mantienen en páginas distintas. Los grupos son plegables y la gestión conserva repositorios, filtros, confirmaciones y migración. La bienvenida debe reflejar este recorrido. Se captura únicamente `LinkageError` además de los errores de consulta existentes para que una extensión binariamente incompatible falle por fuente, sin cerrar toda la búsqueda; se verificó la regresión en teléfono con resultados de otras fuentes. No se ocultan errores fatales de memoria ni se sustituye el motor HTTP.

---

Fecha: 2026-10-05
Decisión: La bienvenida explica Inicio y Descubrir en lugar de ofrecer recomendaciones personalizadas basadas en historial.
Motivo: Inicio ahora utiliza biblioteca e historial locales y Descubrir presenta metadatos de temporada, próximos estrenos y tendencias. El antiguo interruptor prometía un comportamiento que ya no corresponde a esta interfaz.
Impacto: Se sustituye únicamente ese paso por información, se actualizan descripciones en español/inglés y se conserva la configuración real de banner, tipos de contenido, organización y permisos. No se borran preferencias anteriores ni se fuerza a repetir la bienvenida tras actualizar.

---

Fecha: 2026-10-05
Decisión: Cargar Descubrir de forma progresiva, consultar el caché de ambos proveedores primero y limitar la espera a Jikan antes del respaldo AniList.
Motivo: La implementación anterior hacía las tres consultas secuencialmente, retenía todos los resultados hasta la última y podía esperar el límite compartido de dos minutos antes de leer siquiera el caché de respaldo. Dos sondeos Jikan no respondieron en 8 s, mientras AniList respondió en 1.50 s.
Impacto: Cada fila aparece al terminar; los inicios Jikan se espacian 1.2 s para respetar límites y la espera por llamada queda en 4 s. AniList se limita a 10 s; los resultados previos sobreviven a fallos. Se conservan las consultas y clasificación existentes y se añaden claves UI estables. La prueba física mostró contenido en el primer muestreo a 3.03 s, con caché/red disponibles, no como promesa de latencia universal.

---

Fecha: 2026-10-05
Decisión: Integrar la barra de progreso de MediaProgressCard dentro de la imagen, con grosor de 6 dp y esquinas redondeadas.
Motivo: La franja externa de 3 dp formaba una base rectangular bajo la portada redondeada y rompía su continuidad visual. El usuario pidió integración en la portada y mayor tamaño.
Impacto: Se ajusta únicamente la presentación de Continuar viendo/leyendo; progreso, título, navegación y menú conservan sus comportamientos. Validar mediante captura del S23 Ultra antes de considerar el ajuste visual comprobado.

---

Fecha: 2026-10-05
Decisión: Aplicar y validar el arreglo del issue #1 en el worktree de la gran actualización, en vez de entregar un APK de main.
Motivo: El diseño reciente vive en `codex/gran-actualizacion`; main conserva otra base y cambios locales independientes. Ambos APK debug tienen el mismo paquete, por lo que instalar uno sustituye al otro y puede revertir visualmente la app aunque no se borre código.
Impacto: Se retiran únicamente los interceptores incompatibles del cliente compartido, manteniendo UI, cookies, DoH y Cloudflare. Las pruebas de contrato y gzip HTTP no requieren dependencias nuevas. Verificar rama, HEAD y origen del APK antes de instalar o preparar una release; no usar el APK del checkout principal para dar por preservado el rediseño.

---

Fecha: 2026-09-29
Decisión: Retirar la leyenda persistente de procedencia de datos en Descubrir.
Motivo: El usuario prefiere que la interfaz muestre directamente las obras; la leyenda ocupa espacio y no cambia la acción disponible al tocar una tarjeta.
Impacto: Jikan y AniList mantienen sus roles de proveedor inicial y respaldo. Si ambas fuentes fallan, el error y Reintentar siguen visibles.

---

Fecha: 2026-09-29
Decisión: Mantener Jikan como proveedor inicial de Explorar y usar AniList como respaldo para temporada, próximos estrenos y tendencias ante fallos de red, 429 o 5xx.
Motivo: La conexión Jikan-MyAnimeList no puede repararse desde KitsuX. AniList ya se utiliza en el proyecto y sus consultas GraphQL públicas devolvieron datos reales para las tres secciones.
Impacto: No se agregan claves ni dependencias. Tocar una obra sigue abriendo la búsqueda en fuentes instaladas para comprobar disponibilidad. La equivalencia editorial entre catálogos no es exacta.

---

Fecha: 2026-09-29
Decisión: Cargar las portadas de Inicio con los modelos `EntryCover` existentes y mantener el banner del hero como imagen preferida con respaldo en la portada.
Motivo: Las URLs directas de Inicio omiten la caché local, las portadas personalizadas y las cabeceras de fuentes que sí usa Biblioteca; por eso la misma obra podía verse gris solo en Inicio.
Impacto: No cambian los datos ni las fuentes. Falta verificar con el teléfono las portadas específicas reportadas por el usuario.

---

Fecha: 2026-09-29
Decisión: Mantener Jikan `/v4/seasons/now` para la temporada actual y distinguir fallos 5xx en la UI.
Motivo: La petición directa en el teléfono devolvió HTTP 504 de Jikan por falta de conexión con MyAnimeList; cambiar el endpoint o mostrar títulos de otra clasificación como temporada actual sería engañoso.
Impacto: Explorar informa la indisponibilidad y conserva Reintentar. La carga real de temporada depende de que Jikan vuelva a responder.

---

Fecha: 2026-09-29
Decisión: Dar apariencia flotante a la barra inferior dentro del slot de Scaffold en ancho compacto.
Motivo: El usuario busca una navegación más ligera; mantener el slot conserva los insets y evita cubrir listas y controles inferiores.
Impacto: No cambian rutas ni estado de pestañas; el rail permanece para ventanas medianas y grandes. Falta recorrido táctil y rotación.

---

Fecha: 2026-09-29
Decisión: Usar portadas verticales compactas para las filas de Inicio y reducir el ancho automático de la cuadrícula de Anime y Manga.
Motivo: La referencia visual busca mostrar más obras por pantalla y las tarjetas horizontales de 180 dp ocupaban demasiado espacio para imágenes de portada.
Impacto: Se mantienen el avance, los accesos y la elección manual de columnas. La densidad y legibilidad requieren revisión en dispositivo.

---

Fecha: 2026-09-29
Decisión: Dar a Fuentes, Extensiones y Tracking accesos directos desde Más que reutilizan sus pantallas actuales.
Motivo: El PRD sitúa herramientas en Más y conservar las rutas existentes evita duplicar instalación, búsqueda y ajustes. El destino de Fuentes/Extensiones se calcula según los tipos visibles.
Impacto: No cambia contratos de extensiones, descargas ni tracking. Falta revisar táctilmente cada acceso y Back en teléfono.

---

Fecha: 2026-09-29
Decisión: Reutilizar las fichas existentes y sus paneles de tablet; corregir primero los CTA y el progreso visible sin reescribir episodios, capítulos ni motores.
Motivo: Las fichas ya reúnen cabecera, acciones, descripción, listas y dos paneles. El cambio mínimo útil es que Manga use lenguaje de lectura y que un episodio parcialmente visto muestre su progreso.
Impacto: No cambia reproducción, lectura, descargas ni esquema. Tabs de Información/Relacionado y otros cambios de jerarquía siguen pendientes de diseño y prueba táctil.

---

Fecha: 2026-09-29
Decisión: Ubicar descubrimiento Jikan en una primera pestaña de Explorar y abrir los títulos externos mediante la búsqueda en fuentes instaladas.
Motivo: Jikan aporta metadatos de temporada, próximos estrenos y tendencias, pero no confirma que un título esté disponible para reproducir. Mantener Fuentes y Extensiones como pestañas conserva las rutas actuales.
Impacto: Explorar hace consultas Jikan al abrirse, separadas por una pausa para respetar límites. El buscador de cada pestaña de fuentes/extensiones se asocia explícitamente a Anime o Manga, sin depender de la posición de la pestaña. Falta prueba de red y UI en dispositivo.

---

Fecha: 2026-09-29
Decisión: Limitar la sugerencia Jikan al alta de una obra y a una sola categoría ya existente; no reescribir asociaciones al abrir detalles ni después de una elección manual.
Motivo: La creación de una categoría por género duplicaba la organización y podía sustituir una selección explícita. Mantener las asociaciones históricas evita pérdidas hasta contar con migración y rollback probados.
Impacto: La preferencia de Jikan se expone en Ajustes de Biblioteca. Las categorías existentes y la base de datos no se alteran automáticamente; etiquetas y migración siguen pendientes.

---

Fecha: 2026-09-29
Decisión: Añadir una vista virtual «Todo» a Anime y Manga y deduplicar sus obras por ID, sin escribir nuevas relaciones de categoría.
Motivo: La relación obra-categoría existente admite varias categorías y debe conservarse. Un agregado de lectura resuelve la duplicación visual sin migrar datos.
Impacto: Los índices guardados de pestañas posteriores a la primera se desplazan una sola vez. La cuadrícula automática usa el ancho visible; los filtros rápidos actúan solo sobre la vista y los filtros avanzados siguen disponibles.

---

Fecha: 2026-09-29
Decisión: Inicio usa únicamente obras de la biblioteca para el hero y las filas de novedades. Jikan deja de inicializarse al arrancar la app; sus archivos se conservan para una futura fase de Explorar.
Motivo: El PRD separa consumo personal de descubrimiento y la biblioteca ya proporciona progreso, fechas y novedades sin red ni almacenamiento adicional.
Impacto: El hero abre detalles o continúa una obra local; las novedades abren detalles. No cambia la base de datos ni el motor de fuentes. Falta comprobar interacción y tamaños en teléfono y tablet.

---

Fecha: 2026-09-29
Decisión: Centralizar solo los tamaños de layout usados y extraer la tarjeta de progreso existente de Home antes de crear un sistema visual paralelo.
Motivo: El tema, las portadas y los grids ya tienen componentes compartidos. Reutilizarlos reduce el riesgo de duplicar estilos y conserva los temas elegidos por usuarios. Un skeleton estático evita movimiento innecesario durante la carga.
Impacto: Home mantiene sus callbacks y datos; el menú contextual ofrece continuar o quitar, y quitar abre la misma confirmación. No cambia DB ni integraciones. Queda pendiente validación visual y táctil en móvil/tablet.

---

Fecha: 2026-09-29
Decisión: Usar el ancho visible de la ventana (`600dp`) para elegir barra o rail en Home y conservar Voyager/Scaffold existentes.
Motivo: `smallestScreenWidthDp` no responde adecuadamente a pantalla dividida y el PRD requiere cinco destinos estables. Historial y Actualizaciones se mantienen accesibles desde Más; la preferencia anterior se oculta sin borrar su valor.
Impacto: La estructura principal cambia sin migración de DB. El contador de actualizaciones pasa al icono de Más. Falta validación táctil y responsive en dispositivo antes de cerrar Foundations.

---

Fecha: 2026-09-29
Decisión: Comenzar la gran actualización con inventario y rama aislada; reutilizar Voyager, Scaffold y el tema existentes durante Foundations.
Motivo: La navegación y las categorías actuales tienen consumidores y preferencias heredadas. Un cambio visual masivo o una migración de datos temprana pondría en riesgo accesos, biblioteca y backups.
Impacto: `docs/GRAN_ACTUALIZACION.md` documenta el mapa técnico y el orden de cambios. No se cambian todavía rutas, preferencias ni esquemas. Antes de fijar cinco destinos, Historial y Actualizaciones deben quedar accesibles desde Más; el rail debe basarse en el ancho actual de la ventana.

---

Fecha: 2026-08-01
Decision: Construir la bandeja de novedades desde el estado local de la biblioteca y ejecutar las actualizaciones de extensiones de forma secuencial sobre los instaladores existentes.
Motivo: La biblioteca ya conserva las fechas necesarias y el toque de las tarjetas ya resuelve el siguiente episodio o capítulo, por lo que no hace falta otra tabla ni sincronización. En extensiones, reutilizar el flujo individual mantiene la confirmación y la validación de firmas sin crear un instalador paralelo menos seguro.
Alternativas descartadas: Guardar una bandeja duplicada en base de datos (estado redundante), actualizar extensiones en paralelo (varios diálogos y carreras del instalador) y omitir la confirmación global (acción masiva fácil de activar por error).
Impacto: Home muestra únicamente novedades de hoy a los últimos seis días, agrupadas por fecha local. `Actualizar todas` procesa el catálogo completo de pendientes, no solo el resultado filtrado, y permite como máximo un lote activo por pantalla.

---

Fecha: 2026-08-01
Decision: Consolidar el resultado de cada actualizacion de biblioteca en su notificacion resumen existente.
Motivo: Los workers ya separaban contenido nuevo, omitidos y errores, pero solo notificaban el primer grupo, ocultaban los omitidos en logs y duplicaban los errores en otra notificacion. El resumen expandible permite mostrar el total nuevo y ordenar los tres grupos sin introducir almacenamiento ni coordinacion adicional.
Alternativas descartadas: Crear un servicio coordinador nuevo para fusionar anime y manga (descartado por complejidad y carreras entre WorkManager) y mantener una notificacion de error separada (descartado por duplicar el resultado y romper el orden visual solicitado).
Impacto: Anime y manga conservan notificaciones resumen independientes y sus notificaciones accionables por obra. Los registros de error siguen accesibles desde `Mostrar errores`; al ocultar contenido sensible solo se exponen contadores.

---

Fecha: 2026-06-23
Decision: Las notas de release de KitsuX se publican bilingues en GitHub, pero la app muestra solo un idioma.
Motivo: El usuario pidio commits/releases en espanol e ingles, y una experiencia de app sin duplicar texto. La app interpreta secciones markdown `## es` y `## en`: usuarios con idioma de app en espanol ven `es`; cualquier otro idioma ve `en`.
Alternativas descartadas: Mostrar siempre ambos idiomas en la app (descartado por ruido visual) o traducir dinamicamente (descartado por dependencia externa e inconsistencia).
Impacto: Cada GitHub Release debe incluir ambas secciones. Si una seccion falta, la app cae a ingles y finalmente al body completo.

---

Fecha: 2026-06-23
Decisión: Reutilizar el updater existente de Aniyomi/Tachiyomi para KitsuX en vez de portar un sistema paralelo desde Seal Fork.
Motivo: KitsuX ya tiene flujo de actualización integrado con GitHub Releases, WorkManager, notificaciones, pantalla de nueva versión, permisos de instalación y `FileProvider`. El cambio mínimo seguro es adaptar su contrato de GitHub Releases a KitsuX: release `name` como versión limpia, `tag_name` con prefijo `v` y assets APK seleccionables por ABI con fallback `universal`.
Alternativas descartadas: Portar `UpdateUtil`, `AppUpdater`, `UpdateDialog` y `UpdatePage` completos desde Seal Fork (descartado por duplicar responsabilidades y aumentar riesgo en UI/permisos).
Impacto: El sistema queda alineado con releases de `richtunic/Kitsu-X` sin agregar dependencias ni cambiar arquitectura. Los canales estable/pre-release con preferencia de usuario quedan fuera de esta fase; actualmente KitsuX usa repositorios separados para preview y estable según build type.

---

Fecha: 2026-06-20
Decisión: Mantener el namespace de código fuente "eu.kanade.tachiyomi" intacto durante la fase inicial de rebranding.
Motivo: Cambiar el namespace completo del código fuente implicaría refactorizar miles de archivos y enlaces de importación, lo que introduciría alta probabilidad de errores de compilación y podría romper la compatibilidad con el ecosistema de extensiones de Aniyomi.
Alternativas descartadas: Refactorizar todo el namespace a "io.kitsux.app" (descartado por alto riesgo de rotura y excesiva sobrecarga de código).
Impacto: Permite compilar la aplicación de forma rápida y segura, conservando la compatibilidad absoluta del motor de extensiones y base de datos, mientras que de cara al sistema operativo el App ID ("io.kitsux.app") y el nombre de la app ("KitsuX") quedan renombrados de manera limpia.


## 2026-10-06: README detallado y capturas reales

README renovado en español e inglés con funciones contrastadas con el código: Inicio/progreso, bibliotecas/categorías, las cuatro páginas de Explorar, modos de Recientes, lector/reproductor, descargas, tracking, actualizaciones y respaldos. Galería de tres PNG originales del S23 (sin fabricar UI) guardados en .github/assets/screenshots: Home/progreso, biblioteca release 1.1.0 y Recientes agrupado con búsqueda. Se indica procedencia de pruebas y variación de navegación según preferencias. No se afirma que Aniyomi esté discontinuado: repositorio no archivado y release oficial v0.18.2.1 del 2026-09-14 verificados con GitHub API. Se describe continuación independiente y se reconocen créditos. El enlace de descarga lleva a releases publicadas mientras 1.1.0 siga en borrador; se mantienen marcadores ES/EN para automatización al publicar. Validación de rutas locales y marcadores pasa, diff limpio; solo documentación/assets, sin modificar APK, código o secretos.
