package eu.kanade.tachiyomi.ui.browse

import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.presentation.browse.ExploreDiscover
import eu.kanade.presentation.components.TabContent
import eu.kanade.presentation.components.TabbedScreen
import eu.kanade.presentation.util.Tab
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.browse.anime.source.globalsearch.GlobalAnimeSearchScreen
import eu.kanade.tachiyomi.ui.main.MainActivity
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

data object BrowseTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val image = AnimatedImageVector.animatedVectorResource(R.drawable.anim_browse_enter)
            return TabOptions(
                index = 3u,
                title = stringResource(MR.strings.browse),
                icon = rememberAnimatedVectorPainter(image, LocalTabNavigator.current.current is BrowseTab),
            )
        }

    private data class Destination(val page: Int, val anime: Boolean)
    private val destinations = Channel<Destination>(1, BufferOverflow.DROP_OLDEST)
    override suspend fun onReselect(navigator: Navigator) {
        destinations.trySend(Destination(0, true))
    }
    fun showExtension() {
        destinations.trySend(Destination(2, false))
    }
    fun showAnimeExtension() {
        destinations.trySend(Destination(2, true))
    }
    fun showSources(anime: Boolean) {
        destinations.trySend(Destination(1, anime))
    }

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val model = rememberScreenModel { ExploreScreenModel(context) }
        val discover by model.state.collectAsState()
        val preferences = remember { Injekt.get<UiPreferences>() }
        var anime by rememberSaveable { mutableStateOf(preferences.showAnime().get()) }
        val pager = rememberPagerState { 4 }
        val tabs = persistentListOf(
            TabContent(
                titleRes = MR.strings.kitsux_explore_discover,
                content = { padding, _ ->
                    ExploreDiscover(
                        state = discover,
                        onRetry = { model.refresh(force = true) },
                        onSearchAnime = { navigator.push(GlobalAnimeSearchScreen(it)) },
                        bottomPadding = padding.calculateBottomPadding(),
                    )
                },
            ),
            TabContent(
                titleRes = MR.strings.action_search,
                content = { padding, _ -> ExploreSearchPage(anime, { anime = it }, padding) },
            ),
            TabContent(
                titleRes = MR.strings.label_extensions,
                content = { padding, _ -> ExploreExtensionsPage(anime, padding) },
            ),
            TabContent(
                titleRes = MR.strings.kitsux_migrations,
                content = { padding, _ -> ExploreMigrationPage(padding) },
            ),
        )
        TabbedScreen(titleRes = MR.strings.browse, tabs = tabs, state = pager, scrollable = true)
        LaunchedEffect(Unit) {
            destinations.receiveAsFlow().collectLatest {
                anime = it.anime
                pager.scrollToPage(it.page)
            }
        }
        LaunchedEffect(Unit) { (context as? MainActivity)?.ready = true }
    }
}
