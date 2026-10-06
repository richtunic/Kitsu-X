package eu.kanade.presentation.entries.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import tachiyomi.domain.entries.EntryCover

@Composable
internal fun EntryInfoLayout(
    cover: EntryCover,
    coverDescription: String,
    appBarPadding: Dp,
    onCoverClick: () -> Unit,
    info: @Composable ColumnScope.() -> Unit,
) {
    val background = MaterialTheme.colorScheme.background
    Box(Modifier.fillMaxWidth().padding(top = appBarPadding)) {
        AsyncImage(
            model = cover,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize().blur(16.dp).alpha(0.25f),
        )
        Box(
            Modifier.matchParentSize().background(
                Brush.verticalGradient(listOf(background.copy(alpha = 0.5f), background)),
            ),
        )
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ItemCover.Book(
                data = cover,
                modifier = Modifier.width(104.dp),
                shape = RoundedCornerShape(16.dp),
                contentDescription = coverDescription,
                onClick = onCoverClick,
            )
            Column(
                Modifier.weight(1f).padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                content = info,
            )
        }
    }
}
