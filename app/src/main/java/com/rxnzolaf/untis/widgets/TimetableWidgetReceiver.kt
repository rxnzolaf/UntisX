package com.rxnzolaf.untis.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import com.rxnzolaf.untis.R
import com.rxnzolaf.untis.data.database.UserDatabase
import com.rxnzolaf.untis.data.settings.model.AppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TimetableWidgetReceiver : AppWidgetProvider() {

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
			val rv = RemoteViews(context.packageName, R.layout.widget_base)

			val openIntent = getOpenAppPendingIntent(context, appWidgetId)
			rv.setOnClickPendingIntent(R.id.widget_base_root, openIntent)

			val refreshIntent = getRefreshPendingIntent(context, TimetableWidgetReceiver::class.java, appWidgetId)
			rv.setOnClickPendingIntent(R.id.imagebutton_widget_reload, refreshIntent)

			val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d. MMMM")
			val todayStr = LocalDate.now().format(dateFormatter)

			if (userId <= 0L) {
				rv.setTextViewText(R.id.textview_base_widget_account, context.getString(R.string.widget_not_logged_in))
				rv.setTextViewText(R.id.textview_base_widget_school, context.getString(R.string.widget_tap_to_login))
				rv.setViewVisibility(R.id.textview_widget_empty, View.VISIBLE)
				rv.setTextViewText(R.id.textview_widget_empty, context.getString(R.string.widget_tap_to_login))
				rv.setViewVisibility(R.id.listview_widget_base_content, View.GONE)
				appWidgetManager.updateAppWidget(appWidgetId, rv)
				return
			}

			val themeColors = getWidgetThemeColors(AppTheme.STANDARD)
			rv.setInt(R.id.widget_base_root, "setBackgroundColor", themeColors.bgColor)
			rv.setTextColor(R.id.textview_base_widget_account, themeColors.textColorPrimary)
			rv.setTextColor(R.id.textview_base_widget_school, themeColors.textColorSecondary)

			try {
				val db = UserDatabase.getInstance(context)
				val user = runCatching { db.userDao().getById(userId) }.getOrNull()

				if (user != null) {
					rv.setTextViewText(R.id.textview_base_widget_account, user.getDisplayedName(context))
					rv.setTextViewText(R.id.textview_base_widget_school, "${user.userData.schoolName} • $todayStr")

					val serviceIntent = Intent(context, WidgetRemoteViewsService::class.java).apply {
						putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
						data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
					}
					rv.setRemoteAdapter(R.id.listview_widget_base_content, serviceIntent)
					rv.setEmptyView(R.id.listview_widget_base_content, R.id.textview_widget_empty)
					rv.setPendingIntentTemplate(R.id.listview_widget_base_content, openIntent)

					appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listview_widget_base_content)
				} else {
					rv.setTextViewText(R.id.textview_base_widget_account, context.getString(R.string.widget_not_logged_in))
					rv.setTextViewText(R.id.textview_base_widget_school, context.getString(R.string.widget_tap_to_login))
					rv.setViewVisibility(R.id.textview_widget_empty, View.VISIBLE)
					rv.setViewVisibility(R.id.listview_widget_base_content, View.GONE)
				}
			} catch (e: Exception) {
				rv.setTextViewText(R.id.textview_base_widget_account, context.getString(R.string.widget_error_loading))
				rv.setTextViewText(R.id.textview_base_widget_school, context.getString(R.string.widget_tap_to_retry))
				rv.setViewVisibility(R.id.textview_widget_empty, View.VISIBLE)
				rv.setViewVisibility(R.id.listview_widget_base_content, View.GONE)
			}

			appWidgetManager.updateAppWidget(appWidgetId, rv)
		}
	}
}
