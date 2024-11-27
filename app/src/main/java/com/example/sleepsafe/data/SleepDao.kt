// SleepDao.kt
package com.example.sleepsafe.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Data Access Object (DAO) for managing sleep data in the database.
 */
@Dao
interface SleepDao {

    /**
     * Inserts a list of sleep data records into the database.
     * If there is a conflict, the existing data will be replaced.
     *
     * @param sleepData The list of SleepData objects to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sleepData: List<SleepData>)

    /**
     * Retrieves sleep data within a specified time range.
     *
     * @param startTime The start timestamp for the query.
     * @param endTime The end timestamp for the query.
     * @return A list of SleepData objects matching the time range.
     */
    @Query("SELECT * FROM sleep_data WHERE timestamp BETWEEN :startTime AND :endTime")
    suspend fun getSleepDataBetween(startTime: Long, endTime: Long): List<SleepData>
}
