package eu.kanade.tachiyomi.ui.home

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import tachiyomi.domain.history.anime.model.AnimeHistoryWithRelations

class ContinueAnimeHistoryTest {
    private fun entry(anime: Long, episode: Long) = AnimeHistoryWithRelations(
        id = episode,
        episodeId = episode,
        animeId = anime,
        title = "Anime $anime",
        episodeNumber = episode.toDouble(),
        seenAt = null,
        coverData = mockk(),
    )

    @Test
    fun `large history stops querying once fifteen played titles are found`() = runBlocking<Unit> {
        val history = (1L..500L).map { entry(it, it) }
        var queries = 0
        val selected = selectContinueAnimeHistory(history) {
            queries++
            true
        }
        selected shouldBe history.take(15)
        queries shouldBe 15
    }

    @Test
    fun `unplayed entries and duplicate titles preserve previous selection and order`() = runBlocking<Unit> {
        val history = listOf(entry(1, 11), entry(2, 21), entry(1, 12), entry(3, 31), entry(2, 22))
        val selected = selectContinueAnimeHistory(history) { it == 12L || it == 21L }
        selected shouldBe listOf(history[2], history[1])
    }

    @Test
    fun `unplayed titles do not consume the fifteen title limit`() = runBlocking<Unit> {
        val history = (1L..100L).map { entry(it, it) }
        val selected = selectContinueAnimeHistory(history) { it > 20 }
        selected shouldBe history.drop(20).take(15)
    }
}
