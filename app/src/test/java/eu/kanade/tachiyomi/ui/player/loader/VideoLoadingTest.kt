package eu.kanade.tachiyomi.ui.player.loader

import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.animesource.model.Hoster
import eu.kanade.tachiyomi.animesource.model.Video
import eu.kanade.tachiyomi.animesource.online.AnimeHttpSource
import eu.kanade.tachiyomi.ui.player.controls.components.sheets.HosterState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class VideoLoadingTest {
    @Test
    fun stalledHosterBecomesErrorAfterDeadline() = runTest {
        val source = mockk<AnimeHttpSource>()
        val hoster = Hoster(hosterName = "Slow")
        coEvery { source.getVideoList(hoster) } coAnswers { awaitCancellation() }

        assertInstanceOf(HosterState.Error::class.java, EpisodeLoader.loadHosterVideos(source, hoster))
        assertEquals(20_000L, currentTime)
    }

    @Test
    fun stalledResolutionReturnsNullForFallback() = runTest {
        val source = mockk<AnimeHttpSource>()
        val video = Video(videoUrl = "https://example.test/embed")
        coEvery { source.resolveVideo(video) } coAnswers { awaitCancellation() }

        assertNull(HosterLoader.getResolvedVideo(source, video))
        assertEquals(15_000L, currentTime)
    }

    @Test
    fun resolvedVideoKeepsPlaybackMetadata() = runTest {
        val source = mockk<AnimeHttpSource>()
        val video = Video(videoUrl = "https://example.test/embed")
        val resolved = Video(
            videoUrl = "https://example.test/video.m3u8",
            videoTitle = "1080p",
            mpvArgs = listOf("referrer" to "https://example.test"),
        )
        coEvery { source.resolveVideo(video) } returns resolved

        assertEquals(resolved.copy(initialized = true), HosterLoader.getResolvedVideo(source, video))
        assertEquals(0L, currentTime)
    }

    @Test
    fun parentCancellationIsNotConvertedIntoFailure() {
        assertThrows(CancellationException::class.java) {
            runTest {
                val source = mockk<AnimeHttpSource>()
                val video = Video(videoUrl = "https://example.test/embed")
                coEvery { source.resolveVideo(video) } throws CancellationException("episode changed")
                HosterLoader.getResolvedVideo(source, video)
            }
        }
    }

    @Test
    fun lazyHostersRemainUnloaded() = runTest {
        val source = mockk<AnimeSource>()
        val state = EpisodeLoader.loadHosterVideos(source, Hoster(hosterName = "Manual", lazy = true))

        assertInstanceOf(HosterState.Idle::class.java, state)
        assertEquals(0L, currentTime)
    }

    @Test
    fun availableVideoCanBeSelectedWhileAnotherHosterLoads() {
        val video = Video(videoUrl = "https://example.test/video.m3u8")
        val states = listOf(
            HosterState.Loading("Slow"),
            HosterState.Ready("Ready", listOf(video), listOf(Video.State.QUEUE)),
        )

        assertEquals(1 to 0, HosterLoader.selectBestVideo(states))
    }
}
