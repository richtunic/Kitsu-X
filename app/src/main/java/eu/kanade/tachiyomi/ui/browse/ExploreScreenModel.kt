package eu.kanade.tachiyomi.ui.browse

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.network.awaitSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class ExploreScreenModel(
    private val networkHelper: NetworkHelper = Injekt.get(),
) : ScreenModel {
    private val json = Json { ignoreUnknownKeys = true }
    private val mutableState = MutableStateFlow(ExploreState())
    val state = mutableState.asStateFlow()

    init { refresh() }

    fun refresh() {
        if (mutableState.value.isLoading) return
        screenModelScope.launch {
            mutableState.value = mutableState.value.copy(isLoading = true, hasError = false)
            var failed = false
            suspend fun section(path: String): List<ExploreAnime> {
                return try {
                    val response = networkHelper.client.newCall(GET("https://api.jikan.moe/v4/$path")).awaitSuccess()
                    json.decodeFromString<ExploreResponse>(response.body.string()).data
                        .distinctBy { it.malId }
                        .take(15)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    failed = true
                    emptyList()
                }
            }

            val season = section("seasons/now?limit=15")
            delay(1200)
            val upcoming = section("seasons/upcoming?limit=15")
            delay(1200)
            val trending = section("top/anime?limit=15")
            mutableState.value = ExploreState(
                season = season,
                upcoming = upcoming,
                trending = trending,
                isLoading = false,
                hasError = failed,
            )
        }
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
