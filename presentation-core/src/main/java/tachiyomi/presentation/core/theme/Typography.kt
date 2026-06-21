package tachiyomi.presentation.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import tachiyomi.presentation.core.R

val OutfitFontFamily = FontFamily(
    Font(R.font.outfit_regular, FontWeight.Normal),
    Font(R.font.outfit_medium, FontWeight.Medium),
    Font(R.font.outfit_semibold, FontWeight.SemiBold),
    Font(R.font.outfit_bold, FontWeight.Bold),
)

val kitsuXTypography = Typography(
    displayLarge = Typography().displayLarge.copy(fontFamily = OutfitFontFamily),
    displayMedium = Typography().displayMedium.copy(fontFamily = OutfitFontFamily),
    displaySmall = Typography().displaySmall.copy(fontFamily = OutfitFontFamily),
    headlineLarge = Typography().headlineLarge.copy(fontFamily = OutfitFontFamily),
    headlineMedium = Typography().headlineMedium.copy(fontFamily = OutfitFontFamily),
    headlineSmall = Typography().headlineSmall.copy(fontFamily = OutfitFontFamily),
    titleLarge = Typography().titleLarge.copy(fontFamily = OutfitFontFamily),
    titleMedium = Typography().titleMedium.copy(fontFamily = OutfitFontFamily),
    titleSmall = Typography().titleSmall.copy(fontFamily = OutfitFontFamily),
    bodyLarge = Typography().bodyLarge.copy(fontFamily = OutfitFontFamily),
    bodyMedium = Typography().bodyMedium.copy(fontFamily = OutfitFontFamily),
    bodySmall = Typography().bodySmall.copy(fontFamily = OutfitFontFamily),
    labelLarge = Typography().labelLarge.copy(fontFamily = OutfitFontFamily),
    labelMedium = Typography().labelMedium.copy(fontFamily = OutfitFontFamily),
    labelSmall = Typography().labelSmall.copy(fontFamily = OutfitFontFamily),
)

val Typography.header: TextStyle
    @Composable
    get() = bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
    )
