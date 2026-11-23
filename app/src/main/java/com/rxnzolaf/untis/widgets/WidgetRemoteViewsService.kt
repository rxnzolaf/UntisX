package com.rxnzolaf.untis.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.format.DateFormat
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.rxnzolaf.untis.R
import com.rxnzolaf.untis.data.database.UserDatabase
import com.rxnzolaf.untis.data.database.entities.User
import com.rxnzolaf.untis.data.settings.model.AppTheme
import com.rxnzolaf.untis.data.settings.model.Settings
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class WidgetRemoteViewsService : RemoteViewsService() {
	override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
		return WidgetRemoteViewsFactory(this.applicationContext, intent)
	}
}

class WidgetRemoteViewsFactory(
	private val context: Context,
	intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

	private val appWidgetId = intent.getIntExtra(
		AppWidgetManager.EXTRA_APPWIDGET_ID,
		AppWidgetManager.INVALID_APPWIDGET_ID
	)

	private var items: List<WidgetItemData> = emptyList()
	private var themeColors: WidgetThemeColors = getWidgetThemeColors(AppTheme.STANDARD)

	override fun onCreate() {
		loadData()
	}

	override fun onDataSetChanged() {
		loadData()
	}

	override fun onDestroy() {}

	private fun loadData() {
		val userId = loadIdPref(context, appWidgetId)
		if (userId <= 0L) {
			items = emptyList()
			return
		}

		try {
			val db = UserDatabase.getInstance(context)
			val user = runBlocking { db.userDao().getById(userId) } ?: return

			themeColors = getWidgetThemeColors(AppTheme.STANDARD)

			items = loadTodayItems(context, db, user)
		} catch (_: Exception) {
			items = emptyList()
		}
	}

	override fun getCount(): Int = items.size

	override fun getViewAt(position: Int): RemoteViews {
		if (position < 0 || position >= items.size) {
			return RemoteViews(context.packageName, R.layout.widget_base_item)
		}

		val item = items[position]
		val rv = RemoteViews(context.packageName, R.layout.widget_base_item)

		rv.setInt(R.id.linearlayout_widget_listitem_root, "setBackgroundColor", themeColors.cardBgColor)
		rv.setInt(R.id.view_widget_color_bar, "setBackgroundColor", item.barColor)

		rv.setTextViewText(R.id.textview_listitem_line1, item.line1)
		rv.setTextColor(R.id.textview_listitem_line1, themeColors.textColorPrimary)

		rv.setTextViewText(R.id.textview_listitem_line2, item.line2)
		rv.setTextColor(R.id.textview_listitem_line2, themeColors.textColorSecondary)

		if (item.badgeText != null) {
			rv.setViewVisibility(R.id.textview_widget_badge, View.VISIBLE)
			rv.setTextViewText(R.id.textview_widget_badge, item.badgeText)
			rv.setInt(R.id.textview_widget_badge, "setBackgroundColor", item.badgeBgColor)
		} else {
			rv.setViewVisibility(R.id.textview_widget_badge, View.GONE)
		}

		val fillInIntent = Intent()
		rv.setOnClickFillInIntent(R.id.linearlayout_widget_listitem_root, fillInIntent)

		return rv
	}

	override fun getLoadingView(): RemoteViews? = null

	override fun getViewTypeCount(): Int = 1

	override fun getItemId(position: Int): Long = position.toLong()

	override fun hasStableIds(): Boolean = true
}

data class WidgetItemData(
	val line1: String,
	val line2: String,
	val barColor: Int,
	val badgeText: String? = null,
	val badgeBgColor: Int = Color.RED
)

fun loadTodayItems(
	context: Context,
	db: UserDatabase,
	user: User
): List<WidgetItemData> {
	return emptyList()
}
