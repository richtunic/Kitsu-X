# DECISIONS

Fecha: 2026-06-20
Decisión: Mantener el namespace de código fuente "eu.kanade.tachiyomi" intacto durante la fase inicial de rebranding.
Motivo: Cambiar el namespace completo del código fuente implicaría refactorizar miles de archivos y enlaces de importación, lo que introduciría alta probabilidad de errores de compilación y podría romper la compatibilidad con el ecosistema de extensiones de Aniyomi.
Alternativas descartadas: Refactorizar todo el namespace a "io.kitsux.app" (descartado por alto riesgo de rotura y excesiva sobrecarga de código).
Impacto: Permite compilar la aplicación de forma rápida y segura, conservando la compatibilidad absoluta del motor de extensiones y base de datos, mientras que de cara al sistema operativo el App ID ("io.kitsux.app") y el nombre de la app ("KitsuX") quedan renombrados de manera limpia.
