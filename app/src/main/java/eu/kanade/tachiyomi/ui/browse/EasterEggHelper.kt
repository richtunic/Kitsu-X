package eu.kanade.tachiyomi.ui.browse

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.StringResource
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

object EasterEggHelper {

    private val rentAGirlfriendMessages = listOf(
        MR.strings.kitsux_easter_rent_1,
        MR.strings.kitsux_easter_rent_2,
        MR.strings.kitsux_easter_rent_3,
        MR.strings.kitsux_easter_rent_4,
        MR.strings.kitsux_easter_rent_5,
        MR.strings.kitsux_easter_rent_6,
        MR.strings.kitsux_easter_rent_7,
        MR.strings.kitsux_easter_rent_8,
        MR.strings.kitsux_easter_rent_9,
        MR.strings.kitsux_easter_rent_10,
    )

    private val hundredGirlfriendsMessages = listOf(
        MR.strings.kitsux_easter_hundred_1,
        MR.strings.kitsux_easter_hundred_2,
        MR.strings.kitsux_easter_hundred_3,
        MR.strings.kitsux_easter_hundred_4,
        MR.strings.kitsux_easter_hundred_5,
        MR.strings.kitsux_easter_hundred_6,
        MR.strings.kitsux_easter_hundred_7,
        MR.strings.kitsux_easter_hundred_8,
    )

    private val quintupletsMessages = listOf(
        MR.strings.kitsux_easter_quintuplets_1,
        MR.strings.kitsux_easter_quintuplets_2,
        MR.strings.kitsux_easter_quintuplets_3,
        MR.strings.kitsux_easter_quintuplets_4,
    )

    fun getEasterEggMessage(query: String?): StringResource? {
        val normalized = query?.lowercase()?.trim().orEmpty()
        if (normalized.isBlank()) return null

        val isRentAGirlfriend = normalized.contains("rent a girlfriend") ||
            normalized.contains("rent a girlfirend") ||
            normalized.contains("rent a girl") ||
            normalized.contains("kanojo okarishimasu") ||
            normalized.contains("kanojo okari")
        if (isRentAGirlfriend) {
            return rentAGirlfriendMessages.random()
        }

        val is100Girlfriends = normalized.contains("100 novias") ||
            normalized.contains("100 novia") ||
            normalized.contains("100 girlfriend") ||
            normalized.contains("100 girlfriends") ||
            normalized.contains("hyakkano") ||
            normalized.contains("kimi no koto") ||
            normalized.contains("dai dai dai") ||
            normalized.contains("100 nin no") ||
            normalized.contains("100nin no")
        if (is100Girlfriends) {
            return hundredGirlfriendsMessages.random()
        }

        val isQuintuplets = normalized.contains("quintillizas") ||
            normalized.contains("quintilliza") ||
            normalized.contains("quintuplets") ||
            normalized.contains("gotoubun") ||
            normalized.contains("5 toubun")
        if (isQuintuplets) {
            return quintupletsMessages.random()
        }

        return null
    }

    @Composable
    fun EasterEggDialog(
        message: StringResource,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(stringResource(MR.strings.kitsux_easter_continue_search))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(MR.strings.action_cancel))
                }
            },
            title = { Text(stringResource(MR.strings.kitsux_easter_alert_title)) },
            text = { Text(stringResource(message)) },
        )
    }
}
