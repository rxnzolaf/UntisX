package com.rxnzolaf.untis.data.database.entities

import androidx.room.Entity

@Entity(
	tableName = "custom_subject_names",
	primaryKeys = ["userId", "subjectKey"]
)
data class CustomSubjectNameEntity(
	val userId: Long,
	val subjectKey: String,
	val nickname: String = "",
	val abbreviation: String = ""
)
