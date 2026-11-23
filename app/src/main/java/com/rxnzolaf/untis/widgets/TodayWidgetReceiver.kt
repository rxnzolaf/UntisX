package com.rxnzolaf.untis.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.text.format.DateFormat
import android.view.View
import android.widget.RemoteViews
import com.rxnzolaf.untis.R
import com.rxnzolaf.untis.data.database.UserDatabase
import com.rxnzolaf.untis.data.settings.model.AppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TodayWidgetReceiver : AppWidgetProvider() {

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
			val rv = RemoteViews(context.packageName, R.layout.widget_today)

			val openIntent = getOpenAppPendingIntent(context, appWidgetId)
			rv.setOnClickPendingIntent(R.id.widget_today_root, openIntent)

			val refreshIntent = getRefreshPendingIntent(context, TodayWidgetReceiver::class.java, appWidgetId)
			rv.setOnClickPendingIntent(R.id.imagebutton_today_reload, refreshIntent)

			val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d. MMMM")
			val todayStr = LocalDate.now().format(dateFormatter)

			if (userId <= 0L) {
				rv.setTextViewText(R.id.textview_today_profile, context.getString(R.string.widget_not_logged_in))
				rv.setTextViewText(R.id.textview_today_date, context.getString(R.string.widget_tap_to_login))
				rv.setViewVisibility(R.id.textview_today_empty, View.VISIBLE)
				rv.setTextViewText(R.id.textview_today_empty, context.getString(R.string.widget_tap_to_login))
				appWidgetManager.updateAppWidget(appWidgetId, rv)
				return
			}

			val themeColors = getWidgetThemeColors(AppTheme.STANDARD)
			rv.setInt(R.id.widget_today_root, "setBackgroundColor", themeColors.bgColor)
			rv.setTextColor(R.id.textview_today_profile, themeColors.textColorPrimary)
			rv.setTextColor(R.id.textview_today_date, themeColors.textColorSecondary)

			try {
				val db = UserDatabase.getInstance(context)
				val user = runCatching { db.userDao().getById(userId) }.getOrNull()

				if (user != null) {
					rv.setTextViewText(R.id.textview_today_profile, user.getDisplayedName(context))
					rv.setTextViewText(R.id.textview_today_date, todayStr)
					rv.setViewVisibility(R.id.textview_today_empty, View.VISIBLE)
					rv.setTextViewText(R.id.textview_today_empty, context.getString(R.string.widget_no_lessons_today))
				} else {
					rv.setTextViewText(R.id.textview_today_profile, context.getString(R.string.widget_not_logged_in))
					rv.setTextViewText(R.id.textview_today_date, context.getString(R.string.widget_tap_to_login))
					rv.setViewVisibility(R.id.textview_today_empty, View.VISIBLE)
				}
			} catch (e: Exception) {
				rv.setTextViewText(R.id.textview_today_profile, context.getString(R.string.widget_error_loading))
				rv.setTextViewText(R.id.textview_today_date, context.getString(R.string.widget_tap_to_retry))
				rv.setViewVisibility(R.id.textview_today_empty, View.VISIBLE)
			}

			appWidgetManager.updateAppWidget(appWidgetId, rv)
		}
	}
}
