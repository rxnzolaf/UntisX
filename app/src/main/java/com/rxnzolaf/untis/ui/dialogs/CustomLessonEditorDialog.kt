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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.rxnzolaf.untis.data.database.entities.CustomLessonEntity
import com.rxnzolaf.untis.data.database.entities.CustomRoomEntity
import com.rxnzolaf.untis.data.database.entities.CustomSubjectEntity
import com.rxnzolaf.untis.data.database.entities.CustomTeacherEntity
import com.rxnzolaf.untis.data.database.entities.UserDao
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

val defaultCustomColors = listOf(
	"#4F6BFF", // Blue
	"#8B5CF6", // Purple
	"#22D3EE", // Cyan
	"#2DC9A0", // Teal
	"#F29A1F", // Orange
	"#FF4D6D", // Red
	"#A8E6A0"  // Green
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomLessonEditorDialog(
	userId: Long,
	userDao: UserDao,
	onDismiss: () -> Unit
) {
	val scope = rememberCoroutineScope()
	var lessons by remember { mutableStateOf<List<CustomLessonEntity>>(emptyList()) }
	var editingLesson by remember { mutableStateOf<CustomLessonEntity?>(null) }
	var showEditor by remember { mutableStateOf(false) }

	LaunchedEffect(userId) {
		userDao.getCustomLessonsFlow(userId).collectLatest {
			lessons = it
		}
	}

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text("Eigener Stundenplan verwalten") },
		text = {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.height(400.dp)
			) {
				Button(
					onClick = {
						editingLesson = CustomLessonEntity(
							userId = userId,
							subjectName = "",
							shortName = "",
							dayOfWeek = 1,
							startTime = "08:00",
							endTime = "08:45",
							color = defaultCustomColors[0]
						)
						showEditor = true
					},
					modifier = Modifier.fillMaxWidth()
				) {
					Text("+ Stunde hinzufügen")
				}

				Spacer(modifier = Modifier.height(8.dp))

				LazyColumn(
					modifier = Modifier.weight(1f)
				) {
					items(lessons) { lesson ->
						Card(
							modifier = Modifier
								.fillMaxWidth()
								.padding(vertical = 4.dp)
								.clickable {
									editingLesson = lesson
									showEditor = true
								}
						) {
							Row(
								modifier = Modifier
									.fillMaxWidth()
									.padding(12.dp),
								verticalAlignment = Alignment.CenterVertically
							) {
								Box(
									modifier = Modifier
										.size(16.dp)
										.clip(CircleShape)
										.background(
											try {
												Color(lesson.color.toColorInt())
											} catch (_: Throwable) {
												Color.Blue
											}
										)
								)
								Column(
									modifier = Modifier
										.weight(1f)
										.padding(start = 12.dp)
								) {
									Text(
										text = "${lesson.subjectName} (${lesson.shortName})",
										fontWeight = FontWeight.Bold
									)
									Text(
										text = "Tag ${lesson.dayOfWeek} • ${lesson.startTime} - ${lesson.endTime} • ${lesson.room}",
										style = MaterialTheme.typography.bodySmall
									)
								}
								IconButton(onClick = {
									scope.launch {
										userDao.deleteCustomLesson(lesson)
									}
								}) {
									Icon(Icons.Outlined.Delete, contentDescription = "Löschen")
								}
							}
						}
					}
				}
			}
		},
		confirmButton = {
			TextButton(onClick = onDismiss) {
				Text(stringResource(R.string.all_ok))
			}
		}
	)

	if (showEditor && editingLesson != null) {
		SingleLessonEditorDialog(
			userId = userId,
			userDao = userDao,
			lesson = editingLesson!!,
			onSave = { savedLesson ->
				scope.launch {
					userDao.upsertCustomLesson(savedLesson)
					showEditor = false
					editingLesson = null
				}
			},
			onDismiss = {
				showEditor = false
				editingLesson = null
			}
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleLessonEditorDialog(
	userId: Long,
	userDao: UserDao,
	lesson: CustomLessonEntity,
	onSave: (CustomLessonEntity) -> Unit,
	onDismiss: () -> Unit
) {
	val scope = rememberCoroutineScope()

	var customClasses by remember { mutableStateOf<List<CustomClassEntity>>(emptyList()) }
	var customSubjects by remember { mutableStateOf<List<CustomSubjectEntity>>(emptyList()) }
	var customTeachers by remember { mutableStateOf<List<CustomTeacherEntity>>(emptyList()) }
	var customRooms by remember { mutableStateOf<List<CustomRoomEntity>>(emptyList()) }

	LaunchedEffect(userId) {
		launch { userDao.getCustomClassesFlow(userId).collectLatest { customClasses = it } }
		launch { userDao.getCustomSubjectsFlow(userId).collectLatest { customSubjects = it } }
		launch { userDao.getCustomTeachersFlow(userId).collectLatest { customTeachers = it } }
		launch { userDao.getCustomRoomsFlow(userId).collectLatest { customRooms = it } }
	}

	var subjectName by remember { mutableStateOf(lesson.subjectName) }
	var shortName by remember { mutableStateOf(lesson.shortName) }
	var teacher by remember { mutableStateOf(lesson.teacher) }
	var room by remember { mutableStateOf(lesson.room) }
	var className by remember { mutableStateOf(lesson.className) }
	var classId by remember { mutableStateOf(lesson.classId) }
	var teacherId by remember { mutableStateOf(lesson.teacherId) }
	var roomId by remember { mutableStateOf(lesson.roomId) }
	var subjectId by remember { mutableStateOf(lesson.subjectId) }

	var dayOfWeek by remember { mutableIntStateOf(lesson.dayOfWeek) }
	var startTime by remember { mutableStateOf(lesson.startTime) }
	var endTime by remember { mutableStateOf(lesson.endTime) }
	var color by remember { mutableStateOf(lesson.color.ifEmpty { defaultCustomColors[0] }) }

	var subjectExpanded by remember { mutableStateOf(false) }
	var classExpanded by remember { mutableStateOf(false) }
	var teacherExpanded by remember { mutableStateOf(false) }
	var roomExpanded by remember { mutableStateOf(false) }
	var dayExpanded by remember { mutableStateOf(false) }

	var showAddSubjectDialog by remember { mutableStateOf(false) }
	var showAddClassDialog by remember { mutableStateOf(false) }
	var showAddTeacherDialog by remember { mutableStateOf(false) }
	var showAddRoomDialog by remember { mutableStateOf(false) }

	val days = listOf("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag")

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(if (lesson.id == 0L) "Stunde hinzufügen" else "Stunde bearbeiten") },
		text = {
			LazyColumn(
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				// Class Picker
				item {
					ExposedDropdownMenuBox(
						expanded = classExpanded,
						onExpandedChange = { classExpanded = !classExpanded }
					) {
						OutlinedTextField(
							value = className.ifEmpty { stringResource(R.string.all_classes) },
							onValueChange = {},
							readOnly = true,
							label = { Text(stringResource(R.string.all_classes)) },
							trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
							modifier = Modifier
								.fillMaxWidth()
								.menuAnchor()
						)
						ExposedDropdownMenu(
							expanded = classExpanded,
							onDismissRequest = { classExpanded = false }
						) {
							DropdownMenuItem(
								text = { Text("+ " + stringResource(R.string.all_add) + "...") },
								onClick = {
									classExpanded = false
									showAddClassDialog = true
								}
							)
							customClasses.forEach { cls ->
								DropdownMenuItem(
									text = { Text(cls.name) },
									onClick = {
										classId = cls.id
										className = cls.name
										classExpanded = false
									}
								)
							}
						}
					}
				}

				// Subject Picker
				item {
					ExposedDropdownMenuBox(
						expanded = subjectExpanded,
						onExpandedChange = { subjectExpanded = !subjectExpanded }
					) {
						OutlinedTextField(
							value = if (subjectName.isNotBlank()) "$subjectName ($shortName)" else stringResource(R.string.all_subjects),
							onValueChange = {},
							readOnly = true,
							label = { Text(stringResource(R.string.all_subjects)) },
							trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
							modifier = Modifier
								.fillMaxWidth()
								.menuAnchor()
						)
						ExposedDropdownMenu(
							expanded = subjectExpanded,
							onDismissRequest = { subjectExpanded = false }
						) {
							DropdownMenuItem(
								text = { Text("+ " + stringResource(R.string.all_add) + "...") },
								onClick = {
									subjectExpanded = false
									showAddSubjectDialog = true
								}
							)
							customSubjects.forEach { subj ->
								DropdownMenuItem(
									text = { Text("${subj.name} (${subj.shortName})") },
									onClick = {
										subjectId = subj.id
										subjectName = subj.name
										shortName = subj.shortName
										if (subj.color.isNotBlank()) color = subj.color
										subjectExpanded = false
									}
								)
							}
						}
					}
				}

				// Teacher Picker
				item {
					ExposedDropdownMenuBox(
						expanded = teacherExpanded,
						onExpandedChange = { teacherExpanded = !teacherExpanded }
					) {
						OutlinedTextField(
							value = teacher.ifEmpty { stringResource(R.string.all_teachers) },
							onValueChange = {},
							readOnly = true,
							label = { Text(stringResource(R.string.all_teachers)) },
							trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherExpanded) },
							modifier = Modifier
								.fillMaxWidth()
								.menuAnchor()
						)
						ExposedDropdownMenu(
							expanded = teacherExpanded,
							onDismissRequest = { teacherExpanded = false }
						) {
							DropdownMenuItem(
								text = { Text("+ " + stringResource(R.string.all_add) + "...") },
								onClick = {
									teacherExpanded = false
									showAddTeacherDialog = true
								}
							)
							customTeachers.forEach { t ->
								val displayName = if (t.shortName.isNotBlank()) "${t.name} (${t.shortName})" else t.name
								DropdownMenuItem(
									text = { Text(displayName) },
									onClick = {
										teacherId = t.id
										teacher = t.shortName.ifEmpty { t.name }
										teacherExpanded = false
									}
								)
							}
						}
					}
				}

				// Room Picker
				item {
					ExposedDropdownMenuBox(
						expanded = roomExpanded,
						onExpandedChange = { roomExpanded = !roomExpanded }
					) {
						OutlinedTextField(
							value = room.ifEmpty { stringResource(R.string.all_rooms) },
							onValueChange = {},
							readOnly = true,
							label = { Text(stringResource(R.string.all_rooms)) },
							trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roomExpanded) },
							modifier = Modifier
								.fillMaxWidth()
								.menuAnchor()
						)
						ExposedDropdownMenu(
							expanded = roomExpanded,
							onDismissRequest = { roomExpanded = false }
						) {
							DropdownMenuItem(
								text = { Text("+ " + stringResource(R.string.all_add) + "...") },
								onClick = {
									roomExpanded = false
									showAddRoomDialog = true
								}
							)
							customRooms.forEach { r ->
								DropdownMenuItem(
									text = { Text(r.name) },
									onClick = {
										roomId = r.id
										room = r.name
										roomExpanded = false
									}
								)
							}
						}
					}
				}

				// Day of Week Picker
				item {
					ExposedDropdownMenuBox(
						expanded = dayExpanded,
						onExpandedChange = { dayExpanded = !dayExpanded }
					) {
						OutlinedTextField(
							value = days.getOrNull(dayOfWeek - 1) ?: "Montag",
							onValueChange = {},
							readOnly = true,
							label = { Text("Wochentag") },
							trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
							modifier = Modifier
								.fillMaxWidth()
								.menuAnchor()
						)
						ExposedDropdownMenu(
							expanded = dayExpanded,
							onDismissRequest = { dayExpanded = false }
						) {
							days.forEachIndexed { index, day ->
								DropdownMenuItem(
									text = { Text(day) },
									onClick = {
										dayOfWeek = index + 1
										dayExpanded = false
									}
								)
							}
						}
					}
				}

				// Start & End Time
				item {
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.spacedBy(8.dp)
					) {
						OutlinedTextField(
							value = startTime,
							onValueChange = { startTime = it },
							label = { Text("Start (08:00)") },
							modifier = Modifier.weight(1f)
						)
						OutlinedTextField(
							value = endTime,
							onValueChange = { endTime = it },
							label = { Text("Ende (08:45)") },
							modifier = Modifier.weight(1f)
						)
					}
				}

				// Color Picker
				item {
					Text("Farbe wählen:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.spacedBy(8.dp)
					) {
						defaultCustomColors.forEach { hex ->
							Box(
								modifier = Modifier
									.size(32.dp)
									.clip(CircleShape)
									.background(Color(hex.toColorInt()))
									.clickable { color = hex }
									.padding(4.dp),
								contentAlignment = Alignment.Center
							) {
								if (color == hex) {
									Box(
										modifier = Modifier
											.size(10.dp)
											.clip(CircleShape)
											.background(Color.White)
									)
								}
							}
						}
					}
				}
			}
		},
		confirmButton = {
			Button(
				onClick = {
					if (subjectName.isNotBlank() && shortName.isNotBlank()) {
						onSave(
							lesson.copy(
								subjectName = subjectName,
								shortName = shortName,
								teacher = teacher,
								room = room,
								className = className,
								classId = classId,
								teacherId = teacherId,
								roomId = roomId,
								subjectId = subjectId,
								dayOfWeek = dayOfWeek,
								startTime = startTime,
								endTime = endTime,
								color = color
							)
						)
					}
				}
			) {
				Text("Speichern")
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text(stringResource(R.string.all_cancel))
			}
		}
	)

	// Quick Add Dialogs
	if (showAddClassDialog) {
		var newName by remember { mutableStateOf("") }
		AlertDialog(
			onDismissRequest = { showAddClassDialog = false },
			title = { Text("+ " + stringResource(R.string.all_classes)) },
			text = {
				OutlinedTextField(
					value = newName,
					onValueChange = { newName = it },
					label = { Text(stringResource(R.string.custom_full_name)) },
					singleLine = true,
					modifier = Modifier.fillMaxWidth()
				)
			},
			confirmButton = {
				Button(
					onClick = {
						if (newName.isNotBlank()) {
							scope.launch {
								userDao.upsertCustomClass(CustomClassEntity(userId = userId, name = newName.trim()))
								className = newName.trim()
								showAddClassDialog = false
							}
						}
					}
				) {
					Text(stringResource(R.string.all_save))
				}
			},
			dismissButton = {
				TextButton(onClick = { showAddClassDialog = false }) {
					Text(stringResource(R.string.all_cancel))
				}
			}
		)
	}

	if (showAddSubjectDialog) {
		var newName by remember { mutableStateOf("") }
		var newShort by remember { mutableStateOf("") }
		AlertDialog(
			onDismissRequest = { showAddSubjectDialog = false },
			title = { Text("+ " + stringResource(R.string.all_subjects)) },
			text = {
				Column {
					OutlinedTextField(
						value = newName,
						onValueChange = { newName = it },
						label = { Text(stringResource(R.string.custom_full_name)) },
						singleLine = true,
						modifier = Modifier.fillMaxWidth()
					)
					Spacer(modifier = Modifier.height(8.dp))
					OutlinedTextField(
						value = newShort,
						onValueChange = { newShort = it },
						label = { Text(stringResource(R.string.custom_short_name)) },
						singleLine = true,
						modifier = Modifier.fillMaxWidth()
					)
				}
			},
			confirmButton = {
				Button(
					onClick = {
						if (newName.isNotBlank() && newShort.isNotBlank()) {
							scope.launch {
								userDao.upsertCustomSubject(CustomSubjectEntity(userId = userId, name = newName.trim(), shortName = newShort.trim(), color = color))
								subjectName = newName.trim()
								shortName = newShort.trim()
								showAddSubjectDialog = false
							}
						}
					}
				) {
					Text(stringResource(R.string.all_save))
				}
			},
			dismissButton = {
				TextButton(onClick = { showAddSubjectDialog = false }) {
					Text(stringResource(R.string.all_cancel))
				}
			}
		)
	}

	if (showAddTeacherDialog) {
		var newName by remember { mutableStateOf("") }
		var newShort by remember { mutableStateOf("") }
		AlertDialog(
			onDismissRequest = { showAddTeacherDialog = false },
			title = { Text("+ " + stringResource(R.string.all_teachers)) },
			text = {
				Column {
					OutlinedTextField(
						value = newName,
						onValueChange = { newName = it },
						label = { Text(stringResource(R.string.custom_full_name)) },
						singleLine = true,
						modifier = Modifier.fillMaxWidth()
					)
					Spacer(modifier = Modifier.height(8.dp))
					OutlinedTextField(
						value = newShort,
						onValueChange = { newShort = it },
						label = { Text(stringResource(R.string.custom_short_name)) },
						singleLine = true,
						modifier = Modifier.fillMaxWidth()
					)
				}
			},
			confirmButton = {
				Button(
					onClick = {
						if (newName.isNotBlank()) {
							scope.launch {
								userDao.upsertCustomTeacher(CustomTeacherEntity(userId = userId, name = newName.trim(), shortName = newShort.trim()))
								teacher = newShort.ifEmpty { newName.trim() }
								showAddTeacherDialog = false
							}
						}
					}
				) {
					Text(stringResource(R.string.all_save))
				}
			},
			dismissButton = {
				TextButton(onClick = { showAddTeacherDialog = false }) {
					Text(stringResource(R.string.all_cancel))
				}
			}
		)
	}

	if (showAddRoomDialog) {
		var newName by remember { mutableStateOf("") }
		AlertDialog(
			onDismissRequest = { showAddRoomDialog = false },
			title = { Text("+ " + stringResource(R.string.all_rooms)) },
			text = {
				OutlinedTextField(
					value = newName,
					onValueChange = { newName = it },
					label = { Text(stringResource(R.string.custom_full_name)) },
					singleLine = true,
					modifier = Modifier.fillMaxWidth()
				)
			},
			confirmButton = {
				Button(
					onClick = {
						if (newName.isNotBlank()) {
							scope.launch {
								userDao.upsertCustomRoom(CustomRoomEntity(userId = userId, name = newName.trim()))
								room = newName.trim()
								showAddRoomDialog = false
							}
						}
					}
				) {
					Text(stringResource(R.string.all_save))
				}
			},
			dismissButton = {
				TextButton(onClick = { showAddRoomDialog = false }) {
					Text(stringResource(R.string.all_cancel))
				}
			}
		)
	}
}
