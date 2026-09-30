package eu.kanade.presentation.library.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tachiyomi.presentation.core.components.FastScrollLazyVerticalGrid
import tachiyomi.presentation.core.util.plus

@Composable
internal fun LazyLibraryGrid(
    modifier: Modifier = Modifier,
    columns: Int,
    contentPadding: PaddingValues,
    content: LazyGridScope.() -> Unit,
) {
    BoxWithConstraints(modifier = modifier) {
        val minCardWidth = if (maxWidth < 600.dp) 112.dp else 148.dp
        val adaptiveColumns = ((maxWidth - 16.dp).value / minCardWidth.value).toInt().coerceIn(2, 8)
        FastScrollLazyVerticalGrid(
            columns = GridCells.Fixed(if (columns == 0) adaptiveColumns else columns),
            contentPadding = contentPadding + PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(CommonEntryItemDefaults.GridVerticalSpacer),
            horizontalArrangement = Arrangement.spacedBy(CommonEntryItemDefaults.GridHorizontalSpacer),
            content = content,
        )
    }
}

fun LazyGridScope.globalSearchItem(
    searchQuery: String?,
    onGlobalSearchClicked: () -> Unit,
) {
    if (!searchQuery.isNullOrEmpty()) {
        item(
            span = { GridItemSpan(maxLineSpan) },
            contentType = { "library_global_search_item" },
        ) {
            GlobalSearchItem(
                searchQuery = searchQuery,
                onClick = onGlobalSearchClicked,
            )
        }
    }
}
