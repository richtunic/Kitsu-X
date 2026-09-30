package eu.kanade.tachiyomi.ui.browse

import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.presentation.components.TabbedScreen
import eu.kanade.presentation.components.TabContent
import eu.kanade.presentation.browse.ExploreDiscover
import eu.kanade.presentation.util.Tab
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.browse.anime.extension.AnimeExtensionsScreenModel
import eu.kanade.tachiyomi.ui.browse.anime.extension.animeExtensionsTab
import eu.kanade.tachiyomi.ui.browse.anime.migration.sources.migrateAnimeSourceTab
import eu.kanade.tachiyomi.ui.browse.anime.source.animeSourcesTab
import eu.kanade.tachiyomi.ui.browse.anime.source.globalsearch.GlobalAnimeSearchScreen
import eu.kanade.tachiyomi.ui.browse.manga.source.globalsearch.GlobalMangaSearchScreen
import eu.kanade.tachiyomi.ui.browse.manga.extension.MangaExtensionsScreenModel
import eu.kanade.tachiyomi.ui.browse.manga.extension.mangaExtensionsTab
import eu.kanade.tachiyomi.ui.browse.manga.migration.sources.migrateMangaSourceTab
import eu.kanade.tachiyomi.ui.browse.manga.source.mangaSourcesTab
import eu.kanade.tachiyomi.ui.main.MainActivity
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import tachiyomi.presentation.core.util.collectAsState as collectPreferencesAsState

data object BrowseTab : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val isSelected = LocalTabNavigator.current.current is BrowseTab
            val image = AnimatedImageVector.animatedVectorResource(R.drawable.anim_browse_enter)
            return TabOptions(
                index = 3u,
                title = stringResource(MR.strings.browse),
                icon = rememberAnimatedVectorPainter(image, isSelected),
            )
        }

    // TODO: Find a way to let it open Global Anime/Manga Search depending on what Tab(e.g. Anime/Manga Source Tab) is open
    override suspend fun onReselect(navigator: Navigator) {
        navigator.push(GlobalAnimeSearchScreen())
    }

    private enum class ExtensionDestination { Anime, Manga }
    private val switchToExtensionChannel = Channel<ExtensionDestination>(1, BufferOverflow.DROP_OLDEST)

    fun showExtension() {
        switchToExtensionChannel.trySend(ExtensionDestination.Manga)
    }

    fun showAnimeExtension() {
        switchToExtensionChannel.trySend(ExtensionDestination.Anime)
    }

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val exploreScreenModel = rememberScreenModel { ExploreScreenModel() }
        val exploreState by exploreScreenModel.state.collectAsState()

        // Hoisted for extensions tab's search bar
        val mangaExtensionsScreenModel = rememberScreenModel { MangaExtensionsScreenModel() }
        val mangaExtensionsState by mangaExtensionsScreenModel.state.collectAsState()

        val animeExtensionsScreenModel = rememberScreenModel { AnimeExtensionsScreenModel() }
        val animeExtensionsState by animeExtensionsScreenModel.state.collectAsState()

        val uiPreferences = remember { Injekt.get<UiPreferences>() }
        val showAnime by uiPreferences.showAnime().collectPreferencesAsState()
        val showManga by uiPreferences.showManga().collectPreferencesAsState()

        var animeExtensionIndex = -1
        var mangaExtensionIndex = -1
        val tabs = buildList {
            add(
                TabContent(
                    titleRes = MR.strings.kitsux_explore_discover,
                    content = { _, _ ->
                        ExploreDiscover(
                            state = exploreState,
                            onRetry = exploreScreenModel::refresh,
                            onSearchAnime = { navigator.push(GlobalAnimeSearchScreen(it)) },
                            onSearchManga = { navigator.push(GlobalMangaSearchScreen(it)) },
                        )
                    },
                ),
            )
            if (showAnime) {
                add(animeSourcesTab().copy(searchManga = false))
            }
            if (showManga) {
                add(mangaSourcesTab().copy(searchManga = true))
            }
            if (showAnime) {
                animeExtensionIndex = size
                add(animeExtensionsTab(animeExtensionsScreenModel).copy(searchManga = false))
            }
            if (showManga) {
                mangaExtensionIndex = size
                add(mangaExtensionsTab(mangaExtensionsScreenModel).copy(searchManga = true))
            }
            if (showAnime) {
                add(migrateAnimeSourceTab().copy(searchManga = false))
            }
            if (showManga) {
                add(migrateMangaSourceTab().copy(searchManga = true))
            }
        }.toPersistentList()

        val state = rememberPagerState { tabs.size }

        TabbedScreen(
            titleRes = MR.strings.browse,
            tabs = tabs,
            state = state,
            mangaSearchQuery = mangaExtensionsState.searchQuery,
            onChangeMangaSearchQuery = mangaExtensionsScreenModel::search,
            animeSearchQuery = animeExtensionsState.searchQuery,
            onChangeAnimeSearchQuery = animeExtensionsScreenModel::search,
            scrollable = true,
        )
        LaunchedEffect(animeExtensionIndex, mangaExtensionIndex) {
            switchToExtensionChannel.receiveAsFlow()
                .collectLatest { destination ->
                    val index = when (destination) {
                        ExtensionDestination.Anime -> animeExtensionIndex
                        ExtensionDestination.Manga -> mangaExtensionIndex
                    }
                    if (index >= 0) state.scrollToPage(index)
                }
        }

        LaunchedEffect(Unit) {
            (context as? MainActivity)?.ready = true
        }
    }
}
