package com.rxnzolaf.untis.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CustomSubjectEntity(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val userId: Long,
	val name: String,
	val shortName: String,
	val color: String = ""
)
