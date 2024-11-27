// SleepData.kt
package com.example.sleepsafe.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data class representing a single record of sleep data.
 * Annotated as a Room entity for database storage.
 *
 * @property id The unique ID of the sleep record (auto-generated).
 * @property timestamp The timestamp when the record was created.
 * @property motion The motion data recorded during sleep.
 * @property audioLevel The audio level recorded during sleep.
 */
@Entity(tableName = "sleep_data")
data class SleepData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val motion: Float,
    val audioLevel: Float
)
