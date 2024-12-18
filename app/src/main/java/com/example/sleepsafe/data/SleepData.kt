package com.example.sleepsafe.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

/**
 * Entity representing sleep tracking data.
 *
 * @property id Unique identifier for the data point
 * @property timestamp Time when the data was recorded
 * @property motion Motion level detected from accelerometer
 * @property audioLevel Audio level detected from microphone
 * @property sleepStart Time when sleep tracking started
 * @property alarmTime Scheduled alarm time (0 if no alarm set)
 * @property sleepPhase Current sleep phase (AWAKE, LIGHT_SLEEP, DEEP_SLEEP, REM)
 */
@Entity(tableName = "sleep_data")
data class SleepData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "motion")
    val motion: Float,

    @ColumnInfo(name = "audioLevel")
    val audioLevel: Float,

    @ColumnInfo(name = "sleepStart")
    val sleepStart: Long = 0,

    @ColumnInfo(name = "alarmTime")
    val alarmTime: Long = 0,

    @ColumnInfo(name = "sleepPhase")
    val sleepPhase: String = "AWAKE"
) {
    /**
     * Validates that the data point has valid timestamps and values.
     */
    fun isValid(): Boolean {
        return timestamp > 0 &&
                motion >= 0f &&
                audioLevel >= 0f &&
                (sleepStart == 0L || sleepStart <= timestamp) &&
                (alarmTime == 0L || alarmTime > sleepStart) &&
                sleepPhase in setOf("AWAKE", "LIGHT_SLEEP", "DEEP_SLEEP", "REM")
    }

    /**
     * Calculates the duration from this data point to the alarm time.
     */
    fun getRemainingTime(): Long {
        return if (alarmTime > 0 && alarmTime > timestamp) {
            alarmTime - timestamp
        } else {
            0
        }
    }

    /**
     * Gets the elapsed time since sleep tracking started.
     */
    fun getElapsedTime(): Long {
        return if (sleepStart > 0 && timestamp >= sleepStart) {
            timestamp - sleepStart
        } else {
            0
        }
    }

    companion object {
        val SLEEP_PHASES = setOf("AWAKE", "LIGHT_SLEEP", "DEEP_SLEEP", "REM")
    }
}
