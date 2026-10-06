package eu.kanade.tachiyomi.ui.browse

import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.model.ScreenModelStore
import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.ui.home.intelligence.KitsuXIntelDatabase
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Timeout
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class, InternalVoyagerApi::class)
class ExploreScreenModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val replies = TestScope(dispatcher)
    private val cache = mockk<KitsuXIntelDatabase>(relaxed = true)
    private val client = mockk<OkHttpClient>()
    private val network = mockk<NetworkHelper>()
    private val requests = mutableListOf<Long>()
    private var cached = false
    private var cachedEmpty = false
    private var corrupt = false
    private var stale = false
    private var fail = false
    private lateinit var model: ExploreScreenModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { network.client } returns client
        every { cache.getCache(any(), any()) } answers {
            when {
                cachedEmpty && !firstArg<String>().startsWith("explore_anilist_") -> """{"data":[]}"""
                corrupt -> "invalid JSON"
                cached && firstArg<String>().startsWith("explore_anilist_") -> ANILIST_BODY
                stale &&
                    firstArg<String>().startsWith(
                        "explore_seasons",
                    ) &&
                    secondArg<Long>() > 6 * 60 * 60 * 1000L -> SAVED_BODY
                else -> null
            }
        }
        every { client.newCall(any()) } answers {
            val request = firstArg<okhttp3.Request>()
            requests += dispatcher.scheduler.currentTime
            val call = mockk<Call>(relaxed = true)
            val timeout = Timeout()
            every { call.timeout() } returns timeout
            every { call.enqueue(any()) } answers {
                val callback = firstArg<Callback>()
                replies.launch {
                    delay(if (request.url.encodedPath.contains("upcoming")) 3000 else 700)
                    timeout.timeoutNanos() shouldBe TimeUnit.SECONDS.toNanos(
                        if (request.url.host == "api.jikan.moe") 4 else 10,
                    )
                    if (fail) {
                        callback.onFailure(call, IOException("Provider unavailable"))
                    } else {
                        callback.onResponse(
                            call,
                            Response.Builder().request(request).protocol(Protocol.HTTP_1_1)
                                .code(200).message("OK").body(JIKAN_BODY.toResponseBody()).build(),
                        )
                    }
                }
            }
            call
        }
    }

    @AfterEach
    fun tearDown() {
        ScreenModelStore.onDisposeNavigator("explore-test")
        replies.cancel()
        Dispatchers.resetMain()
    }

    private fun createModel() {
        model = ScreenModelStore.getOrPut("explore-test", null) {
            ExploreScreenModel(mockk(), network, cache, dispatcher)
        }
    }

    @Test
    fun `fresh AniList cache is displayed without waiting for Jikan or making requests`() = runTest(dispatcher) {
        cached = true
        createModel()
        advanceUntilIdle()
        model.state.value.season.single().title shouldBe "Cached anime"
        model.state.value.upcoming.single().title shouldBe "Cached anime"
        model.state.value.trending.single().title shouldBe "Cached anime"
        model.state.value.isLoading shouldBe false
        verify(exactly = 0) { client.newCall(any()) }
    }

    @Test
    fun `fast sections publish while a slow section loads and Jikan starts stay spaced`() = runTest(dispatcher) {
        createModel()
        model.refresh()
        advanceTimeBy(1000)
        runCurrent()
        model.state.value.season.single().title shouldBe "Network anime"
        model.state.value.upcoming shouldBe emptyList()
        model.state.value.isLoading shouldBe true
        advanceTimeBy(2500)
        runCurrent()
        model.state.value.trending.single().title shouldBe "Network anime"
        model.state.value.upcoming shouldBe emptyList()
        advanceUntilIdle()
        model.state.value.upcoming.single().title shouldBe "Network anime"
        model.state.value.isLoading shouldBe false
        requests shouldBe listOf(0L, 1200L, 2400L)
        verify(exactly = 3) { cache.saveCache(any(), any()) }
    }

    @Test
    fun `failed forced refresh preserves previous rows and finishes with retry available`() = runTest(dispatcher) {
        cached = true
        createModel()
        advanceUntilIdle()
        val previous = model.state.value
        fail = true
        model.refresh(force = true)
        advanceUntilIdle()
        model.state.value.season shouldBe previous.season
        model.state.value.upcoming shouldBe previous.upcoming
        model.state.value.trending shouldBe previous.trending
        model.state.value.isLoading shouldBe false
        model.state.value.hasError shouldBe true
        model.state.value.isServiceUnavailable shouldBe true
        requests.size shouldBe 6
    }

    @Test
    fun `expired saved rows appear before network and are replaced on success`() = runTest(dispatcher) {
        stale = true
        createModel()
        runCurrent()
        model.state.value.season.single().title shouldBe "Saved anime"
        model.state.value.upcoming.single().title shouldBe "Saved anime"
        model.state.value.savedSections.size shouldBe 2
        model.state.value.isLoading shouldBe true
        advanceUntilIdle()
        model.state.value.season.single().title shouldBe "Network anime"
        model.state.value.savedSections shouldBe emptySet()
    }

    @Test
    fun `expired saved rows remain when both remote providers fail`() = runTest(dispatcher) {
        stale = true
        fail = true
        createModel()
        advanceUntilIdle()
        model.state.value.season.single().title shouldBe "Saved anime"
        model.state.value.savedSections.size shouldBe 2
        model.state.value.hasError shouldBe true
        model.state.value.isLoading shouldBe false
    }

    @Test
    fun `corrupt stored data does not block fresh provider results`() = runTest(dispatcher) {
        corrupt = true
        createModel()
        advanceUntilIdle()
        model.state.value.season.single().title shouldBe "Network anime"
        model.state.value.hasError shouldBe false
        requests.size shouldBe 3
    }

    @Test
    fun `fresh empty sections do not trigger unnecessary network requests`() = runTest(dispatcher) {
        cachedEmpty = true
        createModel()
        advanceUntilIdle()
        model.state.value.isLoading shouldBe false
        model.state.value.hasError shouldBe false
        requests shouldBe emptyList()
    }

    private companion object {
        const val SAVED_BODY = """{"data":[{"mal_id":2,"title":"Saved anime"}]}"""
        const val JIKAN_BODY = """{"data":[{"mal_id":1,"title":"Network anime"}]}"""
        const val ANILIST_BODY = """
            {"data":{"Page":{"media":[{"id":1,"idMal":1,"title":{"english":"Cached anime"}}]}}}
        """
    }
}
