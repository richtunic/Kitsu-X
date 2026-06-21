<div align="center">

<a href="https://github.com/richtunic/Kitsu-X">
    <img src="./.github/assets/logo.png" alt="Kitsu X Logo" title="Kitsu X Logo" width="100"/>
</a>

# Kitsu X

### Reproductor y lector multimedia completo para Android, basado en Mihon/Aniyomi.
*Descubre y disfruta de anime, manga, series y películas en un solo lugar de forma optimizada y personalizada.*

---

[**Español**](#kitsu-x-español) | [**English**](#kitsu-x-english)

---

</div>

<a name="kitsu-x-español"></a>
## 🇪🇸 Kitsu X - Español

Kitsu X es un fork moderno y optimizado de Aniyomi/Mihon diseñado para ofrecer la mejor experiencia de visualización de anime y lectura de manga en dispositivos Android. Con características mejoradas de automatización y correcciones críticas para saltar protecciones como Cloudflare de forma nativa.

### 🚀 Características Clave

* **Autocategorización Inteligente**: Integración con la API de Jikan para obtener automáticamente los géneros de las obras y organizarlas en carpetas/categorías en tu biblioteca al agregarlas o consultar sus detalles.
* **Bypass de Cloudflare Optimizado**: Algoritmo que alinea las cabeceras (User-Agent de WebView y cliente HTTP OkHttp) y realiza un espejo de cookies (`cf_clearance` y `__cf_bm`) entre subdominios para evitar bucles infinitos de verificación en extensiones como *AnimeOnline*.
* **Instalación Directa de Extensiones**: Refactorizado el sistema de descarga de extensiones para utilizar OkHttp en segundo plano, evitando que las extensiones se queden en estado "pendiente" o "cargando" por restricciones de Android.
* **Actualización Automática de Repositorios**: Recarga inmediata de las extensiones al agregar o editar repositorios sin necesidad de reiniciar la aplicación.
* **Donaciones**: Proyecto mantenido gracias al apoyo de la comunidad. Puedes apoyarnos mediante Ko-fi:

<div align="center">

[![Apoyar en Ko-fi](https://storage.ko-fi.com/cdn/kofi3.png?v=6)](https://ko-fi.com/relampagonegr0)

</div>

### 📥 Descarga e Instalación

* **Requisitos**: Android 8.0 (API 26) o superior.
* Descarga la última versión estable o preview desde la sección de [Releases](https://github.com/richtunic/Kitsu-X/releases).

---

## 🛠️ Contribuir

¡Las contribuciones son bienvenidas! Si deseas realizar cambios importantes, abre primero un Issue para discutir lo que te gustaría cambiar.

### Licencia

Este proyecto está bajo la Licencia Apache 2.0. Consulta el archivo [LICENSE](./LICENSE) para más detalles.

---

<br/>
<br/>

<div align="center">
<hr/>
</div>

<br/>
<br/>

<a name="kitsu-x-english"></a>
## 🇺🇸 Kitsu X - English

Kitsu X is a modern and optimized fork of Aniyomi/Mihon designed to provide the best anime watching and manga reading experience on Android devices. It features improved automation and critical fixes to natively bypass protections like Cloudflare.

### 🚀 Key Features

* **Smart Auto-Categorization**: Powered by Jikan API to automatically fetch genres and organize entries into library categories upon adding them or opening their details.
* **Optimized Cloudflare Bypass**: Tailored headers synchronization (matching WebView and OkHttp User-Agents) and bidirectional cookie mirroring (`cf_clearance` & `__cf_bm`) across subdomains to prevent infinite Turnstile verification loops in extensions like *AnimeOnline*.
* **Direct Extension Installer**: Replaced the system DownloadManager with background OkHttp streaming, resolving the "pending" or infinite loading screen bug on modern Android versions.
* **Auto-Reload Repositories**: Real-time reloading of the extension list when repositories are added or edited, without requiring an app restart.
* **Donations**: Support the project development via Ko-fi:

<div align="center">

[![Support on Ko-fi](https://storage.ko-fi.com/cdn/kofi3.png?v=6)](https://ko-fi.com/relampagonegr0)

</div>

### 📥 Download

* **Requirements**: Android 8.0 (API 26) or higher.
* Download the latest build from the [Releases](https://github.com/richtunic/Kitsu-X/releases) page.

---

## 🛠️ Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

### License

Licensed under the Apache License, Version 2.0. See [LICENSE](./LICENSE) for details.
