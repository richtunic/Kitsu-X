package eu.kanade.tachiyomi.ui.download.manga

import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import eu.davidea.viewholders.FlexibleViewHolder
import eu.kanade.presentation.download.safeDownloadError
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.download.manga.model.MangaDownload
import eu.kanade.tachiyomi.databinding.DownloadItemBinding
import eu.kanade.tachiyomi.util.view.popupMenu
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.i18n.MR

/**
 * Class used to hold the data of a download.
 * All the elements from the layout file "download_item" are available in this class.
 *
 * @param view the inflated view for this holder.
 * @constructor creates a new download holder.
 */
class MangaDownloadHolder(private val view: View, val adapter: MangaDownloadAdapter) :
    FlexibleViewHolder(view, adapter) {

    private val binding = DownloadItemBinding.bind(view)

    init {
        binding.container.setOnClickListener {
            if (download.status == MangaDownload.State.ERROR) adapter.onErrorClick(download)
        }
        setDragHandleView(binding.reorder)
        binding.menu.setOnClickListener { it.post { showPopupMenu(it) } }
    }

    private lateinit var download: MangaDownload

    /**
     * Binds this holder with the given category.
     *
     * @param category The category to bind.
     */
    fun bind(download: MangaDownload) {
        this.download = download
        notifyStatus()
        // Update the chapter name.
        binding.chapterTitle.text = download.chapter.name

        // Update the manga title
        binding.mangaFullTitle.text = download.manga.title

        // Update the progress bar and the number of downloaded pages
        val pages = download.pages
        if (pages == null) {
            binding.downloadProgress.progress = 0
            binding.downloadProgress.max = 1
            binding.downloadProgressText.text = ""
        } else {
            binding.downloadProgress.max = pages.size * 100
            notifyProgress()
            notifyDownloadedPages()
        }
    }

    /**
     * Updates the progress bar of the download.
     */
    fun notifyProgress() {
        val pages = download.pages ?: return
        binding.downloadProgress.isIndeterminate = false
        if (binding.downloadProgress.max == 1) {
            binding.downloadProgress.max = pages.size * 100
        }
        binding.downloadProgress.setProgressCompat(download.totalProgress, true)
    }

    /**
     * Updates the text field of the number of downloaded pages.
     */
    fun notifyDownloadedPages() {
        val pages = download.pages ?: return
        binding.downloadProgressText.text = "${download.downloadedImages}/${pages.size}"
    }

    fun notifyStatus() {
        binding.container.isClickable = download.status == MangaDownload.State.ERROR
        binding.downloadStatus.text = view.context.stringResource(
            when (download.status) {
                MangaDownload.State.DOWNLOADING -> MR.strings.update_check_notification_download_in_progress
                MangaDownload.State.ERROR -> MR.strings.update_check_notification_download_error
                MangaDownload.State.DOWNLOADED -> MR.strings.completed
                else -> MR.strings.kitsux_download_waiting
            },
        )
        if (download.status == MangaDownload.State.ERROR && !download.errorMessage.isNullOrBlank()) {
            binding.downloadStatus.text = safeDownloadError(download.errorMessage!!).lineSequence().first().take(200)
        }
        binding.downloadProgress.isIndeterminate =
            download.status == MangaDownload.State.DOWNLOADING &&
            download.progress == 0
    }

    override fun onItemReleased(position: Int) {
        super.onItemReleased(position)
        adapter.downloadItemListener.onItemReleased(position)
        binding.container.isDragged = false
    }

    override fun onActionStateChanged(position: Int, actionState: Int) {
        super.onActionStateChanged(position, actionState)
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            binding.container.isDragged = true
        }
    }

    private fun showPopupMenu(view: View) {
        view.popupMenu(
            menuRes = R.menu.download_single,
            initMenu = {
                findItem(R.id.move_to_top).isVisible = bindingAdapterPosition > 1
                findItem(R.id.move_to_bottom).isVisible =
                    bindingAdapterPosition != adapter.itemCount - 1
            },
            onMenuItemClick = {
                adapter.downloadItemListener.onMenuItemClick(bindingAdapterPosition, this)
            },
        )
    }
}
