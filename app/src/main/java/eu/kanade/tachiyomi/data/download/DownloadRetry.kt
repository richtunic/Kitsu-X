package eu.kanade.tachiyomi.data.download

import eu.kanade.tachiyomi.data.download.anime.model.AnimeDownload
import eu.kanade.tachiyomi.data.download.manga.model.MangaDownload

internal fun prepareMangaRetry(queue: List<MangaDownload>, id: Long): Boolean {
    val target = queue.firstOrNull { it.chapter.id == id } ?: return false
    if (target.status == MangaDownload.State.DOWNLOADED ||
        target.status == MangaDownload.State.DOWNLOADING
    ) {
        return false
    }
    target.errorMessage = null
    target.status = MangaDownload.State.QUEUE
    return true
}

internal fun prepareAnimeRetry(queue: List<AnimeDownload>, id: Long): Boolean {
    val target = queue.firstOrNull { it.episode.id == id } ?: return false
    if (target.status == AnimeDownload.State.DOWNLOADED ||
        target.status == AnimeDownload.State.DOWNLOADING
    ) {
        return false
    }
    target.errorMessage = null
    target.status = AnimeDownload.State.QUEUE
    return true
}
