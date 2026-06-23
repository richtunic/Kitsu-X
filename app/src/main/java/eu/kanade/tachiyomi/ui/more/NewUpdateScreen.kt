package eu.kanade.tachiyomi.ui.more

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.more.NewUpdateScreen
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.data.updater.AppUpdateDownloadJob
import eu.kanade.tachiyomi.data.updater.localizedReleaseInfo
import eu.kanade.tachiyomi.util.system.openInBrowser
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class NewUpdateScreen(
    private val versionName: String,
    private val changelogInfo: String,
    private val releaseLink: String,
    private val downloadLink: String,
) : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val preferenceStore = remember { Injekt.get<PreferenceStore>() }
        val localizedChangelogInfo = remember(changelogInfo) {
            context.localizedReleaseInfo(changelogInfo)
        }

        NewUpdateScreen(
            versionName = versionName,
            changelogInfo = localizedChangelogInfo,
            onOpenInBrowser = { context.openInBrowser(releaseLink) },
            onRejectUpdate = {
                preferenceStore.getString(Preference.appStateKey("last_skipped_version")).set(versionName)
                preferenceStore.getLong(Preference.appStateKey("last_skipped_time")).set(System.currentTimeMillis())
                navigator.pop()
            },
            onAcceptUpdate = {
                AppUpdateDownloadJob.start(
                    context = context,
                    url = downloadLink,
                    title = versionName,
                )
                navigator.pop()
            },
        )
    }
}
