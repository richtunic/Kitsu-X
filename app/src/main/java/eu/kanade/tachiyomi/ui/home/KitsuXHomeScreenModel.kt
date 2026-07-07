package eu.kanade.tachiyomi.ui.home

import android.content.Context
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.tachiyomi.data.download.anime.AnimeDownloadManager
import eu.kanade.tachiyomi.data.download.manga.MangaDownloadManager
import eu.kanade.tachiyomi.data.library.anime.AnimeLibraryUpdateJob
import eu.kanade.tachiyomi.data.library.manga.MangaLibraryUpdateJob
import eu.kanade.tachiyomi.ui.home.intelligence.KitsuXIntelSystem
import eu.kanade.tachiyomi.ui.main.MainActivity
import eu.kanade.tachiyomi.ui.player.settings.PlayerPreferences
import eu.kanade.tachiyomi.ui.reader.ReaderActivity
import eu.kanade.tachiyomi.util.chapter.getNextUnread
import eu.kanade.tachiyomi.util.episode.getNextUnseen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logcat.LogPriority
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.category.anime.interactor.GetAnimeCategories
import tachiyomi.domain.category.manga.interactor.GetMangaCategories
import tachiyomi.domain.entries.anime.interactor.GetAnime
import tachiyomi.domain.entries.anime.interactor.GetLibraryAnime
import tachiyomi.domain.entries.anime.model.Anime
import tachiyomi.domain.entries.manga.interactor.GetLibraryManga
import tachiyomi.domain.entries.manga.interactor.GetManga
import tachiyomi.domain.entries.manga.model.Manga
import tachiyomi.domain.history.anime.interactor.GetAnimeHistory
import tachiyomi.domain.history.manga.interactor.GetMangaHistory
import tachiyomi.domain.items.chapter.interactor.GetChapter
import tachiyomi.domain.items.chapter.interactor.GetChaptersByMangaId
import tachiyomi.domain.items.episode.interactor.GetEpisode
import tachiyomi.domain.items.episode.interactor.GetEpisodesByAnimeId
import tachiyomi.domain.items.episode.model.Episode
import tachiyomi.domain.library.anime.LibraryAnime
import tachiyomi.domain.library.manga.LibraryManga
import tachiyomi.i18n.MR
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class KitsuXHomeScreenModel(
    private val context: Context,
    private val getLibraryAnime: GetLibraryAnime = Injekt.get(),
    private val getLibraryManga: GetLibraryManga = Injekt.get(),
    private val getAnimeHistory: GetAnimeHistory = Injekt.get(),
    private val getMangaHistory: GetMangaHistory = Injekt.get(),
    private val getAnimeCategories: GetAnimeCategories = Injekt.get(),
    private val getMangaCategories: GetMangaCategories = Injekt.get(),
    private val getEpisode: GetEpisode = Injekt.get(),
    private val getChapter: GetChapter = Injekt.get(),
    private val getEpisodesByAnimeId: GetEpisodesByAnimeId = Injekt.get(),
    private val getChaptersByMangaId: GetChaptersByMangaId = Injekt.get(),
    private val animeDownloadManager: AnimeDownloadManager = Injekt.get(),
    private val mangaDownloadManager: MangaDownloadManager = Injekt.get(),
    private val uiPreferences: UiPreferences = Injekt.get(),
    private val preferenceStore: PreferenceStore = Injekt.get(),
) : ScreenModel {

    private val animeCategoriesFlow = getAnimeCategories.subscribe()
    private val mangaCategoriesFlow = getMangaCategories.subscribe()

    private val getAnime: GetAnime = Injekt.get()
    private val getManga: GetManga = Injekt.get()
    private val hiddenContinueItemsPreference = preferenceStore.getStringSet(
        Preference.appStateKey("kitsux_hidden_continue_items"),
        emptySet(),
    )
    private val activeContinueActions = ConcurrentHashMap.newKeySet<String>()

    private val jikanFlow = combine(
        KitsuXIntelSystem.heroBannerAnimeList,
        KitsuXIntelSystem.recommendedAnime,
        KitsuXIntelSystem.recommendedManga,
        KitsuXIntelSystem.genreRecommendations,
        KitsuXIntelSystem.similarToLastWatched,
    ) { heroList, recAnime, recManga, genreRecs, similar ->
        JikanData(heroList, recAnime, recManga, genreRecs, similar)
    }

    private val categoriesAndJikanFlow = combine(
        combine(animeCategoriesFlow, mangaCategoriesFlow, ::Pair),
        jikanFlow,
        ::Pair,
    )

    private val preferencesFlow = combine(
        uiPreferences.showAnime().changes(),
        uiPreferences.showManga().changes(),
        uiPreferences.showRecommendations().changes(),
        uiPreferences.showHeroBanner().changes(),
    ) { showAnime, showManga, showRecommendations, showHeroBanner ->
        HomePreferenceState(showAnime, showManga, showRecommendations, showHeroBanner)
    }

    private val categoriesAndJikanAndPrefsFlow = combine(
        categoriesAndJikanFlow,
        preferencesFlow,
        ::Pair,
    )

    private val homeAuxiliaryFlow = combine(
        categoriesAndJikanAndPrefsFlow,
        hiddenContinueItemsPreference.changes(),
        ::Pair,
    )

    val state: StateFlow<KitsuXHomeState> = combine(
        getLibraryAnime.subscribe(),
        getLibraryManga.subscribe(),
        getAnimeHistory.subscribe(""),
        getMangaHistory.subscribe(""),
        homeAuxiliaryFlow,
    ) { libraryAnime, libraryManga, animeHistory, mangaHistory, auxiliaryData ->
        val (combinedData, hiddenContinueItems) = auxiliaryData
        val (categoriesAndJikan, prefsTriple) = combinedData
        val (categoriesPair, jikanData) = categoriesAndJikan
        val (animeCategories, mangaCategories) = categoriesPair
        val (showAnime, showManga, showRecommendations, showHeroBanner) = prefsTriple

        // 1. Process continue watching items (mix anime and manga history + library items with unseen count)
        val continueWatching = mutableListOf<ContinueWatchingItem>()
        val continueReading = mutableListOf<ContinueWatchingItem>()
        val addedMediaIds = mutableSetOf<Pair<Long, Boolean>>() // Pair(mediaId, isAnime)

        if (showAnime) {
            val animeHistoryLatest = animeHistory.groupBy { it.animeId }
                .map { (_, list) -> list.first() }
                .take(15)

            animeHistoryLatest.forEach { hist ->
                val libAnime = libraryAnime.find { it.id == hist.animeId }
                val anime = libAnime?.anime ?: getAnime.await(hist.animeId)
                if (anime == null) return@forEach

                val tempLibAnime = libAnime ?: LibraryAnime(
                    anime = anime,
                    category = 0,
                    totalCount = 1,
                    seenCount = 1,
                    bookmarkCount = 0,
                    fillermarkCount = 0,
                    latestUpload = 0,
                    episodeFetchedAt = 0,
                    lastSeen = hist.seenAt?.time ?: 0L,
                )

                val currentEpisode = getEpisode.await(hist.episodeId)
                val targetEpisode = currentEpisode?.takeUnless { it.seen }
                    ?: getEpisodesByAnimeId.await(anime.id).getNextUnseen(anime, animeDownloadManager)

                targetEpisode?.let { episode ->
                    val isNewEpisode = currentEpisode?.id != episode.id
                    continueWatching.add(
                        episode.toContinueWatchingItem(
                            anime = tempLibAnime,
                            lastSeen = if (isNewEpisode) {
                                maxOf(tempLibAnime.episodeFetchedAt, tempLibAnime.latestUpload).takeIf { it > 0 }
                                    ?: (hist.seenAt?.time ?: 0L)
                            } else {
                                hist.seenAt?.time ?: tempLibAnime.lastSeen
                            },
                            isNewEpisode = isNewEpisode,
                        ),
                    )
                    addedMediaIds.add(Pair(hist.animeId, true))
                }
            }
        }

        if (showManga) {
            val mangaHistoryLatest = mangaHistory.groupBy { it.mangaId }
                .map { (_, list) -> list.first() }
                .take(15)

            mangaHistoryLatest.forEach { hist ->
                val libManga = libraryManga.find { it.id == hist.mangaId }
                val manga = libManga?.manga ?: getManga.await(hist.mangaId)
                if (manga == null) return@forEach

                val hasUpdates = libManga?.let { it.unreadCount > 0 } ?: false
                val unreadVal = libManga?.unreadCount?.toInt() ?: 0

                // Calculate manga chapter progress from lastPageRead
                val chapter = getChapter.await(hist.chapterId)
                val isChapterPending = chapter != null && !chapter.read
                val hasNewChapters = hasUpdates || unreadVal > 0
                val targetChapter = chapter?.takeUnless { it.read }
                    ?: if (hasNewChapters || libManga == null) {
                        getChaptersByMangaId.await(manga.id, applyScanlatorFilter = true)
                            .getNextUnread(manga, mangaDownloadManager)
                    } else {
                        null
                    }

                if (targetChapter != null && (isChapterPending || hasNewChapters || libManga == null)) {
                    val isNewChapter = chapter?.id != targetChapter.id
                    val chapterProgress = when {
                        chapter == null -> 0f
                        chapter.read || isNewChapter -> 0f
                        chapter.lastPageRead > 0L -> {
                            (chapter.lastPageRead.toFloat() / (chapter.lastPageRead + 3).toFloat()).coerceIn(0.1f, 0.9f)
                        }
                        else -> 0f
                    }
                    val chapterProgressText = if (isNewChapter) {
                        context.stringResource(MR.strings.kitsux_home_new_badge)
                    } else {
                        "Ch ${targetChapter.chapterNumber.toInt()}"
                    }
                    val mediaItem = manga.toMediaItem().copy(
                        hasUpdates = hasUpdates || isNewChapter,
                        unseenCount = unreadVal,
                        isStarted = true,
                    )

                    continueReading.add(
                        ContinueWatchingItem(
                            id = hist.mangaId,
                            title = manga.title,
                            thumbnailUrl = manga.thumbnailUrl,
                            isAnime = false,
                            lastSeen = hist.readAt?.time ?: 0L,
                            progressText = chapterProgressText,
                            episodeProgress = chapterProgress,
                            targetItemId = targetChapter.id,
                            mediaItem = mediaItem,
                            hasUpdates = hasUpdates || isNewChapter,
                            unseenCount = unreadVal,
                        ),
                    )
                    addedMediaIds.add(Pair(hist.mangaId, false))
                }
            }
        }

        // Add library items that have new/unseen episodes/chapters but are NOT in history yet
        if (showAnime) {
            libraryAnime.forEach { libItem ->
                if (libItem.hasStarted && libItem.unseenCount > 0 &&
                    !addedMediaIds.contains(Pair(libItem.id, true))
                ) {
                    val nextEpisode = getEpisodesByAnimeId.await(libItem.id)
                        .getNextUnseen(libItem.anime, animeDownloadManager)

                    nextEpisode?.let { episode ->
                        continueWatching.add(
                            episode.toContinueWatchingItem(
                                anime = libItem,
                                lastSeen = maxOf(libItem.episodeFetchedAt, libItem.latestUpload),
                                isNewEpisode = true,
                            ),
                        )
                        addedMediaIds.add(Pair(libItem.id, true))
                    }
                }
            }
        }

        if (showManga) {
            libraryManga.forEach { libItem ->
                if (libItem.hasStarted && libItem.unreadCount > 0 &&
                    !addedMediaIds.contains(Pair(libItem.id, false))
                ) {
                    val nextChapter = getChaptersByMangaId.await(libItem.id, applyScanlatorFilter = true)
                        .getNextUnread(libItem.manga, mangaDownloadManager)
                        ?: return@forEach
                    val mediaItem = libItem.manga.toMediaItem().copy(
                        hasUpdates = true,
                        unseenCount = libItem.unreadCount.toInt(),
                    )
                    val lastSeenTime = if (libItem.chapterFetchedAt >
                        0L
                    ) {
                        libItem.chapterFetchedAt
                    } else {
                        libItem.latestUpload
                    }
                    continueReading.add(
                        ContinueWatchingItem(
                            id = libItem.id,
                            title = libItem.manga.title,
                            thumbnailUrl = libItem.manga.thumbnailUrl,
                            isAnime = false,
                            lastSeen = lastSeenTime,
                            progressText = context.stringResource(MR.strings.kitsux_home_new_badge),
                            mediaItem = mediaItem,
                            hasUpdates = true,
                            unseenCount = libItem.unreadCount.toInt(),
                            targetItemId = nextChapter.id,
                        ),
                    )
                    addedMediaIds.add(Pair(libItem.id, false))
                }
            }
        }

        val sortedContinueWatching = continueWatching
            .filter { it.isAnime }
            .filterNot { it.hiddenContinueKey() in hiddenContinueItems }
            .distinctBy { it.id }
            .sortedByDescending { it.lastSeen }
            .take(15)
        val sortedContinueReading = continueReading
            .filter { !it.isAnime }
            .filterNot { it.hiddenContinueKey() in hiddenContinueItems }
            .distinctBy { it.id }
            .sortedByDescending { it.lastSeen }
            .take(15)
        val newReleases = mutableListOf<ContinueWatchingItem>()

        if (showAnime) {
            libraryAnime.forEach { libItem ->
                if (libItem.unseenCount > 0) {
                    val nextEpisode = getEpisodesByAnimeId.await(libItem.id)
                        .getNextUnseen(libItem.anime, animeDownloadManager)
                    nextEpisode?.let { episode ->
                        newReleases.add(
                            episode.toContinueWatchingItem(
                                anime = libItem,
                                lastSeen = maxOf(libItem.episodeFetchedAt, libItem.latestUpload),
                                isNewEpisode = true,
                                progressTextOverride = newReleaseLabel(
                                    timestamp = maxOf(libItem.episodeFetchedAt, libItem.latestUpload),
                                    fallback = context.stringResource(
                                        MR.strings.kitsux_home_episode_number,
                                        episode.episodeNumber.toInt(),
                                    ),
                                ),
                            ),
                        )
                    }
                }
            }
        }

        if (showManga) {
            libraryManga.forEach { libItem ->
                if (libItem.unreadCount > 0) {
                    val lastUpdate = if (libItem.chapterFetchedAt >
                        0L
                    ) {
                        libItem.chapterFetchedAt
                    } else {
                        libItem.latestUpload
                    }
                    newReleases.add(libItem.toNewReleaseItem(lastUpdate))
                }
            }
        }

        val sortedNewReleases = newReleases
            .sortedByDescending { it.lastSeen }
            .distinctBy { "${it.id}_${it.isAnime}" }
            .take(20)

        // 2. Group library items by category name
        val animeCategoryMap = animeCategories.associate { it.id to it.name }
        val mangaCategoryMap = mangaCategories.associate { it.id to it.name }
        val homeDefaultCategory = context.stringResource(MR.strings.kitsux_home_my_list)
        val completedCategory = context.stringResource(MR.strings.kitsux_category_completed)

        val miListaItems = mutableListOf<KitsuXMediaItem>()
        val categoryGroups = mutableMapOf<String, MutableList<KitsuXMediaItem>>()

        if (showAnime) {
            libraryAnime.forEach { libAnime ->
                val rawCatName = animeCategoryMap[libAnime.category]
                val catName = rawCatName.toHomeCategoryName(homeDefaultCategory)
                if (catName.isCompletedHomeCategory(completedCategory)) return@forEach
                val mediaItem = libAnime.anime.toMediaItem().copy(
                    hasUpdates = libAnime.unseenCount > 0,
                    unseenCount = libAnime.unseenCount.toInt(),
                    isStarted = libAnime.hasStarted,
                )
                miListaItems.add(mediaItem)
                categoryGroups.getOrPut(catName) { mutableListOf() }.add(mediaItem)
            }
        }

        if (showManga) {
            libraryManga.forEach { libManga ->
                val rawCatName = mangaCategoryMap[libManga.category]
                val catName = rawCatName.toHomeCategoryName(homeDefaultCategory)
                if (catName.isCompletedHomeCategory(completedCategory)) return@forEach
                val mediaItem = libManga.manga.toMediaItem().copy(
                    hasUpdates = libManga.unreadCount > 0,
                    unseenCount = libManga.unreadCount.toInt(),
                    isStarted = libManga.hasStarted,
                )
                miListaItems.add(mediaItem)
                categoryGroups.getOrPut(catName) { mutableListOf() }.add(mediaItem)
            }
        }

        miListaItems.sortBy { it.title.lowercase() }

        // Sort items inside each category title
        categoryGroups.forEach { (_, list) ->
            list.sortBy { it.title.lowercase() }
        }

        // Sort categories: "Mi Lista" first, others alphabetically
        val localRows = categoryGroups.map { (name, items) ->
            KitsuXCategoryRow(name, items)
        }.sortedWith { a, b ->
            when {
                a.name == homeDefaultCategory -> -1
                b.name == homeDefaultCategory -> 1
                else -> a.name.compareTo(b.name, ignoreCase = true)
            }
        }

        val allCategories = mutableListOf<KitsuXCategoryRow>()

        // 1. "Mi Lista" top: aggregate all active library entries.
        if (miListaItems.isNotEmpty()) {
            allCategories.add(KitsuXCategoryRow(homeDefaultCategory, miListaItems))
        }

        // 2. Intelligent/Recommendation rows
        if (showRecommendations) {
            if (showAnime && jikanData.recommendedAnime.isNotEmpty()) {
                allCategories.add(
                    KitsuXCategoryRow(
                        context.stringResource(MR.strings.kitsux_home_recommended_for_you),
                        jikanData.recommendedAnime,
                    ),
                )
            }

            if (showAnime) {
                jikanData.genreRecommendations.forEach { (genre, items) ->
                    if (items.isNotEmpty()) {
                        allCategories.add(
                            KitsuXCategoryRow(
                                context.stringResource(MR.strings.kitsux_home_because_you_like, genre),
                                items,
                            ),
                        )
                    }
                }
            }

            if (showAnime && jikanData.similarToLastWatched.isNotEmpty()) {
                allCategories.add(
                    KitsuXCategoryRow(
                        context.stringResource(MR.strings.kitsux_home_similar_to_watched),
                        jikanData.similarToLastWatched,
                    ),
                )
            }

            if (showAnime && jikanData.recommendedAnime.isNotEmpty()) {
                allCategories.add(
                    KitsuXCategoryRow(
                        context.stringResource(MR.strings.kitsux_home_popular_this_week),
                        jikanData.recommendedAnime,
                    ),
                )
            }

            if (showManga && jikanData.recommendedManga.isNotEmpty()) {
                allCategories.add(
                    KitsuXCategoryRow(
                        context.stringResource(MR.strings.kitsux_home_popular_manga),
                        jikanData.recommendedManga,
                    ),
                )
            }
        }

        // 3. User custom categories below
        localRows.filter { it.name != homeDefaultCategory }.forEach { allCategories.add(it) }

        KitsuXHomeState(
            heroBannerItems = if (showAnime && showHeroBanner) {
                jikanData.heroBannerItems.enrichWithLibraryEntries(libraryAnime, libraryManga)
            } else {
                emptyList()
            },
            continueWatching = sortedContinueWatching,
            continueReading = sortedContinueReading,
            newReleases = sortedNewReleases,
            categories = allCategories,
            isLoading = false,
        )
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = KitsuXHomeState(isLoading = true),
    )

    private fun Anime.toMediaItem() = KitsuXMediaItem(
        id = id,
        title = title,
        thumbnailUrl = thumbnailUrl,
        description = description ?: "",
        genres = genre ?: emptyList(),
        isAnime = true,
        realModel = this,
    )

    private fun List<KitsuXMediaItem>.enrichWithLibraryEntries(
        libraryAnime: List<LibraryAnime>,
        libraryManga: List<LibraryManga>,
    ): List<KitsuXMediaItem> {
        val animeByTitle = libraryAnime.associateBy { it.anime.title.normalizedHomeTitle() }
        val mangaByTitle = libraryManga.associateBy { it.manga.title.normalizedHomeTitle() }
        return map { item ->
            if (item.isAnime) {
                val local = animeByTitle[item.title.normalizedHomeTitle()]
                if (local != null) {
                    item.copy(
                        id = local.id,
                        thumbnailUrl = local.anime.thumbnailUrl ?: item.thumbnailUrl,
                        description = local.anime.description ?: item.description,
                        genres = local.anime.genre ?: item.genres,
                        realModel = local.anime,
                        hasUpdates = local.unseenCount > 0,
                        unseenCount = local.unseenCount.toInt(),
                        isRecommendation = false,
                        isStarted = local.hasStarted,
                    )
                } else {
                    item
                }
            } else {
                val local = mangaByTitle[item.title.normalizedHomeTitle()]
                if (local != null) {
                    item.copy(
                        id = local.id,
                        thumbnailUrl = local.manga.thumbnailUrl ?: item.thumbnailUrl,
                        description = local.manga.description ?: item.description,
                        genres = local.manga.genre ?: item.genres,
                        realModel = local.manga,
                        hasUpdates = local.unreadCount > 0,
                        unseenCount = local.unreadCount.toInt(),
                        isRecommendation = false,
                        isStarted = local.hasStarted,
                    )
                } else {
                    item
                }
            }
        }
    }

    private fun String.normalizedHomeTitle(): String {
        return lowercase()
            .replace(Regex("[^a-z0-9áéíóúñ]+"), "")
    }

    private fun Episode.toContinueWatchingItem(
        anime: LibraryAnime,
        lastSeen: Long,
        isNewEpisode: Boolean,
        progressTextOverride: String? = null,
    ): ContinueWatchingItem {
        val progress = if (!isNewEpisode && totalSeconds > 0) {
            (lastSecondSeen.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
        val progressLabel = progressTextOverride ?: if (!isNewEpisode && totalSeconds > 0 && lastSecondSeen > 0) {
            val remainingSecs = (totalSeconds - lastSecondSeen) / 1000
            if (remainingSecs > 60) {
                context.stringResource(MR.strings.kitsux_home_minutes_remaining, remainingSecs / 60)
            } else {
                context.stringResource(MR.strings.kitsux_home_episode_number, episodeNumber.toInt())
            }
        } else {
            context.stringResource(MR.strings.kitsux_home_episode_number, episodeNumber.toInt())
        }
        val mediaItem = anime.anime.toMediaItem().copy(
            hasUpdates = isNewEpisode,
            unseenCount = 0,
            isStarted = anime.hasStarted,
        )

        return ContinueWatchingItem(
            id = anime.id,
            title = anime.anime.title,
            thumbnailUrl = anime.anime.thumbnailUrl,
            isAnime = true,
            lastSeen = lastSeen,
            progressText = progressLabel,
            episodeProgress = progress,
            targetItemId = id,
            mediaItem = mediaItem,
            hasUpdates = isNewEpisode,
            unseenCount = 0,
        )
    }

    private fun Manga.toMediaItem() = KitsuXMediaItem(
        id = id,
        title = title,
        thumbnailUrl = thumbnailUrl,
        description = description ?: "",
        genres = genre ?: emptyList(),
        isAnime = false,
        realModel = this,
    )

    private fun LibraryManga.toNewReleaseItem(lastUpdate: Long): ContinueWatchingItem {
        val mediaItem = manga.toMediaItem().copy(
            hasUpdates = true,
            unseenCount = unreadCount.toInt(),
            isStarted = hasStarted,
        )
        return ContinueWatchingItem(
            id = id,
            title = manga.title,
            thumbnailUrl = manga.thumbnailUrl,
            isAnime = false,
            lastSeen = lastUpdate,
            progressText = newReleaseLabel(lastUpdate, context.stringResource(MR.strings.kitsux_home_new_badge)),
            mediaItem = mediaItem,
            hasUpdates = true,
            unseenCount = unreadCount.toInt(),
        )
    }

    private fun newReleaseLabel(timestamp: Long, fallback: String): String {
        if (timestamp <= 0L) return fallback
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val daysAgo = TimeUnit.MILLISECONDS.toDays(startOfToday - timestamp)
        return when {
            timestamp >= startOfToday -> context.stringResource(MR.strings.kitsux_home_today)
            daysAgo == 0L -> context.stringResource(MR.strings.kitsux_home_yesterday)
            daysAgo in 1L..6L -> context.stringResource(MR.strings.kitsux_home_days_ago, daysAgo + 1)
            else -> fallback
        }
    }

    fun hideContinueItem(continueItem: ContinueWatchingItem) {
        hiddenContinueItemsPreference.set(
            hiddenContinueItemsPreference.get() + continueItem.hiddenContinueKey(),
        )
    }

    fun continueWatchingOrReading(
        context: Context,
        continueItem: ContinueWatchingItem,
        navigator: cafe.adriel.voyager.navigator.Navigator,
    ) {
        val continueKey = "${continueItem.isAnime}_${continueItem.id}"
        if (!activeContinueActions.add(continueKey)) return

        screenModelScope.launch {
            try {
                if (continueItem.isAnime) {
                    val animeId = continueItem.id
                    var targetEpisodeId: Long? = continueItem.targetItemId

                    val lastHist = if (targetEpisodeId == null) {
                        getAnimeHistory.await(animeId).firstOrNull()
                    } else {
                        null
                    }
                    if (lastHist != null) {
                        val episode = getEpisode.await(lastHist.episodeId)
                        if (episode != null && !episode.seen) {
                            targetEpisodeId = episode.id
                        }
                    }

                    if (targetEpisodeId == null) {
                        val getAnime = Injekt.get<GetAnime>()
                        val getEpisodesByAnimeId = Injekt.get<GetEpisodesByAnimeId>()
                        val downloadManager = Injekt.get<AnimeDownloadManager>()
                        val anime = getAnime.await(animeId)
                        if (anime != null) {
                            val nextUnseen = getEpisodesByAnimeId.await(anime.id).getNextUnseen(anime, downloadManager)
                            if (nextUnseen != null) {
                                targetEpisodeId = nextUnseen.id
                            }
                        }
                    }

                    if (targetEpisodeId != null) {
                        withContext(Dispatchers.Main) {
                            val playerPreferences = Injekt.get<PlayerPreferences>()
                            val extPlayer = playerPreferences.alwaysUseExternalPlayer().get()
                            MainActivity.startPlayerActivity(context, animeId, targetEpisodeId, extPlayer)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            navigator.push(eu.kanade.tachiyomi.ui.entries.anime.AnimeScreen(animeId))
                        }
                    }
                } else {
                    val mangaId = continueItem.id
                    var targetChapterId: Long? = continueItem.targetItemId

                    val lastHist = if (targetChapterId == null) {
                        getMangaHistory.await(mangaId).firstOrNull()
                    } else {
                        null
                    }
                    if (lastHist != null) {
                        val chapter = getChapter.await(lastHist.chapterId)
                        if (chapter != null && !chapter.read) {
                            targetChapterId = chapter.id
                        }
                    }

                    if (targetChapterId == null) {
                        val getManga = Injekt.get<GetManga>()
                        val getChaptersByMangaId = Injekt.get<GetChaptersByMangaId>()
                        val downloadManager = Injekt.get<MangaDownloadManager>()
                        val manga = getManga.await(mangaId)
                        if (manga != null) {
                            val nextUnread = getChaptersByMangaId.await(manga.id, applyScanlatorFilter = true)
                                .getNextUnread(manga, downloadManager)
                            if (nextUnread != null) {
                                targetChapterId = nextUnread.id
                            }
                        }
                    }

                    if (targetChapterId == null && lastHist != null) {
                        targetChapterId = lastHist.chapterId
                    }

                    if (targetChapterId != null) {
                        withContext(Dispatchers.Main) {
                            context.startActivity(
                                ReaderActivity.newIntent(
                                    context,
                                    mangaId,
                                    targetChapterId,
                                ),
                            )
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            navigator.push(eu.kanade.tachiyomi.ui.entries.manga.MangaScreen(mangaId))
                        }
                    }
                }
            } finally {
                activeContinueActions.remove(continueKey)
            }
        }
    }

    fun refresh() {
        screenModelScope.launch {
            try {
                KitsuXIntelSystem.loadAllRecommendations()
            } catch (e: Exception) {
                logcat(LogPriority.ERROR, e) { "Failed to reload recommendations" }
            }
            withContext(Dispatchers.Main) {
                AnimeLibraryUpdateJob.startNow(context)
                MangaLibraryUpdateJob.startNow(context)
            }
        }
    }
}

data class KitsuXHomeState(
    val heroBannerItems: List<KitsuXMediaItem> = emptyList(),
    val continueWatching: List<ContinueWatchingItem> = emptyList(),
    val continueReading: List<ContinueWatchingItem> = emptyList(),
    val newReleases: List<ContinueWatchingItem> = emptyList(),
    val categories: List<KitsuXCategoryRow> = emptyList(),
    val isLoading: Boolean = true,
)

data class KitsuXCategoryRow(
    val name: String,
    val items: List<KitsuXMediaItem>,
)

data class ContinueWatchingItem(
    val id: Long,
    val title: String,
    val thumbnailUrl: String?,
    val isAnime: Boolean,
    val lastSeen: Long,
    val progressText: String,
    val mediaItem: KitsuXMediaItem,
    val hasUpdates: Boolean = false,
    val unseenCount: Int = 0,
    /** Real progress 0.0..1.0. 0f = unknown/not started, 1f = completed. */
    val episodeProgress: Float = 0f,
    val targetItemId: Long? = null,
)

private fun ContinueWatchingItem.hiddenContinueKey(): String {
    val type = if (isAnime) "anime" else "manga"
    return "$type:$id:${targetItemId ?: 0L}"
}

data class KitsuXMediaItem(
    val id: Long,
    val title: String,
    val thumbnailUrl: String?,
    val description: String,
    val genres: List<String>,
    val rating: String = "8.8",
    val isAnime: Boolean,
    val realModel: Any? = null,
    val hasUpdates: Boolean = false,
    val unseenCount: Int = 0,
    val isRecommendation: Boolean = false,
    val isStarted: Boolean = false,
)

private data class HomePreferenceState(
    val showAnime: Boolean,
    val showManga: Boolean,
    val showRecommendations: Boolean,
    val showHeroBanner: Boolean,
)

data class JikanData(
    val heroBannerItems: List<KitsuXMediaItem>,
    val recommendedAnime: List<KitsuXMediaItem>,
    val recommendedManga: List<KitsuXMediaItem>,
    val genreRecommendations: Map<String, List<KitsuXMediaItem>>,
    val similarToLastWatched: List<KitsuXMediaItem>,
)

private fun String?.toHomeCategoryName(defaultName: String): String {
    if (isNullOrBlank() || equals("Default", ignoreCase = true)) return defaultName
    return if (looksLikeSourceTagCategory(this)) defaultName else this
}

private fun looksLikeSourceTagCategory(name: String): Boolean {
    val normalized = name.trim().lowercase()
    return normalized in SOURCE_TAG_CATEGORY_NAMES ||
        (normalized.contains("anime") && normalized !in ALLOWED_ANIME_CATEGORY_NAMES) ||
        normalized.endsWith("flv") ||
        normalized.contains("jkanime") ||
        normalized.contains("otakus")
}

private fun String.isCompletedHomeCategory(localizedName: String): Boolean {
    return equals(localizedName, ignoreCase = true) ||
        equals("Terminados", ignoreCase = true) ||
        equals("Terminado", ignoreCase = true)
}

private val ALLOWED_ANIME_CATEGORY_NAMES = setOf("anime", "animes")

private val SOURCE_TAG_CATEGORY_NAMES = setOf(
    "anime flv",
    "anime id",
    "anime movil",
    "anime online",
    "anime tv",
    "anime yt",
    "anime-pro",
    "descargar animes",
    "jk anime",
    "series flv",
)
