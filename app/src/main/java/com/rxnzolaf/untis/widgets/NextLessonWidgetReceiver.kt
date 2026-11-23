package com.rxnzolaf.untis.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.Color
import android.text.format.DateFormat
import android.view.View
import android.widget.RemoteViews
import com.rxnzolaf.untis.R
import com.rxnzolaf.untis.data.database.UserDatabase
import com.rxnzolaf.untis.data.database.entities.UserWithData
import com.rxnzolaf.untis.data.settings.model.AppTheme
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class NextLessonWidgetReceiver : AppWidgetProvider() {

	override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
		for (id in appWidgetIds) {
			updateWidget(context, id, appWidgetManager)
		}
	}

	override fun onDeleted(context: Context, appWidgetIds: IntArray) {
		for (id in appWidgetIds) {
			deleteIdPref(context, id)
		}
	}

	companion object {
		fun updateWidget(context: Context, appWidgetId: Int, appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)) {
			val userId = loadIdPref(context, appWidgetId)
			val rv = RemoteViews(context.packageName, R.layout.widget_next_lesson)

			val openIntent = getOpenAppPendingIntent(context, appWidgetId)
			rv.setOnClickPendingIntent(R.id.widget_next_lesson_root, openIntent)

			if (userId <= 0L) {
				rv.setTextViewText(R.id.textview_next_lesson_header, context.getString(R.string.widget_next_lesson))
				rv.setTextViewText(R.id.textview_next_lesson_subject, context.getString(R.string.widget_not_logged_in))
				rv.setTextViewText(R.id.textview_next_lesson_details, context.getString(R.string.widget_tap_to_login))
				rv.setTextViewText(R.id.textview_next_lesson_time, "")
				rv.setViewVisibility(R.id.textview_next_lesson_countdown, View.GONE)
				rv.setViewVisibility(R.id.textview_next_lesson_badge, View.GONE)
				appWidgetManager.updateAppWidget(appWidgetId, rv)
				return
			}

			val themeColors = getWidgetThemeColors(AppTheme.STANDARD)
			rv.setInt(R.id.widget_next_lesson_root, "setBackgroundColor", themeColors.cardBgColor)
			rv.setTextColor(R.id.textview_next_lesson_header, themeColors.textColorSecondary)
			rv.setTextColor(R.id.textview_next_lesson_subject, themeColors.textColorPrimary)
			rv.setTextColor(R.id.textview_next_lesson_details, themeColors.textColorSecondary)
			rv.setTextColor(R.id.textview_next_lesson_time, themeColors.textColorSecondary)

			try {
				val db = UserDatabase.getInstance(context)
				val userWithData = runCatching { db.userDao().getById(userId) }.getOrNull()

				if (userWithData == null) {
					rv.setTextViewText(R.id.textview_next_lesson_subject, context.getString(R.string.widget_not_logged_in))
					rv.setTextViewText(R.id.textview_next_lesson_details, context.getString(R.string.widget_tap_to_login))
					rv.setViewVisibility(R.id.textview_next_lesson_countdown, View.GONE)
					rv.setViewVisibility(R.id.textview_next_lesson_badge, View.GONE)
					appWidgetManager.updateAppWidget(appWidgetId, rv)
					return
				}

				rv.setTextViewText(R.id.textview_next_lesson_header, "${context.getString(R.string.widget_next_lesson)} • ${userWithData.getDisplayedName(context)}")
				rv.setTextViewText(R.id.textview_next_lesson_subject, context.getString(R.string.widget_no_upcoming_lessons))
				rv.setTextViewText(R.id.textview_next_lesson_details, "")
				rv.setTextViewText(R.id.textview_next_lesson_time, "")
				rv.setViewVisibility(R.id.textview_next_lesson_countdown, View.GONE)
				rv.setViewVisibility(R.id.textview_next_lesson_badge, View.GONE)

			} catch (e: Exception) {
				rv.setTextViewText(R.id.textview_next_lesson_subject, context.getString(R.string.widget_error_loading))
				rv.setTextViewText(R.id.textview_next_lesson_details, context.getString(R.string.widget_tap_to_retry))
				rv.setViewVisibility(R.id.textview_next_lesson_countdown, View.GONE)
				rv.setViewVisibility(R.id.textview_next_lesson_badge, View.GONE)
			}

			appWidgetManager.updateAppWidget(appWidgetId, rv)
		}
	}
}
