# Organización 2.0: contrato de migración

## Estado actual

Anime y Manga usan relaciones muchos a muchos (`animes_categories` y `mangas_categories`). Una obra puede estar en varias categorías. Las tablas no guardan si una categoría la creó Jikan o una persona. El backup actual conserva categorías por orden y sus asociaciones por obra; la restauración vuelve a enlazarlas por nombre. Por ello, inferir que una categoría llamada «Fantasía» es automática sería inseguro.

La pestaña virtual «Todo» ya elimina duplicados visuales sin modificar esas relaciones. Las nuevas altas pueden usar una categoría existente o la selección explícita del usuario.

## Modelo aditivo propuesto

1. Guardar categoría principal por obra en un campo o tabla independiente para Anime y Manga. No reutilizar ni borrar relaciones actuales al introducirlo.
2. Guardar etiquetas por obra en tablas separadas. Una etiqueta Jikan necesita procedencia y fecha; una etiqueta personal debe distinguirse de la automática.
3. Las categorías existentes permanecen como asociaciones heredadas hasta que el usuario revise cada obra ambigua.
4. El backup debe incluir los campos nuevos en un formato compatible con backups anteriores y restaurarlos sin alterar relaciones heredadas.

## Conversión opcional

1. Crear backup completo con categorías, obras y preferencias, y restaurarlo en una base de prueba.
2. Mostrar vista previa por obra: asociaciones actuales, categoría principal propuesta, etiquetas propuestas y motivo de cada sugerencia.
3. Solo proponer conversión automática cuando exista procedencia inequívoca. Nombres de géneros o coincidencias de Jikan por sí solos no son prueba de procedencia.
4. Ante ambigüedad, conservar todas las asociaciones y pedir revisión manual. «Revisar después» no cambia datos.
5. Aplicar cada lote en transacción, con un registro de relaciones originales para rollback. No eliminar categorías personalizadas ni quitar obras de biblioteca.
6. Verificar recuentos de obras, relaciones, categorías y backup antes y después. Probar cancelación, fallo a mitad, actualización desde versiones anteriores y restauración.

## Límite de esta fase

No se activa una migración automática en el APK actual. Antes de añadir tablas o convertir asociaciones se necesitan fixtures representativos y prueba de backup/restauración en dispositivo o emulador con datos de ejemplo. Un build de Gradle no verifica esa seguridad de datos.
