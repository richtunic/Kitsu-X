package eu.kanade.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import tachiyomi.presentation.core.components.material.PullRefresh
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding
import eu.kanade.presentation.entries.components.ItemCover
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import eu.kanade.tachiyomi.ui.home.ContinueWatchingItem
import eu.kanade.tachiyomi.ui.home.KitsuXCategoryRow
import eu.kanade.tachiyomi.ui.home.KitsuXHomeState
import eu.kanade.tachiyomi.ui.home.KitsuXMediaItem
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun HomeScreenContent(
    state: KitsuXHomeState,
    onItemClick: (KitsuXMediaItem) -> Unit,
    onHeroClick: (KitsuXMediaItem) -> Unit,
    onContinueClick: (ContinueWatchingItem) -> Unit,
    onRemoveContinueItem: (ContinueWatchingItem) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF000000)),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
            )
        }
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
                        if (item.isAnime) MR.strings.kitsux_home_remove_continue_watching_title
                        else MR.strings.kitsux_home_remove_continue_reading_title
                    ),
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = stringResource(
                        if (item.isAnime) MR.strings.kitsux_home_remove_continue_watching_description
                        else MR.strings.kitsux_home_remove_continue_reading_description,
                        item.title
                    ),
                    color = Color.White
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemoveContinueItem(item)
                        itemToRemove = null
                    }
                ) {
                    Text(text = stringResource(MR.strings.action_remove))
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToRemove = null }) {
                    Text(text = stringResource(MR.strings.action_cancel))
                }
            }
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
        if (continueWatchingItems.isEmpty() && continueReadingItems.isEmpty() && state.newReleases.isEmpty() && state.categories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF000000))
                    .verticalScroll(rememberScrollState())
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🍿",
                        fontSize = 64.sp
                    )
                    Text(
                        text = stringResource(MR.strings.kitsux_home_empty_library_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.1.sp
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(MR.strings.kitsux_home_empty_library_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
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
                        HeroBannerSection(
                            items = state.heroBannerItems,
                            onClick = onHeroClick,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
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

                if (state.newReleases.isNotEmpty()) {
                    item {
                        ContinueWatchingSection(
                            title = stringResource(MR.strings.kitsux_home_new_episodes_chapters),
                            items = state.newReleases,
                            onContinueClick = onContinueClick,
                            onContinueLongClick = { itemToRemove = it },
                        )
                    }
                }

                // 2. Custom Category Rows
                state.categories.forEach { categoryRow ->
                    if (categoryRow.items.isNotEmpty()) {
                        item(key = categoryRow.name) {
                            MediaSection(
                                title = categoryRow.name,
                                items = categoryRow.items,
                                onItemClick = onItemClick,
                            )
                        }
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
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            val distinctItems = items.distinctBy { "${it.id}_${it.isAnime}" }
            items(distinctItems, key = { "${it.id}_${it.isAnime}" }) { continueItem ->
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF141414))
                        .combinedClickable(
                            onClick = { onContinueClick(continueItem) },
                            onLongClick = { onContinueLongClick(continueItem) }
                        ),
                ) {
                    Column {
                        // Poster thumb aspect ratio (16:9)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.77f),
                        ) {
                            ItemCover.Thumb(
                                data = continueItem.thumbnailUrl,
                                modifier = Modifier.fillMaxSize(),
                            )

                            // Play overlay icon in the middle
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(Color.Black.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            }

                            // "NUEVO" update badge at top left of thumbnail
                            if (continueItem.hasUpdates) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFFE50914)) // Rojo KitsuX
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                ) {
                                    val text = if (continueItem.unseenCount > 0) {
                                            stringResource(MR.strings.kitsux_home_new_count, continueItem.unseenCount)
                                    } else {
                                            stringResource(MR.strings.kitsux_home_new_badge)
                                    }
                                    Text(
                                        text = text,
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }

                            // Episode progress text at bottom right
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                ) {
                                Text(
                                    text = continueItem.progressText,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        // Progress indicator (Dynamic progress bar)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(Color.Gray.copy(alpha = 0.4f)),
                        ) {
                            if (continueItem.episodeProgress > 0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(continueItem.episodeProgress)
                                        .background(Color(0xFFE50914)),
                                )
                            }
                        }

                        // Text Title (Crunchyroll-style bold)
                        Text(
                            text = continueItem.mediaItem.title,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 2.dp),
                        )
                        // Subtitle with type and progress info
                        Text(
                            text = if (continueItem.isAnime) "Anime • ${continueItem.progressText}" else "Manga • ${continueItem.progressText}",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                        )
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
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            val distinctItems = items.distinctBy { "${it.id}_${it.isAnime}" }
            items(distinctItems, key = { "${it.id}_${it.isAnime}" }) { item ->
                Column(
                    modifier = Modifier
                        .width(125.dp)
                        .clickable { onItemClick(item) }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.67f) // 2:3 aspect ratio
                            .clip(RoundedCornerShape(6.dp))
                    ) {
                        ItemCover.Book(
                            data = item.thumbnailUrl,
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
                        lineHeight = 16.sp
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    // Subtitle / type (Anime or Manga)
                    Text(
                        text = if (item.isAnime) "Anime" else "Manga",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
        modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
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
    
    // Auto-scroll loop
    LaunchedEffect(pagerState) {
        while (true) {
            kotlinx.coroutines.delay(6000)
            if (items.isNotEmpty()) {
                val nextPage = (pagerState.currentPage + 1) % items.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF141414))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val item = items[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onClick(item) }
            ) {
                // Image
                AsyncImage(
                    model = item.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
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
                                    Color.Black
                                )
                            )
                        )
                )

                // Info Column
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 16.dp, start = 16.dp, end = 72.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Rating Badges
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE50914), RoundedCornerShape(3.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "POPULAR",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "★ ${item.rating}",
                            color = Color(0xFFFFB300),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Title
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Genres
                    if (item.genres.isNotEmpty()) {
                        Text(
                            text = item.genres.take(3).joinToString(" • "),
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Synopsis
                    if (item.description.isNotBlank()) {
                        Text(
                            text = item.description,
                            color = Color.Gray,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 15.sp
                        )
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Play / Search Button
                        Row(
                            modifier = Modifier
                                .background(Color(0xFFE50914), RoundedCornerShape(4.dp))
                                .clickable { onClick(item) }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                                Text(
                                    text = when {
                                        item.isStarted && item.isAnime -> stringResource(MR.strings.kitsux_home_continue_watching)
                                        item.isStarted -> stringResource(MR.strings.kitsux_home_continue_reading)
                                        else -> stringResource(MR.strings.kitsux_home_watch_now)
                                    },
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(items.size) { index ->
                val active = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .size(if (active) 8.dp else 6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (active) Color(0xFFE50914) else Color.Gray.copy(alpha = 0.5f))
                )
            }
        }
    }
}
