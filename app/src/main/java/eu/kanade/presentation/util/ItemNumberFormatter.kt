package eu.kanade.presentation.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

private val formatter = DecimalFormat(
    "#.###",
    DecimalFormatSymbols().apply { decimalSeparator = '.' },
)

fun formatChapterNumber(chapterNumber: Double): String {
    return synchronized(formatter) { formatter.format(chapterNumber) }
}

fun formatEpisodeNumber(episodeNumber: Double): String {
    return synchronized(formatter) { formatter.format(episodeNumber) }
}
