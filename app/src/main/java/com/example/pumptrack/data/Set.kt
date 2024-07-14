package com.example.pumptrack.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "sets",
    foreignKeys = [ForeignKey(
        entity = Exercise::class,
        parentColumns = ["id"],
        childColumns = ["exerciseId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Set(
    @PrimaryKey(autoGenerate = true) val setId: Int,
    val weight: Double = 0.0,
    val reps: Int = 0,
    val date: String,
    val exerciseId: Int
)
