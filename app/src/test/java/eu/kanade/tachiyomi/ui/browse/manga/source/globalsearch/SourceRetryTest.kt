package eu.kanade.tachiyomi.ui.browse.manga.source.globalsearch

import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.model.ScreenModelStore
import eu.kanade.domain.source.service.SourcePreferences
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.tachiyomi.source.CatalogueSource
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.addSingleton
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class, InternalVoyagerApi::class)
class SourceRetryTest {
    private val sources = (1L..2L).map { id ->
        mockk<CatalogueSource> {
            every { this@mockk.id } returns id
            every { name } returns "Source $id"
            every { lang } returns "en"
            every { getFilterList() } returns FilterList()
        }
    }
    private lateinit var model: MangaSearchScreenModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        Injekt.addSingleton(mockk<UiPreferences>(relaxed = true))
        val preferences = mockk<SourcePreferences> {
            every { enabledLanguages().get() } returns setOf("en")
            every { disabledMangaSources().get() } returns emptySet()
            every { pinnedMangaSources().get() } returns emptySet()
            every { globalSearchFilterState().changes() } returns flowOf(false)
        }
        model = ScreenModelStore.getOrPut("source-retry-test", null) {
            object : MangaSearchScreenModel(
                initialState = MangaSearchScreenModel.State(searchQuery = "first"),
                sourcePreferences = preferences,
                sourceManager = mockk(),
                extensionManager = mockk(),
                networkToLocalManga = mockk(),
                getManga = mockk(),
                preferences = preferences,
            ) {
                override fun getEnabledSources() = sources
            }
        }
    }

    @AfterEach
    fun tearDown() {
        ScreenModelStore.onDisposeNavigator("source-retry-test")
        Dispatchers.resetMain()
    }

    @Test
    fun `retry keeps successful source results and does not request them again`() = runBlocking {
        val retryGate = CompletableDeferred<Unit>()
        var attempts = 0
        coEvery { sources[0].getSearchManga(any(), any(), any()) } coAnswers {
            if (++attempts == 1) throw IOException("first attempt")
            retryGate.await()
            MangasPage(emptyList(), false)
        }
        coEvery { sources[1].getSearchManga(any(), any(), any()) } returns MangasPage(emptyList(), false)
        model.search()
        withTimeout(5000) {
            model.state.first { it.items.values.none { value -> value is MangaSearchItemResult.Loading } }
        }
        val successful = model.state.value.items[sources[1]]
        model.retrySource(sources[0])
        model.retrySource(sources[0])
        model.state.value.items[sources[0]] shouldBe MangaSearchItemResult.Loading
        model.state.value.items[sources[1]] shouldBe successful
        retryGate.complete(Unit)
        withTimeout(5000) {
            model.state.first { it.items.values.all { value -> value is MangaSearchItemResult.Success } }
        }
        coVerify(exactly = 1) { sources[1].getSearchManga(any(), any(), any()) }
        coVerify(exactly = 2) { sources[0].getSearchManga(any(), any(), any()) }
    }

    @Test
    fun `new search cancels a pending retry so old results cannot overwrite it`() = runBlocking {
        val retryStarted = CompletableDeferred<Unit>()
        val retryCancelled = CompletableDeferred<Unit>()
        val retryGate = CompletableDeferred<Unit>()
        var firstAttempts = 0
        coEvery { sources[0].getSearchManga(any(), any(), any()) } coAnswers {
            if (secondArg<String>() == "new") throw IOException("new query")
            if (++firstAttempts == 1) throw IOException("first attempt")
            retryStarted.complete(Unit)
            try {
                retryGate.await()
                MangasPage(emptyList(), false)
            } catch (e: CancellationException) {
                retryCancelled.complete(Unit)
                throw e
            }
        }
        coEvery { sources[1].getSearchManga(any(), any(), any()) } throws IOException("source unavailable")
        model.search()
        withTimeout(5000) {
            model.state.first { it.items.values.all { value -> value is MangaSearchItemResult.Error } }
        }
        model.retrySource(sources[0])
        withTimeout(5000) { retryStarted.await() }
        model.updateSearchQuery("new")
        model.search()
        withTimeout(5000) { retryCancelled.await() }
        withTimeout(5000) {
            model.state.first { it.items.values.all { value -> value is MangaSearchItemResult.Error } }
        }
        (model.state.value.items[sources[0]] as MangaSearchItemResult.Error).throwable.message shouldBe "new query"
        coVerify(exactly = 1) { sources[0].getSearchManga(any(), "new", any()) }
    }
}
