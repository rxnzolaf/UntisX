package com.rxnzolaf.untis.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CustomLessonEntity(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val userId: Long,
	val subjectName: String,
	val shortName: String,
	val teacher: String = "",
	val room: String = "",
	val dayOfWeek: Int, // 1 = Monday to 7 = Sunday
	val startTime: String, // "08:00"
	val endTime: String,   // "08:45"
	val color: String = "", // Hex color
	val note: String = "",
	val classId: Long? = null,
	val teacherId: Long? = null,
	val roomId: Long? = null,
	val subjectId: Long? = null,
	@androidx.room.ColumnInfo(defaultValue = "") val className: String = ""
)
