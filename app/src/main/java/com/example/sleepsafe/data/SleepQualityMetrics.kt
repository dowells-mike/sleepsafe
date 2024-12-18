package com.example.sleepsafe.data

import androidx.room.ColumnInfo

/**
 * Data class representing sleep quality metrics.
 */
data class SleepQualityMetrics(
    @ColumnInfo(name = "totalReadings") val totalReadings: Int,
    @ColumnInfo(name = "avgMotion") val avgMotion: Float,
    @ColumnInfo(name = "maxMotion") val maxMotion: Float,
    @ColumnInfo(name = "avgAudio") val avgAudio: Float,
    @ColumnInfo(name = "maxAudio") val maxAudio: Float
) {
    /**
     * Calculates a quality score based on the metrics.
     * @return A score between 0 (poor) and 100 (excellent)
     */
    fun calculateQualityScore(): Int {
        if (totalReadings == 0) return 0

        // Calculate motion score (lower is better)
        val motionScore = ((1 - avgMotion / maxMotion) * 100).coerceIn(0f, 100f)

        // Calculate audio score (lower is better)
        val audioScore = ((1 - avgAudio / maxAudio) * 100).coerceIn(0f, 100f)

        // Weight the scores (motion is weighted more heavily than audio)
        return ((motionScore * 0.6f) + (audioScore * 0.4f)).toInt()
    }

    /**
     * Gets a qualitative assessment of the metrics.
     */
    fun getQualitativeAssessment(): String {
        return when (calculateQualityScore()) {
            in 0..20 -> "Poor"
            in 21..40 -> "Fair"
            in 41..60 -> "Good"
            in 61..80 -> "Very Good"
            else -> "Excellent"
        }
    }

    /**
     * Gets detailed analysis of the metrics.
     */
    fun getDetailedAnalysis(): String {
        val score = calculateQualityScore()

        return buildString {
            appendLine("Sleep Quality: ${getQualitativeAssessment()} ($score/100)")
            appendLine("Based on $totalReadings measurements:")
            appendLine("- Movement: Average ${avgMotion.format(2)}, Peak ${maxMotion.format(2)}")
            appendLine("- Noise: Average ${avgAudio.format(2)}, Peak ${maxAudio.format(2)}")

            // Add specific insights
            when {
                avgMotion > maxMotion * 0.7f ->
                    appendLine("High movement detected throughout sleep period")
                avgAudio > maxAudio * 0.7f ->
                    appendLine("Consistent noise levels detected")
                score > 80 ->
                    appendLine("Deep, restful sleep detected")
                score < 40 ->
                    appendLine("Sleep disruptions detected")
            }
        }
    }

    private fun Float.format(decimals: Int): String = "%.${decimals}f".format(this)
}
