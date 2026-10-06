package eu.kanade.tachiyomi.ui.download

import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.model.ScreenModelStore
import eu.kanade.tachiyomi.data.download.anime.AnimeDownloadManager
import eu.kanade.tachiyomi.data.download.anime.model.AnimeDownload
import eu.kanade.tachiyomi.data.download.manga.MangaDownloadManager
import eu.kanade.tachiyomi.data.download.manga.model.MangaDownload
import eu.kanade.tachiyomi.ui.download.anime.AnimeDownloadQueueScreenModel
import eu.kanade.tachiyomi.ui.download.manga.MangaDownloadQueueScreenModel
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class, InternalVoyagerApi::class)
class DownloadErrorsTest {
    @Test
    fun `error counts react to status changes retries and queue removal for both media types`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val manga = MangaDownload(
                mockk {
                    every { id } returns 1
                    every { name } returns "Manga source"
                },
                mockk(),
                mockk(),
            )
            val anime = AnimeDownload(
                mockk {
                    every { id } returns 2
                    every { name } returns "Anime source"
                },
                mockk(),
                mockk(),
            )
            val mangaQueue = MutableStateFlow(listOf(manga))
            val animeQueue = MutableStateFlow(listOf(anime))
            val mangaManager = mockk<MangaDownloadManager> {
                every { queueState } returns mangaQueue
                every { isDownloaderRunning } returns MutableStateFlow(false)
            }
            val animeManager = mockk<AnimeDownloadManager> {
                every { queueState } returns animeQueue
                every { isDownloaderRunning } returns MutableStateFlow(false)
            }
            val mangaModel = ScreenModelStore.getOrPut("download-errors-test", "manga") {
                MangaDownloadQueueScreenModel(mangaManager)
            }
            val animeModel = ScreenModelStore.getOrPut("download-errors-test", "anime") {
                AnimeDownloadQueueScreenModel(animeManager)
            }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { mangaModel.errorCount.collect {} }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { animeModel.errorCount.collect {} }
            runCurrent()
            mangaModel.errorCount.value shouldBe 0
            animeModel.errorCount.value shouldBe 0
            manga.status = MangaDownload.State.ERROR
            anime.status = AnimeDownload.State.ERROR
            runCurrent()
            mangaModel.errorCount.value shouldBe 1
            animeModel.errorCount.value shouldBe 1
            manga.status = MangaDownload.State.QUEUE
            anime.status = AnimeDownload.State.DOWNLOADING
            runCurrent()
            mangaModel.errorCount.value shouldBe 0
            animeModel.errorCount.value shouldBe 0
            manga.status = MangaDownload.State.ERROR
            anime.status = AnimeDownload.State.ERROR
            runCurrent()
            mangaQueue.value = emptyList()
            animeQueue.value = emptyList()
            runCurrent()
            mangaModel.errorCount.value shouldBe 0
            animeModel.errorCount.value shouldBe 0
        } finally {
            ScreenModelStore.onDisposeNavigator("download-errors-test")
            runCurrent()
            Dispatchers.resetMain()
        }
    }
}
