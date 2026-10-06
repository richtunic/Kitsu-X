package eu.kanade.presentation.updates.manga

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.relativeDateText
import eu.kanade.presentation.entries.components.DotSeparatorText
import eu.kanade.presentation.entries.components.ItemCover
import eu.kanade.presentation.entries.manga.components.ChapterDownloadAction
import eu.kanade.presentation.entries.manga.components.ChapterDownloadIndicator
import eu.kanade.presentation.updates.RecentGroupCard
import eu.kanade.presentation.util.animateItemFastScroll
import eu.kanade.presentation.util.relativeTimeSpanString
import eu.kanade.tachiyomi.data.download.manga.model.MangaDownload
import eu.kanade.tachiyomi.ui.updates.manga.MangaUpdatesItem
import tachiyomi.domain.updates.manga.model.MangaUpdatesWithRelations
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.DISABLED_ALPHA
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.selectedBackground
import java.time.LocalDate

internal fun LazyListScope.mangaUpdatesLastUpdatedItem(
    lastUpdated: Long,
) {
    item(key = "mangaUpdates-lastUpdated") {
        Box(
            modifier = Modifier
                .animateItem(fadeInSpec = null, fadeOutSpec = null)
                .padding(horizontal = MaterialTheme.padding.medium, vertical = MaterialTheme.padding.small),
        ) {
            Text(
                text = stringResource(MR.strings.updates_last_update_info, relativeTimeSpanString(lastUpdated)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

internal fun LazyListScope.mangaUpdatesUiItems(
    uiModels: List<MangaUpdatesUiModel>,
    expandedGroups: Set<Long>? = null,
    onToggleGroup: (Long) -> Unit = {},
    selectionMode: Boolean,
    onUpdateSelected: (MangaUpdatesItem, Boolean, Boolean, Boolean) -> Unit,
    onClickCover: (MangaUpdatesItem) -> Unit,
    onClickUpdate: (MangaUpdatesItem) -> Unit,
    onDownloadChapter: (List<MangaUpdatesItem>, ChapterDownloadAction) -> Unit,
) {
    val groupItems = uiModels.filterIsInstance<MangaUpdatesUiModel.Item>().associateBy { it.item.update.mangaId }
    val visibleModels = uiModels.filter {
        it !is MangaUpdatesUiModel.Item || expandedGroups == null || selectionMode ||
            it.item.update.mangaId in expandedGroups
    }
    items(
        items = visibleModels,
        contentType = {
            when (it) {
                is MangaUpdatesUiModel.Group -> "group"
                is MangaUpdatesUiModel.Header -> "header"
                is MangaUpdatesUiModel.Item -> "item"
            }
        },
        key = {
            when (it) {
                is MangaUpdatesUiModel.Group -> "mangaUpdatesGroup-${it.id}"
                is MangaUpdatesUiModel.Header -> "mangaUpdatesHeader-${it.hashCode()}"
                is MangaUpdatesUiModel.Item -> "mangaUpdates-${it.item.update.mangaId}-${it.item.update.chapterId}"
            }
        },
    ) { item ->
        when (item) {
            is MangaUpdatesUiModel.Group -> {
                val representative = groupItems.getValue(item.id).item
                RecentGroupCard(
                    title = item.title,
                    cover = representative.update.coverData,
                    count = item.count,
                    expanded = selectionMode || item.id in expandedGroups.orEmpty(),
                    enabled = !selectionMode,
                    onToggle = { onToggleGroup(item.id) },
                    onCoverClick = { onClickCover(representative) },
                )
            }
            is MangaUpdatesUiModel.Header -> {
                Text(
                    modifier = Modifier.animateItemFastScroll().padding(horizontal = 20.dp, vertical = 16.dp),
                    text = when (item.date) {
                        LocalDate.now() -> stringResource(MR.strings.kitsux_home_today)
                        LocalDate.now().minusDays(1) -> stringResource(MR.strings.kitsux_home_yesterday)
                        else -> relativeDateText(item.date)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            is MangaUpdatesUiModel.Item -> {
                val updatesItem = item.item
                MangaUpdatesUiItem(
                    modifier = Modifier.animateItemFastScroll(),
                    update = updatesItem.update,
                    compact = expandedGroups != null,
                    selected = updatesItem.selected,
                    readProgress = updatesItem.update.lastPageRead
                        .takeIf { !updatesItem.update.read && it > 0L }
                        ?.let {
                            stringResource(
                                MR.strings.chapter_progress,
                                it + 1,
                            )
                        },
                    onLongClick = {
                        onUpdateSelected(updatesItem, !updatesItem.selected, true, true)
                    },
                    onClick = {
                        when {
                            selectionMode -> onUpdateSelected(
                                updatesItem,
                                !updatesItem.selected,
                                true,
                                false,
                            )
                            else -> onClickUpdate(updatesItem)
                        }
                    },
                    onClickCover = { onClickCover(updatesItem) }.takeIf { !selectionMode },
                    onDownloadChapter = { action: ChapterDownloadAction ->
                        onDownloadChapter(listOf(updatesItem), action)
                    }.takeIf { !selectionMode },
                    downloadStateProvider = updatesItem.downloadStateProvider,
                    downloadProgressProvider = updatesItem.downloadProgressProvider,
                )
            }
        }
    }
}

@Composable
private fun MangaUpdatesUiItem(
    update: MangaUpdatesWithRelations,
    compact: Boolean = false,
    selected: Boolean,
    readProgress: String?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onClickCover: (() -> Unit)?,
    onDownloadChapter: ((ChapterDownloadAction) -> Unit)?,
    // Download Indicator
    downloadStateProvider: () -> MangaDownload.State,
    downloadProgressProvider: () -> Int,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val textAlpha = if (update.read) DISABLED_ALPHA else 1f

    Row(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .selectedBackground(selected)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    onLongClick()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
            )
            .height(if (compact) 64.dp else 88.dp)
            .padding(horizontal = MaterialTheme.padding.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!compact) {
            ItemCover.Book(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .fillMaxHeight(),
                data = update.coverData,
                shape = RoundedCornerShape(10.dp),
                onClick = onClickCover,
            )
        }
        Column(
            modifier = Modifier
                .padding(horizontal = MaterialTheme.padding.medium)
                .weight(1f),
        ) {
            if (!compact) {
                Text(
                    text = update.mangaTitle,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalContentColor.current.copy(alpha = textAlpha),
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                var textHeight by remember { mutableIntStateOf(0) }
                if (!update.read) {
                    Icon(
                        imageVector = Icons.Filled.Circle,
                        contentDescription = stringResource(MR.strings.unread),
                        modifier = Modifier
                            .height(8.dp)
                            .padding(end = 4.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                if (update.bookmark) {
                    Icon(
                        imageVector = Icons.Filled.Bookmark,
                        contentDescription = stringResource(MR.strings.action_filter_bookmarked),
                        modifier = Modifier
                            .sizeIn(
                                maxHeight = with(LocalDensity.current) { textHeight.toDp() - 2.dp },
                            ),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                }
                Text(
                    text = update.chapterName,
                    maxLines = if (compact) 2 else 1,
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalContentColor.current.copy(alpha = textAlpha),
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { textHeight = it.size.height },
                    modifier = Modifier
                        .weight(weight = 1f, fill = false),
                )
                if (readProgress != null) {
                    DotSeparatorText()
                    Text(
                        text = readProgress,
                        maxLines = 1,
                        color = LocalContentColor.current.copy(alpha = DISABLED_ALPHA),
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        ChapterDownloadIndicator(
            enabled = onDownloadChapter != null,
            modifier = Modifier.padding(start = 4.dp),
            downloadStateProvider = downloadStateProvider,
            downloadProgressProvider = downloadProgressProvider,
            onClick = { onDownloadChapter?.invoke(it) },
        )
    }
}
