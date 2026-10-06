package eu.kanade.tachiyomi.ui.download.anime

import android.view.LayoutInflater
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import eu.kanade.presentation.download.DownloadEmptyState
import eu.kanade.presentation.download.DownloadErrorDialog
import eu.kanade.tachiyomi.data.download.anime.model.AnimeDownload
import eu.kanade.tachiyomi.databinding.DownloadListBinding
import kotlinx.coroutines.CoroutineScope
import tachiyomi.core.common.util.lang.launchUI
import tachiyomi.presentation.core.components.material.Scaffold
import kotlin.math.roundToInt

@Composable
fun AnimeDownloadQueueScreen(
    contentPadding: PaddingValues,
    scope: CoroutineScope,
    screenModel: AnimeDownloadQueueScreenModel,
    downloadList: List<AnimeDownloadHeaderItem>,
    nestedScrollConnection: NestedScrollConnection,
) {
    var errorDownload by remember { mutableStateOf<AnimeDownload?>(null) }
    errorDownload?.let { download ->
        DownloadErrorDialog(
            title = download.anime.title,
            part = download.episode.name,
            source = download.source.name,
            error = download.errorMessage,
            onRetry = {
                screenModel.retryDownload(download)
                errorDownload = null
            },
            onDismiss = { errorDownload = null },
        )
    }
    Scaffold {
        if (downloadList.isEmpty()) {
            DownloadEmptyState(
                isAnime = true,
                modifier = Modifier.padding(contentPadding),
            )
            return@Scaffold
        }

        val density = LocalDensity.current
        val layoutDirection = LocalLayoutDirection.current
        val left = with(density) { contentPadding.calculateLeftPadding(layoutDirection).toPx().roundToInt() }
        val top = with(density) { contentPadding.calculateTopPadding().toPx().roundToInt() }
        val right = with(density) { contentPadding.calculateRightPadding(layoutDirection).toPx().roundToInt() }
        val bottom = with(density) { contentPadding.calculateBottomPadding().toPx().roundToInt() }

        DisposableEffect(screenModel) {
            val statusJob = scope.launchUI {
                screenModel.getDownloadStatusFlow().collect(screenModel::onStatusChange)
            }
            val progressJob = scope.launchUI {
                screenModel.getDownloadProgressFlow().collect(screenModel::onUpdateDownloadedPages)
            }
            onDispose {
                statusJob.cancel()
                progressJob.cancel()
            }
        }

        Box(modifier = Modifier.nestedScroll(nestedScrollConnection)) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { context ->
                    val binding = DownloadListBinding.inflate(
                        LayoutInflater.from(context),
                    )
                    screenModel.controllerBinding = binding
                    screenModel.adapter = AnimeDownloadAdapter(screenModel.listener) { errorDownload = it }
                    binding.root.adapter = screenModel.adapter
                    screenModel.adapter?.isHandleDragEnabled = true
                    binding.root.layoutManager = LinearLayoutManager(
                        context,
                    )

                    ViewCompat.setNestedScrollingEnabled(binding.root, true)

                    binding.root
                },
                onReset = null,
                onRelease = { view -> screenModel.detachView(view) },
                update = { view ->
                    view
                        .updatePadding(
                            left = left,
                            top = top,
                            right = right,
                            bottom = bottom,
                        )

                    screenModel.adapter?.updateDataSet(downloadList)
                },
            )
        }
    }
}
