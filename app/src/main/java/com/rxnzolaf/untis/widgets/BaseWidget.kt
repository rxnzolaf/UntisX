package com.rxnzolaf.untis.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import com.rxnzolaf.untis.activities.MainActivity
import com.rxnzolaf.untis.data.settings.model.AppTheme

private const val PREFS_NAME = "com.rxnzolaf.untis.widgets"
private const val PREF_PREFIX_KEY = "appwidget_"

internal fun saveIdPref(context: Context, appWidgetId: Int, userId: Long) {
	context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
		.edit()
		.putLong(PREF_PREFIX_KEY + appWidgetId, userId)
		.apply()
}

internal fun loadIdPref(context: Context, appWidgetId: Int): Long {
	return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
		.getLong(PREF_PREFIX_KEY + appWidgetId, 0L)
}

internal fun deleteIdPref(context: Context, appWidgetId: Int) {
	context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
		.edit()
		.remove(PREF_PREFIX_KEY + appWidgetId)
		.remove(PREF_PREFIX_KEY + appWidgetId + "_mode")
		.apply()
}

internal fun saveModePref(context: Context, appWidgetId: Int, mode: String) {
	context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
		.edit()
		.putString(PREF_PREFIX_KEY + appWidgetId + "_mode", mode)
		.apply()
}

internal fun loadModePref(context: Context, appWidgetId: Int): String {
	return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
		.getString(PREF_PREFIX_KEY + appWidgetId + "_mode", "day") ?: "day"
}

data class WidgetThemeColors(
	val bgColor: Int,
	val cardBgColor: Int,
	val textColorPrimary: Int,
	val textColorSecondary: Int,
	val accentColor: Int,
	val dividerColor: Int
)

fun getWidgetThemeColors(appTheme: AppTheme): WidgetThemeColors {
	return when (appTheme) {
		AppTheme.GAMING -> WidgetThemeColors(
			bgColor = Color.parseColor("#0B0B1A"),
			cardBgColor = Color.parseColor("#17173A"),
			textColorPrimary = Color.parseColor("#F3F4FF"),
			textColorSecondary = Color.parseColor("#A5A8D6"),
			accentColor = Color.parseColor("#4F6BFF"),
			dividerColor = Color.parseColor("#1E1E4A")
		)
		AppTheme.LEGACY -> WidgetThemeColors(
			bgColor = Color.parseColor("#FFFFFF"),
			cardBgColor = Color.parseColor("#F5F6F7"),
			textColorPrimary = Color.parseColor("#2B2F33"),
			textColorSecondary = Color.parseColor("#8A9097"),
			accentColor = Color.parseColor("#4AA3DF"),
			dividerColor = Color.parseColor("#E3E5E8")
		)
		else -> WidgetThemeColors(
			bgColor = Color.parseColor("#1C1B1F"),
			cardBgColor = Color.parseColor("#2B292F"),
			textColorPrimary = Color.parseColor("#E6E1E5"),
			textColorSecondary = Color.parseColor("#CAC4D0"),
			accentColor = Color.parseColor("#D0BCFF"),
			dividerColor = Color.parseColor("#36343B")
		)
	}
}

fun getOpenAppPendingIntent(context: Context, appWidgetId: Int): PendingIntent {
	val intent = Intent(context, MainActivity::class.java).apply {
		flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
	}
	val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
		PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
	} else {
		PendingIntent.FLAG_UPDATE_CURRENT
	}
	return PendingIntent.getActivity(context, appWidgetId, intent, flags)
}

fun getRefreshPendingIntent(context: Context, receiverClass: Class<*>, appWidgetId: Int): PendingIntent {
	val intent = Intent(context, receiverClass).apply {
		action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
		putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
	}
	val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
		PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
	} else {
		PendingIntent.FLAG_UPDATE_CURRENT
	}
	return PendingIntent.getBroadcast(context, appWidgetId, intent, flags)
}
