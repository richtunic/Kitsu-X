package eu.kanade.tachiyomi.ui.download

import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.model.ScreenModelStore
import eu.kanade.tachiyomi.data.download.anime.AnimeDownloadManager
import eu.kanade.tachiyomi.data.download.anime.model.AnimeDownload
import eu.kanade.tachiyomi.data.download.manga.MangaDownloadManager
import eu.kanade.tachiyomi.data.download.manga.model.MangaDownload
import eu.kanade.tachiyomi.ui.download.anime.AnimeDownloadQueueScreenModel
import eu.kanade.tachiyomi.ui.download.manga.MangaDownloadQueueScreenModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class, InternalVoyagerApi::class)
class DownloadRetrySelectionTest {
    @Test
    fun `retry targets one entry and a stale error dialog cannot recreate a removed download`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val manga = MangaDownload(
                mockk {
                    every { id } returns 1L
                    every { name } returns "Source"
                },
                mockk(),
                mockk { every { id } returns 10L },
            ).apply { status = MangaDownload.State.ERROR }
            val anime = AnimeDownload(
                mockk {
                    every { id } returns 2L
                    every { name } returns "Source"
                },
                mockk(),
                mockk { every { id } returns 20L },
            ).apply { status = AnimeDownload.State.ERROR }
            val mangaQueue = MutableStateFlow(listOf(manga))
            val animeQueue = MutableStateFlow(listOf(anime))
            val mangaManager = mockk<MangaDownloadManager>(relaxed = true) {
                every { queueState } returns mangaQueue
                every { isDownloaderRunning } returns MutableStateFlow(false)
            }
            val animeManager = mockk<AnimeDownloadManager>(relaxed = true) {
                every { queueState } returns animeQueue
                every { isDownloaderRunning } returns MutableStateFlow(false)
            }
            val m = ScreenModelStore.getOrPut("retry-selection-test", "manga") {
                MangaDownloadQueueScreenModel(mangaManager)
            }
            val a = ScreenModelStore.getOrPut("retry-selection-test", "anime") {
                AnimeDownloadQueueScreenModel(animeManager)
            }
            m.retryDownload(manga)
            a.retryDownload(anime)
            mangaQueue.value = emptyList()
            animeQueue.value = emptyList()
            m.retryDownload(manga)
            a.retryDownload(anime)
            verify(exactly = 1) { mangaManager.retryDownload(10L) }
            verify(exactly = 1) { animeManager.retryDownload(20L) }
            verify(exactly = 0) { mangaManager.startDownloads() }
            verify(exactly = 0) { animeManager.startDownloads() }
        } finally {
            ScreenModelStore.onDisposeNavigator("retry-selection-test")
            runCurrent()
            Dispatchers.resetMain()
        }
    }
}
