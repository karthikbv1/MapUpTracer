package com.karthik.mapuptracer.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [LocationEntity::class, SessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
}