// AnalysisViewModel.kt
package com.example.sleepsafe.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.sleepsafe.data.SleepData
import com.example.sleepsafe.data.SleepDatabase
import com.example.sleepsafe.data.SleepQualityMetrics
import com.example.sleepsafe.data.SleepSessionSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*

class AnalysisViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "AnalysisViewModel"
    private val sleepDao = SleepDatabase.getDatabase(application).sleepDao()

    private val _sleepData = MutableLiveData<List<SleepData>>()
    val sleepData: LiveData<List<SleepData>> get() = _sleepData

    private val _selectedDate = MutableLiveData<Date>(Date())
    val selectedDate: LiveData<Date> get() = _selectedDate

    private val _sleepQuality = MutableLiveData<SleepQualityMetrics>()
    val sleepQuality: LiveData<SleepQualityMetrics> get() = _sleepQuality

    private val _sessionSummary = MutableLiveData<SleepSessionSummary>()
    val sessionSummary: LiveData<SleepSessionSummary> get() = _sessionSummary

    private val _availableSessions = MutableLiveData<List<Long>>()
    val availableSessions: LiveData<List<Long>> get() = _availableSessions

    init {
        loadSleepDataForDate(Date()) // Load data for today initially
        loadAvailableSessions()
    }

    private fun loadAvailableSessions() {
        viewModelScope.launch {
            try {
                val sessions = sleepDao.getAllSleepSessions()
                _availableSessions.postValue(sessions)
                Log.d(TAG, "Loaded ${sessions.size} available sleep sessions")
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
                // Load sleep data for the date range
                val data = sleepDao.getSleepDataBetween(startTime, endTime)
                _sleepData.postValue(data)
                Log.d(TAG, "Retrieved ${data.size} data points for the selected date")

                // If we have data, get the sleep session details
                if (data.isNotEmpty()) {
                    val sleepStart = data.first().sleepStart
                    loadSleepSessionDetails(sleepStart)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading sleep data", e)
            }
        }
    }

    private suspend fun loadSleepSessionDetails(sleepStart: Long) {
        try {
            // Load sleep quality metrics
            val metrics = sleepDao.getSleepQualityMetrics(sleepStart)
            _sleepQuality.postValue(metrics)
            Log.d(TAG, "Sleep quality metrics loaded: ${metrics.getQualitativeAssessment()}")

            // Load session summary
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

                // Update selected date to match session start
                _selectedDate.postValue(Date(sleepStart))

                Log.d(TAG, "Loaded sleep session data for start time: $sleepStart")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading sleep session", e)
            }
        }
    }

    fun getLatestSession() {
        viewModelScope.launch {
            try {
                val latestStart = sleepDao.getLatestSleepSessionStart()
                latestStart?.let { loadSleepSession(it) }
                Log.d(TAG, "Latest session loaded with start time: $latestStart")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading latest session", e)
            }
        }
    }

    fun cleanupOldSessions(keepSessions: Int = 30) {
        viewModelScope.launch {
            try {
                sleepDao.keepRecentSessions(keepSessions)
                loadAvailableSessions() // Refresh the available sessions list
                Log.d(TAG, "Cleaned up old sessions, keeping $keepSessions most recent")
            } catch (e: Exception) {
                Log.e(TAG, "Error cleaning up old sessions", e)
            }
        }
    }

    /**
     * Gets a summary of sleep quality trends over time.
     */
    fun getSleepQualityTrend(): String {
        val quality = _sleepQuality.value ?: return "Insufficient data for analysis"
        val summary = _sessionSummary.value ?: return "Insufficient data for analysis"

        return buildString {
            appendLine(quality.getDetailedAnalysis())
            appendLine()
            appendLine("Session Summary:")
            appendLine("- Average Motion: ${summary.avgMotion}")
            appendLine("- Average Audio Level: ${summary.avgAudioLevel}")
            appendLine("- Session Duration: ${formatDuration(summary.timestamp, summary.alarmTime)}")
        }
    }

    private fun formatDuration(startTime: Long, endTime: Long): String {
        if (endTime <= startTime) return "Unknown duration"
        val durationMillis = endTime - startTime
        val hours = durationMillis / (1000 * 60 * 60)
        val minutes = (durationMillis % (1000 * 60 * 60)) / (1000 * 60)
        return String.format("%d hours %d minutes", hours, minutes)
    }
}
