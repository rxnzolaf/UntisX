package com.rxnzolaf.untis.helpers

import android.os.Build
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.sapuseven.compose.protostore.ui.preferences.materialColors
import com.rxnzolaf.untis.data.settings.model.AppTheme as SettingsAppTheme
import com.sapuseven.untis.ui.material.scheme.Scheme
import com.rxnzolaf.untis.ui.theme.animated
import com.rxnzolaf.untis.ui.theme.toColorScheme

val GamingColorScheme = ColorScheme(
	primary = Color(0xFF4F6BFF),
	onPrimary = Color(0xFFFFFFFF),
	primaryContainer = Color(0xFF1E295C),
	onPrimaryContainer = Color(0xFFDBEAFE),
	inversePrimary = Color(0xFF93C5FD),
	secondary = Color(0xFF8B5CF6),
	onSecondary = Color(0xFFFFFFFF),
	secondaryContainer = Color(0xFF311057),
	onSecondaryContainer = Color(0xFFEDE9FE),
	tertiary = Color(0xFF22D3EE),
	onTertiary = Color(0xFF00363D),
	tertiaryContainer = Color(0xFF083344),
	onTertiaryContainer = Color(0xFFCFFAFE),
	background = Color(0xFF0B0B1A),
	onBackground = Color(0xFFF3F4FF),
	surface = Color(0xFF17173A),
	onSurface = Color(0xFFF3F4FF),
	surfaceVariant = Color(0xFF1E1E4A),
	onSurfaceVariant = Color(0xFFA5A8D6),
	surfaceTint = Color(0xFF4F6BFF),
	inverseSurface = Color(0xFFF3F4FF),
	inverseOnSurface = Color(0xFF0B0B1A),
	error = Color(0xFFFF4D6D),
	onError = Color(0xFFFFFFFF),
	errorContainer = Color(0xFF5A1827),
	onErrorContainer = Color(0xFFFFD8E4),
	outline = Color(0xFF475569),
	outlineVariant = Color(0xFF334155),
	scrim = Color(0xFF000000),
	surfaceBright = Color(0xFF262661),
	surfaceDim = Color(0xFF12122B),
	surfaceContainer = Color(0xFF17173A),
	surfaceContainerHigh = Color(0xFF1E1E4A),
	surfaceContainerHighest = Color(0xFF2A2A69),
	surfaceContainerLow = Color(0xFF11112D),
	surfaceContainerLowest = Color(0xFF0B0B1A)
)

val LegacyColorScheme = ColorScheme(
	primary = Color(0xFF4AA3DF),
	onPrimary = Color(0xFFFFFFFF),
	primaryContainer = Color(0xFFE8F4FC),
	onPrimaryContainer = Color(0xFF1B4965),
	inversePrimary = Color(0xFF4AA3DF),
	secondary = Color(0xFF4AA3DF),
	onSecondary = Color(0xFFFFFFFF),
	secondaryContainer = Color(0xFFF5F6F7),
	onSecondaryContainer = Color(0xFF2B2F33),
	tertiary = Color(0xFFE5483B),
	onTertiary = Color(0xFFFFFFFF),
	tertiaryContainer = Color(0xFFFFEBEA),
	onTertiaryContainer = Color(0xFF7A150D),
	background = Color(0xFFFFFFFF),
	onBackground = Color(0xFF2B2F33),
	surface = Color(0xFFF5F6F7),
	onSurface = Color(0xFF2B2F33),
	surfaceVariant = Color(0xFFF5F6F7),
	onSurfaceVariant = Color(0xFF8A9097),
	surfaceTint = Color(0xFF4AA3DF),
	inverseSurface = Color(0xFF2B2F33),
	inverseOnSurface = Color(0xFFFFFFFF),
	error = Color(0xFFE5483B),
	onError = Color(0xFFFFFFFF),
	errorContainer = Color(0xFFFFEBEA),
	onErrorContainer = Color(0xFF7A150D),
	outline = Color(0xFFE3E5E8),
	outlineVariant = Color(0xFFE3E5E8),
	scrim = Color(0xFF000000),
	surfaceBright = Color(0xFFFFFFFF),
	surfaceDim = Color(0xFFF0F2F5),
	surfaceContainer = Color(0xFFF5F6F7),
	surfaceContainerHigh = Color(0xFFEBECEE),
	surfaceContainerHighest = Color(0xFFE3E5E8),
	surfaceContainerLow = Color(0xFFFAFAFA),
	surfaceContainerLowest = Color(0xFFFFFFFF)
)

private fun setSystemUiColor(
	systemUiController: SystemUiController,
	color: Color = Color.Transparent,
	darkIcons: Boolean = color.luminance() > 0.5f
) {
	systemUiController.run {
		setSystemBarsColor(
			color = color, darkIcons = darkIcons
		)

		setNavigationBarColor(
			color = color, darkIcons = darkIcons
		)
	}
}

@Composable
fun AppTheme(
	darkTheme: ThemeMode = ThemeMode.FollowSystem,
	darkThemeOled: Boolean = false,
	themeColor: Color? = null,
	appTheme: SettingsAppTheme = SettingsAppTheme.STANDARD,
	systemUiController: SystemUiController? = rememberSystemUiController(),
	content: @Composable () -> Unit
) {
	val isGaming = appTheme == SettingsAppTheme.GAMING
	val isLegacy = appTheme == SettingsAppTheme.LEGACY
	val defaultThemeColor = materialColors[0]
	val isDarkTheme = when {
		isGaming -> true
		isLegacy -> false
		else -> when (darkTheme) {
			ThemeMode.AlwaysLight -> false
			ThemeMode.AlwaysDark -> true
			ThemeMode.FollowSystem -> isSystemInDarkTheme()
		}
	}
	Log.d("AppTheme", "Theme mode: $darkTheme, isDarkTheme: $isDarkTheme, isGaming: $isGaming, isLegacy: $isLegacy")

	val colorScheme = when {
		isGaming -> GamingColorScheme
		isLegacy -> LegacyColorScheme
		themeColor == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
			val context = LocalContext.current
			if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
		}

		isDarkTheme -> Scheme.dark((themeColor ?: defaultThemeColor).toArgb()).toColorScheme()
		else -> Scheme.light((themeColor ?: defaultThemeColor).toArgb()).toColorScheme()
	}.run {
		if (isDarkTheme && darkThemeOled && !isGaming && !isLegacy)
			copy(background = Color.Black, surface = Color.Black)
		else
			this
	}

	val darkIcons = colorScheme.background.luminance() > .5f
	systemUiController?.let {
		setSystemUiColor(it, Color.Transparent, darkIcons)
	}

	MaterialTheme(
		colorScheme = colorScheme.animated(),
		content = content
	)
}

enum class ThemeMode {
	AlwaysLight,
	AlwaysDark,
	FollowSystem
}
