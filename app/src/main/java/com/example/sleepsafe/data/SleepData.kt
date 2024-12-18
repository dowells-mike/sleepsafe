// SleepData.kt
package com.example.sleepsafe.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing sleep tracking data.
 *
 * @property id Unique identifier for the data point
 * @property timestamp Time when the data was recorded
 * @property motion Motion level detected from accelerometer
 * @property audioLevel Audio level detected from microphone
 * @property sleepStart Time when sleep tracking started
 * @property alarmTime Scheduled alarm time (0 if no alarm set)
 */
@Entity(tableName = "sleep_data")
data class SleepData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val timestamp: Long,
    val motion: Float,
    val audioLevel: Float,

    // Added fields for tracking session context
    val sleepStart: Long = 0,
    val alarmTime: Long = 0
) {
    /**
     * Validates that the data point has valid timestamps.
     */
    fun isValid(): Boolean {
        return timestamp > 0 &&
                (sleepStart == 0L || sleepStart <= timestamp) &&
                (alarmTime == 0L || alarmTime > sleepStart)
    }
}
