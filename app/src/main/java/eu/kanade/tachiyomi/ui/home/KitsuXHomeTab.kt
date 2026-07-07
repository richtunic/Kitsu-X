package eu.kanade.tachiyomi.ui.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.PermissionChecker
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.TabOptions
import eu.kanade.presentation.home.HomeScreenContent
import eu.kanade.presentation.util.Tab
import eu.kanade.tachiyomi.ui.browse.anime.source.globalsearch.GlobalAnimeSearchScreen
import eu.kanade.tachiyomi.ui.browse.manga.source.globalsearch.GlobalMangaSearchScreen
import eu.kanade.tachiyomi.ui.entries.anime.AnimeScreen
import eu.kanade.tachiyomi.ui.entries.manga.MangaScreen
import kotlinx.coroutines.launch
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.i18n.MR

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
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                screenModel.refresh()
                scope.launch {
                    snackbarHostState.showSnackbar(context.stringResource(MR.strings.updating_library))
                }
            } else {
                screenModel.refresh()
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            contentWindowInsets = WindowInsets(0),
        ) { contentPadding ->
            HomeScreenContent(
                state = state,
                modifier = Modifier.padding(contentPadding),
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
                onRemoveContinueItem = { continueItem ->
                    screenModel.removeFromContinueWatching(continueItem)
                },
                onRefresh = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        PermissionChecker.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS,
                        ) != PermissionChecker.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        screenModel.refresh()
                        scope.launch {
                            snackbarHostState.showSnackbar(context.stringResource(MR.strings.updating_library))
                        }
                    }
                },
            )
        }
    }
}
