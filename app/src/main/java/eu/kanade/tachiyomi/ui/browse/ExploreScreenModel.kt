package eu.kanade.tachiyomi.ui.browse

import android.content.Context
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.network.awaitSuccess
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
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

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
            var requestedNetwork = false
            suspend fun section(path: String, fallback: List<ExploreAnime>): List<ExploreAnime> {
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
                } catch (_: Exception) {
                    failed = true
                    fallback
                }
            }

            val season = section("seasons/now?limit=15", previous.season)
            val upcoming = section("seasons/upcoming?limit=15", previous.upcoming)
            val trending = section("top/anime?limit=15", previous.trending)
            mutableState.value = ExploreState(
                season = season,
                upcoming = upcoming,
                trending = trending,
                isLoading = false,
                hasError = failed,
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
)

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
) {
    val imageUrl: String?
        get() = images?.jpg?.largeImageUrl ?: images?.jpg?.imageUrl
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
