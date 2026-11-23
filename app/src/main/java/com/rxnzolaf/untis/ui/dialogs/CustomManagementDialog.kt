package com.rxnzolaf.untis.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.rxnzolaf.untis.R
import com.rxnzolaf.untis.data.database.entities.CustomClassEntity
import com.rxnzolaf.untis.data.database.entities.CustomRoomEntity
import com.rxnzolaf.untis.data.database.entities.CustomSubjectEntity
import com.rxnzolaf.untis.data.database.entities.CustomTeacherEntity
import com.rxnzolaf.untis.data.database.entities.UserDao
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomManagementDialog(
	userId: Long,
	userDao: UserDao,
	onDismiss: () -> Unit
) {
	val scope = rememberCoroutineScope()
	var selectedTabIndex by remember { mutableIntStateOf(0) }

	val tabs = listOf(
		stringResource(R.string.all_classes),
		stringResource(R.string.all_teachers),
		stringResource(R.string.all_rooms),
		stringResource(R.string.all_subjects)
	)

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(stringResource(R.string.custom_manage_title)) },
		text = {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.height(450.dp)
			) {
				ScrollableTabRow(
					selectedTabIndex = selectedTabIndex,
					modifier = Modifier.fillMaxWidth()
				) {
					tabs.forEachIndexed { index, title ->
						Tab(
							selected = selectedTabIndex == index,
							onClick = { selectedTabIndex = index },
							text = { Text(title) }
						)
					}
				}

				Spacer(modifier = Modifier.height(8.dp))

				when (selectedTabIndex) {
					0 -> CustomClassesSection(userId, userDao)
					1 -> CustomTeachersSection(userId, userDao)
					2 -> CustomRoomsSection(userId, userDao)
					3 -> CustomSubjectsSection(userId, userDao)
				}
			}
		},
		confirmButton = {
			TextButton(onClick = onDismiss) {
				Text(stringResource(R.string.all_save))
			}
		}
	)
}

@Composable
private fun CustomClassesSection(userId: Long, userDao: UserDao) {
	val scope = rememberCoroutineScope()
	var classes by remember { mutableStateOf<List<CustomClassEntity>>(emptyList()) }
	var editingItem by remember { mutableStateOf<CustomClassEntity?>(null) }
	var deletingItem by remember { mutableStateOf<CustomClassEntity?>(null) }
	var showEditor by remember { mutableStateOf(false) }

	LaunchedEffect(userId) {
		userDao.getCustomClassesFlow(userId).collectLatest { classes = it }
	}

	Column(modifier = Modifier.fillMaxWidth()) {
		Button(
			onClick = {
				editingItem = CustomClassEntity(userId = userId, name = "")
				showEditor = true
			},
			modifier = Modifier.fillMaxWidth()
		) {
			Text("+ " + stringResource(R.string.all_add))
		}

		Spacer(modifier = Modifier.height(8.dp))

		LazyColumn(modifier = Modifier.weight(1f)) {
			items(classes) { item ->
				Card(
					modifier = Modifier
						.fillMaxWidth()
						.padding(vertical = 4.dp)
						.clickable {
							editingItem = item
							showEditor = true
						}
				) {
					Row(
						modifier = Modifier
							.padding(12.dp)
							.fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceBetween,
						verticalAlignment = Alignment.CenterVertically
					) {
						Text(text = item.name, fontWeight = FontWeight.Bold)

						IconButton(onClick = { deletingItem = item }) {
							Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.all_delete))
						}
					}
				}
			}
		}
	}

	if (showEditor && editingItem != null) {
		var name by remember { mutableStateOf(editingItem!!.name) }
		AlertDialog(
			onDismissRequest = { showEditor = false },
			title = { Text(if (editingItem!!.id == 0L) stringResource(R.string.all_add) else stringResource(R.string.all_classes)) },
			text = {
				OutlinedTextField(
					value = name,
					onValueChange = { name = it },
					label = { Text(stringResource(R.string.custom_full_name)) },
					singleLine = true,
					modifier = Modifier.fillMaxWidth()
				)
			},
			confirmButton = {
				Button(
					onClick = {
						if (name.isNotBlank()) {
							scope.launch {
								userDao.upsertCustomClass(editingItem!!.copy(name = name.trim()))
								showEditor = false
							}
						}
					}
				) {
					Text(stringResource(R.string.all_save))
				}
			},
			dismissButton = {
				TextButton(onClick = { showEditor = false }) {
					Text(stringResource(R.string.all_cancel))
				}
			}
		)
	}

	deletingItem?.let { item ->
		AlertDialog(
			onDismissRequest = { deletingItem = null },
			title = { Text(stringResource(R.string.all_delete)) },
			text = { Text(stringResource(R.string.custom_confirm_delete)) },
			confirmButton = {
				Button(
					onClick = {
						scope.launch {
							userDao.deleteCustomClass(item)
							deletingItem = null
						}
					}
				) {
					Text(stringResource(R.string.all_delete))
				}
			},
			dismissButton = {
				TextButton(onClick = { deletingItem = null }) {
					Text(stringResource(R.string.all_cancel))
				}
			}
		)
	}
}

@Composable
private fun CustomTeachersSection(userId: Long, userDao: UserDao) {
	val scope = rememberCoroutineScope()
	var teachers by remember { mutableStateOf<List<CustomTeacherEntity>>(emptyList()) }
	var editingItem by remember { mutableStateOf<CustomTeacherEntity?>(null) }
	var deletingItem by remember { mutableStateOf<CustomTeacherEntity?>(null) }
	var showEditor by remember { mutableStateOf(false) }

	LaunchedEffect(userId) {
		userDao.getCustomTeachersFlow(userId).collectLatest { teachers = it }
	}

	Column(modifier = Modifier.fillMaxWidth()) {
		Button(
			onClick = {
				editingItem = CustomTeacherEntity(userId = userId, name = "", shortName = "")
				showEditor = true
			},
			modifier = Modifier.fillMaxWidth()
		) {
			Text("+ " + stringResource(R.string.all_add))
		}

		Spacer(modifier = Modifier.height(8.dp))

		LazyColumn(modifier = Modifier.weight(1f)) {
			items(teachers) { item ->
				Card(
					modifier = Modifier
						.fillMaxWidth()
						.padding(vertical = 4.dp)
						.clickable {
							editingItem = item
							showEditor = true
						}
				) {
					Row(
						modifier = Modifier
							.padding(12.dp)
							.fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceBetween,
						verticalAlignment = Alignment.CenterVertically
					) {
						Column {
							Text(text = item.name, fontWeight = FontWeight.Bold)
							if (item.shortName.isNotBlank()) {
								Text(text = item.shortName, style = MaterialTheme.typography.bodySmall)
							}
						}

						IconButton(onClick = { deletingItem = item }) {
							Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.all_delete))
						}
					}
				}
			}
		}
	}

	if (showEditor && editingItem != null) {
		var name by remember { mutableStateOf(editingItem!!.name) }
		var shortName by remember { mutableStateOf(editingItem!!.shortName) }

		AlertDialog(
			onDismissRequest = { showEditor = false },
			title = { Text(if (editingItem!!.id == 0L) stringResource(R.string.all_add) else stringResource(R.string.all_teachers)) },
			text = {
				Column {
					OutlinedTextField(
						value = name,
						onValueChange = { name = it },
						label = { Text(stringResource(R.string.custom_full_name)) },
						singleLine = true,
						modifier = Modifier.fillMaxWidth()
					)
					Spacer(modifier = Modifier.height(8.dp))
					OutlinedTextField(
						value = shortName,
						onValueChange = { shortName = it },
						label = { Text(stringResource(R.string.custom_short_name)) },
						singleLine = true,
						modifier = Modifier.fillMaxWidth()
					)
				}
			},
			confirmButton = {
				Button(
					onClick = {
						if (name.isNotBlank()) {
							scope.launch {
								userDao.upsertCustomTeacher(editingItem!!.copy(name = name.trim(), shortName = shortName.trim()))
								showEditor = false
							}
						}
					}
				) {
					Text(stringResource(R.string.all_save))
				}
			},
			dismissButton = {
				TextButton(onClick = { showEditor = false }) {
					Text(stringResource(R.string.all_cancel))
				}
			}
		)
	}

	deletingItem?.let { item ->
		AlertDialog(
			onDismissRequest = { deletingItem = null },
			title = { Text(stringResource(R.string.all_delete)) },
			text = { Text(stringResource(R.string.custom_confirm_delete)) },
			confirmButton = {
				Button(
					onClick = {
						scope.launch {
							userDao.deleteCustomTeacher(item)
							deletingItem = null
						}
					}
				) {
					Text(stringResource(R.string.all_delete))
				}
			},
			dismissButton = {
				TextButton(onClick = { deletingItem = null }) {
					Text(stringResource(R.string.all_cancel))
				}
			}
		)
	}
}

@Composable
private fun CustomRoomsSection(userId: Long, userDao: UserDao) {
	val scope = rememberCoroutineScope()
	var rooms by remember { mutableStateOf<List<CustomRoomEntity>>(emptyList()) }
	var editingItem by remember { mutableStateOf<CustomRoomEntity?>(null) }
	var deletingItem by remember { mutableStateOf<CustomRoomEntity?>(null) }
	var showEditor by remember { mutableStateOf(false) }

	LaunchedEffect(userId) {
		userDao.getCustomRoomsFlow(userId).collectLatest { rooms = it }
	}

	Column(modifier = Modifier.fillMaxWidth()) {
		Button(
			onClick = {
				editingItem = CustomRoomEntity(userId = userId, name = "")
				showEditor = true
			},
			modifier = Modifier.fillMaxWidth()
		) {
			Text("+ " + stringResource(R.string.all_add))
		}

		Spacer(modifier = Modifier.height(8.dp))

		LazyColumn(modifier = Modifier.weight(1f)) {
			items(rooms) { item ->
				Card(
					modifier = Modifier
						.fillMaxWidth()
						.padding(vertical = 4.dp)
						.clickable {
							editingItem = item
							showEditor = true
						}
				) {
					Row(
						modifier = Modifier
							.padding(12.dp)
							.fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceBetween,
						verticalAlignment = Alignment.CenterVertically
					) {
						Text(text = item.name, fontWeight = FontWeight.Bold)

						IconButton(onClick = { deletingItem = item }) {
							Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.all_delete))
						}
					}
				}
			}
		}
	}

	if (showEditor && editingItem != null) {
		var name by remember { mutableStateOf(editingItem!!.name) }

		AlertDialog(
			onDismissRequest = { showEditor = false },
			title = { Text(if (editingItem!!.id == 0L) stringResource(R.string.all_add) else stringResource(R.string.all_rooms)) },
			text = {
				OutlinedTextField(
					value = name,
					onValueChange = { name = it },
					label = { Text(stringResource(R.string.custom_full_name)) },
					singleLine = true,
					modifier = Modifier.fillMaxWidth()
				)
			},
			confirmButton = {
				Button(
					onClick = {
						if (name.isNotBlank()) {
							scope.launch {
								userDao.upsertCustomRoom(editingItem!!.copy(name = name.trim()))
								showEditor = false
							}
						}
					}
				) {
					Text(stringResource(R.string.all_save))
				}
			},
			dismissButton = {
				TextButton(onClick = { showEditor = false }) {
					Text(stringResource(R.string.all_cancel))
				}
			}
		)
	}

	deletingItem?.let { item ->
		AlertDialog(
			onDismissRequest = { deletingItem = null },
			title = { Text(stringResource(R.string.all_delete)) },
			text = { Text(stringResource(R.string.custom_confirm_delete)) },
			confirmButton = {
				Button(
					onClick = {
						scope.launch {
							userDao.deleteCustomRoom(item)
							deletingItem = null
						}
					}
				) {
					Text(stringResource(R.string.all_delete))
				}
			},
			dismissButton = {
				TextButton(onClick = { deletingItem = null }) {
					Text(stringResource(R.string.all_cancel))
				}
			}
		)
	}
}

@Composable
private fun CustomSubjectsSection(userId: Long, userDao: UserDao) {
	val scope = rememberCoroutineScope()
	var subjects by remember { mutableStateOf<List<CustomSubjectEntity>>(emptyList()) }
	var editingItem by remember { mutableStateOf<CustomSubjectEntity?>(null) }
	var deletingItem by remember { mutableStateOf<CustomSubjectEntity?>(null) }
	var showEditor by remember { mutableStateOf(false) }

	LaunchedEffect(userId) {
		userDao.getCustomSubjectsFlow(userId).collectLatest { subjects = it }
	}

	Column(modifier = Modifier.fillMaxWidth()) {
		Button(
			onClick = {
				editingItem = CustomSubjectEntity(userId = userId, name = "", shortName = "", color = defaultCustomColors[0])
				showEditor = true
			},
			modifier = Modifier.fillMaxWidth()
		) {
			Text("+ " + stringResource(R.string.all_add))
		}

		Spacer(modifier = Modifier.height(8.dp))

		LazyColumn(modifier = Modifier.weight(1f)) {
			items(subjects) { item ->
				Card(
					modifier = Modifier
						.fillMaxWidth()
						.padding(vertical = 4.dp)
						.clickable {
							editingItem = item
							showEditor = true
						}
				) {
					Row(
						modifier = Modifier
							.padding(12.dp)
							.fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceBetween,
						verticalAlignment = Alignment.CenterVertically
					) {
						Row(verticalAlignment = Alignment.CenterVertically) {
							val itemColor = runCatching { Color(item.color.toColorInt()) }.getOrDefault(Color.Blue)
							Box(
								modifier = Modifier
									.size(20.dp)
									.clip(CircleShape)
									.background(itemColor)
							)
							Spacer(modifier = Modifier.size(8.dp))
							Column {
								Text(text = item.name, fontWeight = FontWeight.Bold)
								if (item.shortName.isNotBlank()) {
									Text(text = item.shortName, style = MaterialTheme.typography.bodySmall)
								}
							}
						}

						IconButton(onClick = { deletingItem = item }) {
							Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.all_delete))
						}
					}
				}
			}
		}
	}

	if (showEditor && editingItem != null) {
		var name by remember { mutableStateOf(editingItem!!.name) }
		var shortName by remember { mutableStateOf(editingItem!!.shortName) }
		var selectedColor by remember { mutableStateOf(editingItem!!.color.ifEmpty { defaultCustomColors[0] }) }

		AlertDialog(
			onDismissRequest = { showEditor = false },
			title = { Text(if (editingItem!!.id == 0L) stringResource(R.string.all_add) else stringResource(R.string.all_subjects)) },
			text = {
				Column {
					OutlinedTextField(
						value = name,
						onValueChange = { name = it },
						label = { Text(stringResource(R.string.custom_full_name)) },
						singleLine = true,
						modifier = Modifier.fillMaxWidth()
					)
					Spacer(modifier = Modifier.height(8.dp))
					OutlinedTextField(
						value = shortName,
						onValueChange = { shortName = it },
						label = { Text(stringResource(R.string.custom_short_name)) },
						singleLine = true,
						modifier = Modifier.fillMaxWidth()
					)
					Spacer(modifier = Modifier.height(8.dp))
					Text(stringResource(R.string.custom_color), style = MaterialTheme.typography.bodySmall)
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.padding(top = 4.dp),
						horizontalArrangement = Arrangement.SpaceBetween
					) {
						defaultCustomColors.forEach { hex ->
							val color = Color(hex.toColorInt())
							Box(
								modifier = Modifier
									.size(28.dp)
									.clip(CircleShape)
									.background(color)
									.clickable { selectedColor = hex }
							)
						}
					}
				}
			},
			confirmButton = {
				Button(
					onClick = {
						if (name.isNotBlank()) {
							scope.launch {
								userDao.upsertCustomSubject(editingItem!!.copy(name = name.trim(), shortName = shortName.trim(), color = selectedColor))
								showEditor = false
							}
						}
					}
				) {
					Text(stringResource(R.string.all_save))
				}
			},
			dismissButton = {
				TextButton(onClick = { showEditor = false }) {
					Text(stringResource(R.string.all_cancel))
				}
			}
		)
	}

	deletingItem?.let { item ->
		AlertDialog(
			onDismissRequest = { deletingItem = null },
			title = { Text(stringResource(R.string.all_delete)) },
			text = { Text(stringResource(R.string.custom_confirm_delete)) },
			confirmButton = {
				Button(
					onClick = {
						scope.launch {
							userDao.deleteCustomSubject(item)
							deletingItem = null
						}
					}
				) {
					Text(stringResource(R.string.all_delete))
				}
			},
			dismissButton = {
				TextButton(onClick = { deletingItem = null }) {
					Text(stringResource(R.string.all_cancel))
				}
			}
		)
	}
}
