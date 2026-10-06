package eu.kanade.tachiyomi.data.download

import eu.kanade.tachiyomi.data.download.anime.model.AnimeDownload
import eu.kanade.tachiyomi.data.download.manga.model.MangaDownload
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class DownloadRetryTest {
    private fun manga(id: Long, state: MangaDownload.State) = MangaDownload(
        mockk(),
        mockk(),
        mockk {
            every { this@mockk.id } returns id
        },
    ).apply {
        status = state
        errorMessage = "failure"
    }

    private fun anime(id: Long, state: AnimeDownload.State) = AnimeDownload(
        mockk(),
        mockk(),
        mockk {
            every { this@mockk.id } returns id
        },
    ).apply {
        status = state
        errorMessage = "failure"
    }

    @Test
    fun `individual retry does not restart other errors or interrupt active entries`() {
        val mangas = listOf(
            manga(1, MangaDownload.State.ERROR),
            manga(2, MangaDownload.State.ERROR),
            manga(3, MangaDownload.State.DOWNLOADING),
            manga(4, MangaDownload.State.DOWNLOADED),
        )
        prepareMangaRetry(mangas, 1) shouldBe true
        mangas.map { it.status } shouldBe listOf(
            MangaDownload.State.QUEUE,
            MangaDownload.State.ERROR,
            MangaDownload.State.DOWNLOADING,
            MangaDownload.State.DOWNLOADED,
        )
        mangas[0].errorMessage shouldBe null
        mangas[1].errorMessage shouldBe "failure"
        val animes = listOf(
            anime(1, AnimeDownload.State.ERROR),
            anime(2, AnimeDownload.State.ERROR),
            anime(3, AnimeDownload.State.DOWNLOADING),
            anime(4, AnimeDownload.State.DOWNLOADED),
        )
        prepareAnimeRetry(animes, 1) shouldBe true
        animes.map { it.status } shouldBe listOf(
            AnimeDownload.State.QUEUE,
            AnimeDownload.State.ERROR,
            AnimeDownload.State.DOWNLOADING,
            AnimeDownload.State.DOWNLOADED,
        )
        animes[0].errorMessage shouldBe null
        animes[1].errorMessage shouldBe "failure"
    }

    @Test
    fun `missing completed and active downloads cannot be retried`() {
        val mangas = listOf(manga(1, MangaDownload.State.DOWNLOADED), manga(2, MangaDownload.State.DOWNLOADING))
        val animes = listOf(anime(1, AnimeDownload.State.DOWNLOADED), anime(2, AnimeDownload.State.DOWNLOADING))
        listOf(1L, 2L, 99L).forEach {
            prepareMangaRetry(mangas, it) shouldBe false
            prepareAnimeRetry(animes, it) shouldBe false
        }
        mangas[0].status shouldBe MangaDownload.State.DOWNLOADED
        animes[1].status shouldBe AnimeDownload.State.DOWNLOADING
    }

    @Test
    fun `worker retry accepts a pending entry restored after process restart`() {
        val m = manga(1, MangaDownload.State.NOT_DOWNLOADED)
        val a = anime(1, AnimeDownload.State.NOT_DOWNLOADED)
        prepareMangaRetry(listOf(m), 1) shouldBe true
        prepareAnimeRetry(listOf(a), 1) shouldBe true
        m.status shouldBe MangaDownload.State.QUEUE
        a.status shouldBe AnimeDownload.State.QUEUE
    }
}
