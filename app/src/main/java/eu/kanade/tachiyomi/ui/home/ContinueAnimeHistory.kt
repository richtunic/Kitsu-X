package eu.kanade.tachiyomi.ui.home

import tachiyomi.domain.history.anime.model.AnimeHistoryWithRelations

internal suspend fun selectContinueAnimeHistory(
    history: List<AnimeHistoryWithRelations>,
    hasPlayback: suspend (Long) -> Boolean,
): List<AnimeHistoryWithRelations> {
    val selected = ArrayList<AnimeHistoryWithRelations>(15)
    for (group in history.groupBy { it.animeId }.values) {
        val played = group.firstOrNull { hasPlayback(it.episodeId) }
        if (played != null) selected.add(played)
        if (selected.size == 15) break
    }
    return selected
}
