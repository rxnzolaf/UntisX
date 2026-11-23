package com.rxnzolaf.untis.models

import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.timetable.Period
import com.sapuseven.untis.api.model.untis.timetable.PeriodElement
import com.sapuseven.untis.api.model.untis.timetable.PeriodText
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class PeriodItemCustomFallbackTest {

	@Test
	fun periodItem_customLessonFallback() {
		val period = Period(
			id = 1L,
			lessonId = 1L,
			startDateTime = LocalDateTime.of(2023, 6, 19, 8, 0),
			endDateTime = LocalDateTime.of(2023, 6, 19, 8, 45),
			foreColor = "#FFFFFF",
			backColor = "#4F6BFF",
			innerForeColor = "",
			innerBackColor = "",
			text = PeriodText(
				lesson = "Mathematik",
				substitution = "Herr Müller",
				info = "Raum 101",
				attachments = null
			),
			elements = listOf(
				PeriodElement(ElementType.SUBJECT, 1L),
				PeriodElement(ElementType.TEACHER, 1L),
				PeriodElement(ElementType.ROOM, 1L)
			),
			homeWorks = null
		)

		val periodItem = PeriodItem(masterDataRepository = null, originalPeriod = period)

		assertEquals("Mathematik", periodItem.getShort(ElementType.SUBJECT))
		assertEquals("Herr Müller", periodItem.getShort(ElementType.TEACHER))
		assertEquals("Raum 101", periodItem.getShort(ElementType.ROOM))

		assertEquals("Mathematik", periodItem.getLong(ElementType.SUBJECT))
		assertEquals("Herr Müller", periodItem.getLong(ElementType.TEACHER))
		assertEquals("Raum 101", periodItem.getLong(ElementType.ROOM))

		assertEquals("Mathematik", periodItem.getShortAnnotated(ElementType.SUBJECT).text)
		assertEquals("Herr Müller", periodItem.getShortAnnotated(ElementType.TEACHER).text)
		assertEquals("Raum 101", periodItem.getShortAnnotated(ElementType.ROOM).text)
	}
}
