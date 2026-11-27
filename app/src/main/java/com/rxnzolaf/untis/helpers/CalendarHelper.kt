package com.rxnzolaf.untis.helpers

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import com.rxnzolaf.untis.R
import java.time.LocalDateTime
import java.time.ZoneId

object CalendarHelper {

	/**
	 * Opens the system calendar app's "new event" screen pre-filled with event details.
	 */
	fun addToCalendar(
		context: Context,
		title: String,
		startDateTime: LocalDateTime? = null,
		endDateTime: LocalDateTime? = null,
		isAllDay: Boolean = false,
		location: String? = null,
		subject: String? = null,
		teachers: String? = null,
		classes: String? = null,
		type: String? = null,
		notes: String? = null,
		defaultDurationMinutes: Long = 60,
	) {
		val zoneId = ZoneId.systemDefault()

		val (beginMillis, endMillis) = when {
			(startDateTime != null && endDateTime != null) -> {
				val begin = startDateTime.atZone(zoneId).toInstant().toEpochMilli()
				val end = endDateTime.atZone(zoneId).toInstant().toEpochMilli()
				if (end > begin) Pair(begin, end)
				else Pair(begin, startDateTime.plusMinutes(defaultDurationMinutes).atZone(zoneId).toInstant().toEpochMilli())
			}
			startDateTime != null -> {
				val begin = startDateTime.atZone(zoneId).toInstant().toEpochMilli()
				val end = startDateTime.plusMinutes(defaultDurationMinutes).atZone(zoneId).toInstant().toEpochMilli()
				Pair(begin, end)
			}
			else -> Pair(null, null)
		}

		val description = buildString {
			if (!subject.isNullOrBlank()) {
				append(context.getString(R.string.calendar_desc_subject, subject))
				append("\n")
			}
			if (!teachers.isNullOrBlank()) {
				append(context.getString(R.string.calendar_desc_teachers, teachers))
				append("\n")
			}
			if (!classes.isNullOrBlank()) {
				append(context.getString(R.string.calendar_desc_classes, classes))
				append("\n")
			}
			if (!type.isNullOrBlank()) {
				append(context.getString(R.string.calendar_desc_type, type))
				append("\n")
			}
			if (!notes.isNullOrBlank()) {
				append(context.getString(R.string.calendar_desc_notes, notes))
				append("\n")
			}
			if (isNotEmpty()) append("\n")
			append(context.getString(R.string.calendar_desc_footer))
		}

		val intent = Intent(Intent.ACTION_INSERT).apply {
			data = CalendarContract.Events.CONTENT_URI
			putExtra(CalendarContract.Events.TITLE, title)
			putExtra(CalendarContract.Events.DESCRIPTION, description)
			if (!location.isNullOrBlank()) {
				putExtra(CalendarContract.Events.EVENT_LOCATION, location)
			}
			beginMillis?.let {
				putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, it)
			}
			endMillis?.let {
				putExtra(CalendarContract.EXTRA_EVENT_END_TIME, it)
			}
			if (isAllDay) {
				putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
			}
		}

		try {
			context.startActivity(intent)
		} catch (_: ActivityNotFoundException) {
			Toast.makeText(context, R.string.calendar_app_not_found, Toast.LENGTH_SHORT).show()
		} catch (_: Exception) {
			Toast.makeText(context, R.string.calendar_app_not_found, Toast.LENGTH_SHORT).show()
		}
	}
}
