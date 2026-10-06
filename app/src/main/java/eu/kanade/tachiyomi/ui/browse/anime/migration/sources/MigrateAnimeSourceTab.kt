package eu.kanade.tachiyomi.ui.browse.anime.migration.sources

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.browse.anime.MigrateAnimeSourceScreen
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.TabContent
import eu.kanade.tachiyomi.ui.browse.anime.migration.anime.MigrateAnimeScreen
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tachiyomi.i18n.MR
import tachiyomi.i18n.aniyomi.AYMR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun Screen.migrateAnimeSourceTab(
    query: String = "",
    listContent: (@Composable (LazyListScope.() -> Unit) -> Unit)? = null,
): TabContent {
    val uriHandler = LocalUriHandler.current
    val navigator = LocalNavigator.currentOrThrow
    val screenModel = rememberScreenModel { MigrateAnimeSourceScreenModel() }
    val state by screenModel.state.collectAsState()

    return TabContent(
        titleRes = AYMR.strings.label_migration_anime,
        actions = persistentListOf(
            AppBar.Action(
                title = stringResource(MR.strings.migration_help_guide),
                icon = Icons.AutoMirrored.Outlined.HelpOutline,
                onClick = {
                    uriHandler.openUri("https://github.com/richtunic/Kitsu-X#source-migration")
                },
            ),
        ),
        content = { contentPadding, _ ->
            val filteredState = state.copy(
                items = state.items.filter { (source, _) ->
                    source.name.contains(query, ignoreCase = true)
                }.toImmutableList(),
            )
            if (filteredState.isLoading || filteredState.isEmpty) {
                val empty: @Composable () -> Unit = {
                    if (filteredState.isLoading) {
                        eu.kanade.tachiyomi.ui.browse.ExploreGroupStatus(true)
                    } else {
                        Text(
                            text = stringResource(
                                if (query.isNotBlank()) {
                                    MR.strings.no_results_found
                                } else {
                                    MR.strings.information_empty_library
                                },
                            ),
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                if (listContent != null) {
                    listContent { item(key = "anime-migration-empty") { empty() } }
                } else {
                    empty()
                }
            } else {
                MigrateAnimeSourceScreen(
                    state = filteredState,
                    listContent = listContent,
                    contentPadding = contentPadding,
                    onClickItem = { source ->
                        navigator.push(MigrateAnimeScreen(source.id))
                    },
                    onToggleSortingDirection = screenModel::toggleSortingDirection,
                    onToggleSortingMode = screenModel::toggleSortingMode,
                )
            }
        },
    )
}
