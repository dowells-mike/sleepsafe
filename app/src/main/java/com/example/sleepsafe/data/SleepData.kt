// SleepData.kt
package com.example.sleepsafe.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_data")
data class SleepData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val motion: Float,
    val audioLevel: Float
)
