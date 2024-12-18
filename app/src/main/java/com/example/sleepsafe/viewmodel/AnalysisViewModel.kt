// AnalysisViewModel.kt
package com.example.sleepsafe.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.sleepsafe.data.SleepData
import com.example.sleepsafe.data.SleepDatabase
import com.example.sleepsafe.data.SleepQualityMetrics
import com.example.sleepsafe.data.SleepSessionSummary
import kotlinx.coroutines.*
import java.util.*

class AnalysisViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "AnalysisViewModel"
    private val sleepDao = SleepDatabase.getDatabase(application).sleepDao()
    private val refreshInterval = 5_000L // 5 seconds refresh for testing
    private var refreshJob: Job? = null

    private val _sleepData = MutableLiveData<List<SleepData>>()
    val sleepData: LiveData<List<SleepData>> = _sleepData

    private val _selectedDate = MutableLiveData<Date>(Date())
    val selectedDate: LiveData<Date> = _selectedDate

    private val _sleepQuality = MutableLiveData<SleepQualityMetrics>()
    val sleepQuality: LiveData<SleepQualityMetrics> = _sleepQuality

    private val _sessionSummary = MutableLiveData<SleepSessionSummary>()
    val sessionSummary: LiveData<SleepSessionSummary> = _sessionSummary

    private val _availableSessions = MutableLiveData<List<Long>>()
    val availableSessions: LiveData<List<Long>> = _availableSessions

    init {
        loadSleepDataForDate(Date())
        loadAvailableSessions()
    }

    fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (isActive) {
                try {
                    loadLatestSession()
                    delay(refreshInterval)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in auto refresh", e)
                    delay(1000) // Wait before retrying
                }
            }
        }
    }

    private fun loadAvailableSessions() {
        viewModelScope.launch {
            try {
                val sessions = sleepDao.getAllSleepSessions()
                _availableSessions.postValue(sessions)
                Log.d(TAG, "Loaded ${sessions.size} available sessions")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading available sessions", e)
            }
        }
    }

    private fun loadSleepDataForDate(date: Date) {
        val calendarStart = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendarStart.timeInMillis

        val calendarEnd = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val endTime = calendarEnd.timeInMillis

        viewModelScope.launch {
            try {
                val data = sleepDao.getSleepDataBetween(startTime, endTime)
                _sleepData.postValue(data)
                Log.d(TAG, "Retrieved ${data.size} data points for the selected date")

                if (data.isNotEmpty()) {
                    val sleepStart = data.first().sleepStart
                    loadSleepSessionDetails(sleepStart)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading sleep data", e)
            }
        }
    }

    @SuppressLint("NullSafeMutableLiveData")
    private suspend fun loadSleepSessionDetails(sleepStart: Long) {
        try {
            val metrics = sleepDao.getSleepQualityMetrics(sleepStart)
            _sleepQuality.postValue(metrics)
            Log.d(TAG, "Sleep quality metrics loaded: ${metrics.getQualitativeAssessment()}")

            val summary = sleepDao.getSleepSessionSummary(sleepStart)
            _sessionSummary.postValue(summary)
            Log.d(TAG, "Session summary loaded for start time: $sleepStart")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading session details", e)
        }
    }

    fun updateSelectedDate(date: Date) {
        _selectedDate.value = date
        loadSleepDataForDate(date)
    }

    fun loadSleepSession(sleepStart: Long) {
        viewModelScope.launch {
            try {
                val sessionData = sleepDao.getSleepSessionData(sleepStart)
                _sleepData.postValue(sessionData)
                loadSleepSessionDetails(sleepStart)
                _selectedDate.postValue(Date(sleepStart))
                Log.d(TAG, "Loaded sleep session data for start time: $sleepStart")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading sleep session", e)
            }
        }
    }

    private fun loadLatestSession() {
        viewModelScope.launch {
            try {
                val latestStart = sleepDao.getLatestSleepSessionStart()
                latestStart?.let { start ->
                    val sessionData = sleepDao.getSleepSessionData(start)
                    _sleepData.postValue(sessionData)
                    loadSleepSessionDetails(start)
                    Log.d(TAG, "Loaded latest session with ${sessionData.size} data points")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading latest session", e)
            }
        }
    }

    fun cleanupOldSessions(keepSessions: Int = 30) {
        viewModelScope.launch {
            try {
                sleepDao.keepRecentSessions(keepSessions)
                loadAvailableSessions()
                Log.d(TAG, "Cleaned up old sessions, keeping $keepSessions most recent")
            } catch (e: Exception) {
                Log.e(TAG, "Error cleaning up old sessions", e)
            }
        }
    }

    fun getSleepQualityTrend(): String {
        val quality = _sleepQuality.value ?: return "Insufficient data for analysis"
        val summary = _sessionSummary.value ?: return "Insufficient data for analysis"

        return buildString {
            appendLine("Sleep Quality Analysis:")
            appendLine("- Quality Score: ${quality.calculateQualityScore()}%")
            appendLine("- Assessment: ${quality.getQualitativeAssessment()}")
            appendLine()
            appendLine("Motion Analysis:")
            appendLine("- Average: ${summary.avgMotion.format(2)}")
            appendLine("- Maximum: ${quality.maxMotion.format(2)}")
            appendLine()
            appendLine("Audio Analysis:")
            appendLine("- Average: ${summary.avgAudioLevel.format(2)}")
            appendLine("- Maximum: ${quality.maxAudio.format(2)}")
            if (quality.maxAudio > SNORING_THRESHOLD) {
                appendLine("- Potential snoring detected")
            }
            appendLine()
            appendLine("Session Duration: ${formatDuration(summary.timestamp, summary.alarmTime)}")
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatDuration(startTime: Long, endTime: Long): String {
        if (endTime <= startTime) return "Unknown duration"
        val durationMillis = endTime - startTime
        val hours = durationMillis / (1000 * 60 * 60)
        val minutes = (durationMillis % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (durationMillis % (1000 * 60)) / 1000
        return String.format("%d hours %d minutes %d seconds", hours, minutes, seconds)
    }

    private fun Float.format(decimals: Int): String = "%.${decimals}f".format(this)

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
    }

    companion object {
        private const val SNORING_THRESHOLD = 0.4f
    }
}
