package com.rxnzolaf.untis.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CustomTimeGridEntity(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val userId: Long,
	val label: String,
	val startTime: String,
	val endTime: String
)
