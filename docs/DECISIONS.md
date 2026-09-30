# DECISIONS

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
