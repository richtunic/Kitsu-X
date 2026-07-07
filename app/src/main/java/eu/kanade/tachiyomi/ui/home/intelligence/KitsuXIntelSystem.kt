package eu.kanade.tachiyomi.ui.home.intelligence

import android.content.Context
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.network.awaitSuccess
import eu.kanade.tachiyomi.ui.home.KitsuXMediaItem
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import logcat.LogPriority
import okhttp3.OkHttpClient
import tachiyomi.core.common.util.lang.launchIO
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.entries.anime.interactor.GetAnime
import tachiyomi.domain.entries.anime.model.Anime
import tachiyomi.domain.entries.manga.interactor.GetManga
import tachiyomi.domain.entries.manga.model.Manga
import tachiyomi.domain.history.anime.interactor.GetAnimeHistory
import tachiyomi.domain.history.manga.interactor.GetMangaHistory
import tachiyomi.domain.items.chapter.interactor.GetChapter
import tachiyomi.domain.items.episode.interactor.GetEpisode
import tachiyomi.domain.entries.anime.interactor.GetLibraryAnime
import tachiyomi.domain.entries.manga.interactor.GetLibraryManga
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.net.URLEncoder
import java.util.Date

object KitsuXIntelSystem {

    private lateinit var db: KitsuXIntelDatabase
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    private val getLibraryAnime: GetLibraryAnime by lazy { Injekt.get() }
    private val getLibraryManga: GetLibraryManga by lazy { Injekt.get() }
    private val getAnimeHistory: GetAnimeHistory by lazy { Injekt.get() }
    private val getMangaHistory: GetMangaHistory by lazy { Injekt.get() }
    private val getAnime: GetAnime by lazy { Injekt.get() }
    private val getManga: GetManga by lazy { Injekt.get() }
    private val getEpisode: GetEpisode by lazy { Injekt.get() }
    private val getChapter: GetChapter by lazy { Injekt.get() }
    private val networkHelper: NetworkHelper by lazy { Injekt.get() }

    private const val CACHE_EXPIRY = 24 * 60 * 60 * 1000L // 24 hours in millis
    private const val API_DELAY = 1200L // Rate limit prevention: 1.2s

    // Jikan Genre mapping
    private val JIKAN_GENRE_IDS = mapOf(
        "action" to 1,
        "adventure" to 2,
        "comedy" to 4,
        "drama" to 8,
        "fantasy" to 10,
        "romance" to 22,
        "sci-fi" to 24,
        "slice of life" to 36,
        "supernatural" to 37,
        "sports" to 30,
        "suspense" to 41,
        "comedia" to 4,
        "acción" to 1,
        "fantasía" to 10
    )

    // In-memory states to prevent duplicate updates
    private val processedAnimeFavorites = mutableSetOf<Long>()
    private val processedMangaFavorites = mutableSetOf<Long>()
    private val lastSeenEpisodeProgress = mutableMapOf<Long, Long>() // episodeId -> lastSecondSeen
    private val lastSeenChapterProgress = mutableMapOf<Long, Long>() // chapterId -> lastPageRead
    private val completedEpisodeIds = mutableSetOf<Long>()
    private val completedChapterIds = mutableSetOf<Long>()
    private var similarToLastWatchedJob: Job? = null
    private val malIdLookupJobs = mutableMapOf<String, Deferred<Long?>>()
    private val malIdLookupLock = Any()

    // Exposed flows for the UI
    val recommendedAnime = MutableStateFlow<List<KitsuXMediaItem>>(emptyList())
    val recommendedManga = MutableStateFlow<List<KitsuXMediaItem>>(emptyList())
    val topGenresList = MutableStateFlow<List<String>>(emptyList())
    val genreRecommendations = MutableStateFlow<Map<String, List<KitsuXMediaItem>>>(emptyMap())
    val similarToLastWatched = MutableStateFlow<List<KitsuXMediaItem>>(emptyList())
    val heroBannerAnimeList = MutableStateFlow<List<KitsuXMediaItem>>(emptyList())

    fun initialize(context: Context) {
        db = KitsuXIntelDatabase(context)
        logcat(LogPriority.INFO) { "KitsuX Intelligence System initialized." }

        // Start reactive background observers
        observeLibraryFavorites()
        observeHistoryProgress()

        // Fetch home metadata and recommendations after scanning the library
        scope.launch {
            scanLibraryToSeedProfile()
            delay(1000) // Delay startup requests slightly to avoid interrupting initial boot
            loadAllRecommendations()
        }
    }

    // --- Dynamic Recommendations Loading ---

    suspend fun loadAllRecommendations() {
        withContext(Dispatchers.IO) {
            try {
                logcat(LogPriority.INFO) { "Loading KitsuX home recommendations..." }

                // 1. Fetch Top Airing Anime for Hero Banner & Trending Anime
                val airingAnime = fetchTopAnimeCached()
                if (airingAnime.isNotEmpty()) {
                    recommendedAnime.value = airingAnime
                    // Translate descriptions for the top 5 banner items to Spanish!
                    val bannerItems = airingAnime.take(5).map { item ->
                        if (item.description.isNotBlank()) {
                            val translatedDesc = translateToSpanish(item.description)
                            item.copy(description = translatedDesc)
                        } else {
                            item
                        }
                    }
                    // Choose top 5 trending items for Hero Banner slider
                    heroBannerAnimeList.value = bannerItems
                }

                // 2. Fetch Top Manga
                val topManga = fetchTopMangaCached()
                if (topManga.isNotEmpty()) {
                    recommendedManga.value = topManga
                }

                // 3. Update top genres based on profile
                val topGenres = getTopUserGenres()
                topGenresList.value = topGenres

                // 4. Fetch recommendations by top genres
                val genreRecs = mutableMapOf<String, List<KitsuXMediaItem>>()
                topGenres.forEach { genre ->
                    val recs = fetchGenreRecommendationsCached(genre)
                    if (recs.isNotEmpty()) {
                        genreRecs[genre] = recs
                    }
                }
                genreRecommendations.value = genreRecs

                // 5. Fetch recommendations similar to last watched/read
                fetchSimilarToLastWatchedCached()

                logcat(LogPriority.INFO) { "KitsuX home recommendations loaded successfully." }
            } catch (e: Exception) {
                logcat(LogPriority.ERROR, e) { "Failed to load home recommendations" }
            }
        }
    }

    private suspend fun fetchTopAnimeCached(): List<KitsuXMediaItem> {
        val cached = db.getCache("top_anime", CACHE_EXPIRY)
        if (cached != null) {
            return parseMediaListJson(cached, isAnime = true)
        }

        delay(API_DELAY)
        return try {
            logcat(LogPriority.INFO) { "Fetching Top Airing Anime from Jikan..." }
            val response = networkHelper.client
                .newCall(GET("https://api.jikan.moe/v4/top/anime?filter=airing&limit=15"))
                .awaitSuccess()
            val body = response.body.string()
            db.saveCache("top_anime", body)
            parseMediaListJson(body, isAnime = true)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e) { "Failed to fetch top anime from Jikan" }
            emptyList()
        }
    }

    private suspend fun fetchTopMangaCached(): List<KitsuXMediaItem> {
        val cached = db.getCache("top_manga", CACHE_EXPIRY)
        if (cached != null) {
            return parseMediaListJson(cached, isAnime = false)
        }

        delay(API_DELAY)
        return try {
            logcat(LogPriority.INFO) { "Fetching Top Manga from Jikan..." }
            val response = networkHelper.client
                .newCall(GET("https://api.jikan.moe/v4/top/manga?filter=bypopularity&limit=15"))
                .awaitSuccess()
            val body = response.body.string()
            db.saveCache("top_manga", body)
            parseMediaListJson(body, isAnime = false)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e) { "Failed to fetch top manga from Jikan" }
            emptyList()
        }
    }

    private suspend fun fetchGenreRecommendationsCached(genre: String): List<KitsuXMediaItem> {
        val genreId = JIKAN_GENRE_IDS[genre.lowercase()] ?: return emptyList()
        val cacheKey = "genre_anime_$genreId"
        val cached = db.getCache(cacheKey, CACHE_EXPIRY)
        if (cached != null) {
            return parseMediaListJson(cached, isAnime = true)
        }

        delay(API_DELAY)
        return try {
            logcat(LogPriority.INFO) { "Fetching Genre Recommendations for $genre (ID $genreId) from Jikan..." }
            val response = networkHelper.client
                .newCall(GET("https://api.jikan.moe/v4/anime?genres=$genreId&order_by=score&sort=desc&limit=15"))
                .awaitSuccess()
            val body = response.body.string()
            db.saveCache(cacheKey, body)
            parseMediaListJson(body, isAnime = true)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e) { "Failed to fetch genre recommendations from Jikan" }
            emptyList()
        }
    }

    private fun fetchSimilarToLastWatchedCached() {
        if (similarToLastWatchedJob?.isActive == true) {
            return
        }

        similarToLastWatchedJob = combine(
            getAnimeHistory.subscribe(""),
            getMangaHistory.subscribe(""),
            ::Pair
        ).onEach { (animeHistory, mangaHistory) ->
            val latestAnime = animeHistory.firstOrNull()
            val latestManga = mangaHistory.firstOrNull()
            if (latestAnime == null && latestManga == null) return@onEach

            val isAnime = when {
                latestAnime == null -> false
                latestManga == null -> true
                else -> (latestAnime.seenAt?.time ?: 0) >= (latestManga.readAt?.time ?: 0)
            }

            val title = if (isAnime) latestAnime!!.title else latestManga!!.title
            val mediaId = if (isAnime) latestAnime!!.animeId else latestManga!!.mangaId

            scope.launchIO {
                val cachedKey = "similar_${if (isAnime) "anime" else "manga"}_$mediaId"
                val cached = db.getCache(cachedKey, CACHE_EXPIRY)
                if (cached != null) {
                    similarToLastWatched.value = parseRecommendationListJson(cached, isAnime = isAnime)
                    return@launchIO
                }

                // 1. Search MAL ID by title
                val malId = fetchMalIdByTitle(title, isAnime = isAnime)
                if (malId == null) return@launchIO

                // 2. Fetch recommendations for this MAL ID
                delay(API_DELAY)
                try {
                    logcat(LogPriority.INFO) { "Fetching similar recommendations for $title (ID $malId) from Jikan..." }
                    val typePath = if (isAnime) "anime" else "manga"
                    val response = networkHelper.client
                        .newCall(GET("https://api.jikan.moe/v4/$typePath/$malId/recommendations"))
                        .awaitSuccess()
                    val body = response.body.string()
                    db.saveCache(cachedKey, body)
                    similarToLastWatched.value = parseRecommendationListJson(body, isAnime = isAnime)
                } catch (e: Exception) {
                    logcat(LogPriority.ERROR, e) { "Failed to fetch similar recommendations from Jikan" }
                }
            }
        }.launchIn(scope)
    }

    private suspend fun fetchMalIdByTitle(title: String, isAnime: Boolean): Long? {
        val cacheKey = "mal_id_${if (isAnime) "anime" else "manga"}_${title.lowercase()}"
        val cached = db.getCache(cacheKey, CACHE_EXPIRY * 7) // Keep title mapping cached for longer (7 days)
        if (cached != null) {
            return cached.toLongOrNull()
        }

        val lookupJob = synchronized(malIdLookupLock) {
            malIdLookupJobs[cacheKey] ?: scope.async {
                fetchMalIdByTitleFromNetwork(cacheKey, title, isAnime)
            }.also {
                malIdLookupJobs[cacheKey] = it
            }
        }

        return try {
            lookupJob.await()
        } finally {
            synchronized(malIdLookupLock) {
                if (malIdLookupJobs[cacheKey] === lookupJob) {
                    malIdLookupJobs.remove(cacheKey)
                }
            }
        }
    }

    private suspend fun fetchMalIdByTitleFromNetwork(cacheKey: String, title: String, isAnime: Boolean): Long? {
        delay(API_DELAY)
        try {
            val typePath = if (isAnime) "anime" else "manga"
            val encodedTitle = URLEncoder.encode(title, "UTF-8")
            logcat(LogPriority.INFO) { "Searching Jikan mal_id for title '$title'..." }
            val response = networkHelper.client
                .newCall(GET("https://api.jikan.moe/v4/$typePath?q=$encodedTitle&limit=1"))
                .awaitSuccess()
            val parsed = json.decodeFromString<JikanResponse<List<JikanMedia>>>(response.body.string())
            val malId = parsed.data.firstOrNull()?.malId
            if (malId != null) {
                db.saveCache(cacheKey, malId.toString())
                return malId
            }
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e) { "Failed to search mal_id for title '$title'" }
        }
        return null
    }

    // --- Scoring Actions ---

fun onMediaViewed(genres: List<String>) {
    genres.mapNotNull(::normalizeRecommendationGenre).forEach { genre ->
            val points = when (genre.lowercase()) {
                "romance" -> 5
                "comedy", "comedia" -> 3
                "drama" -> 2
                "action", "acción" -> 1
                else -> 1
            }
            db.incrementGenreScore(genre, points)
        }
        // Reload recommendations after changes
        scope.launch {
            loadAllRecommendations()
        }
    }

    fun onMediaCompleted(genres: List<String>) {
        genres.mapNotNull(::normalizeRecommendationGenre).forEach { genre ->
            val points = when (genre.lowercase()) {
                "romance" -> 10
                "comedy", "comedia" -> 6
                "drama" -> 4
                else -> 2
            }
            db.incrementGenreScore(genre, points)
        }
        // Reload recommendations after changes
        scope.launch {
            loadAllRecommendations()
        }
    }

    fun onMediaFavorited(genres: List<String>, isAdded: Boolean) {
        if (!isAdded) return // We only add points when favorited
        genres.mapNotNull(::normalizeRecommendationGenre).forEach { genre ->
            val points = when (genre.lowercase()) {
                "romance" -> 20
                "comedy", "comedia" -> 10
                else -> 5
            }
            db.incrementGenreScore(genre, points)
        }
        // Reload recommendations after changes
        scope.launch {
            loadAllRecommendations()
        }
    }

    // --- Reactive Observers ---

    private fun observeLibraryFavorites() {
        getLibraryAnime.subscribe().onEach { libraryList ->
            val currentIds = libraryList.map { it.id }.toSet()
            if (processedAnimeFavorites.isEmpty()) {
                processedAnimeFavorites.addAll(currentIds)
                return@onEach
            }

            val addedIds = currentIds - processedAnimeFavorites
            addedIds.forEach { animeId ->
                val animeObj = libraryList.find { it.id == animeId }?.anime
                animeObj?.genre?.let { onMediaFavorited(it, isAdded = true) }
            }

            processedAnimeFavorites.clear()
            processedAnimeFavorites.addAll(currentIds)
        }.launchIn(scope)

        getLibraryManga.subscribe().onEach { libraryList ->
            val currentIds = libraryList.map { it.id }.toSet()
            if (processedMangaFavorites.isEmpty()) {
                processedMangaFavorites.addAll(currentIds)
                return@onEach
            }

            val addedIds = currentIds - processedMangaFavorites
            addedIds.forEach { mangaId ->
                val mangaObj = libraryList.find { it.id == mangaId }?.manga
                mangaObj?.genre?.let { onMediaFavorited(it, isAdded = true) }
            }

            processedMangaFavorites.clear()
            processedMangaFavorites.addAll(currentIds)
        }.launchIn(scope)
    }

    private fun observeHistoryProgress() {
        getAnimeHistory.subscribe("").onEach { history ->
            history.forEach { histItem ->
                scope.launchIO {
                    val episode = getEpisode.await(histItem.episodeId) ?: return@launchIO
                    val episodeId = histItem.episodeId

                    // Check progress increase
                    val lastSeen = lastSeenEpisodeProgress[episodeId] ?: 0L
                    if (episode.lastSecondSeen > lastSeen) {
                        lastSeenEpisodeProgress[episodeId] = episode.lastSecondSeen
                        val animeObj = getAnime.await(histItem.animeId)
                        animeObj?.genre?.let { onMediaViewed(it) }
                    }

                    // Check completion
                    if (episode.seen && !completedEpisodeIds.contains(episodeId)) {
                        completedEpisodeIds.add(episodeId)
                        val animeObj = getAnime.await(histItem.animeId)
                        animeObj?.genre?.let { onMediaCompleted(it) }
                    }
                }
            }
        }.launchIn(scope)

        getMangaHistory.subscribe("").onEach { history ->
            history.forEach { histItem ->
                scope.launchIO {
                    val chapter = getChapter.await(histItem.chapterId) ?: return@launchIO
                    val chapterId = histItem.chapterId

                    // Check progress increase
                    val lastRead = lastSeenChapterProgress[chapterId] ?: 0L
                    if (chapter.lastPageRead > lastRead) {
                        lastSeenChapterProgress[chapterId] = chapter.lastPageRead
                        val mangaObj = getManga.await(histItem.mangaId)
                        mangaObj?.genre?.let { onMediaViewed(it) }
                    }

                    // Check completion
                    if (chapter.read && !completedChapterIds.contains(chapterId)) {
                        completedChapterIds.add(chapterId)
                        val mangaObj = getManga.await(histItem.mangaId)
                        mangaObj?.genre?.let { onMediaCompleted(it) }
                    }
                }
            }
        }.launchIn(scope)
    }

    private fun getTopUserGenres(): List<String> {
        val scores = db.getGenreScores()
        return scores.entries
            .mapNotNull { (genre, score) ->
                normalizeRecommendationGenre(genre)?.let { it to score }
            }
            .sortedByDescending { it.second }
            .take(2)
            .map { it.first.capitalize() }
    }

    private fun normalizeRecommendationGenre(genre: String): String? {
        val normalized = genre.trim()
            .lowercase()
            .replace("-", " ")
            .replace(Regex("\\s+"), " ")

        if (normalized.isBlank()) return null
        return when (normalized) {
            "slice of life" -> "slice life"
            "sci fi", "science fiction" -> "sci-fi"
            "accion" -> "acción"
            "fantasia" -> "fantasía"
            else -> normalized
        }.takeIf { it in JIKAN_GENRE_IDS }
    }

    // --- JSON Parsers ---

    private fun parseMediaListJson(jsonStr: String, isAnime: Boolean): List<KitsuXMediaItem> {
        return try {
            val parsed = json.decodeFromString<JikanResponse<List<JikanMedia>>>(jsonStr)
            parsed.data.map { media ->
                KitsuXMediaItem(
                    id = media.malId,
                    title = media.title,
                    thumbnailUrl = media.images?.jpg?.largeImageUrl ?: media.images?.jpg?.imageUrl,
                    description = media.synopsis ?: "",
                    genres = media.genres.map { it.name },
                    rating = String.format("%.1f", media.score ?: 8.8),
                    isAnime = isAnime,
                    isRecommendation = true
                )
            }
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e) { "Error parsing JikanMedia JSON list" }
            emptyList()
        }
    }

    private fun parseRecommendationListJson(jsonStr: String, isAnime: Boolean): List<KitsuXMediaItem> {
        return try {
            val parsed = json.decodeFromString<JikanResponse<List<JikanRecommendation>>>(jsonStr)
            parsed.data.take(12).map { rec ->
                KitsuXMediaItem(
                    id = rec.entry.malId,
                    title = rec.entry.title,
                    thumbnailUrl = rec.entry.images?.jpg?.largeImageUrl ?: rec.entry.images?.jpg?.imageUrl,
                    description = "",
                    genres = emptyList(),
                    rating = "8.5",
                    isAnime = isAnime,
                    isRecommendation = true
                )
            }
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e) { "Error parsing JikanRecommendation JSON list" }
            emptyList()
        }
    }

    private suspend fun scanLibraryToSeedProfile() {
        val seeded = db.getCache("library_seeded", Long.MAX_VALUE)
        if (seeded == "true") {
            logcat(LogPriority.INFO) { "Library already seeded for recommendations. Skipping scan." }
            return
        }

        try {
            logcat(LogPriority.INFO) { "Seeding user intelligence profile from existing library items..." }
            val animeLibrary = getLibraryAnime.await()
            val mangaLibrary = getLibraryManga.await()

            val sqlDb = db.writableDatabase
            sqlDb.beginTransaction()
            try {
                animeLibrary.forEach { libItem ->
                    libItem.anime.genre?.forEach { genre ->
                    val normalizedGenre = normalizeRecommendationGenre(genre)
                    if (normalizedGenre != null) {
                            val cursor = sqlDb.rawQuery(
                                "SELECT score FROM user_genre_profile WHERE genre = ?",
                                arrayOf(normalizedGenre)
                            )
                            var currentScore = 0
                            if (cursor.moveToFirst()) {
                                currentScore = cursor.getInt(0)
                            }
                            cursor.close()

                            val points = when (normalizedGenre) {
                                "romance" -> 20
                                "comedy", "comedia" -> 10
                                else -> 5
                            }
                            val values = android.content.ContentValues().apply {
                                put("genre", normalizedGenre)
                                put("score", currentScore + points)
                            }
                            sqlDb.insertWithOnConflict("user_genre_profile", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
                        }
                    }
                }

                mangaLibrary.forEach { libItem ->
                    libItem.manga.genre?.forEach { genre ->
                    val normalizedGenre = normalizeRecommendationGenre(genre)
                    if (normalizedGenre != null) {
                            val cursor = sqlDb.rawQuery(
                                "SELECT score FROM user_genre_profile WHERE genre = ?",
                                arrayOf(normalizedGenre)
                            )
                            var currentScore = 0
                            if (cursor.moveToFirst()) {
                                currentScore = cursor.getInt(0)
                            }
                            cursor.close()

                            val points = when (normalizedGenre) {
                                "romance" -> 20
                                "comedy", "comedia" -> 10
                                else -> 5
                            }
                            val values = android.content.ContentValues().apply {
                                put("genre", normalizedGenre)
                                put("score", currentScore + points)
                            }
                            sqlDb.insertWithOnConflict("user_genre_profile", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
                        }
                    }
                }
                sqlDb.setTransactionSuccessful()
            } finally {
                sqlDb.endTransaction()
            }

            db.saveCache("library_seeded", "true")
            logcat(LogPriority.INFO) { "User intelligence profile seeded successfully from library." }
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e) { "Failed to seed profile from library" }
        }
    }

    private suspend fun translateToSpanish(text: String): String {
        if (text.isBlank()) return text
        try {
            val encodedText = URLEncoder.encode(text, "UTF-8")
            val url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=es&dt=t&q=$encodedText"
            val response = networkHelper.client.newCall(GET(url)).awaitSuccess()
            val body = response.body.string()

            val jsonArray = json.parseToJsonElement(body) as? JsonArray
            val segments = jsonArray?.firstOrNull() as? JsonArray
            if (segments != null) {
                val translatedBuilder = StringBuilder()
                segments.forEach { segment ->
                    val segmentArray = segment as? JsonArray
                    val translatedSegment = segmentArray?.firstOrNull()?.toString()?.trim('"')
                    if (translatedSegment != null) {
                        val cleanSegment = translatedSegment
                            .replace("\\n", "\n")
                            .replace("\\\"", "\"")
                        translatedBuilder.append(cleanSegment)
                    }
                }
                val translatedText = translatedBuilder.toString()
                if (translatedText.isNotBlank()) {
                    return translatedText
                }
            }
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e) { "Failed to translate text to Spanish" }
        }
        return text
    }
}

@Serializable
private data class JikanResponse<T>(
    val data: T
)

@Serializable
private data class JikanMedia(
    @SerialName("mal_id") val malId: Long,
    val title: String,
    val synopsis: String? = null,
    val score: Double? = null,
    val popularity: Int? = null,
    val images: JikanImages? = null,
    val genres: List<JikanGenre> = emptyList()
)

@Serializable
private data class JikanImages(
    val jpg: JikanImageUrls? = null
)

@Serializable
private data class JikanImageUrls(
    @SerialName("large_image_url") val largeImageUrl: String? = null,
    @SerialName("image_url") val imageUrl: String? = null
)

@Serializable
private data class JikanGenre(
    @SerialName("mal_id") val malId: Long,
    val name: String
)

@Serializable
private data class JikanRecommendation(
    val entry: JikanRecommendationEntry
)

@Serializable
private data class JikanRecommendationEntry(
    @SerialName("mal_id") val malId: Long,
    val title: String,
    val images: JikanImages? = null
)

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
