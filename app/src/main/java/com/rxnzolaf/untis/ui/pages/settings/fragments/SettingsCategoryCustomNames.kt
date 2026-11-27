package com.rxnzolaf.untis.ui.pages.settings.fragments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rxnzolaf.untis.R
import com.rxnzolaf.untis.data.database.entities.CustomSubjectNameEntity
import com.rxnzolaf.untis.data.database.entities.CustomWeekdayNameEntity
import com.rxnzolaf.untis.data.repository.CustomNamesEntryPoint
import com.rxnzolaf.untis.data.repository.LocalMasterDataRepository
import com.rxnzolaf.untis.ui.dialogs.CustomSubjectEditorDialog
import com.sapuseven.untis.api.model.untis.timetable.PeriodElement
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsCategoryCustomNames() {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val masterDataRepository = LocalMasterDataRepository.current
	val userId = masterDataRepository.user?.id ?: -1L

	val customNamesRepo = remember {
		EntryPointAccessors.fromApplication(context.applicationContext, CustomNamesEntryPoint::class.java).customNamesRepository()
	}

	var selectedTab by remember { mutableStateOf(0) }
	var searchQuery by remember { mutableStateOf("") }

	var subjectNamesMap by remember { mutableStateOf<Map<String, CustomSubjectNameEntity>>(emptyMap()) }
	var weekdayNamesMap by remember { mutableStateOf<Map<Int, CustomWeekdayNameEntity>>(emptyMap()) }

	var editingSubject by remember { mutableStateOf<Pair<PeriodElement, String>?>(null) }
	var showResetAllConfirm by remember { mutableStateOf(false) }

	val allSubjects by masterDataRepository.allSubjects.collectAsState()

	LaunchedEffect(userId) {
		if (userId > 0L) {
			customNamesRepo.getSubjectNamesFlow(userId).collect { subjectNamesMap = it }
		}
	}

	LaunchedEffect(userId) {
		if (userId > 0L) {
			customNamesRepo.getWeekdayNamesFlow(userId).collect { weekdayNamesMap = it }
		}
	}

	Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
		TabRow(selectedTabIndex = selectedTab) {
			Tab(
				selected = selectedTab == 0,
				onClick = { selectedTab = 0 },
				text = { Text(stringResource(R.string.custom_names_subjects_tab)) }
			)
			Tab(
				selected = selectedTab == 1,
				onClick = { selectedTab = 1 },
				text = { Text(stringResource(R.string.custom_names_weekdays_tab)) }
			)
		}

		Spacer(modifier = Modifier.height(16.dp))

		if (selectedTab == 0) {
			OutlinedTextField(
				value = searchQuery,
				onValueChange = { searchQuery = it },
				label = { Text(stringResource(R.string.custom_names_search_hint)) },
				modifier = Modifier.fillMaxWidth(),
				singleLine = true
			)

			Spacer(modifier = Modifier.height(8.dp))

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				OutlinedButton(
					onClick = { showResetAllConfirm = true },
					modifier = Modifier.weight(1f)
				) {
					Text(stringResource(R.string.custom_names_reset_all))
				}
			}

			Spacer(modifier = Modifier.height(8.dp))

			val filteredSubjects = allSubjects.filter { element ->
				val name = masterDataRepository.getLongName(element)
				val shortName = masterDataRepository.getShortName(element)
				searchQuery.isBlank() || name.contains(searchQuery, true) || shortName.contains(searchQuery, true)
			}

			LazyColumn(
				modifier = Modifier.weight(1f),
				verticalArrangement = Arrangement.spacedBy(4.dp)
			) {
				items(filteredSubjects) { element ->
					val longName = masterDataRepository.getLongName(element)
					val shortName = masterDataRepository.getShortName(element)
					val custom = subjectNamesMap[element.id.toString()]

					ListItem(
						headlineContent = { Text(custom?.nickname?.ifBlank { longName } ?: longName) },
						supportingContent = {
							val abbr = custom?.abbreviation?.takeIf { it.isNotBlank() } ?: shortName
							Text("Kürzel: $abbr" + if (!custom?.nickname.isNullOrBlank()) " • (Org: $longName)" else "")
						},
						modifier = Modifier
							.fillMaxWidth()
							.clickable {
								editingSubject = Pair(element, longName)
							}
					)
					HorizontalDivider()
				}
			}
		} else {
			LazyColumn(
				modifier = Modifier.weight(1f),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				items(DayOfWeek.entries) { day ->
					val dayVal = day.value
					val custom = weekdayNamesMap[dayVal]
					val defaultName = day.getDisplayName(TextStyle.FULL, Locale.getDefault())

					var nick by remember(custom) { mutableStateOf(custom?.nickname ?: "") }
					var abbr by remember(custom) { mutableStateOf(custom?.abbreviation ?: "") }

					Card(modifier = Modifier.fillMaxWidth()) {
						Column(modifier = Modifier.padding(12.dp)) {
							Text(text = defaultName, style = MaterialTheme.typography.titleMedium)
							Spacer(modifier = Modifier.height(8.dp))
							OutlinedTextField(
								value = nick,
								onValueChange = {
									if (it.length <= 20) {
										nick = it
										scope.launch { customNamesRepo.saveWeekdayName(userId, dayVal, it, abbr) }
									}
								},
								label = { Text(stringResource(R.string.custom_names_nickname)) },
								singleLine = true,
								modifier = Modifier.fillMaxWidth()
							)
							Spacer(modifier = Modifier.height(4.dp))
							OutlinedTextField(
								value = abbr,
								onValueChange = {
									if (it.length <= 6) {
										abbr = it
										scope.launch { customNamesRepo.saveWeekdayName(userId, dayVal, nick, it) }
									}
								},
								label = { Text(stringResource(R.string.custom_names_abbreviation)) },
								singleLine = true,
								modifier = Modifier.fillMaxWidth()
							)
						}
					}
				}
			}
		}
	}

	editingSubject?.let { (element, originalLong) ->
		if (userId > 0L) {
			CustomSubjectEditorDialog(
				userId = userId,
				subjectKey = element.id.toString(),
				originalLongName = originalLong,
				originalShortName = masterDataRepository.getShortName(element),
				customNamesRepository = customNamesRepo,
				existingCustom = subjectNamesMap[element.id.toString()],
				onDismiss = { editingSubject = null }
			)
		}
	}

	if (showResetAllConfirm) {
		AlertDialog(
			onDismissRequest = { showResetAllConfirm = false },
			title = { Text(stringResource(R.string.custom_names_reset_all)) },
			text = { Text(stringResource(R.string.custom_names_reset_confirm)) },
			confirmButton = {
				TextButton(
					onClick = {
						scope.launch {
							customNamesRepo.deleteAllSubjectNames(userId)
							customNamesRepo.deleteAllWeekdayNames(userId)
						}
						showResetAllConfirm = false
					}
				) {
					Text(stringResource(R.string.all_ok))
				}
			},
			dismissButton = {
				TextButton(onClick = { showResetAllConfirm = false }) {
					Text(stringResource(R.string.all_cancel))
				}
			}
		)
	}
}
