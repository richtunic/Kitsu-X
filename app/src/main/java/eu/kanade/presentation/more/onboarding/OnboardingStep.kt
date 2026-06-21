package eu.kanade.presentation.more.onboarding

import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.StringResource

internal interface OnboardingStep {

    val isComplete: Boolean

    val title: StringResource

    val description: StringResource

    @Composable
    fun Content()
}
