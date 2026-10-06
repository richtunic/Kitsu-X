package eu.kanade.presentation.updates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import tachiyomi.i18n.MR
import tachiyomi.i18n.aniyomi.AYMR
import tachiyomi.presentation.core.i18n.stringResource

enum class RecentMode { New, History, All, Grouped }

@Composable
fun RecentControls(
    mode: RecentMode,
    onModeChange: (RecentMode) -> Unit,
    isAnime: Boolean,
    showAnime: Boolean,
    showManga: Boolean,
    onTypeChange: (Boolean) -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
) {
    val focus = LocalFocusManager.current
    Column {
        if (showAnime && showManga) {
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RecentChip(stringResource(AYMR.strings.label_anime), isAnime) {
                    focus.clearFocus()
                    onTypeChange(true)
                }
                RecentChip(stringResource(AYMR.strings.label_manga), !isAnime) {
                    focus.clearFocus()
                    onTypeChange(false)
                }
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(RecentMode.entries) { value ->
                RecentChip(
                    text = stringResource(
                        when (value) {
                            RecentMode.New -> MR.strings.kitsux_recent_new
                            RecentMode.History -> MR.strings.history
                            RecentMode.All -> MR.strings.kitsux_recent_all
                            RecentMode.Grouped -> MR.strings.kitsux_recent_grouped
                        },
                    ),
                    selected = mode == value,
                    onClick = {
                        focus.clearFocus()
                        onModeChange(value)
                    },
                )
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            placeholder = { Text(stringResource(MR.strings.kitsux_recent_search)) },
            leadingIcon = { Icon(Icons.Outlined.Search, null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Outlined.Close, stringResource(MR.strings.action_close))
                    }
                }
            },
            shape = RoundedCornerShape(18.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
        )
    }
}

@Composable
private fun RecentChip(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
        ),
        border = null,
        label = { Text(text) },
    )
}
