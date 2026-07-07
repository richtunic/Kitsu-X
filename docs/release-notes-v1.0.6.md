## es

### Kitsu X 1.0.6

Esta version corrige detalles detectados despues de la version anterior y mejora el flujo de actualizacion desde GitHub.

### Cambios

- Se corrigio un bug menor de compatibilidad de reproduccion que se paso por alto en la version anterior y podia hacer que algunos servidores devolvieran enlaces no reproducibles.
- Se agrego fallback automatico al siguiente servidor disponible cuando el reproductor rechaza un enlace con errores como `loading failed` o formato no reconocido.
- Se normalizan enlaces de video incompletos antes de enviarlos al reproductor interno o externo.
- La app ahora revisa actualizaciones al volver a primer plano y muestra el dialogo de nueva version sin que el usuario tenga que buscar manualmente.
- La descarga de actualizacion ahora prioriza el APK compatible con la ABI instalada para evitar cambios de variante entre versiones.
- `Continuar viendo` y `Continuar leyendo` permiten ocultar una tarjeta con pulsacion larga sin borrar historial ni progreso.
- Las tarjetas de continuar pueden mostrarse aunque la obra ya no este en la biblioteca, siempre que exista historial local y contenido pendiente.
- Se redujeron solicitudes duplicadas de recomendaciones para evitar ruido de red y limites temporales de servicios externos.

### Checksums SHA-256

| Variante | SHA-256 |
| --- | --- |
| Universal | `13156b9c52898df680661244fcc52c6272ee37b1b4933723c240a27c095466dd` |
| arm64-v8a | `d6301e61f9a50c4bb3a57bdad86768126255c709a52aa3fb7774f64e8139e62d` |
| armeabi-v7a | `1e877b903e6fbe09ae8e35c1f67671e8b7a0055170d8b3e578eb4dc5ec96182e` |
| x86 | `dba8dd4c7e3471d396e5654b3b901e184130063f2ad6d68f0f20b89637155e1c` |
| x86_64 | `d6ca293196ac8e5e45e51e9aad66b66553620e08947bd32401ed97ee11be4336` |

## en

### Kitsu X 1.0.6

This version fixes details found after the previous release and improves the GitHub update flow.

### Changes

- Fixed a minor playback compatibility bug that was missed in the previous version and could make some servers return links the player could not open.
- Added automatic fallback to the next available server when playback rejects a link with errors such as `loading failed` or an unrecognized format.
- Normalized incomplete video links before sending them to the internal or external player.
- The app now checks for updates when it returns to the foreground and shows the new-version dialog without requiring a manual check.
- Update downloads now prioritize the APK compatible with the installed ABI to avoid variant mismatches between versions.
- `Continue watching` and `Continue reading` cards can be hidden with a long press without deleting history or progress.
- Continue cards can appear even when the title is no longer in the library, as long as local history and pending content exist.
- Reduced duplicate recommendation requests to avoid network noise and temporary limits from external services.

### SHA-256 Checksums

| Variant | SHA-256 |
| --- | --- |
| Universal | `13156b9c52898df680661244fcc52c6272ee37b1b4933723c240a27c095466dd` |
| arm64-v8a | `d6301e61f9a50c4bb3a57bdad86768126255c709a52aa3fb7774f64e8139e62d` |
| armeabi-v7a | `1e877b903e6fbe09ae8e35c1f67671e8b7a0055170d8b3e578eb4dc5ec96182e` |
| x86 | `dba8dd4c7e3471d396e5654b3b901e184130063f2ad6d68f0f20b89637155e1c` |
| x86_64 | `d6ca293196ac8e5e45e51e9aad66b66553620e08947bd32401ed97ee11be4336` |
