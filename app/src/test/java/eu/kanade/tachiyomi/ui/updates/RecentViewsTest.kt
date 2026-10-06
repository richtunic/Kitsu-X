package eu.kanade.tachiyomi.ui.updates

import eu.kanade.presentation.updates.anime.AnimeUpdatesUiModel
import eu.kanade.presentation.updates.manga.MangaUpdatesUiModel
import eu.kanade.tachiyomi.data.download.anime.model.AnimeDownload
import eu.kanade.tachiyomi.data.download.manga.model.MangaDownload
import eu.kanade.tachiyomi.ui.updates.anime.AnimeUpdatesItem
import eu.kanade.tachiyomi.ui.updates.anime.AnimeUpdatesScreenModel
import eu.kanade.tachiyomi.ui.updates.manga.MangaUpdatesItem
import eu.kanade.tachiyomi.ui.updates.manga.MangaUpdatesScreenModel
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.junit.jupiter.api.Test
import tachiyomi.domain.updates.anime.model.AnimeUpdatesWithRelations
import tachiyomi.domain.updates.manga.model.MangaUpdatesWithRelations

class RecentViewsTest {

    @Test
    fun `anime search stays within new items and accepts title or episode name`() {
        val state = AnimeUpdatesScreenModel.State(
            isLoading = false,
            items = persistentListOf(animeItem(1, 11, false), animeItem(1, 12, true), animeItem(2, 21, false)),
        )
        state.filtered("  SHARED  ", onlyNew = true).items.map { it.update.episodeId } shouldBe listOf(11L, 21L)
        state.filtered("Part 12", onlyNew = true).items.size shouldBe 0
        state.filtered("part 12", onlyNew = false).items.map { it.update.episodeId } shouldBe listOf(12L)
        state.filtered("missing", onlyNew = false).items.size shouldBe 0
        state.items.size shouldBe 3
    }

    @Test
    fun `anime grouping keeps same-title sources separate and preserves all items`() {
        val state = AnimeUpdatesScreenModel.State(
            isLoading = false,
            items = persistentListOf(animeItem(1, 11, false), animeItem(2, 21, false), animeItem(1, 12, true)),
        )
        val grouped = state.getUiModel(grouped = true)
        grouped.filterIsInstance<AnimeUpdatesUiModel.Group>().map { it.id to it.count } shouldBe
            listOf(1L to 2, 2L to 1)
        grouped.filterIsInstance<AnimeUpdatesUiModel.Item>().map { it.item.update.episodeId } shouldBe
            listOf(11L, 12L, 21L)
        grouped.filterIsInstance<AnimeUpdatesUiModel.Header>().size shouldBe 0
        state.getUiModel().filterIsInstance<AnimeUpdatesUiModel.Item>().map { it.item.update.episodeId } shouldBe
            listOf(11L, 21L, 12L)
    }

    @Test
    fun `five hundred updates retain every identity when filtering and grouping`() {
        val anime = AnimeUpdatesScreenModel.State(
            isLoading = false,
            items = (1L..500L).map { animeItem(it % 50, it, it % 2 == 0L) }.toPersistentList(),
        )
        val manga = MangaUpdatesScreenModel.State(
            isLoading = false,
            items = (1L..500L).map { mangaItem(it % 50, it, it % 2 == 0L) }.toPersistentList(),
        )
        anime.filtered("shared", onlyNew = true).items.size shouldBe 250
        manga.filtered("shared", onlyNew = true).items.size shouldBe 250
        val animeGroups = anime.getUiModel(grouped = true)
        val mangaGroups = manga.getUiModel(grouped = true)
        animeGroups.filterIsInstance<AnimeUpdatesUiModel.Group>().map { it.count } shouldBe List(50) { 10 }
        mangaGroups.filterIsInstance<MangaUpdatesUiModel.Group>().map { it.count } shouldBe List(50) { 10 }
        animeGroups.filterIsInstance<AnimeUpdatesUiModel.Item>().map { it.item.update.episodeId }.toSet() shouldBe
            (1L..500L).toSet()
        mangaGroups.filterIsInstance<MangaUpdatesUiModel.Item>().map { it.item.update.chapterId }.toSet() shouldBe
            (1L..500L).toSet()
    }

    private fun animeItem(id: Long, episodeId: Long, seen: Boolean) = AnimeUpdatesItem(
        update = AnimeUpdatesWithRelations(
            animeId = id,
            animeTitle = "Shared title",
            episodeId = episodeId,
            episodeName = "Part $episodeId",
            seen = seen,
            bookmark = false,
            fillermark = false,
            scanlator = null,
            totalSeconds = 0,
            lastSecondSeen = 0,
            sourceId = id,
            dateFetch = 1_759_600_000_000L,
            coverData = mockk(),
        ),
        downloadStateProvider = { AnimeDownload.State.NOT_DOWNLOADED },
        downloadProgressProvider = { 0 },
    )

    @Test
    fun `manga search stays within new items and accepts title or chapter name`() {
        val state = MangaUpdatesScreenModel.State(
            isLoading = false,
            items = persistentListOf(mangaItem(1, 11, false), mangaItem(1, 12, true), mangaItem(2, 21, false)),
        )
        state.filtered("  SHARED  ", onlyNew = true).items.map { it.update.chapterId } shouldBe listOf(11L, 21L)
        state.filtered("Part 12", onlyNew = true).items.size shouldBe 0
        state.filtered("part 12", onlyNew = false).items.map { it.update.chapterId } shouldBe listOf(12L)
        state.filtered("missing", onlyNew = false).items.size shouldBe 0
        state.items.size shouldBe 3
    }

    @Test
    fun `manga grouping keeps same-title sources separate and preserves all items`() {
        val state = MangaUpdatesScreenModel.State(
            isLoading = false,
            items = persistentListOf(mangaItem(1, 11, false), mangaItem(2, 21, false), mangaItem(1, 12, true)),
        )
        val grouped = state.getUiModel(grouped = true)
        grouped.filterIsInstance<MangaUpdatesUiModel.Group>().map { it.id to it.count } shouldBe
            listOf(1L to 2, 2L to 1)
        grouped.filterIsInstance<MangaUpdatesUiModel.Item>().map { it.item.update.chapterId } shouldBe
            listOf(11L, 12L, 21L)
        grouped.filterIsInstance<MangaUpdatesUiModel.Header>().size shouldBe 0
        state.getUiModel().filterIsInstance<MangaUpdatesUiModel.Item>().map { it.item.update.chapterId } shouldBe
            listOf(11L, 21L, 12L)
    }

    private fun mangaItem(id: Long, chapterId: Long, read: Boolean) = MangaUpdatesItem(
        update = MangaUpdatesWithRelations(
            mangaId = id,
            mangaTitle = "Shared title",
            chapterId = chapterId,
            chapterName = "Part $chapterId",
            read = read,
            bookmark = false,
            scanlator = null,
            lastPageRead = 0,
            sourceId = id,
            dateFetch = 1_759_600_000_000L,
            coverData = mockk(),
        ),
        downloadStateProvider = { MangaDownload.State.NOT_DOWNLOADED },
        downloadProgressProvider = { 0 },
    )
}
