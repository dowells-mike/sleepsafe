package com.example.sleepsafe.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.sleepsafe.data.SleepDatabase
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "SettingsViewModel"
    private val prefs = application.getSharedPreferences("sleep_settings", Context.MODE_PRIVATE)
    private val sleepDao = SleepDatabase.getDatabase(application).sleepDao()

    // Motion Detection Settings
    private val _motionSensitivity = MutableLiveData(
        prefs.getInt("motion_sensitivity", 5)
    )
    val motionSensitivity: LiveData<Int> = _motionSensitivity

    // Audio Detection Settings
    private val _audioSensitivity = MutableLiveData(
        prefs.getInt("audio_sensitivity", 5)
    )
    val audioSensitivity: LiveData<Int> = _audioSensitivity

    // Data Collection Settings
    private val _updateInterval = MutableLiveData(
        prefs.getInt("update_interval", 5)
    )
    val updateInterval: LiveData<Int> = _updateInterval

    // Smart Alarm Settings
    private val _useSmartAlarm = MutableLiveData(
        prefs.getBoolean("use_smart_alarm", true)
    )
    val useSmartAlarm: LiveData<Boolean> = _useSmartAlarm

    private val _smartAlarmWindow = MutableLiveData(
        prefs.getInt("smart_alarm_window", 30)
    )
    val smartAlarmWindow: LiveData<Int> = _smartAlarmWindow

    // App Settings
    private val _isDarkTheme = MutableLiveData(
        prefs.getBoolean("dark_theme", false)
    )
    val isDarkTheme: LiveData<Boolean> = _isDarkTheme

    fun setMotionSensitivity(value: Int) {
        _motionSensitivity.value = value
        prefs.edit().putInt("motion_sensitivity", value).apply()
        Log.d(TAG, "Motion sensitivity set to: $value")
    }

    fun setAudioSensitivity(value: Int) {
        _audioSensitivity.value = value
        prefs.edit().putInt("audio_sensitivity", value).apply()
        Log.d(TAG, "Audio sensitivity set to: $value")
    }

    fun setUpdateInterval(value: Int) {
        _updateInterval.value = value
        prefs.edit().putInt("update_interval", value).apply()
        Log.d(TAG, "Update interval set to: $value seconds")
    }

    fun setUseSmartAlarm(enabled: Boolean) {
        _useSmartAlarm.value = enabled
        prefs.edit().putBoolean("use_smart_alarm", enabled).apply()
        Log.d(TAG, "Smart alarm ${if (enabled) "enabled" else "disabled"}")
    }

    fun setSmartAlarmWindow(minutes: Int) {
        _smartAlarmWindow.value = minutes
        prefs.edit().putInt("smart_alarm_window", minutes).apply()
        Log.d(TAG, "Smart alarm window set to: $minutes minutes")
    }

    fun setDarkTheme(enabled: Boolean) {
        _isDarkTheme.value = enabled
        prefs.edit().putBoolean("dark_theme", enabled).apply()
        Log.d(TAG, "Dark theme ${if (enabled) "enabled" else "disabled"}")
    }

    fun clearAllData() {
        viewModelScope.launch {
            try {
                // Clear database
                sleepDao.deleteAllData()
                Log.d(TAG, "All sleep data cleared")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing sleep data", e)
            }
        }
    }

    // Helper method to get current settings as a map
    fun getCurrentSettings(): Map<String, Any> {
        return mapOf(
            "motionSensitivity" to (_motionSensitivity.value ?: 5),
            "audioSensitivity" to (_audioSensitivity.value ?: 5),
            "updateInterval" to (_updateInterval.value ?: 5),
            "useSmartAlarm" to (_useSmartAlarm.value ?: true),
            "smartAlarmWindow" to (_smartAlarmWindow.value ?: 30),
            "isDarkTheme" to (_isDarkTheme.value ?: false)
        )
    }
}
