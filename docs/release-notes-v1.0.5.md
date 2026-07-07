## es

### Kitsu X 1.0.5

Esta version se enfoca en estabilidad de extensiones, migraciones y acceso rapido al contenido reciente.

### Cambios

- Se corrigio la carga de repositorios de extensiones actuales que publican IDs de fuente como numero o texto.
- Se mejoro la actualizacion manual de extensiones para refrescar el repositorio antes de fallar cuando una extension instalada no aparece en el indice local.
- Se corrigio un caso donde marcar una extension como confiable podia no persistir si Android devolvia multiples huellas de firma.
- Se agrego timeout a la migracion de anime y manga para evitar que la pantalla quede cargando indefinidamente si una fuente no responde.
- Se separo Inicio en `Continuar viendo` solo para anime y `Continuar leyendo` solo para manga.
- Se ocultaran las filas de continuar cuando no exista progreso real o no haya siguiente episodio/capitulo disponible.
- Se optimizo el toque en tarjetas de continuar para abrir directamente el episodio/capitulo objetivo y evitar dobles pulsaciones.
- Al entrar a una fuente, ahora se muestra `Latest` por defecto cuando la extension lo soporta, con fallback a `Popular`.
- Se corrigieron los enlaces de descarga del README para usar los nombres reales de los APKs publicados en GitHub Releases.

### Checksums SHA-256

| Variante | SHA-256 |
| --- | --- |
| Universal | `1e4526b4b0455c89ed5d4585fa37506a46947615c0c4bf60005ee807fd3015b6` |
| arm64-v8a | `595c0cc514b4012304b42b8cbaae9cb1ca3d99a646d36a40fe4980d0ddea56a5` |
| armeabi-v7a | `d822e5c681dec81ff26779ba557e68851a88faa03d679565f7a7db5fe71de9e8` |
| x86 | `7d5a60ae65fdb4d4f75b625937a040d75a3a8f944d5f1657918b915f447d726b` |
| x86_64 | `080bcc391010fcedde179be042edf64b21d1742d8502c6e5a9498e918763e4a8` |

## en

### Kitsu X 1.0.5

This version focuses on extension stability, migrations, and faster access to recently updated content.

### Changes

- Fixed loading current extension repositories that publish source IDs as either numbers or strings.
- Improved manual extension updates by refreshing the repository before failing when an installed extension is missing from the local index.
- Fixed a case where trusting an extension could fail to persist if Android returned multiple signing fingerprints.
- Added a timeout to anime and manga migration to prevent the screen from loading forever when a source does not respond.
- Split Home into `Continue watching` for anime only and `Continue reading` for manga only.
- Continue rows are hidden when there is no real progress or no next episode/chapter available.
- Optimized tapping continue cards so they open the target episode/chapter directly and avoid duplicate taps.
- Opening a source now defaults to `Latest` when supported by the extension, with a fallback to `Popular`.
- Fixed README download links to use the real APK asset names published in GitHub Releases.

### SHA-256 Checksums

| Variant | SHA-256 |
| --- | --- |
| Universal | `1e4526b4b0455c89ed5d4585fa37506a46947615c0c4bf60005ee807fd3015b6` |
| arm64-v8a | `595c0cc514b4012304b42b8cbaae9cb1ca3d99a646d36a40fe4980d0ddea56a5` |
| armeabi-v7a | `d822e5c681dec81ff26779ba557e68851a88faa03d679565f7a7db5fe71de9e8` |
| x86 | `7d5a60ae65fdb4d4f75b625937a040d75a3a8f944d5f1657918b915f447d726b` |
| x86_64 | `080bcc391010fcedde179be042edf64b21d1742d8502c6e5a9498e918763e4a8` |
