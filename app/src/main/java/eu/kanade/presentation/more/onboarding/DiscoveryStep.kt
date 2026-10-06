package eu.kanade.presentation.more.onboarding

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

internal class DiscoveryStep : OnboardingStep {
    override val title = MR.strings.kitsux_onboarding_discovery_title
    override val description = MR.strings.kitsux_onboarding_discovery_description
    override val isComplete: Boolean = true

    @Composable
    override fun Content() {
        Text(
            text = stringResource(MR.strings.kitsux_onboarding_discovery_info),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(24.dp),
        )
    }
}
