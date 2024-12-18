// SleepDatabase.kt
package com.example.sleepsafe.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [SleepData::class], version = 2, exportSchema = false)
abstract class SleepDatabase : RoomDatabase() {

    abstract fun sleepDao(): SleepDao

    companion object {
        @Volatile
        private var INSTANCE: SleepDatabase? = null

        // Migration from version 1 to 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns to existing table
                database.execSQL(
                    "ALTER TABLE sleep_data ADD COLUMN sleepStart INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE sleep_data ADD COLUMN alarmTime INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        fun getDatabase(context: Context): SleepDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SleepDatabase::class.java,
                    "sleep_database"
                )
                    .addMigrations(MIGRATION_1_2) // Add migration strategy
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
