package com.rxnzolaf.untis.data.repository

import com.rxnzolaf.untis.data.database.entities.CustomSubjectNameEntity
import com.rxnzolaf.untis.data.database.entities.CustomWeekdayNameEntity
import com.rxnzolaf.untis.data.database.entities.UserDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface CustomNamesEntryPoint {
	fun customNamesRepository(): CustomNamesRepository
}

@Serializable
data class CustomNamesExportData(
	val version: Int = 1,
	val subjects: List<ExportedSubjectName> = emptyList(),
	val weekdays: List<ExportedWeekdayName> = emptyList()
)

@Serializable
data class ExportedSubjectName(
	val subjectKey: String,
	val nickname: String,
	val abbreviation: String
)

@Serializable
data class ExportedWeekdayName(
	val dayOfWeek: Int,
	val nickname: String,
	val abbreviation: String
)

@Singleton
class CustomNamesRepository @Inject constructor(
	private val userDao: UserDao,
	private val userRepository: UserRepository
) {
	fun getSubjectNamesFlow(userId: Long): Flow<Map<String, CustomSubjectNameEntity>> {
		return userDao.getCustomSubjectNamesFlow(userId).map { list ->
			list.associateBy { it.subjectKey }
		}
	}

	fun getWeekdayNamesFlow(userId: Long): Flow<Map<Int, CustomWeekdayNameEntity>> {
		return userDao.getCustomWeekdayNamesFlow(userId).map { list ->
			list.associateBy { it.dayOfWeek }
		}
	}

	suspend fun getSubjectNamesMap(userId: Long): Map<String, CustomSubjectNameEntity> {
		return userDao.getCustomSubjectNames(userId).associateBy { it.subjectKey }
	}

	suspend fun getWeekdayNamesMap(userId: Long): Map<Int, CustomWeekdayNameEntity> {
		return userDao.getCustomWeekdayNames(userId).associateBy { it.dayOfWeek }
	}

	suspend fun saveSubjectName(userId: Long, subjectKey: String, nickname: String, abbreviation: String) {
		val cleanNickname = nickname.trim().take(30)
		val cleanAbbr = abbreviation.trim().take(6)
		if (cleanNickname.isEmpty() && cleanAbbr.isEmpty()) {
			userDao.deleteCustomSubjectName(CustomSubjectNameEntity(userId, subjectKey))
		} else {
			userDao.upsertCustomSubjectName(
				CustomSubjectNameEntity(
					userId = userId,
					subjectKey = subjectKey,
					nickname = cleanNickname,
					abbreviation = cleanAbbr
				)
			)
		}
	}

	suspend fun deleteSubjectName(userId: Long, subjectKey: String) {
		userDao.deleteCustomSubjectName(CustomSubjectNameEntity(userId, subjectKey))
	}

	suspend fun deleteAllSubjectNames(userId: Long) {
		userDao.deleteAllCustomSubjectNames(userId)
	}

	suspend fun saveWeekdayName(userId: Long, dayOfWeek: Int, nickname: String, abbreviation: String) {
		val cleanNickname = nickname.trim().take(20)
		val cleanAbbr = abbreviation.trim().take(6)
		if (cleanNickname.isEmpty() && cleanAbbr.isEmpty()) {
			userDao.deleteCustomWeekdayName(CustomWeekdayNameEntity(userId, dayOfWeek))
		} else {
			userDao.upsertCustomWeekdayName(
				CustomWeekdayNameEntity(
					userId = userId,
					dayOfWeek = dayOfWeek,
					nickname = cleanNickname,
					abbreviation = cleanAbbr
				)
			)
		}
	}

	suspend fun deleteAllWeekdayNames(userId: Long) {
		userDao.deleteAllCustomWeekdayNames(userId)
	}

	suspend fun exportToJson(userId: Long): String {
		val subjects = userDao.getCustomSubjectNames(userId).map {
			ExportedSubjectName(it.subjectKey, it.nickname, it.abbreviation)
		}
		val weekdays = userDao.getCustomWeekdayNames(userId).map {
			ExportedWeekdayName(it.dayOfWeek, it.nickname, it.abbreviation)
		}
		val export = CustomNamesExportData(subjects = subjects, weekdays = weekdays)
		return Json { prettyPrint = true }.encodeToString(CustomNamesExportData.serializer(), export)
	}

	suspend fun importFromJson(userId: Long, jsonString: String): Boolean {
		return try {
			val data = Json { ignoreUnknownKeys = true }.decodeFromString(CustomNamesExportData.serializer(), jsonString)
			data.subjects.forEach {
				saveSubjectName(userId, it.subjectKey, it.nickname, it.abbreviation)
			}
			data.weekdays.forEach {
				saveWeekdayName(userId, it.dayOfWeek, it.nickname, it.abbreviation)
			}
			true
		} catch (_: Exception) {
			false
		}
	}

	companion object {
		fun resolveSubjectLongName(
			customMap: Map<String, CustomSubjectNameEntity>?,
			subjectKey: String,
			originalLongName: String
		): String {
			val custom = customMap?.get(subjectKey) ?: return originalLongName
			return custom.nickname.ifBlank { originalLongName }
		}

		fun resolveSubjectShortName(
			customMap: Map<String, CustomSubjectNameEntity>?,
			subjectKey: String,
			originalShortName: String
		): String {
			val custom = customMap?.get(subjectKey) ?: return originalShortName
			if (custom.abbreviation.isNotBlank()) {
				return custom.abbreviation
			}
			if (custom.nickname.isNotBlank() && custom.nickname.length <= 6) {
				return custom.nickname
			}
			return originalShortName
		}

		fun resolveWeekdayName(
			customMap: Map<Int, CustomWeekdayNameEntity>?,
			dayOfWeek: DayOfWeek,
			locale: Locale = Locale.getDefault()
		): String {
			val custom = customMap?.get(dayOfWeek.value)
			if (custom != null && custom.nickname.isNotBlank()) {
				return custom.nickname
			}
			return dayOfWeek.getDisplayName(TextStyle.FULL, locale)
		}

		fun resolveWeekdayShortName(
			customMap: Map<Int, CustomWeekdayNameEntity>?,
			dayOfWeek: DayOfWeek,
			locale: Locale = Locale.getDefault()
		): String {
			val custom = customMap?.get(dayOfWeek.value)
			if (custom != null) {
				if (custom.abbreviation.isNotBlank()) return custom.abbreviation
				if (custom.nickname.isNotBlank()) return custom.nickname
			}
			return dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
		}
	}
}
