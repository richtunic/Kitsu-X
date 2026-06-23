# DECISIONS

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
