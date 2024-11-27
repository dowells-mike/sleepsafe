// SleepDatabase.kt
package com.example.sleepsafe.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database class for storing and managing sleep data.
 * Includes the SleepDao for database operations.
 */
@Database(entities = [SleepData::class], version = 1, exportSchema = false)
abstract class SleepDatabase : RoomDatabase() {

    /**
     * Provides access to the DAO (Data Access Object) for sleep data.
     */
    abstract fun sleepDao(): SleepDao

    companion object {
        @Volatile
        private var INSTANCE: SleepDatabase? = null

        /**
         * Retrieves the singleton instance of the SleepDatabase.
         * Ensures only one instance is created using synchronized block.
         *
         * @param context The application context.
         * @return The singleton instance of the database.
         */
        fun getDatabase(context: Context): SleepDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SleepDatabase::class.java,
                    "sleep_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
