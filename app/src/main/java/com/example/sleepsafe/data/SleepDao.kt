// SleepDao.kt
package com.example.sleepsafe.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sleepData: List<SleepData>)

    @Query("SELECT * FROM sleep_data WHERE timestamp BETWEEN :startTime AND :endTime")
    fun getSleepDataBetween(startTime: Long, endTime: Long): Flow<List<SleepData>>
}
