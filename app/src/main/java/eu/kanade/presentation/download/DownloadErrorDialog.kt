package eu.kanade.presentation.download

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import eu.kanade.tachiyomi.util.system.copyToClipboard
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

internal fun safeDownloadError(message: String): String = message
    .replace(Regex("https?://[^\\s]+", RegexOption.IGNORE_CASE), "[URL]")
    .replace(
        Regex("^(authorization|cookie|set-cookie)\\s*:.*$", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)),
        "$1: [redacted]",
    )
    .replace(
        Regex("(token|password|api_key|apikey|secret)\\s*[:=]\\s*[^\\s,;]+", RegexOption.IGNORE_CASE),
        "$1=[redacted]",
    )

@Composable
internal fun DownloadErrorDialog(
    title: String,
    part: String,
    source: String,
    error: String?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val label = stringResource(MR.strings.kitsux_download_error_details)
    val reason = safeDownloadError(
        error?.takeIf { it.isNotBlank() }
            ?: stringResource(MR.strings.download_notifier_unknown_error),
    )
    val summary = "$title\n$part\n$source\n\n$reason"
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        title = { Text(label) },
        text = {
            SelectionContainer {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "$part · $source",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text("\n$reason", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = { TextButton(onClick = onRetry) { Text(stringResource(MR.strings.action_retry)) } },
        dismissButton = {
            Row {
                TextButton(onClick = { context.copyToClipboard(label, summary) }) {
                    Text(stringResource(MR.strings.kitsux_download_copy_error))
                }
                TextButton(onClick = onDismiss) { Text(stringResource(MR.strings.action_close)) }
            }
        },
    )
}
