# QA de la gran actualización

## Estado

Las compilaciones de Kotlin y APK debug son validación de código. La prueba de Jikan, fuentes, instalación, reproducción, lectura y layout Android requiere teléfono o emulador. El usuario pidió hacer las pruebas de teléfono después.

## Recorrido en teléfono

1. Abrir Inicio con biblioteca vacía; tocar Explorar. Repetir con anime/manga ocultos y con biblioteca poblada.
2. Verificar hero solo de biblioteca, paginación manual y acción Continuar/Detalles. Comprobar que una reproducción fallida o con cero progreso no cree tarjeta.
3. Abrir Continuar viendo/leyendo y Novedades; validar episodio/capítulo correcto y menú contextual de quitar sin borrar historial.
4. En Anime y Manga, comprobar «Todo» sin duplicados al tener una obra en varias categorías; probar chips, orden, selección múltiple, lista y cuadrícula. Girar y usar pantalla dividida.
5. Añadir obras en los cuatro modos de organización, con Jikan activado/desactivado en Automática y también sin categorías creadas; confirmar que la elección manual no cambie después, que cancelar no abra tracking y que categorías anteriores sigan intactas. Crear backup y restaurarlo en entorno de prueba antes de cualquier futura migración.
6. Abrir Descubrir con y sin red; comprobar temporada, próximos estrenos, tendencias, reintento y búsqueda en fuentes. No asumir disponibilidad por una portada Jikan.
7. Abrir ficha de Anime/Manga, progreso parcial, CTA, lista y Back. En Manga no debe aparecer icono de reproducción.
8. Desde Más abrir Descargas, Historial, Actualizaciones, Fuentes, Extensiones, Tracking y Ajustes. Comprobar Back y estados de extensión instalada, pendiente, error y confianza.
9. En tablet o emulador ancho, revisar rail, dos paneles de fichas, densidad de cuadrícula y navegación con ventana dividida.

## Criterio para fases pendientes

- Organización 2.0: hoja de alta renovada, etiquetas/categoría principal y migración reversible con backup restaurado y asociaciones ambiguas preservadas.
- Fichas: tabs Información/Relacionado sin alterar el motor de episodios/capítulos.
- Player/Reader: cambios visuales solo tras comprobar reproducción, gestos, subtítulos y lectura en dispositivo.
