# AI_CONTEXT

## Resumen corto
KitsuX es un fork y evolución visual y de experiencia de usuario (UX) de Aniyomi para Android, enfocado en un diseño moderno, premium y cinematográfico (estilo Netflix/Plex).

## Stack
- Kotlin
- Android
- Jetpack Compose / Material 3
- Room Database
- Coroutines
- ExoPlayer / mpv-android
- Compose Navigation, Coil, Lottie, Compose Animation

## Arquitectura
Modular:
- core/
- domain/
- database/
- ui/ (design-system, features: home, explore, library, player, reader, downloads, profile, settings, tracking)

## Base de datos
Room (conservando los esquemas para preservar los datos locales del usuario de Aniyomi).

## Autenticación
N/A (Integraciones de tracking mediante OAuth con AniList/MyAnimeList/Kitsu).

## Integraciones
APIs de tracking y fuentes de extensiones de Aniyomi.

## Principios
- Seguridad primero.
- Código mínimo.
- Cambios reversibles.
- Documentación viva.

## No usar
- Dependencias innecesarias.
- Refactors no solicitados.
- Cambios que rompan la compatibilidad del motor de extensiones o base de datos de Aniyomi.

## Prioridades actuales
1. Verificar e inicializar el entorno de compilación Gradle.
2. Modificar la configuración de Gradle para cambiar el Application ID a `io.kitsux.app`.
3. Actualizar los recursos de strings base para renombrar la aplicación de Aniyomi a KitsuX.
