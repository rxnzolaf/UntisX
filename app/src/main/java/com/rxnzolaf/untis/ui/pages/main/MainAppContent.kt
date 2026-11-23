package com.rxnzolaf.untis.ui.pages.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.rxnzolaf.untis.R
import com.rxnzolaf.untis.data.repository.GlobalSettingsRepository
import com.rxnzolaf.untis.data.repository.UserRepository
import com.rxnzolaf.untis.data.settings.model.DarkTheme
import com.rxnzolaf.untis.data.settings.model.UserSettings
import com.rxnzolaf.untis.helpers.AppTheme
import com.rxnzolaf.untis.helpers.ThemeMode
import com.rxnzolaf.untis.ui.common.AppScaffold
import com.rxnzolaf.untis.ui.navigation.AppNavHost
import com.rxnzolaf.untis.ui.navigation.AppNavigator
import com.rxnzolaf.untis.ui.navigation.AppRoutes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
	userState: UserRepository.UserState,
	globalSettingsRepository: GlobalSettingsRepository,
	settingsFlow: Flow<UserSettings>,
	navigator: AppNavigator
) {
	val settings by settingsFlow.collectAsState(initial = UserSettings.getDefaultInstance())

	val darkTheme = when (settings.darkTheme) {
		DarkTheme.DARK -> ThemeMode.AlwaysDark
		DarkTheme.LIGHT -> ThemeMode.AlwaysLight
		else -> ThemeMode.FollowSystem
	}
	val darkThemeOled by remember { derivedStateOf { settings.darkThemeOled } }
	val themeColor by remember { derivedStateOf { if (settings.hasThemeColor()) Color(settings.themeColor) else null } }
	val appTheme = settings.appTheme

	AppTheme(darkTheme, darkThemeOled, themeColor, appTheme) {
		Surface(modifier = Modifier.fillMaxSize()) {
			when (userState) {
				is UserRepository.UserState.Loading -> {
					AppScaffold(
						topBar = {
							CenterAlignedTopAppBar(
								title = { Text(stringResource(id = R.string.app_name)) },
								navigationIcon = {
									IconButton(onClick = {}) {
										Icon(
											imageVector = Icons.Outlined.Menu,
											contentDescription = null
										)
									}
								},
								colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
									containerColor = Color.Transparent,
									scrolledContainerColor = Color.Transparent
								)
							)
						},
						modifier = Modifier.safeDrawingPadding()
					) {}
				}

				is UserRepository.UserState.NoUsers -> {
					AppNavHost(
						navigator = navigator,
						startDestination = AppRoutes.Login
					)
				}

				is UserRepository.UserState.User -> {
					key(userState.user.id) {
						AppNavHost(
							navigator = navigator,
							startDestination = AppRoutes.Timetable()
						)
					}
				}
			}
		}
	}
}
