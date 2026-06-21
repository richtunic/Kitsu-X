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
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

internal class HeroBannerStep : OnboardingStep {

    private val uiPreferences: UiPreferences = Injekt.get()

    override val title = MR.strings.kitsux_onboarding_hero_banner_title
    override val description = MR.strings.kitsux_onboarding_hero_banner_description
    override val isComplete: Boolean = true

    @Composable
    override fun Content() {
        val showHeroBannerPref = uiPreferences.showHeroBanner()
        val showHeroBanner by showHeroBannerPref.collectAsState()

        Column(modifier = Modifier.padding(16.dp)) {
            SwitchPreferenceWidget(
                title = stringResource(MR.strings.kitsux_pref_show_hero_banner),
                subtitle = stringResource(MR.strings.kitsux_pref_show_hero_banner_summary),
                checked = showHeroBanner,
                onCheckedChanged = { showHeroBannerPref.set(it) },
            )
        }
    }
}
