package eu.kanade.presentation.library.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

enum class LibraryQuickFilter { All, Started, Pending, Completed }

@Composable
fun LibraryQuickFilters(
    selected: LibraryQuickFilter,
    isAnime: Boolean,
    onSelect: (LibraryQuickFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(LibraryQuickFilter.entries) { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelect(filter) },
                label = {
                    Text(
                        stringResource(
                            when (filter) {
                                LibraryQuickFilter.All -> MR.strings.kitsux_library_all
                                LibraryQuickFilter.Started -> if (isAnime) {
                                    MR.strings.kitsux_library_watching
                                } else {
                                    MR.strings.kitsux_library_reading
                                }
                                LibraryQuickFilter.Pending -> MR.strings.kitsux_library_pending
                                LibraryQuickFilter.Completed -> MR.strings.completed
                            },
                        ),
                    )
                },
            )
        }
    }
}
