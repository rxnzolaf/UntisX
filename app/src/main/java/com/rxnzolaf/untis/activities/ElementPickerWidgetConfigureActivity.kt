package com.rxnzolaf.untis.activities

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.work.WorkManager
import com.rxnzolaf.untis.R
import com.rxnzolaf.untis.data.database.UserDatabase
import com.rxnzolaf.untis.data.database.entities.User
import com.rxnzolaf.untis.helpers.AppTheme
import com.rxnzolaf.untis.widgets.NextLessonWidgetReceiver
import com.rxnzolaf.untis.widgets.TimetableWidgetReceiver
import com.rxnzolaf.untis.widgets.TodayWidgetReceiver
import com.rxnzolaf.untis.widgets.saveIdPref
import com.rxnzolaf.untis.widgets.saveModePref
import com.rxnzolaf.untis.workers.WidgetUpdateWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class ElementPickerWidgetConfigureActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setResult(RESULT_CANCELED)

		val appWidgetId = intent?.extras?.getInt(
			AppWidgetManager.EXTRA_APPWIDGET_ID,
			AppWidgetManager.INVALID_APPWIDGET_ID
		) ?: AppWidgetManager.INVALID_APPWIDGET_ID

		if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish()
			return
		}

		val users = runBlocking {
			try {
				UserDatabase.getInstance(applicationContext).userDao().getAllAsync()
			} catch (_: Throwable) {
				emptyList()
			}
		}

		setContent {
			AppTheme {
				Surface(modifier = Modifier.fillMaxSize()) {
					WidgetConfigureScreen(
						users = users,
						onConfirm = { selectedUser, selectedMode ->
							saveIdPref(this@ElementPickerWidgetConfigureActivity, appWidgetId, selectedUser.id)
							saveModePref(this@ElementPickerWidgetConfigureActivity, appWidgetId, selectedMode)

							NextLessonWidgetReceiver.updateWidget(this@ElementPickerWidgetConfigureActivity, appWidgetId)
							TodayWidgetReceiver.updateWidget(this@ElementPickerWidgetConfigureActivity, appWidgetId)
							TimetableWidgetReceiver.updateWidget(this@ElementPickerWidgetConfigureActivity, appWidgetId)

							WidgetUpdateWorker.enqueue(WorkManager.getInstance(this@ElementPickerWidgetConfigureActivity))

							val resultValue = Intent().putExtra(
								AppWidgetManager.EXTRA_APPWIDGET_ID,
								appWidgetId
							)
							setResult(RESULT_OK, resultValue)
							finish()
						},
						onCancel = {
							setResult(RESULT_CANCELED)
							finish()
						}
					)
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigureScreen(
	users: List<User>,
	onConfirm: (User, String) -> Unit,
	onCancel: () -> Unit
) {
	val context = LocalContext.current
	var selectedUser by remember { mutableStateOf(users.firstOrNull()) }
	var selectedMode by remember { mutableStateOf("day") } // "day" or "week"

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(stringResource(R.string.widget_configuration)) }
			)
		}
	) { innerPadding ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding)
				.padding(16.dp)
		) {
			Text(
				text = stringResource(R.string.widget_not_logged_in),
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Bold
			)

			Spacer(modifier = Modifier.height(8.dp))

			if (users.isEmpty()) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.padding(24.dp),
					contentAlignment = Alignment.Center
				) {
					Text(
						text = stringResource(R.string.widget_tap_to_login),
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			} else {
				LazyColumn(
					modifier = Modifier.weight(1f)
				) {
					items(users) { user ->
						val isSelected = selectedUser?.id == user.id
						Card(
							modifier = Modifier
								.fillMaxWidth()
								.padding(vertical = 4.dp)
								.clickable { selectedUser = user },
							colors = CardDefaults.cardColors(
								containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
							)
						) {
							Row(
								modifier = Modifier
									.fillMaxWidth()
									.padding(12.dp),
								verticalAlignment = Alignment.CenterVertically
							) {
								RadioButton(
									selected = isSelected,
									onClick = { selectedUser = user }
								)
								Column(
									modifier = Modifier.padding(start = 12.dp)
								) {
									Text(
										text = user.getDisplayedName(context),
										style = MaterialTheme.typography.titleMedium,
										fontWeight = FontWeight.SemiBold
									)
									Text(
										text = user.userData.schoolName,
										style = MaterialTheme.typography.bodySmall,
										color = MaterialTheme.colorScheme.onSurfaceVariant
									)
								}
							}
						}
					}

					item {
						Spacer(modifier = Modifier.height(12.dp))
						Text(
							text = "Display mode",
							style = MaterialTheme.typography.titleMedium,
							fontWeight = FontWeight.Bold
						)
						Spacer(modifier = Modifier.height(4.dp))
					}

					item {
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.clickable { selectedMode = "day" }
								.padding(vertical = 4.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							RadioButton(
								selected = selectedMode == "day",
								onClick = { selectedMode = "day" }
							)
							Text(
								text = stringResource(R.string.widget_mode_day),
								style = MaterialTheme.typography.bodyMedium,
								modifier = Modifier.padding(start = 8.dp)
							)
						}
					}

					item {
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.clickable { selectedMode = "week" }
								.padding(vertical = 4.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							RadioButton(
								selected = selectedMode == "week",
								onClick = { selectedMode = "week" }
							)
							Text(
								text = stringResource(R.string.widget_mode_week),
								style = MaterialTheme.typography.bodyMedium,
								modifier = Modifier.padding(start = 8.dp)
							)
						}
					}
				}
			}

			Spacer(modifier = Modifier.height(16.dp))

			Button(
				onClick = {
					selectedUser?.let { onConfirm(it, selectedMode) } ?: onCancel()
				},
				enabled = selectedUser != null,
				modifier = Modifier.fillMaxWidth()
			) {
				Text(stringResource(R.string.all_ok))
			}
		}
	}
}
