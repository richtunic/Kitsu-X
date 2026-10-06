package eu.kanade.presentation.browse

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.entries.components.ItemCover
import eu.kanade.tachiyomi.ui.browse.ExploreAnime
import eu.kanade.tachiyomi.ui.browse.ExploreState
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun ExploreDiscover(
    state: ExploreState,
    onRetry: () -> Unit,
    onSearchAnime: (String) -> Unit,
    modifier: Modifier = Modifier,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    listState: LazyListState = rememberLazyListState(),
) {
    var selectedGenre by rememberSaveable { mutableStateOf<String?>(null) }
    val genres = remember(state.season, state.trending) {
        (state.season + state.trending).flatMap { it.genres }.map { it.name }.distinct().take(8)
    }
    val season = state.season.filter {
        selectedGenre == null || it.genres.any { genre -> genre.name == selectedGenre }
    }
    val upcoming = state.upcoming.filter {
        selectedGenre == null || it.genres.any { genre -> genre.name == selectedGenre }
    }
    val trending = state.trending.filter {
        selectedGenre == null || it.genres.any { genre -> genre.name == selectedGenre }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            bottom =
            bottomPadding + 24.dp,
        ),
    ) {
        if (state.isLoading && state.season.isEmpty() && state.upcoming.isEmpty() && state.trending.isEmpty()) {
            item(key = "initial_loading") {
                Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else {
            if (state.isLoading) {
                item(key = "refresh_loading") {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
                }
            }
            if (state.savedSections.isNotEmpty()) {
                item(key = "saved_content") {
                    Text(
                        stringResource(MR.strings.kitsux_explore_saved_content),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
            }
            if (state.hasError) {
                item(key = "error") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(
                                if (state.isServiceUnavailable) {
                                    MR.strings.kitsux_explore_service_unavailable
                                } else {
                                    MR.strings.kitsux_explore_load_failed
                                },
                            ),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        TextButton(onClick = onRetry) { Text(stringResource(MR.strings.action_retry)) }
                    }
                }
            }
            if (season.isNotEmpty()) {
                item(key = "season") {
                    ExploreAnimeRow(stringResource(MR.strings.kitsux_explore_current_season), season, onSearchAnime)
                }
            }
            if (upcoming.isNotEmpty()) {
                item(key = "upcoming") {
                    ExploreAnimeRow(stringResource(MR.strings.kitsux_explore_upcoming), upcoming, onSearchAnime)
                }
            }
            if (trending.isNotEmpty()) {
                item(key = "trending") {
                    ExploreAnimeRow(stringResource(MR.strings.kitsux_explore_trending), trending, onSearchAnime)
                }
            }
            if (genres.isNotEmpty()) {
                item(key = "genres") {
                    Column(modifier = Modifier.padding(vertical = 12.dp)) {
                        Text(
                            stringResource(MR.strings.kitsux_explore_genres),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(genres) { genre ->
                                FilterChip(
                                    selected = selectedGenre == genre,
                                    onClick = { selectedGenre = if (selectedGenre == genre) null else genre },
                                    label = { Text(genre) },
                                )
                            }
                        }
                    }
                }
            }
            if (selectedGenre != null && season.isEmpty() && upcoming.isEmpty() && trending.isEmpty()) {
                item(key = "no_results") {
                    Text(
                        stringResource(MR.strings.no_results_found),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun ExploreAnimeRow(title: String, items: List<ExploreAnime>, onItemClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            title,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium,
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(items, key = { it.malId }) { anime ->
                Column(modifier = Modifier.width(125.dp).clickable { onItemClick(anime.title) }) {
                    ItemCover.Book(data = anime.imageUrl, modifier = Modifier.fillMaxWidth().aspectRatio(0.67f))
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        anime.title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    val dateLabel = listOfNotNull(
                        anime.season?.replaceFirstChar(Char::uppercase),
                        anime.year?.toString(),
                    )
                        .joinToString(" · ")
                    if (dateLabel.isNotBlank()) {
                        Text(
                            dateLabel,
                            maxLines = 1,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
