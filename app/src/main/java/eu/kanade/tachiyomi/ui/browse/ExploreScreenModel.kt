package eu.kanade.tachiyomi.ui.browse

import android.content.Context
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.HttpException
import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.network.awaitSuccess
import eu.kanade.tachiyomi.network.jsonMime
import eu.kanade.tachiyomi.ui.home.intelligence.KitsuXIntelDatabase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.RequestBody.Companion.toRequestBody
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.io.IOException
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ExploreScreenModel(
    context: Context,
    private val networkHelper: NetworkHelper = Injekt.get(),
    private val cache: KitsuXIntelDatabase = KitsuXIntelDatabase(context.applicationContext),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ScreenModel {
    private val json = Json { ignoreUnknownKeys = true }
    private val mutableState = MutableStateFlow(ExploreState())
    val state = mutableState.asStateFlow()

    init {
        refresh()
    }

    fun refresh(force: Boolean = false) {
        if (mutableState.value.isLoading) return
        mutableState.update { it.copy(isLoading = true, hasError = false, isServiceUnavailable = false) }
        screenModelScope.launch {
            suspend fun section(
                path: String,
                aniListSection: String,
                networkDelay: Long,
                hasContent: ExploreState.() -> Boolean,
                publish: ExploreState.(List<ExploreAnime>) -> ExploreState,
            ) {
                try {
                    val result = withContext(ioDispatcher) {
                        val key = "explore_$path"
                        val cached = if (force) {
                            null
                        } else {
                            cache.getCache(key, CACHE_AGE)
                                ?.let { runCatching { decodeJikan(it) }.getOrNull() }
                        }
                        if (cached != null) return@withContext cached
                        if (!force) {
                            val backup = try {
                                loadAniListSection(aniListSection, force = false, cachedOnly = true)
                            } catch (e: CancellationException) {
                                throw e
                            } catch (_: Exception) {
                                emptyList()
                            }
                            if (backup.isNotEmpty()) return@withContext backup
                        }
                        val saved = cache.getCache(key, SAVED_CACHE_AGE)
                            ?.let { runCatching { decodeJikan(it) }.getOrNull() }
                            ?.takeIf { it.isNotEmpty() }
                            ?: try {
                                loadAniListSection(aniListSection, false, cachedOnly = true, maxAge = SAVED_CACHE_AGE)
                            } catch (e: CancellationException) {
                                throw e
                            } catch (_: Exception) {
                                emptyList()
                            }
                        if (saved.isNotEmpty()) {
                            mutableState.update {
                                if (it.hasContent()) {
                                    it
                                } else {
                                    it.publish(saved).copy(
                                        savedSections =
                                        it.savedSections + path,
                                    )
                                }
                            }
                        }
                        delay(networkDelay)
                        try {
                            val body = networkHelper.client.newCall(GET("https://api.jikan.moe/v4/$path"))
                                .apply { timeout().timeout(4, TimeUnit.SECONDS) }
                                .awaitSuccess().use { it.body.string() }
                            val items = decodeJikan(body)
                            cache.saveCache("explore_$path", body)
                            items
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            if (!isUnavailable(e)) throw e
                            loadAniListSection(aniListSection, force)
                        }
                    }
                    mutableState.update { it.publish(result).copy(savedSections = it.savedSections - path) }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    mutableState.update {
                        it.copy(hasError = true, isServiceUnavailable = it.isServiceUnavailable || isUnavailable(e))
                    }
                }
            }

            try {
                coroutineScope {
                    launch {
                        section("seasons/now?limit=15", "RELEASING", 0, { season.isNotEmpty() }) { copy(season = it) }
                    }
                    launch {
                        section("seasons/upcoming?limit=15", "NOT_YET_RELEASED", 1200, {
                            upcoming.isNotEmpty()
                        }) { copy(upcoming = it) }
                    }
                    launch {
                        section("top/anime?limit=15", "TRENDING", 2400, {
                            trending.isNotEmpty()
                        }) { copy(trending = it) }
                    }
                }
            } finally {
                mutableState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun decodeJikan(body: String): List<ExploreAnime> =
        json.decodeFromString<ExploreResponse>(body).data.distinctBy { it.malId }.take(15)

    private fun isUnavailable(error: Exception): Boolean =
        (error is HttpException && (error.code == 429 || error.code in 500..599)) || error is IOException

    private suspend fun loadAniListSection(
        section: String,
        force: Boolean,
        cachedOnly: Boolean = false,
        maxAge: Long = CACHE_AGE,
    ): List<ExploreAnime> {
        val calendar = Calendar.getInstance()
        val seasons = listOf("WINTER", "SPRING", "SUMMER", "FALL")
        val current = calendar.get(Calendar.MONTH) / 3
        val selected = if (section == "RELEASING") current else (current + 1) % seasons.size
        val year = calendar.get(Calendar.YEAR) + if (section == "NOT_YET_RELEASED" && selected == 0) 1 else 0
        val key = if (section == "TRENDING") {
            "explore_anilist_trending"
        } else {
            "explore_anilist_${section}_${seasons[selected]}_$year"
        }
        val arguments = if (section == "TRENDING") {
            "type: ANIME, sort: TRENDING_DESC"
        } else {
            "type: ANIME, season: ${seasons[selected]}, seasonYear: $year, status: $section, sort: POPULARITY_DESC"
        }
        val cached = if (force) null else cache.getCache(key, maxAge)
        if (cachedOnly && cached == null) return emptyList()
        val body = cached ?: run {
            val query = """
                query {
                    Page(page: 1, perPage: 15) {
                        media($arguments) {
                            id idMal title { romaji english } coverImage { extraLarge large }
                            genres season seasonYear
                        }
                    }
                }
            """.trimIndent()
            networkHelper.client.newCall(
                POST(
                    "https://graphql.anilist.co",
                    body = buildJsonObject { put("query", query) }.toString().toRequestBody(jsonMime),
                ),
            ).apply { timeout().timeout(10, TimeUnit.SECONDS) }
                .awaitSuccess().use { it.body.string() }
        }
        val media = json.decodeFromString<AniListExploreResponse>(body).data.page.media
        if (cached == null) cache.saveCache(key, body)
        return media.mapNotNull { media ->
            val title = media.title.english?.takeIf { it.isNotBlank() }
                ?: media.title.romaji?.takeIf { it.isNotBlank() }
                ?: return@mapNotNull null
            ExploreAnime(
                malId = media.idMal ?: -media.id,
                title = title,
                season = media.season?.lowercase(),
                year = media.seasonYear,
                genres = media.genres.orEmpty().map(::ExploreGenre),
                fallbackImageUrl = media.coverImage?.extraLarge ?: media.coverImage?.large,
            )
        }
    }

    private companion object {
        const val SAVED_CACHE_AGE = 7 * 24 * 60 * 60 * 1000L
        const val CACHE_AGE = 6 * 60 * 60 * 1000L
    }

    override fun onDispose() {
        cache.close()
    }
}

data class ExploreState(
    val season: List<ExploreAnime> = emptyList(),
    val upcoming: List<ExploreAnime> = emptyList(),
    val trending: List<ExploreAnime> = emptyList(),
    val savedSections: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val isServiceUnavailable: Boolean = false,
)

@Serializable
private data class AniListExploreResponse(val data: AniListExploreData)

@Serializable
private data class AniListExploreData(@SerialName("Page") val page: AniListExplorePage)

@Serializable
private data class AniListExplorePage(val media: List<AniListExploreMedia>)

@Serializable
private data class AniListExploreMedia(
    val id: Long,
    val idMal: Long? = null,
    val title: AniListExploreTitle,
    val coverImage: AniListExploreCover? = null,
    val genres: List<String>? = null,
    val season: String? = null,
    val seasonYear: Int? = null,
)

@Serializable
private data class AniListExploreTitle(val romaji: String? = null, val english: String? = null)

@Serializable
private data class AniListExploreCover(val extraLarge: String? = null, val large: String? = null)

@Serializable
private data class ExploreResponse(val data: List<ExploreAnime>)

@Serializable
data class ExploreAnime(
    @SerialName("mal_id") val malId: Long,
    val title: String,
    val season: String? = null,
    val year: Int? = null,
    val status: String? = null,
    val images: ExploreImages? = null,
    val genres: List<ExploreGenre> = emptyList(),
    val fallbackImageUrl: String? = null,
) {
    val imageUrl: String?
        get() = fallbackImageUrl ?: images?.jpg?.largeImageUrl ?: images?.jpg?.imageUrl
}

@Serializable
data class ExploreImages(val jpg: ExploreImageUrls? = null)

@Serializable
data class ExploreImageUrls(
    @SerialName("large_image_url") val largeImageUrl: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
)

@Serializable
data class ExploreGenre(val name: String)
