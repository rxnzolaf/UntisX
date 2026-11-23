package com.rxnzolaf.untis.workers

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.rxnzolaf.untis.data.repository.TimetableRepository
import com.rxnzolaf.untis.widgets.NextLessonWidgetReceiver
import com.rxnzolaf.untis.widgets.TimetableWidgetReceiver
import com.rxnzolaf.untis.widgets.TodayWidgetReceiver
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * This worker updates all home screen widgets.
 */
@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
	@Assisted context: Context,
	@Assisted params: WorkerParameters,
	timetableRepository: TimetableRepository,
) :
	TimetableDependantWorker(context, params, timetableRepository) {
	companion object {
		private const val TAG_WIDGET_UPDATE_WORK = "WidgetUpdateWork"
		private const val PERIODIC_WIDGET_UPDATE_WORK = "PeriodicWidgetUpdateWork"

		fun enqueue(workManager: WorkManager) {
			workManager.enqueue(
				OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
					.addTag(TAG_WIDGET_UPDATE_WORK)
					.build()
			)

			val periodicRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(30, TimeUnit.MINUTES)
				.addTag(PERIODIC_WIDGET_UPDATE_WORK)
				.build()

			workManager.enqueueUniquePeriodicWork(
				PERIODIC_WIDGET_UPDATE_WORK,
				ExistingPeriodicWorkPolicy.KEEP,
				periodicRequest
			)
		}
	}

	override suspend fun doWork(): Result {
		val appWidgetManager = AppWidgetManager.getInstance(applicationContext)

		val nextLessonComponent = ComponentName(applicationContext, NextLessonWidgetReceiver::class.java)
		val nextLessonIds = appWidgetManager.getAppWidgetIds(nextLessonComponent)
		for (id in nextLessonIds) {
			NextLessonWidgetReceiver.updateWidget(applicationContext, id, appWidgetManager)
		}

		val todayComponent = ComponentName(applicationContext, TodayWidgetReceiver::class.java)
		val todayIds = appWidgetManager.getAppWidgetIds(todayComponent)
		for (id in todayIds) {
			TodayWidgetReceiver.updateWidget(applicationContext, id, appWidgetManager)
		}

		val timetableComponent = ComponentName(applicationContext, TimetableWidgetReceiver::class.java)
		val timetableIds = appWidgetManager.getAppWidgetIds(timetableComponent)
		for (id in timetableIds) {
			TimetableWidgetReceiver.updateWidget(applicationContext, id, appWidgetManager)
		}

		return Result.success()
	}
}
