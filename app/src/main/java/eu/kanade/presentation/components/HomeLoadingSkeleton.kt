package eu.kanade.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.theme.KitsuXLayoutTokens
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun HomeLoadingSkeleton(modifier: Modifier = Modifier) {
    val placeholderColor = MaterialTheme.colorScheme.surfaceContainerHigh

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(top = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(
            text = stringResource(MR.strings.loading),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = KitsuXLayoutTokens.gutter),
        )
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .widthIn(max = KitsuXLayoutTokens.heroMaxWidth)
                    .fillMaxWidth()
                    .padding(horizontal = KitsuXLayoutTokens.gutter)
                    .height(240.dp)
                    .clip(RoundedCornerShape(KitsuXLayoutTokens.cardRadius))
                    .background(placeholderColor)
                    .clearAndSetSemantics {},
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = KitsuXLayoutTokens.gutter),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(4) {
                Column(
                    modifier = Modifier
                        .width(160.dp)
                        .clearAndSetSemantics {},
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(KitsuXLayoutTokens.cardRadius))
                            .background(placeholderColor),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(placeholderColor),
                    )
                }
            }
        }
    }
}
