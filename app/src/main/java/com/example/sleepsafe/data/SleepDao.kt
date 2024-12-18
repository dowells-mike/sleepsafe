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
     * Basic data operations
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sleepData: SleepData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sleepData: List<SleepData>)

    /**
     * Session queries
     */
    @Query("SELECT * FROM sleep_data WHERE sleepStart = :sleepStart ORDER BY timestamp ASC")
    suspend fun getSleepSessionData(sleepStart: Long): List<SleepData>

    @Query("SELECT * FROM sleep_data WHERE sleepStart = :sleepStart ORDER BY timestamp ASC")
    fun getSleepSessionDataFlow(sleepStart: Long): Flow<List<SleepData>>

    @Query("SELECT DISTINCT sleepStart FROM sleep_data WHERE sleepStart > 0 ORDER BY sleepStart DESC")
    suspend fun getAllSleepSessions(): List<Long>

    @Query("SELECT MAX(sleepStart) FROM sleep_data")
    suspend fun getLatestSleepSessionStart(): Long?

    /**
     * Time-based queries
     */
    @Query("SELECT * FROM sleep_data WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    suspend fun getSleepDataBetween(startTime: Long, endTime: Long): List<SleepData>

    @Query("""
        SELECT * FROM sleep_data 
        WHERE sleepStart = (SELECT MAX(sleepStart) FROM sleep_data) 
        ORDER BY timestamp ASC
    """)
    suspend fun getLatestSleepSessionData(): List<SleepData>

    /**
     * Sleep phase analysis
     */
    @Query("""
        SELECT 
            sleepPhase,
            COUNT(*) * 100.0 / (SELECT COUNT(*) FROM sleep_data WHERE sleepStart = :sleepStart) as percentage
        FROM sleep_data 
        WHERE sleepStart = :sleepStart 
        GROUP BY sleepPhase
    """)
    suspend fun getSleepPhaseDistribution(sleepStart: Long): List<SleepPhaseStats>

    /**
     * Session summary
     */
    @Query("""
        SELECT 
            AVG(motion) as avgMotion,
            AVG(audioLevel) as avgAudioLevel,
            sleepStart,
            alarmTime,
            MIN(timestamp) as timestamp,
            (SELECT COUNT(*) * 100.0 / total FROM sleep_data 
             WHERE sleepStart = :sleepStart AND sleepPhase = 'DEEP_SLEEP') as deepSleepPercentage,
            (SELECT COUNT(*) * 100.0 / total FROM sleep_data 
             WHERE sleepStart = :sleepStart AND sleepPhase = 'LIGHT_SLEEP') as lightSleepPercentage,
            (SELECT COUNT(*) * 100.0 / total FROM sleep_data 
             WHERE sleepStart = :sleepStart AND sleepPhase = 'REM') as remSleepPercentage,
            (SELECT COUNT(*) * 100.0 / total FROM sleep_data 
             WHERE sleepStart = :sleepStart AND sleepPhase = 'AWAKE') as awakePercentage,
            (MAX(timestamp) - MIN(timestamp)) as totalSleepDuration,
            (SELECT COUNT(*) * 30000 FROM sleep_data 
             WHERE sleepStart = :sleepStart AND sleepPhase = 'DEEP_SLEEP') as deepSleepDuration,
            (SELECT COUNT(*) FROM sleep_data 
             WHERE sleepStart = :sleepStart AND audioLevel > 0.4) as snoreCount
        FROM sleep_data
        CROSS JOIN (SELECT COUNT(*) as total FROM sleep_data WHERE sleepStart = :sleepStart)
        WHERE sleepStart = :sleepStart
        GROUP BY sleepStart, alarmTime
    """)
    suspend fun getSleepSessionSummary(sleepStart: Long): SleepSessionSummary?

    /**
     * Sleep quality metrics
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
     * Data management
     */
    @Query("DELETE FROM sleep_data WHERE timestamp < :timestamp")
    suspend fun deleteDataBefore(timestamp: Long)

    @Query("DELETE FROM sleep_data WHERE sleepStart = :sleepStart")
    suspend fun deleteSleepSession(sleepStart: Long)

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

    /**
     * Analysis queries
     */
    @Query("""
        SELECT AVG(audioLevel) as avgAudioLevel
        FROM sleep_data
        WHERE sleepStart = :sleepStart
        AND timestamp BETWEEN :startTime AND :endTime
    """)
    suspend fun getAverageAudioLevel(sleepStart: Long, startTime: Long, endTime: Long): Float

    @Query("""
        SELECT COUNT(DISTINCT (timestamp / 300000)) as episodes
        FROM sleep_data
        WHERE sleepStart = :sleepStart
        AND audioLevel > 0.4
        AND timestamp BETWEEN :startTime AND :endTime
    """)
    suspend fun getSnoreEpisodes(sleepStart: Long, startTime: Long, endTime: Long): Int

    @Query("""
        SELECT sleepPhase, COUNT(*) * 30 as durationSeconds
        FROM sleep_data
        WHERE sleepStart = :sleepStart
        GROUP BY sleepPhase
        ORDER BY durationSeconds DESC
    """)
    suspend fun getSleepPhaseDurations(sleepStart: Long): List<SleepPhaseDuration>
}
