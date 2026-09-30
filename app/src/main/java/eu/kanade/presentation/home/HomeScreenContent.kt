package eu.kanade.presentation.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import eu.kanade.presentation.components.HomeLoadingSkeleton
import eu.kanade.presentation.components.MediaProgressCard
import eu.kanade.presentation.entries.components.ItemCover
import eu.kanade.presentation.theme.KitsuXLayoutTokens
import eu.kanade.tachiyomi.ui.home.ContinueWatchingItem
import eu.kanade.tachiyomi.ui.home.KitsuXHomeState
import eu.kanade.tachiyomi.ui.home.KitsuXMediaItem
import eu.kanade.tachiyomi.ui.home.KitsuXNewReleaseGroup
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.PullRefresh
import tachiyomi.presentation.core.i18n.stringResource
import kotlin.time.Duration.Companion.seconds

@Composable
fun HomeScreenContent(
    state: KitsuXHomeState,
    onItemClick: (KitsuXMediaItem) -> Unit,
    onHeroClick: (KitsuXMediaItem) -> Unit,
    onContinueClick: (ContinueWatchingItem) -> Unit,
    onRemoveContinueItem: (ContinueWatchingItem) -> Unit,
    onExploreClick: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isLoading) {
        HomeLoadingSkeleton(modifier)
        return
    }

    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    var itemToRemove by remember { mutableStateOf<ContinueWatchingItem?>(null) }
    val continueWatchingItems = state.continueWatching.filter { it.isAnime }
    val continueReadingItems = state.continueReading.filter { !it.isAnime }

    itemToRemove?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToRemove = null },
            title = {
                Text(
                    text = stringResource(
                        if (item.isAnime) {
                            MR.strings.kitsux_home_remove_continue_watching_title
                        } else {
                            MR.strings.kitsux_home_remove_continue_reading_title
                        },
                    ),
                    color = Color.White,
                )
            },
            text = {
                Text(
                    text = stringResource(
                        if (item.isAnime) {
                            MR.strings.kitsux_home_remove_continue_watching_description
                        } else {
                            MR.strings.kitsux_home_remove_continue_reading_description
                        },
                        item.title,
                    ),
                    color = Color.White,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemoveContinueItem(item)
                        itemToRemove = null
                    },
                ) {
                    Text(text = stringResource(MR.strings.action_remove))
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToRemove = null }) {
                    Text(text = stringResource(MR.strings.action_cancel))
                }
            },
        )
    }

    PullRefresh(
        refreshing = isRefreshing,
        enabled = true,
        onRefresh = {
            onRefresh()
            scope.launch {
                isRefreshing = true
                delay(1.seconds)
                isRefreshing = false
            }
        },
        modifier = modifier,
    ) {
        if (continueWatchingItems.isEmpty() &&
            continueReadingItems.isEmpty() &&
            state.newReleaseGroups.isEmpty() &&
            state.recentlyAdded.isEmpty()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CollectionsBookmark,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp),
                    )
                    Text(
                        text = stringResource(
                            if (state.isLibraryEmpty) MR.strings.kitsux_home_empty_library_title
                            else MR.strings.kitsux_home_nothing_to_show_title,
                        ),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.1.sp,
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(
                            if (state.isLibraryEmpty) MR.strings.kitsux_home_empty_library_description
                            else MR.strings.kitsux_home_nothing_to_show_description,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    TextButton(onClick = onExploreClick) {
                        Text(stringResource(MR.strings.browse))
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                // Spacer to avoid status bar overlap and add premium top spacing
                item {
                    Spacer(modifier = Modifier.statusBarsPadding().height(24.dp))
                }

                // Hero Banner Section (Slider)
                if (state.heroBannerItems.isNotEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            HeroBannerSection(
                                items = state.heroBannerItems,
                                onClick = onHeroClick,
                                modifier = Modifier
                                    .widthIn(max = KitsuXLayoutTokens.heroMaxWidth)
                                    .padding(horizontal = KitsuXLayoutTokens.gutter, vertical = 8.dp),
                            )
                        }
                    }
                }

                // 1. Continue Watching Row
                if (continueWatchingItems.isNotEmpty()) {
                    item {
                        ContinueWatchingSection(
                            title = stringResource(MR.strings.kitsux_home_continue_watching),
                            items = continueWatchingItems,
                            onContinueClick = onContinueClick,
                            onContinueLongClick = { itemToRemove = it },
                        )
                    }
                }

                // 2. Continue Reading Row
                if (continueReadingItems.isNotEmpty()) {
                    item {
                        ContinueWatchingSection(
                            title = stringResource(MR.strings.kitsux_home_continue_reading),
                            items = continueReadingItems,
                            onContinueClick = onContinueClick,
                            onContinueLongClick = { itemToRemove = it },
                        )
                    }
                }

                if (state.newReleaseGroups.isNotEmpty()) {
                    item {
                        NewReleaseSection(
                            groups = state.newReleaseGroups,
                            onItemClick = onItemClick,
                        )
                    }
                }

                if (state.recentlyAdded.isNotEmpty()) {
                    item {
                        MediaSection(
                            title = stringResource(MR.strings.kitsux_home_recently_added),
                            items = state.recentlyAdded,
                            onItemClick = onItemClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContinueWatchingSection(
    title: String? = null,
    items: List<ContinueWatchingItem>,
    onContinueClick: (ContinueWatchingItem) -> Unit,
    onContinueLongClick: (ContinueWatchingItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
    ) {
        SectionTitle(title = title ?: stringResource(MR.strings.kitsux_home_continue_watching))

        LazyRow(
            contentPadding = PaddingValues(horizontal = KitsuXLayoutTokens.gutter),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            val distinctItems = items.distinctBy { "${it.id}_${it.isAnime}" }
            items(distinctItems, key = { "${it.id}_${it.isAnime}" }) { continueItem ->
                MediaProgressCard(
                    title = continueItem.mediaItem.title,
                    coverData = continueItem.mediaItem.coverData ?: continueItem.thumbnailUrl,
                    progressText = continueItem.progressText,
                    progress = continueItem.episodeProgress,
                    isAnime = continueItem.isAnime,
                    hasUpdates = continueItem.hasUpdates,
                    unseenCount = continueItem.unseenCount,
                    onContinue = { onContinueClick(continueItem) },
                    onRemove = { onContinueLongClick(continueItem) },
                )
            }
        }
    }
}

@Composable
fun NewReleaseSection(
    groups: List<KitsuXNewReleaseGroup>,
    onItemClick: (KitsuXMediaItem) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        SectionTitle(stringResource(MR.strings.kitsux_home_news_tray))
        LazyRow(
            contentPadding = PaddingValues(horizontal = KitsuXLayoutTokens.gutter),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            groups.forEach { group ->
                items(group.items, key = { "${it.isAnime}_${it.id}" }) { item ->
                    Column(
                        modifier = Modifier.width(108.dp).clickable { onItemClick(item.mediaItem) },
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().aspectRatio(0.67f).clip(RoundedCornerShape(6.dp))) {
                            ItemCover.Book(
                                data = item.mediaItem.coverData ?: item.thumbnailUrl,
                                modifier = Modifier.fillMaxSize(),
                            )
                            Text(
                                text = stringResource(MR.strings.kitsux_home_new_badge),
                                modifier = Modifier.align(Alignment.TopStart)
                                    .padding(4.dp)
                                    .background(Color(0xFFE50914), RoundedCornerShape(3.dp))
                                    .padding(horizontal = 5.dp, vertical = 2.dp),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Text(item.title, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                        Text(item.progressText, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall)
                        Text(group.title, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun MediaSection(
    title: String,
    items: List<KitsuXMediaItem>,
    onItemClick: (KitsuXMediaItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
    ) {
        SectionTitle(title = title)

        LazyRow(
            contentPadding = PaddingValues(horizontal = KitsuXLayoutTokens.gutter),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            val distinctItems = items.distinctBy { "${it.id}_${it.isAnime}" }
            items(distinctItems, key = { "${it.id}_${it.isAnime}" }) { item ->
                Column(
                    modifier = Modifier
                        .width(108.dp)
                        .clickable { onItemClick(item) },
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.67f) // 2:3 aspect ratio
                            .clip(RoundedCornerShape(6.dp)),
                    ) {
                        ItemCover.Book(
                            data = item.coverData ?: item.thumbnailUrl,
                            modifier = Modifier.fillMaxSize(),
                        )

                        // Show "NUEVO" badge on category elements if there are updates
                        if (item.hasUpdates) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFFE50914))
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    text = stringResource(MR.strings.kitsux_home_new_badge),
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Title below the poster (bold, max 2 lines)
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp,
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Subtitle / type (Anime or Manga)
                    Text(
                        text = if (item.isAnime) "Anime" else "Manga",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = modifier.padding(
            start = KitsuXLayoutTokens.gutter,
            end = KitsuXLayoutTokens.gutter,
            top = 12.dp,
            bottom = 8.dp,
        ),
    )
}

@Composable
fun HeroBannerSection(
    items: List<KitsuXMediaItem>,
    onClick: (KitsuXMediaItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { items.size })

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF141414)),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            val item = items[page]
            var useCover by remember(item.id, item.heroArtworkUrl) {
                mutableStateOf(item.heroArtworkUrl.isNullOrBlank())
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onClick(item) },
            ) {
                // Image
                AsyncImage(
                    model = if (useCover) item.coverData ?: item.thumbnailUrl else item.heroArtworkUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onError = { if (!useCover) useCover = true },
                )

                // Gradient overlay (Netflix style)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.2f),
                                    Color.Black.copy(alpha = 0.6f),
                                    Color.Black,
                                ),
                            ),
                        ),
                )

                // Info Column
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 16.dp, start = 16.dp, end = 72.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    // Title
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = if (item.unseenCount > 0) {
                            stringResource(MR.strings.kitsux_home_new_count, item.unseenCount)
                        } else {
                            stringResource(MR.strings.kitsux_home_in_library)
                        },
                        color = Color.LightGray,
                        fontSize = 11.sp,
                    )

                    // Action Buttons
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .background(Color(0xFFE50914), RoundedCornerShape(4.dp))
                                .clickable { onClick(item) }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = when {
                                    item.isStarted && item.isAnime -> stringResource(
                                        MR.strings.kitsux_home_continue_watching,
                                    )
                                    item.isStarted -> stringResource(MR.strings.kitsux_home_continue_reading)
                                    else -> stringResource(MR.strings.kitsux_home_view_details)
                                },
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }

        // Indicators (dots) at the bottom right
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(items.size) { index ->
                val active = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .size(if (active) 8.dp else 6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (active) Color(0xFFE50914) else Color.Gray.copy(alpha = 0.5f)),
                )
            }
        }
    }
}
