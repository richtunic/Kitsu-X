package eu.kanade.presentation.updates

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.entries.components.ItemCover
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
internal fun RecentGroupCard(
    title: String,
    cover: Any,
    count: Int,
    expanded: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
    onCoverClick: () -> Unit,
) {
    val status = stringResource(if (expanded) MR.strings.kitsux_group_expanded else MR.strings.kitsux_group_collapsed)
    val angle by animateFloatAsState(if (expanded) 180f else 0f, tween(150), label = "groupExpansion")
    Surface(
        onClick = onToggle,
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp).semantics { stateDescription = status },
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ItemCover.Book(
                data = cover,
                modifier = Modifier.height(84.dp),
                shape = RoundedCornerShape(12.dp),
                contentDescription = title,
                onClick = onCoverClick.takeIf { enabled },
            )
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(
                    text = title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(
                        if (count == 1) MR.strings.kitsux_group_one else MR.strings.kitsux_group_many,
                        count,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            Icon(Icons.Outlined.ExpandMore, contentDescription = null, modifier = Modifier.rotate(angle))
        }
    }
}
