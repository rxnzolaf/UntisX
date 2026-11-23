package com.rxnzolaf.untis.data.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.sapuseven.untis.api.model.untis.SchoolInfo
import com.sapuseven.untis.api.model.untis.Settings
import com.sapuseven.untis.api.model.untis.UserData
import com.sapuseven.untis.api.model.untis.masterdata.TimeGrid
import com.rxnzolaf.untis.data.database.entities.AbsenceReasonEntity
import com.rxnzolaf.untis.data.database.entities.DepartmentEntity
import com.rxnzolaf.untis.data.database.entities.DutyEntity
import com.rxnzolaf.untis.data.database.entities.EventReasonEntity
import com.rxnzolaf.untis.data.database.entities.EventReasonGroupEntity
import com.rxnzolaf.untis.data.database.entities.ExcuseStatusEntity
import com.rxnzolaf.untis.data.database.entities.HolidayEntity
import com.rxnzolaf.untis.data.database.entities.KlasseEntity
import com.rxnzolaf.untis.data.database.entities.RoomEntity
import com.rxnzolaf.untis.data.database.entities.SchoolYearEntity
import com.rxnzolaf.untis.data.database.entities.SubjectEntity
import com.rxnzolaf.untis.data.database.entities.TeacherEntity
import com.rxnzolaf.untis.data.database.entities.TeachingMethodEntity
import com.rxnzolaf.untis.data.database.entities.User
import com.rxnzolaf.untis.data.database.entities.UserDao
import com.rxnzolaf.untis.data.database.entities.CustomLessonEntity
import com.rxnzolaf.untis.data.database.entities.CustomClassEntity
import com.rxnzolaf.untis.data.database.entities.CustomTeacherEntity
import com.rxnzolaf.untis.data.database.entities.CustomRoomEntity
import com.rxnzolaf.untis.data.database.entities.CustomSubjectEntity
import com.rxnzolaf.untis.data.database.entities.CustomTimeGridEntity
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Database(
	version = 16,
	entities = [
		User::class,
		AbsenceReasonEntity::class,
		DepartmentEntity::class,
		DutyEntity::class,
		EventReasonEntity::class,
		EventReasonGroupEntity::class,
		ExcuseStatusEntity::class,
		HolidayEntity::class,
		KlasseEntity::class,
		RoomEntity::class,
		SubjectEntity::class,
		TeacherEntity::class,
		TeachingMethodEntity::class,
		SchoolYearEntity::class,
		CustomLessonEntity::class,
		CustomClassEntity::class,
		CustomTeacherEntity::class,
		CustomRoomEntity::class,
		CustomSubjectEntity::class,
		CustomTimeGridEntity::class,
	],
	autoMigrations = [
		AutoMigration(from = 8, to = 9),
		AutoMigration(from = 9, to = 10),
		AutoMigration(from = 10, to = 11, spec = MigrationSpec10to11::class),
		AutoMigration(from = 11, to = 12, spec = MigrationSpec11to12::class),
		AutoMigration(from = 12, to = 13),
		AutoMigration(from = 13, to = 14),
		AutoMigration(from = 14, to = 15),
		AutoMigration(from = 15, to = 16)
	]
)
@TypeConverters(UserConverters::class)
abstract class UserDatabase : RoomDatabase() {
	abstract fun userDao(): UserDao

	companion object {
		@Volatile
		private var INSTANCE: UserDatabase? = null

		fun getInstance(context: Context): UserDatabase {
			return INSTANCE ?: synchronized(this) {
				val instance = androidx.room.Room.databaseBuilder(
					context.applicationContext,
					UserDatabase::class.java,
					"userdata.db"
				)
				.addMigrations(
					*MIGRATIONS_USER_LEGACY.toTypedArray(),
					MIGRATION_USER_7_8,
				)
				.build()
				INSTANCE = instance
				instance
			}
		}
	}
}

internal class UserConverters {
	private inline fun <reified T> encode(value: T?): String? =
		value?.let { Json.encodeToString<T>(it) }

	private inline fun <reified T> decode(string: String?): T? =
		string?.let { Json.decodeFromString<T>(it) }

	@TypeConverter
	fun encodeTimeGrid(timeGrid: TimeGrid?): String? = encode(timeGrid)

	@TypeConverter
	fun decodeTimeGrid(string: String?): TimeGrid? = decode(string)

	@TypeConverter
	fun encodeUntisUserData(untisUserData: UserData?): String? = encode(untisUserData)

	@TypeConverter
	fun decodeUntisUserData(string: String?): UserData? = decode(string)

	@TypeConverter
	fun encodeUntisSettings(untisSettings: Settings?): String? = encode(untisSettings)

	@TypeConverter
	fun decodeUntisSettings(string: String?): Settings? = decode(string)

	@TypeConverter
	fun encodeUntisSchoolInfo(untisSchoolInfo: SchoolInfo?): String? = encode(untisSchoolInfo)

	@TypeConverter
	fun decodeUntisSchoolInfo(string: String?): SchoolInfo? = decode(string)

	@TypeConverter
	fun encodeLongList(intList: List<Long>?): String? = encode(intList)

	@TypeConverter
	fun decodeLongList(string: String?): List<Long>? = decode(string)

	@TypeConverter
	fun encodeDate(date: LocalDate?): String? = date?.format(DateTimeFormatter.ISO_LOCAL_DATE)

	@TypeConverter
	fun decodeDate(date: String?): LocalDate? =
		date?.let { runCatching { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull() }

	@TypeConverter
	fun encodeTime(date: LocalTime?): String? = date?.format(DateTimeFormatter.ISO_LOCAL_TIME)

	@TypeConverter
	fun decodeTime(date: String?): LocalTime? =
		date?.let { runCatching { LocalTime.parse(it, DateTimeFormatter.ISO_LOCAL_TIME) }.getOrNull() }
}
