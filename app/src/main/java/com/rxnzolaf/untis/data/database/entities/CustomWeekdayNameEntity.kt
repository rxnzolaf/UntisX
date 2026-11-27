package com.rxnzolaf.untis.data.database.entities

import androidx.room.Entity

@Entity(
	tableName = "custom_weekday_names",
	primaryKeys = ["userId", "dayOfWeek"]
)
data class CustomWeekdayNameEntity(
	val userId: Long,
	val dayOfWeek: Int,
	val nickname: String = "",
	val abbreviation: String = ""
)
