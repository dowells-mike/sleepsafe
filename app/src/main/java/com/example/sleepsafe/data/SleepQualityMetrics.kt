// SleepQualityMetrics.kt
package com.example.sleepsafe.data

/**
 * Data class representing sleep quality metrics for analysis.
 * This class is used to aggregate and analyze sleep tracking data.
 */
data class SleepQualityMetrics(
    val totalReadings: Int = 0,
    val avgMotion: Float = 0f,
    val maxMotion: Float = 0f,
    val avgAudio: Float = 0f,
    val maxAudio: Float = 0f
) {
    /**
     * Calculates a sleep quality score based on the metrics.
     * @return A score between 0 (poor) and 100 (excellent)
     */
    fun calculateQualityScore(): Int {
        if (totalReadings == 0) return 0

        // Convert metrics to scores between 0-100
        val motionScore = (100 - (avgMotion / maxMotion) * 100).coerceIn(0f, 100f)
        val audioScore = (100 - (avgAudio / maxAudio) * 100).coerceIn(0f, 100f)

        // Weight the scores (motion is weighted more heavily than audio)
        return ((motionScore * 0.6f) + (audioScore * 0.4f)).toInt()
    }

    /**
     * Provides a qualitative assessment of sleep quality.
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
     * Provides detailed analysis of the sleep metrics.
     */
    fun getDetailedAnalysis(): String {
        val quality = getQualitativeAssessment()
        val score = calculateQualityScore()

        return buildString {
            appendLine("Sleep Quality: $quality ($score/100)")
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
