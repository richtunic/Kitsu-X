package eu.kanade.presentation.theme

import androidx.compose.ui.unit.dp

enum class KitsuXWindowClass { Compact, Medium, Expanded }

object KitsuXLayoutTokens {
    const val MediumMinWidthDp = 600
    const val ExpandedMinWidthDp = 840

    val gutter = 16.dp
    val cardRadius = 12.dp
    val heroMaxWidth = 920.dp

    fun windowClass(widthDp: Int) = when {
        widthDp >= ExpandedMinWidthDp -> KitsuXWindowClass.Expanded
        widthDp >= MediumMinWidthDp -> KitsuXWindowClass.Medium
        else -> KitsuXWindowClass.Compact
    }
}
