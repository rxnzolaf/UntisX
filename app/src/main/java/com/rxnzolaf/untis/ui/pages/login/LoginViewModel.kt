package com.rxnzolaf.untis.ui.pages.login

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.sapuseven.untis.api.model.untis.SchoolInfo
import com.sapuseven.untis.api.model.untis.Settings
import com.sapuseven.untis.api.model.untis.UserData
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.masterdata.TimeGrid
import com.rxnzolaf.untis.data.database.entities.User
import com.rxnzolaf.untis.data.database.entities.UserDao
import com.rxnzolaf.untis.data.repository.UserRepository
import com.rxnzolaf.untis.helpers.SerializationUtils.getJSON
import com.rxnzolaf.untis.services.CodeScanService
import com.rxnzolaf.untis.ui.navigation.AppNavigator
import com.rxnzolaf.untis.ui.navigation.AppRoutes
import com.rxnzolaf.untis.ui.pages.ActivityViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
	private val codeScanService: CodeScanService,
	private val navigator: AppNavigator,
	private val userDao: UserDao,
	private val userRepository: UserRepository,
) : ActivityViewModel() {
	var searchMode by mutableStateOf(false)
		private set

	var shouldShowBackButton = derivedStateOf {
		searchMode
	}

	private val _schoolSearchText = MutableStateFlow<String>("")
	val schoolSearchText: StateFlow<String> = _schoolSearchText

	val events = MutableSharedFlow<LoginEvents>()

	val codeScanResultHandler: (String?) -> Unit = {
		navigator.navigate(AppRoutes.LoginDataInput(autoLoginData = it))
	}

	fun setCodeScanLauncher(launcher: ManagedActivityResultLauncher<ScanOptions, ScanIntentResult>) {
		codeScanService.setLauncher(launcher)
	}

	fun onSchoolSearchFocusChanged(focused: Boolean) {
		if (focused) searchMode = true
	}

	private fun updateSearchMode(enabled: Boolean) {
		searchMode = enabled
		if (!enabled) {
			viewModelScope.launch {
				events.emit(LoginEvents.ClearFocus)
				_schoolSearchText.value = ""
			}
		}
	}

	fun goBack() {
		if (searchMode) {
			updateSearchMode(false)
		} else {
			navigator.popBackStack()
		}
	}

	fun updateSchoolSearchText(text: String) {
		_schoolSearchText.value = text
	}

	fun onSchoolSelected(school: SchoolInfo) {
		navigator.navigate(
			AppRoutes.LoginDataInput(schoolInfoSerialized = getJSON().encodeToString<SchoolInfo>(school))
		)
	}

	fun onCodeScanClick() {
		codeScanService.scanCode(codeScanResultHandler)
	}

	fun onDemoClick() {
		navigator.navigate(AppRoutes.LoginDataInput(demoLogin = true))
	}

	fun onManualDataInputClick() {
		navigator.navigate(AppRoutes.LoginDataInput())
	}

	fun onCustomTimetableClick() {
		viewModelScope.launch {
			val user = User(
				id = 0,
				profileName = "Eigener Stundenplan",
				apiHost = "custom",
				anonymous = true,
				masterDataTimestamp = 0L,
				timeGrid = TimeGrid(days = emptyList()),
				userData = UserData(
					elemType = ElementType.STUDENT,
					elemId = 1,
					displayName = "Eigener Stundenplan",
					schoolName = "Offline",
					departmentId = 0
				),
				settings = Settings()
			)
			val userId = userDao.insert(user)
			userRepository.switchUser(userId)
			navigator.navigate(AppRoutes.Timetable()) {
				popUpTo(AppRoutes.Login) { inclusive = true }
			}
		}
	}
}
