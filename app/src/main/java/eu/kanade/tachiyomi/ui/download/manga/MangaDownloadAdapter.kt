package eu.kanade.tachiyomi.ui.download.manga

import android.view.MenuItem
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.kanade.tachiyomi.data.download.manga.model.MangaDownload

/**
 * Adapter storing a list of downloads.
 *
 * @param downloadItemListener Listener called when an item of the list is released.
 */
class MangaDownloadAdapter(
    val downloadItemListener: DownloadItemListener,
    val onErrorClick: (MangaDownload) -> Unit,
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
