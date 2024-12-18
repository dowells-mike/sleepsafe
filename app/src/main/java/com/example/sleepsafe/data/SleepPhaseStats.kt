package com.example.sleepsafe.data

import androidx.room.ColumnInfo

/**
 * Data class for sleep phase statistics.
 */
data class SleepPhaseStats(
    @ColumnInfo(name = "sleepPhase") val phase: String,
    @ColumnInfo(name = "percentage") val percentage: Float
)

/**
 * Data class for sleep phase durations.
 */
data class SleepPhaseDuration(
    @ColumnInfo(name = "sleepPhase") val phase: String,
    @ColumnInfo(name = "durationSeconds") val duration: Int
)
