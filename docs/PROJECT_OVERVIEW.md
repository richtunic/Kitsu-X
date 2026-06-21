# PROJECT_OVERVIEW

## Nombre del proyecto
KitsuX

## Objetivo
Evolución moderna de Aniyomi diseñada para ofrecer una experiencia premium inspirada en Netflix, Crunchyroll, Plex y Jellyfin, conservando la compatibilidad con el ecosistema de extensiones.

## Usuarios objetivo
Fans del Anime y Manga que buscan una experiencia de streaming y lectura premium y moderna en Android.

## Funcionalidades actuales
- Ecosistema de extensiones de Aniyomi (Anime/Manga)
- Gestor de descargas y reproductor mpv/ExoPlayer
- Lector de Manga local y online
- Base de datos local (Room) y trackeo (AniList/MyAnimeList/Kitsu)

## Funcionalidades pendientes
- Rediseño completo de la interfaz visual (Netflix-style)
- Nuevo sistema de navegación (Compose Navigation)
- Pantallas principales rediseñadas (Home, Explore, Library, Anime Details, Manga Details, Player UI, Reader)
- Rebranding completo de strings y recursos

## Stack
- Kotlin
- Android (SDK 35/34)
- Jetpack Compose / Material 3
- Room Database
- Coroutines
- Coil, Compose Navigation, Lottie, Compose Animation

## Estado actual
- Repositorio de Aniyomi clonado (Fase 1: Forking & Rebranding)
- Configurando entorno y realizando build inicial

## Restricciones importantes
- Mantener compatible el sistema de extensiones, base de datos y APIs de fuentes de datos.
- No tocar los extractores de video ni el motor del lector.
