package eu.kanade.presentation.more.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.presentation.more.settings.widget.SwitchPreferenceWidget
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.util.collectAsState
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

internal class ContentTypeStep : OnboardingStep {

    private val uiPreferences: UiPreferences = Injekt.get()

    override val title = MR.strings.kitsux_onboarding_content_type_title
    override val description = MR.strings.kitsux_onboarding_content_type_description

    override val isComplete: Boolean
        get() = uiPreferences.showAnime().get() || uiPreferences.showManga().get()

    @Composable
    override fun Content() {
        val showAnimePref = uiPreferences.showAnime()
        val showMangaPref = uiPreferences.showManga()

        val showAnime by showAnimePref.collectAsState()
        val showManga by showMangaPref.collectAsState()

        Column(modifier = Modifier.padding(16.dp)) {
            SwitchPreferenceWidget(
                title = "Anime",
                checked = showAnime,
                onCheckedChanged = { showAnimePref.set(it) }
            )
            SwitchPreferenceWidget(
                title = "Manga",
                checked = showManga,
                onCheckedChanged = { showMangaPref.set(it) }
            )
        }
    }
}
