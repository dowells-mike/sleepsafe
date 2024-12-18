// SleepDao.kt
package com.example.sleepsafe.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing sleep data in the database.
 */
@Dao
interface SleepDao {
    /**
     * Inserts a single sleep data point.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sleepData: SleepData)

    /**
     * Inserts multiple sleep data points.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sleepData: List<SleepData>)

    /**
     * Gets all sleep data between two timestamps.
     */
    @Query("SELECT * FROM sleep_data WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    suspend fun getSleepDataBetween(startTime: Long, endTime: Long): List<SleepData>

    /**
     * Gets all sleep data for a specific sleep session.
     */
    @Query("SELECT * FROM sleep_data WHERE sleepStart = :sleepStart ORDER BY timestamp ASC")
    suspend fun getSleepSessionData(sleepStart: Long): List<SleepData>

    /**
     * Gets all sleep data for a specific sleep session as Flow.
     */
    @Query("SELECT * FROM sleep_data WHERE sleepStart = :sleepStart ORDER BY timestamp ASC")
    fun getSleepSessionDataFlow(sleepStart: Long): Flow<List<SleepData>>

    /**
     * Gets the most recent sleep session start time.
     */
    @Query("SELECT MAX(sleepStart) FROM sleep_data")
    suspend fun getLatestSleepSessionStart(): Long?

    /**
     * Gets all data from the latest sleep session.
     */
    @Query("""
        SELECT * FROM sleep_data 
        WHERE sleepStart = (SELECT MAX(sleepStart) FROM sleep_data) 
        ORDER BY timestamp ASC
    """)
    suspend fun getLatestSleepSessionData(): List<SleepData>

    /**
     * Gets all unique sleep session start times.
     */
    @Query("SELECT DISTINCT sleepStart FROM sleep_data WHERE sleepStart > 0 ORDER BY sleepStart DESC")
    suspend fun getAllSleepSessions(): List<Long>

    /**
     * Deletes all sleep data before a certain timestamp.
     */
    @Query("DELETE FROM sleep_data WHERE timestamp < :timestamp")
    suspend fun deleteDataBefore(timestamp: Long)

    /**
     * Deletes all data for a specific sleep session.
     */
    @Query("DELETE FROM sleep_data WHERE sleepStart = :sleepStart")
    suspend fun deleteSleepSession(sleepStart: Long)

    /**
     * Gets the average motion and audio levels for a sleep session.
     */
    @Query("""
        SELECT 
            AVG(motion) as avgMotion, 
            AVG(audioLevel) as avgAudioLevel, 
            sleepStart, 
            alarmTime, 
            MIN(timestamp) as timestamp
        FROM sleep_data 
        WHERE sleepStart = :sleepStart 
        GROUP BY sleepStart, alarmTime
    """)
    suspend fun getSleepSessionSummary(sleepStart: Long): SleepSessionSummary?

    /**
     * Gets sleep quality metrics for analysis.
     */
    @Query("""
        SELECT 
            COUNT(*) as totalReadings,
            AVG(motion) as avgMotion,
            MAX(motion) as maxMotion,
            AVG(audioLevel) as avgAudio,
            MAX(audioLevel) as maxAudio
        FROM sleep_data 
        WHERE sleepStart = :sleepStart
    """)
    suspend fun getSleepQualityMetrics(sleepStart: Long): SleepQualityMetrics

    /**
     * Cleans up old data, keeping only the specified number of most recent sessions.
     */
    @Query("""
        DELETE FROM sleep_data 
        WHERE sleepStart NOT IN (
            SELECT sleepStart 
            FROM sleep_data 
            GROUP BY sleepStart 
            ORDER BY sleepStart DESC 
            LIMIT :keepSessions
        )
    """)
    suspend fun keepRecentSessions(keepSessions: Int)
}
