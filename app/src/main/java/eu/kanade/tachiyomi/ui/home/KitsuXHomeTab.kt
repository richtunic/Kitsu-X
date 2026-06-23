package eu.kanade.tachiyomi.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.TabOptions
import eu.kanade.presentation.home.HomeScreenContent
import eu.kanade.presentation.util.Tab
import eu.kanade.tachiyomi.ui.entries.anime.AnimeScreen
import eu.kanade.tachiyomi.ui.entries.manga.MangaScreen

import eu.kanade.tachiyomi.ui.browse.anime.source.globalsearch.GlobalAnimeSearchScreen
import eu.kanade.tachiyomi.ui.browse.manga.source.globalsearch.GlobalMangaSearchScreen

data object KitsuXHomeTab : Tab {

    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 0u,
            title = "Home",
            icon = rememberVectorPainter(Icons.Outlined.Home),
        )

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { KitsuXHomeScreenModel(context.applicationContext) }
        val state by screenModel.state.collectAsState()

        HomeScreenContent(
            state = state,
            onItemClick = { item ->
                if (item.isRecommendation) {
                    if (item.isAnime) {
                        navigator.push(GlobalAnimeSearchScreen(item.title))
                    } else {
                        navigator.push(GlobalMangaSearchScreen(item.title))
                    }
                } else {
                    if (item.isAnime) {
                        navigator.push(AnimeScreen(item.id))
                    } else {
                        navigator.push(MangaScreen(item.id))
                    }
                }
            },
            onHeroClick = { item ->
                if (item.isRecommendation) {
                    if (item.isAnime) {
                        navigator.push(GlobalAnimeSearchScreen(item.title))
                    } else {
                        navigator.push(GlobalMangaSearchScreen(item.title))
                    }
                } else if (item.isStarted) {
                    screenModel.continueWatchingOrReading(
                        context = context,
                        continueItem = ContinueWatchingItem(
                            id = item.id,
                            title = item.title,
                            thumbnailUrl = item.thumbnailUrl,
                            isAnime = item.isAnime,
                            lastSeen = 0L,
                            progressText = "",
                            mediaItem = item,
                        ),
                        navigator = navigator,
                    )
                } else if (item.isAnime) {
                    navigator.push(AnimeScreen(item.id))
                } else {
                    navigator.push(MangaScreen(item.id))
                }
            },
            onContinueClick = { continueItem ->
                screenModel.continueWatchingOrReading(context, continueItem, navigator)
            },
            onRefresh = {
                screenModel.refresh()
            },
        )
    }
}
