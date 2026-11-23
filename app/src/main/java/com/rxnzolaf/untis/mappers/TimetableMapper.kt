package com.rxnzolaf.untis.mappers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.graphics.toColorInt
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.enumeration.PeriodState
import com.sapuseven.untis.api.model.untis.masterdata.timegrid.Day
import com.sapuseven.untis.api.model.untis.timetable.Period
import com.rxnzolaf.untis.data.repository.MasterDataRepository
import com.rxnzolaf.untis.data.repository.UserRepository
import com.rxnzolaf.untis.data.repository.UserSettingsRepository
import com.rxnzolaf.untis.data.repository.withDefault
import com.rxnzolaf.untis.data.settings.model.UserSettings
import com.rxnzolaf.untis.helpers.BuildConfigFieldsProvider
import com.rxnzolaf.untis.models.PeriodItem
import com.rxnzolaf.untis.ui.weekview.Event
import com.rxnzolaf.untis.ui.weekview.EventStyle
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class TimetableMapper @Inject constructor(
	private val userRepository: UserRepository,
	private val userSettingsRepository: UserSettingsRepository,
	private val masterDataRepository: MasterDataRepository,
	private val buildConfigFieldsProvider: BuildConfigFieldsProvider,
) {
	/**
	 * Prepares periods for further processing or displaying.
	 *
	 * This function filters out items that should be hidden and merges multi-hour periods.
	 *
	 * @param items List of items that should be prepared
	 * @param hideCancelled Whether cancelled items should be removed
	 * @return A list of prepared items
	 */
	fun preparePeriods(
		items: List<Period>,
		hideCancelled: Boolean
	): List<Period> = items
		.filterPeriods(hideCancelled)
		.mergePeriods(userRepository.currentUser!!.timeGrid.days)

	suspend fun mapTimetablePeriodsToWeekViewEvents(
		items: List<Period>,
		contextType: ElementType
	): List<Event<PeriodItem>> {
		waitForSettings().apply {
			return preparePeriods(items, timetableHideCancelled)
				.mapToEvents(
					this,
					contextType
				)
				.prepareEvents(
					timetableSubstitutionsIrregular,
					timetableBackgroundIrregular
				)
		}
	}

	private suspend fun waitForSettings() = userSettingsRepository.getSettings().filterNotNull().first()

	private fun List<Period>.mapToEvents(
		userSettings: UserSettings,
		contextType: ElementType,
		includeOrgIds: Boolean = true,
	): List<Event<PeriodItem>> {
		return map { period ->
			val periodItem = PeriodItem(
				masterDataRepository = masterDataRepository,
				originalPeriod = period
			)

			Event(
				title = periodItem.getShort(ElementType.SUBJECT),
				top = (
					if (contextType == ElementType.TEACHER)
						periodItem.getShortAnnotated(
							ElementType.CLASS,
							includeOrgIds = includeOrgIds
						)
					else
						periodItem.getShortAnnotated(
							ElementType.TEACHER,
							includeOrgIds = includeOrgIds
						)
					),
				bottom = (
					if (contextType == ElementType.ROOM)
						periodItem.getShortAnnotated(
							ElementType.CLASS,
							includeOrgIds = includeOrgIds
						)
					else
						periodItem.getShortAnnotated(
							ElementType.ROOM,
							includeOrgIds = includeOrgIds
						)
					),
				eventStyle = getColorScheme(userSettings, periodItem),
				start = period.startDateTime,
				end = period.endDateTime,
				data = periodItem
			)
		}
	}

	private val legacyFallbackPalette = listOf(
		Color(0xFFF29A1F), // Orange
		Color(0xFF2DC9A0), // Teal-green
		Color(0xFF1FA9E6), // Blue
		Color(0xFFFFB933), // Yellow
		Color(0xFFA8E6A0), // Light green
		Color(0xFFD9A3F0), // Purple
		Color(0xFFD9D9D9)  // Light grey
	)

	private fun getColorScheme(
		userSettings: UserSettings,
		periodItem: PeriodItem
	): EventStyle = with(userSettings) {
		val subjectEntity = masterDataRepository.userData?.subjects?.find { it.id == periodItem.subjects.firstOrNull()?.id }

		val isExam = periodItem.isExam()
		val isCancelled = periodItem.isCancelled()
		val isIrregular = periodItem.isIrregular()

		if (appTheme == com.rxnzolaf.untis.data.settings.model.AppTheme.LEGACY) {
			val rawColor = (subjectEntity?.backColor ?: periodItem.originalPeriod.backColor).trim()
			val isDefaultOrInvalid = rawColor.isEmpty() || rawColor == "0" || rawColor == "000000" ||
				rawColor == "#000000" || rawColor.equals("FFFFFF", ignoreCase = true) || rawColor.equals("#FFFFFF", ignoreCase = true)

			val color = if (!isDefaultOrInvalid) {
				try {
					val hex = if (rawColor.startsWith("#")) rawColor else "#$rawColor"
					Color(hex.toColorInt())
				} catch (_: Throwable) {
					val subjectName = periodItem.getShort(ElementType.SUBJECT).toString()
					val paletteIndex = kotlin.math.abs(subjectName.hashCode() + periodItem.originalPeriod.id.toInt()) % legacyFallbackPalette.size
					legacyFallbackPalette[paletteIndex]
				}
			} else {
				val subjectName = periodItem.getShort(ElementType.SUBJECT).toString()
				val paletteIndex = kotlin.math.abs(subjectName.hashCode() + periodItem.originalPeriod.id.toInt()) % legacyFallbackPalette.size
				legacyFallbackPalette[paletteIndex]
			}

			val legacyBadge = when {
				isCancelled -> "Entfall"
				isExam -> "Test"
				isIrregular -> "Sub"
				else -> null
			}

			val legacyBadgeBg = when {
				isCancelled -> Color(0xFFE5483B)
				isExam -> Color(0xFFE5483B)
				isIrregular -> Color(0xFF4AA3DF)
				else -> null
			}

			val legacyTextColor = if (
				androidx.core.graphics.ColorUtils.calculateContrast(Color.White.toArgb(), color.toArgb()) >= 4.5
			) Color.White else Color(0xFF2B2F33)

			return EventStyle.Custom(
				color = if (isCancelled) color.copy(alpha = 0.55f) else color,
				textStyle = TextStyle(
					color = legacyTextColor,
					textDecoration = if (isCancelled) TextDecoration.LineThrough else TextDecoration.None
				),
				border = null,
				badge = legacyBadge,
				badgeBg = legacyBadgeBg
			)
		}

		val border = when {
			isCancelled -> Color(0xFFFF4D6D) // neon red
			isExam -> Color(0xFFD946EF)     // neon magenta
			isIrregular -> Color(0xFF22D3EE)  // neon cyan
			else -> null
		}

		val badge = when {
			isCancelled -> "Entfall"
			isExam -> "Test"
			isIrregular -> "Sub"
			else -> null
		}

		val badgeBg = when {
			isCancelled -> Color(0xFFFF4D6D)
			isExam -> Color(0xFFD946EF)
			isIrregular -> Color(0xFF22D3EE)
			else -> null
		}

		val defaultColor = EventStyle.Custom(
			color = Color((subjectEntity?.backColor ?: periodItem.originalPeriod.backColor).toColorInt()),
			textStyle = TextStyle(color = Color((subjectEntity?.foreColor ?: periodItem.originalPeriod.foreColor).toColorInt())),
			border = border,
			badge = badge,
			badgeBg = badgeBg
		)

		val regularColor = EventStyle.Custom(Color(backgroundRegular)).withDefault(hasBackgroundRegular(), EventStyle.ThemePrimary)
		val examColor = EventStyle.Custom(Color(backgroundExam), border = Color(0xFFD946EF), badge = "Test", badgeBg = Color(0xFFD946EF)).withDefault(hasBackgroundExam(), EventStyle.ThemeError)
		val cancelledColor = EventStyle.Custom(Color(backgroundCancelled), border = Color(0xFFFF4D6D), badge = "Entfall", badgeBg = Color(0xFFFF4D6D)).withDefault(hasBackgroundCancelled(), EventStyle.ThemeTertiary)
		val irregularColor = EventStyle.Custom(Color(backgroundIrregular), border = Color(0xFF22D3EE), badge = "Sub", badgeBg = Color(0xFF22D3EE)).withDefault(hasBackgroundIrregular(), EventStyle.ThemeSecondary)

		return when {
			isExam -> if (schoolBackgroundList.contains("exam")) defaultColor else examColor
			isCancelled -> (if (schoolBackgroundList.contains("cancelled")) defaultColor else cancelledColor)
			isIrregular -> if (schoolBackgroundList.contains("irregular")) defaultColor else irregularColor
			else -> if (schoolBackgroundList.contains("regular")) defaultColor else regularColor
		}.withTextStyle(TextStyle(textDecoration = if (periodItem.isCancelled()) TextDecoration.LineThrough else TextDecoration.None))
	}

	/**
	 * Prepares the items for the timetable.
	 *
	 * This function filters out items that should be hidden
	 *
	 * @param hideCancelled Whether cancelled items should be removed
	 * @return A list of prepared items
	 */
	private fun List<Period>.filterPeriods(
		hideCancelled: Boolean,
	): List<Period> = mapNotNull { item ->
		if (hideCancelled && item.`is`(PeriodState.CANCELLED)) return@mapNotNull null
		item
	}

	/**
	 * Prepares the items for the timetable.
	 *
	 * This function marks items as irregular when they match certain rules
	 *
	 * @param substitutionsIrregular Whether items with substitutions should be marked as irregular
	 * @param backgroundIrregular Whether irregular items should have a different background color
	 * @return A list of prepared items
	 */
	private fun List<Event<PeriodItem>>.prepareEvents(
		substitutionsIrregular: Boolean,
		backgroundIrregular: Boolean
	): List<Event<PeriodItem>> = mapNotNull { item ->
		if (substitutionsIrregular) {
			item.data?.apply {
				forceIrregular =
					classes.find { it.id != it.orgId } != null
						|| teachers.find { it.id != it.orgId } != null
						|| subjects.find { it.id != it.orgId } != null
						|| rooms.find { it.id != it.orgId } != null
				//TODO|| backgroundIrregular.getValue() && item.data.element.backColor != UNTIS_DEFAULT_COLOR
			}
		}
		item
	}

	private fun List<Period>.mergePeriods(days: List<Day>): List<Period> {
		val effectiveDays = days.ifEmpty { com.sapuseven.untis.api.model.untis.masterdata.TimeGrid.generateDefault().days }
		val itemGrid: Array<Array<MutableList<Period>>> =
			Array(effectiveDays.size) { Array(effectiveDays.maxByOrNull { it.units.size }!!.units.size) { mutableListOf() } }
		val leftover: MutableList<Period> = mutableListOf()

		// TODO: Check if the day from the Untis API is always an english string
		val firstDayOfWeek =
			DayOfWeek.MONDAY //DateTimeFormat.forPattern("EEE").withLocale(Locale.ENGLISH).parseDateTime(days.first().day).dayOfWeek

		// Put all items into a two dimensional array depending on day and hour
		forEach { item ->
			val startDateTime = item.startDateTime
			val endDateTime = item.endDateTime

			val day = endDateTime.dayOfWeek.value - firstDayOfWeek.value

			if (day < 0 || day >= effectiveDays.size) return@forEach

			val thisUnitStartIndex = effectiveDays[day].units.indexOfFirst {
				it.startTime.truncatedTo(ChronoUnit.MINUTES)
					.equals(startDateTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES))
			}

			val thisUnitEndIndex = effectiveDays[day].units.indexOfFirst {
				it.endTime.truncatedTo(ChronoUnit.MINUTES)
					.equals(endDateTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES))
			}

			if (thisUnitStartIndex != -1 && thisUnitEndIndex != -1) itemGrid[day][thisUnitStartIndex].add(
				item
			)
			else leftover.add(item)
		}

		val newItems = itemGrid.flatMap { unitsOfDay ->
			unitsOfDay.flatMapIndexed { unitIndex, items ->
				items.onEach {
					var i = 1
					while (unitIndex + i < unitsOfDay.size && it.mergeWith(unitsOfDay[unitIndex + i])) i++
				}
			}
		}.toMutableList()

		newItems.addAll(leftover) // Add items that didn't fit inside the timegrid. These will always be single lessons.

		return newItems
	}

	private fun Period.mergeWith(items: MutableList<Period>): Boolean {
		items.toList().forEachIndexed { i, _ ->
			if (i >= items.size) return@forEachIndexed // Needed because the number of elements can change

			val candidate = items[i]

			if (candidate.startDateTime.dayOfYear != startDateTime.dayOfYear) return@forEachIndexed

			if (this.equalsIgnoreTime(candidate)) {
				endDateTime = candidate.endDateTime
				items.removeAt(i)
				return true
			}
		}
		return false
	}
}
