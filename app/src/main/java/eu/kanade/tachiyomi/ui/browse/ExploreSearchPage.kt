package eu.kanade.tachiyomi.ui.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.browse.anime.AnimeSourceItem
import eu.kanade.presentation.browse.anime.AnimeSourceOptionsDialog
import eu.kanade.presentation.browse.anime.AnimeSourceUiModel
import eu.kanade.presentation.browse.manga.MangaSourceOptionsDialog
import eu.kanade.presentation.browse.manga.MangaSourceUiModel
import eu.kanade.presentation.browse.manga.SourceItem
import eu.kanade.tachiyomi.ui.browse.anime.source.AnimeSourcesFilterScreen
import eu.kanade.tachiyomi.ui.browse.anime.source.AnimeSourcesScreenModel
import eu.kanade.tachiyomi.ui.browse.anime.source.browse.BrowseAnimeSourceScreen
import eu.kanade.tachiyomi.ui.browse.anime.source.globalsearch.GlobalAnimeSearchScreen
import eu.kanade.tachiyomi.ui.browse.manga.source.MangaSourcesFilterScreen
import eu.kanade.tachiyomi.ui.browse.manga.source.MangaSourcesScreenModel
import eu.kanade.tachiyomi.ui.browse.manga.source.browse.BrowseMangaSourceScreen
import eu.kanade.tachiyomi.ui.browse.manga.source.globalsearch.GlobalMangaSearchScreen
import tachiyomi.i18n.MR
import tachiyomi.i18n.aniyomi.AYMR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
internal fun Screen.ExploreSearchPage(anime: Boolean, onAnimeChange: (Boolean) -> Unit, padding: PaddingValues) {
    val navigator = LocalNavigator.currentOrThrow
    val animeModel = rememberScreenModel { AnimeSourcesScreenModel() }
    val mangaModel = rememberScreenModel { MangaSourcesScreenModel() }
    val animeState by animeModel.state.collectAsState()
    val mangaState by mangaModel.state.collectAsState()
    val animeSources = animeState.items.filterIsInstance<AnimeSourceUiModel.Item>()
        .map { it.source }.distinctBy { it.id }
    val mangaSources = mangaState.items.filterIsInstance<MangaSourceUiModel.Item>()
        .map { it.source }.distinctBy { it.id }
    var query by rememberSaveable { mutableStateOf("") }
    var animeExpanded by rememberSaveable { mutableStateOf(false) }
    var mangaExpanded by rememberSaveable { mutableStateOf(false) }
    val search = {
        if (query.isNotBlank()) {
            navigator.push(if (anime) GlobalAnimeSearchScreen(query.trim()) else GlobalMangaSearchScreen(query.trim()))
        }
    }
    LazyColumn(contentPadding = padding) {
        item {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(MR.strings.kitsux_search_page_intro), style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(MR.strings.kitsux_explore_search_hint)) },
                    singleLine = true,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { search() }, enabled = query.isNotBlank()) {
                            Icon(Icons.Outlined.Search, stringResource(MR.strings.action_search))
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { search() }),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = anime,
                        onClick = { onAnimeChange(true) },
                        label = { Text(stringResource(AYMR.strings.label_anime)) },
                    )
                    FilterChip(
                        selected = !anime,
                        onClick = { onAnimeChange(false) },
                        label = { Text(stringResource(AYMR.strings.label_manga)) },
                    )
                }
                Text(
                    stringResource(MR.strings.kitsux_explore_installed_sources),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
        item {
            ExploreGroupHeader(
                title = stringResource(MR.strings.kitsux_installed_anime_sources),
                count = animeSources.size,
                expanded = animeExpanded,
                onClick = { animeExpanded = !animeExpanded },
                actions = {
                    IconButton(onClick = { navigator.push(AnimeSourcesFilterScreen()) }) {
                        Icon(Icons.Outlined.FilterList, stringResource(MR.strings.action_filter))
                    }
                },
            )
        }
        if (animeExpanded) {
            if (animeState.isLoading || animeSources.isEmpty()) {
                item { ExploreGroupStatus(animeState.isLoading) }
            }
            items(animeSources, key = { "anime-${it.id}" }) { source ->
                AnimeSourceItem(
                    source = source,
                    onClickItem = { item, listing -> navigator.push(BrowseAnimeSourceScreen(item.id, listing.query)) },
                    onLongClickItem = animeModel::showSourceDialog,
                    onClickPin = animeModel::togglePin,
                )
            }
        }
        item {
            ExploreGroupHeader(
                title = stringResource(MR.strings.kitsux_installed_manga_sources),
                count = mangaSources.size,
                expanded = mangaExpanded,
                onClick = { mangaExpanded = !mangaExpanded },
                actions = {
                    IconButton(onClick = { navigator.push(MangaSourcesFilterScreen()) }) {
                        Icon(Icons.Outlined.FilterList, stringResource(MR.strings.action_filter))
                    }
                },
            )
        }
        if (mangaExpanded) {
            if (mangaState.isLoading || mangaSources.isEmpty()) {
                item { ExploreGroupStatus(mangaState.isLoading) }
            }
            items(mangaSources, key = { "manga-${it.id}" }) { source ->
                SourceItem(
                    source = source,
                    onClickItem = { item, listing -> navigator.push(BrowseMangaSourceScreen(item.id, listing.query)) },
                    onLongClickItem = mangaModel::showSourceDialog,
                    onClickPin = mangaModel::togglePin,
                )
            }
        }
    }
    animeState.dialog?.let { dialog ->
        AnimeSourceOptionsDialog(
            source = dialog.source,
            onClickPin = { animeModel.togglePin(dialog.source) },
            onClickDisable = { animeModel.toggleSource(dialog.source) },
            onDismiss = animeModel::closeDialog,
        )
    }
    mangaState.dialog?.let { dialog ->
        MangaSourceOptionsDialog(
            source = dialog.source,
            onClickPin = { mangaModel.togglePin(dialog.source) },
            onClickDisable = { mangaModel.toggleSource(dialog.source) },
            onClickToggleDataSaver = if (mangaState.dataSaverEnabled) {
                { mangaModel.toggleExcludeFromMangaDataSaver(dialog.source) }
            } else {
                null
            },
            onDismiss = mangaModel::closeDialog,
        )
    }
}
