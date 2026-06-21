package eu.kanade.presentation.more.onboarding

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
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
import eu.kanade.tachiyomi.util.system.powerManager
import eu.kanade.tachiyomi.util.system.toast
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

internal class BatteryOptimizationStep : OnboardingStep {

    override val title = MR.strings.onboarding_permission_ignore_battery_opts
    override val description = MR.strings.onboarding_permission_ignore_battery_opts_description
    override val isComplete: Boolean = true

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        var isIgnoring by remember {
            mutableStateOf(context.powerManager.isIgnoringBatteryOptimizations(context.packageName))
        }

        DisposableEffect(lifecycleOwner.lifecycle) {
            val observer = object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    isIgnoring = context.powerManager.isIgnoringBatteryOptimizations(context.packageName)
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            TextPreferenceWidget(
                title = stringResource(MR.strings.pref_disable_battery_optimization),
                subtitle = if (isIgnoring) {
                    stringResource(MR.strings.battery_optimization_disabled)
                } else {
                    stringResource(MR.strings.pref_disable_battery_optimization_summary)
                },
                onPreferenceClick = {
                    val packageName = context.packageName
                    if (!context.powerManager.isIgnoringBatteryOptimizations(packageName)) {
                        try {
                            @SuppressLint("BatteryLife")
                            val intent = Intent().apply {
                                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                                data = "package:$packageName".toUri()
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            context.toast(MR.strings.battery_optimization_setting_activity_not_found)
                        }
                    } else {
                        context.toast(MR.strings.battery_optimization_disabled)
                    }
                },
            )
        }
    }
}
