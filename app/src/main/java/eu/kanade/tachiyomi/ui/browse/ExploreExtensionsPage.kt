package eu.kanade.tachiyomi.ui.browse

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.components.AppBarActions
import eu.kanade.presentation.more.settings.screen.browse.UnifiedExtensionReposScreen
import eu.kanade.tachiyomi.ui.browse.anime.extension.AnimeExtensionsScreenModel
import eu.kanade.tachiyomi.ui.browse.anime.extension.animeExtensionsTab
import eu.kanade.tachiyomi.ui.browse.manga.extension.MangaExtensionsScreenModel
import eu.kanade.tachiyomi.ui.browse.manga.extension.mangaExtensionsTab
import tachiyomi.i18n.MR
import tachiyomi.i18n.aniyomi.AYMR
import tachiyomi.presentation.core.components.material.PullRefresh
import tachiyomi.presentation.core.i18n.stringResource

@Composable
internal fun Screen.ExploreExtensionsPage(anime: Boolean, padding: PaddingValues) {
    val navigator = LocalNavigator.currentOrThrow
    val animeModel = rememberScreenModel { AnimeExtensionsScreenModel() }
    val mangaModel = rememberScreenModel { MangaExtensionsScreenModel() }
    val animeState by animeModel.state.collectAsState()
    val mangaState by mangaModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var query by rememberSaveable { mutableStateOf("") }
    var animeExpanded by rememberSaveable { mutableStateOf(anime) }
    var mangaExpanded by rememberSaveable { mutableStateOf(!anime) }
    LaunchedEffect(anime) {
        animeExpanded = anime
        mangaExpanded = !anime
    }
    val animeActions = animeExtensionsTab(animeModel).actions
    val mangaActions = mangaExtensionsTab(mangaModel).actions
    PullRefresh(
        refreshing = animeState.isRefreshing || mangaState.isRefreshing,
        onRefresh = {
            animeModel.findAvailableExtensions()
            mangaModel.findAvailableExtensions()
        },
        enabled = !animeState.isLoading && !mangaState.isLoading,
    ) {
        animeExtensionsTab(animeModel, listContent = { animeItems ->
            mangaExtensionsTab(mangaModel, listContent = { mangaItems ->
                LazyColumn(Modifier.fillMaxSize(), contentPadding = padding) {
                    item(key = "search") {
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            OutlinedTextField(
                                value = query,
                                onValueChange = {
                                    query = it
                                    animeModel.search(it)
                                    mangaModel.search(it)
                                    if (it.isNotBlank()) {
                                        animeExpanded = true
                                        mangaExpanded = true
                                    }
                                },
                                label = { Text(stringResource(MR.strings.kitsux_extensions_filter)) },
                                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth(),
                            )
                            OutlinedButton(
                                onClick = { navigator.push(UnifiedExtensionReposScreen()) },
                                modifier = Modifier.padding(top = 8.dp),
                            ) { Text(stringResource(MR.strings.pref_manage_repos)) }
                        }
                    }
                    item(key = "anime-group") {
                        ExploreGroupHeader(
                            title = stringResource(AYMR.strings.label_anime_extensions),
                            count = animeState.updates,
                            expanded = animeExpanded,
                            onClick = { animeExpanded = !animeExpanded },
                            updates = true,
                            actions = { AppBarActions(animeActions) },
                        )
                    }
                    if (animeExpanded) animeItems()
                    item(key = "manga-group") {
                        ExploreGroupHeader(
                            title = stringResource(AYMR.strings.label_manga_extensions),
                            count = mangaState.updates,
                            expanded = mangaExpanded,
                            onClick = { mangaExpanded = !mangaExpanded },
                            updates = true,
                            actions = { AppBarActions(mangaActions) },
                        )
                    }
                    if (mangaExpanded) mangaItems()
                }
            }).content(PaddingValues(), snackbar)
        }).content(PaddingValues(), snackbar)
    }
}

@Composable
internal fun ExploreGroupHeader(
    title: String,
    count: Int,
    expanded: Boolean,
    onClick: () -> Unit,
    updates: Boolean = false,
    actions: @Composable () -> Unit = {},
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier.weight(1f).clickable(role = Role.Button, onClick = onClick).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall)
                    if (!updates || count > 0) {
                        Text(
                            if (updates) {
                                stringResource(
                                    MR.strings.kitsux_extension_updates,
                                    count,
                                )
                            } else {
                                stringResource(MR.strings.kitsux_source_count, count)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Icon(
                    if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null,
                )
            }
            actions()
        }
    }
}

@Composable
internal fun ExploreGroupStatus(loading: Boolean) {
    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        if (loading) {
            CircularProgressIndicator()
        } else {
            Text(stringResource(MR.strings.source_empty_screen), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
