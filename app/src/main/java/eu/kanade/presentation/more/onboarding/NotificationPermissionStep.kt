package eu.kanade.presentation.more.onboarding

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.PermissionChecker
import eu.kanade.presentation.more.settings.widget.TextPreferenceWidget
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

internal class NotificationPermissionStep : OnboardingStep {

    override val title = MR.strings.onboarding_permission_notifications
    override val description = MR.strings.onboarding_permission_notifications_description
    override val isComplete: Boolean = true

    @Composable
    override fun Content() {
        val context = LocalContext.current
        var hasPermission by remember {
            mutableStateOf(context.hasNotificationPermission())
        }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            hasPermission = isGranted
        }

        Column(modifier = Modifier.padding(16.dp)) {
            TextPreferenceWidget(
                title = stringResource(MR.strings.onboarding_permission_notifications),
                subtitle = if (hasPermission) {
                    stringResource(MR.strings.onboarding_permission_notifications_granted)
                } else {
                    stringResource(MR.strings.onboarding_permission_notifications_description)
                },
                onPreferenceClick = {
                    if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
            )
        }
    }

    private fun Context.hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionChecker.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PermissionChecker.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
