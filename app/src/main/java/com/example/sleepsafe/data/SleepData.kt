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
                sleepStart > 0 &&
                sleepPhase in SLEEP_PHASES
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

    /**
     * Formats the motion value for display.
     */
    fun getFormattedMotion(): String {
        return "%.2f".format(motion)
    }

    /**
     * Formats the audio level for display.
     */
    fun getFormattedAudioLevel(): String {
        return "%.2f".format(audioLevel)
    }

    companion object {
        val SLEEP_PHASES = setOf("AWAKE", "LIGHT_SLEEP", "DEEP_SLEEP", "REM")

        /**
         * Creates a test data point with current timestamp.
         */
        fun createTestData(
            motion: Float = 0f,
            audioLevel: Float = 0f,
            sleepPhase: String = "AWAKE"
        ): SleepData {
            val now = System.currentTimeMillis()
            return SleepData(
                timestamp = now,
                motion = motion,
                audioLevel = audioLevel,
                sleepStart = now,
                alarmTime = now + (3 * 60 * 1000), // 3 minutes from now
                sleepPhase = sleepPhase
            )
        }
    }
}
