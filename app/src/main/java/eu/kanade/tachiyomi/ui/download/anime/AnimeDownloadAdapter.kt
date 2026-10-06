package eu.kanade.tachiyomi.ui.download.anime

import android.view.MenuItem
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.kanade.tachiyomi.data.download.anime.model.AnimeDownload

/**
 * Adapter storing a list of downloads.
 *
 * @param downloadItemListener Listener called when an item of the list is released.
 */
class AnimeDownloadAdapter(
    val downloadItemListener: DownloadItemListener,
    val onErrorClick: (AnimeDownload) -> Unit,
) : FlexibleAdapter<AbstractFlexibleItem<*>>(
    null,
    downloadItemListener,
    true,
) {

    override fun shouldMove(fromPosition: Int, toPosition: Int): Boolean {
        // Don't let sub-items changing group
        return getHeaderOf(getItem(fromPosition)) == getHeaderOf(getItem(toPosition))
    }

    interface DownloadItemListener {
        fun onItemReleased(position: Int)
        fun onMenuItemClick(position: Int, menuItem: MenuItem)
    }
}
