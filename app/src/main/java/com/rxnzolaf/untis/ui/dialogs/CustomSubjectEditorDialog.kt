package com.rxnzolaf.untis.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rxnzolaf.untis.R
import com.rxnzolaf.untis.data.database.entities.CustomSubjectNameEntity
import com.rxnzolaf.untis.data.repository.CustomNamesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun CustomSubjectEditorDialog(
	userId: Long,
	subjectKey: String,
	originalLongName: String,
	originalShortName: String,
	customNamesRepository: CustomNamesRepository,
	existingCustom: CustomSubjectNameEntity?,
	onDismiss: () -> Unit
) {
	var nickname by rememberSaveable(existingCustom) { mutableStateOf(existingCustom?.nickname ?: "") }
	var abbreviation by rememberSaveable(existingCustom) { mutableStateOf(existingCustom?.abbreviation ?: "") }

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(stringResource(R.string.custom_names_edit_subject)) },
		text = {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 8.dp)
			) {
				Text(text = "Original: $originalLongName ($originalShortName)")
				Spacer(modifier = Modifier.height(12.dp))
				OutlinedTextField(
					value = nickname,
					onValueChange = { if (it.length <= 30) nickname = it },
					label = { Text(stringResource(R.string.custom_names_nickname)) },
					placeholder = { Text(originalLongName) },
					singleLine = true,
					modifier = Modifier.fillMaxWidth()
				)
				Spacer(modifier = Modifier.height(8.dp))
				OutlinedTextField(
					value = abbreviation,
					onValueChange = { if (it.length <= 6) abbreviation = it },
					label = { Text(stringResource(R.string.custom_names_abbreviation)) },
					placeholder = { Text(originalShortName) },
					singleLine = true,
					modifier = Modifier.fillMaxWidth()
				)
			}
		},
		confirmButton = {
			TextButton(
				onClick = {
					CoroutineScope(Dispatchers.IO).launch {
						customNamesRepository.saveSubjectName(userId, subjectKey, nickname, abbreviation)
					}
					onDismiss()
				}
			) {
				Text(stringResource(R.string.all_save))
			}
		},
		dismissButton = {
			TextButton(
				onClick = {
					CoroutineScope(Dispatchers.IO).launch {
						customNamesRepository.deleteSubjectName(userId, subjectKey)
					}
					onDismiss()
				}
			) {
				Text(stringResource(R.string.custom_names_reset_subject))
			}
		}
	)
}
