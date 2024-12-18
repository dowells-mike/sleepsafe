package com.example.sleepsafe.data

import androidx.room.ColumnInfo

/**
 * Data class representing a summary of a sleep session.
 * This is used for aggregated data queries that don't need the full SleepData structure.
 */
data class SleepSessionSummary(
    @ColumnInfo(name = "avgMotion") val avgMotion: Float,
    @ColumnInfo(name = "avgAudioLevel") val avgAudioLevel: Float,
    @ColumnInfo(name = "sleepStart") val sleepStart: Long,
    @ColumnInfo(name = "alarmTime") val alarmTime: Long,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "deepSleepPercentage") val deepSleepPercentage: Float,
    @ColumnInfo(name = "lightSleepPercentage") val lightSleepPercentage: Float,
    @ColumnInfo(name = "remSleepPercentage") val remSleepPercentage: Float,
    @ColumnInfo(name = "awakePercentage") val awakePercentage: Float,
    @ColumnInfo(name = "totalSleepDuration") val totalSleepDuration: Long,
    @ColumnInfo(name = "deepSleepDuration") val deepSleepDuration: Long,
    @ColumnInfo(name = "snoreCount") val snoreCount: Int
) {
    /**
     * Gets the sleep quality score based on sleep phase distribution.
     */
    fun getSleepQualityScore(): Int {
        // Ideal sleep phase distribution (approximate):
        // Deep sleep: 20-25%
        // Light sleep: 45-55%
        // REM: 20-25%
        // Awake: <5%

        val deepSleepScore = calculatePhaseScore(deepSleepPercentage, 20f, 25f)
        val lightSleepScore = calculatePhaseScore(lightSleepPercentage, 45f, 55f)
        val remScore = calculatePhaseScore(remSleepPercentage, 20f, 25f)
        val awakeScore = if (awakePercentage <= 5f) 100 else (100 - (awakePercentage - 5f) * 5).toInt()

        // Weight the scores (deep sleep and REM are most important)
        return (deepSleepScore * 0.35f +
                lightSleepScore * 0.25f +
                remScore * 0.25f +
                awakeScore * 0.15f).toInt()
    }

    private fun calculatePhaseScore(percentage: Float, idealMin: Float, idealMax: Float): Int {
        return when {
            percentage in idealMin..idealMax -> 100
            percentage < idealMin -> (100 * (percentage / idealMin)).toInt()
            else -> (100 * (1 - (percentage - idealMax) / 50)).coerceIn(0f, 100f).toInt()
        }
    }

    /**
     * Gets a qualitative assessment of the sleep quality.
     */
    fun getQualitativeAssessment(): String {
        val score = getSleepQualityScore()
        return when {
            score >= 90 -> "Excellent"
            score >= 80 -> "Very Good"
            score >= 70 -> "Good"
            score >= 60 -> "Fair"
            else -> "Poor"
        }
    }

    /**
     * Gets detailed analysis of the sleep session.
     */
    fun getDetailedAnalysis(): String {
        val duration = formatDuration(totalSleepDuration)
        val deepSleep = formatDuration(deepSleepDuration)
        val score = getSleepQualityScore()

        return buildString {
            appendLine("Sleep Quality: ${getQualitativeAssessment()} ($score/100)")
            appendLine("Total Sleep Duration: $duration")
            appendLine()
            appendLine("Sleep Phases:")
            appendLine("- Deep Sleep: ${deepSleepPercentage.format(1)}% ($deepSleep)")
            appendLine("- Light Sleep: ${lightSleepPercentage.format(1)}%")
            appendLine("- REM Sleep: ${remSleepPercentage.format(1)}%")
            appendLine("- Awake: ${awakePercentage.format(1)}%")
            appendLine()
            appendLine("Additional Metrics:")
            appendLine("- Average Motion: ${avgMotion.format(2)}")
            appendLine("- Average Noise: ${avgAudioLevel.format(2)}")
            appendLine("- Snoring Episodes: $snoreCount")

            // Add recommendations
            appendLine()
            appendLine("Recommendations:")
            when {
                deepSleepPercentage < 15f ->
                    appendLine("- Try to improve deep sleep by maintaining a consistent sleep schedule")
                remSleepPercentage < 15f ->
                    appendLine("- REM sleep is low. Consider reducing caffeine intake before bed")
                awakePercentage > 10f ->
                    appendLine("- High wake time. Consider improving sleep environment")
                snoreCount > 10 ->
                    appendLine("- Frequent snoring detected. Consider consulting a sleep specialist")
            }
        }
    }

    private fun formatDuration(millis: Long): String {
        val hours = millis / (1000 * 60 * 60)
        val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)
        return String.format("%dh %02dm", hours, minutes)
    }

    private fun Float.format(decimals: Int) = "%.${decimals}f".format(this)
}
