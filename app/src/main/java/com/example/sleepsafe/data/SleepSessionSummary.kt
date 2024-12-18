// SleepSessionSummary.kt
package com.example.sleepsafe.data

/**
 * Data class representing a summary of a sleep session.
 * This is used for aggregated data queries that don't need the full SleepData structure.
 */
data class SleepSessionSummary(
    val avgMotion: Float,
    val avgAudioLevel: Float,
    val sleepStart: Long,
    val alarmTime: Long,
    val timestamp: Long
)
