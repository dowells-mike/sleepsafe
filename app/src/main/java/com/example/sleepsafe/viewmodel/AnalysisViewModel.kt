// AnalysisViewModel.kt
package com.example.sleepsafe.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.sleepsafe.data.SleepData
import com.example.sleepsafe.data.SleepDatabase
import kotlinx.coroutines.launch
import java.util.*

/**
 * ViewModel to handle data and logic for the Analysis screen.
 *
 * @param application The application context for accessing the database.
 */
class AnalysisViewModel(application: Application) : AndroidViewModel(application) {

    private val sleepDao = SleepDatabase.getDatabase(application).sleepDao()

    private val _sleepData = MutableLiveData<List<SleepData>>()
    val sleepData: LiveData<List<SleepData>> get() = _sleepData

    private val _selectedDate = MutableLiveData<Date>(Date())
    val selectedDate: LiveData<Date> get() = _selectedDate

    /**
     * Loads sleep data for a specific date by querying the database.
     *
     * @param date The selected date.
     */
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
            val data = sleepDao.getSleepDataBetween(startTime, endTime)
            _sleepData.postValue(data)
            Log.d("AnalysisViewModel", "Retrieved ${data.size} data points for the selected date.")
        }
    }

    /**
     * Updates the selected date and loads corresponding sleep data.
     *
     * @param date The new selected date.
     */
    fun updateSelectedDate(date: Date) {
        _selectedDate.postValue(date)
        loadSleepDataForDate(date)
    }
}
