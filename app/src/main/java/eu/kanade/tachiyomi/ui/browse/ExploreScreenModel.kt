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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

class ExploreScreenModel(
    context: Context,
    private val networkHelper: NetworkHelper = Injekt.get(),
) : ScreenModel {
    private val cache = KitsuXIntelDatabase(context.applicationContext)
    private val json = Json { ignoreUnknownKeys = true }
    private val mutableState = MutableStateFlow(ExploreState())
    val state = mutableState.asStateFlow()

    init { refresh() }

    fun refresh(force: Boolean = false) {
        if (mutableState.value.isLoading) return
        screenModelScope.launch {
            val previous = mutableState.value
            mutableState.value = mutableState.value.copy(isLoading = true, hasError = false)
            var failed = false
            var serviceUnavailable = false
            var requestedNetwork = false
            suspend fun section(
                path: String,
                fallback: List<ExploreAnime>,
                aniListSection: String? = null,
            ): List<ExploreAnime> {
                return try {
                    withContext(Dispatchers.IO) {
                        val cached = if (force) null else cache.getCache("explore_$path", 6 * 60 * 60 * 1000L)
                        val body = cached ?: run {
                            if (requestedNetwork) delay(1200)
                            requestedNetwork = true
                            networkHelper.client.newCall(GET("https://api.jikan.moe/v4/$path"))
                                .awaitSuccess().body.string()
                                .also { cache.saveCache("explore_$path", it) }
                        }
                        json.decodeFromString<ExploreResponse>(body).data
                            .distinctBy { it.malId }
                            .take(15)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    val jikanUnavailable = (e is HttpException && (e.code == 429 || e.code in 500..599)) ||
                        e is IOException
                    if (jikanUnavailable && aniListSection != null) {
                        try {
                            val backup = withContext(Dispatchers.IO) { loadAniListSection(aniListSection, force) }
                            if (backup.isNotEmpty()) {
                                return backup
                            }
                        } catch (cancelled: CancellationException) {
                            throw cancelled
                        } catch (_: Exception) {
                            // Keep the previous result when both providers fail.
                        }
                    }
                    failed = true
                    serviceUnavailable = serviceUnavailable || jikanUnavailable
                    fallback
                }
            }

            val season = section("seasons/now?limit=15", previous.season, "RELEASING")
            val upcoming = section("seasons/upcoming?limit=15", previous.upcoming, "NOT_YET_RELEASED")
            val trending = section("top/anime?limit=15", previous.trending, "TRENDING")
            mutableState.value = ExploreState(
                season = season,
                upcoming = upcoming,
                trending = trending,
                isLoading = false,
                hasError = failed,
                isServiceUnavailable = serviceUnavailable,
            )
        }
    }

    private suspend fun loadAniListSection(section: String, force: Boolean): List<ExploreAnime> {
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
        val cached = if (force) null else cache.getCache(key, 6 * 60 * 60 * 1000L)
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
            ).awaitSuccess().body.string()
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

    override fun onDispose() {
        cache.close()
    }
}

data class ExploreState(
    val season: List<ExploreAnime> = emptyList(),
    val upcoming: List<ExploreAnime> = emptyList(),
    val trending: List<ExploreAnime> = emptyList(),
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
