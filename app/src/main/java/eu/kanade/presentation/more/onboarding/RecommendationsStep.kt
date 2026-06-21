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

internal class RecommendationsStep : OnboardingStep {

    private val uiPreferences: UiPreferences = Injekt.get()

    override val title = MR.strings.kitsux_onboarding_recommendations_title
    override val description = MR.strings.kitsux_onboarding_recommendations_description

    override val isComplete: Boolean = true

    @Composable
    override fun Content() {
        val showRecommendationsPref = uiPreferences.showRecommendations()
        val showRecommendations by showRecommendationsPref.collectAsState()

        Column(modifier = Modifier.padding(16.dp)) {
            SwitchPreferenceWidget(
                title = stringResource(MR.strings.pref_show_recommendations),
                checked = showRecommendations,
                onCheckedChanged = { showRecommendationsPref.set(it) }
            )
        }
    }
}
