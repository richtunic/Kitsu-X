package eu.kanade.presentation.more.onboarding

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import eu.kanade.presentation.more.settings.widget.TextPreferenceWidget
import eu.kanade.tachiyomi.util.system.toast
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

internal class InstallPermissionStep : OnboardingStep {

    override val title = MR.strings.onboarding_permission_install_apps
    override val description = MR.strings.onboarding_permission_install_apps_description
    override val isComplete: Boolean = true

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        var hasPermission by remember {
            mutableStateOf(context.packageManager.canRequestPackageInstalls())
        }

        DisposableEffect(lifecycleOwner.lifecycle) {
            val observer = object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    hasPermission = context.packageManager.canRequestPackageInstalls()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            TextPreferenceWidget(
                title = stringResource(MR.strings.onboarding_permission_install_apps),
                subtitle = if (hasPermission) {
                    stringResource(MR.strings.onboarding_permission_install_apps_granted)
                } else {
                    stringResource(MR.strings.onboarding_permission_install_apps_description)
                },
                onPreferenceClick = {
                    if (!hasPermission) {
                        try {
                            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                                data = "package:${context.packageName}".toUri()
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (ex: Exception) {
                                context.toast("No se pudo abrir la configuración")
                            }
                        }
                    } else {
                        context.toast(MR.strings.onboarding_permission_install_apps_granted)
                    }
                },
            )
        }
    }
}
