package eu.kanade.tachiyomi.ui.updates

import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.AppBarActions
import eu.kanade.presentation.updates.RecentControls
import eu.kanade.presentation.updates.RecentMode
import eu.kanade.presentation.util.Tab
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.download.DownloadsTab
import eu.kanade.tachiyomi.ui.history.anime.animeHistoryTab
import eu.kanade.tachiyomi.ui.history.manga.mangaHistoryTab
import eu.kanade.tachiyomi.ui.main.MainActivity
import eu.kanade.tachiyomi.ui.updates.anime.animeUpdatesTab
import eu.kanade.tachiyomi.ui.updates.manga.mangaUpdatesTab
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

data object UpdatesTab : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val isSelected = LocalTabNavigator.current.current.key == key
            val image = AnimatedImageVector.animatedVectorResource(R.drawable.anim_recents_enter)
            return TabOptions(
                index = 4u,
                title = stringResource(MR.strings.kitsux_recent),
                icon = rememberAnimatedVectorPainter(image, isSelected),
            )
        }
    override suspend fun onReselect(navigator: Navigator) {
        navigator.push(DownloadsTab)
    }

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val fromMore = LocalNavigator.currentOrThrow.lastItem == this

        val uiPreferences = remember { Injekt.get<UiPreferences>() }
        val showAnime by uiPreferences.showAnime().collectAsState()
        val showManga by uiPreferences.showManga().collectAsState()

        var animeSelected by rememberSaveable { mutableStateOf(showAnime) }
        val isAnime = showAnime && (animeSelected || !showManga)
        var mode by rememberSaveable { mutableStateOf(RecentMode.New) }
        var query by rememberSaveable(isAnime, mode) { mutableStateOf("") }
        val snackbar = remember { SnackbarHostState() }

        key(isAnime, mode) {
            val tab = when {
                mode == RecentMode.History && isAnime -> animeHistoryTab(context, fromMore, query)
                mode == RecentMode.History -> mangaHistoryTab(context, fromMore, query)
                isAnime -> animeUpdatesTab(context, fromMore, query, mode)
                else -> mangaUpdatesTab(context, fromMore, query, mode)
            }
            Scaffold(
                topBar = {
                    AppBar(
                        title = stringResource(MR.strings.kitsux_recent),
                        backgroundColor = MaterialTheme.colorScheme.background,
                        navigateUp = tab.navigateUp,
                        actions = { AppBarActions(tab.actions) },
                        actionModeCounter = tab.numberTitle,
                        onCancelActionMode = tab.cancelAction,
                        actionModeActions = { AppBarActions(tab.actions) },
                    )
                },
            ) { padding ->
                Column(
                    Modifier.fillMaxSize()
                        .padding(top = padding.calculateTopPadding())
                        .consumeWindowInsets(PaddingValues(top = padding.calculateTopPadding())),
                ) {
                    RecentControls(
                        mode = mode,
                        onModeChange = { mode = it },
                        isAnime = isAnime,
                        showAnime = showAnime,
                        showManga = showManga,
                        onTypeChange = { animeSelected = it },
                        query = query,
                        onQueryChange = { query = it },
                    )
                    tab.content(PaddingValues(bottom = padding.calculateBottomPadding()), snackbar)
                }
            }
        }

        LaunchedEffect(Unit) {
            (context as? MainActivity)?.ready = true
        }
    }
}
