package eu.kanade.tachiyomi.ui.browse

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import eu.kanade.presentation.components.AppBarActions
import eu.kanade.tachiyomi.ui.browse.anime.migration.sources.migrateAnimeSourceTab
import eu.kanade.tachiyomi.ui.browse.manga.migration.sources.migrateMangaSourceTab
import tachiyomi.i18n.MR
import tachiyomi.i18n.aniyomi.AYMR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
internal fun Screen.ExploreMigrationPage(padding: PaddingValues) {
    var query by rememberSaveable { mutableStateOf("") }
    var animeExpanded by rememberSaveable { mutableStateOf(false) }
    var mangaExpanded by rememberSaveable { mutableStateOf(false) }
    val animeActions = migrateAnimeSourceTab(query).actions
    val mangaActions = migrateMangaSourceTab(query).actions
    val snackbar = remember { SnackbarHostState() }
    migrateAnimeSourceTab(query, listContent = { animeItems ->
        migrateMangaSourceTab(query, listContent = { mangaItems ->
            LazyColumn(Modifier.fillMaxSize(), contentPadding = padding) {
                item(key = "migration-filter") {
                    OutlinedTextField(
                        value = query,
                        onValueChange = {
                            query = it
                            if (it.isNotBlank()) {
                                animeExpanded = true
                                mangaExpanded = true
                            }
                        },
                        label = { Text(stringResource(MR.strings.kitsux_migrations_filter)) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                    )
                }
                item(key = "anime-migration-group") {
                    ExploreGroupHeader(
                        title = stringResource(AYMR.strings.label_migration_anime),
                        count = 0,
                        updates = true,
                        expanded = animeExpanded,
                        onClick = { animeExpanded = !animeExpanded },
                        actions = { AppBarActions(animeActions) },
                    )
                }
                if (animeExpanded) animeItems()
                item(key = "manga-migration-group") {
                    ExploreGroupHeader(
                        title = stringResource(AYMR.strings.label_migration_manga),
                        count = 0,
                        updates = true,
                        expanded = mangaExpanded,
                        onClick = { mangaExpanded = !mangaExpanded },
                        actions = { AppBarActions(mangaActions) },
                    )
                }
                if (mangaExpanded) mangaItems()
            }
        }).content(PaddingValues(), snackbar)
    }).content(PaddingValues(), snackbar)
}
