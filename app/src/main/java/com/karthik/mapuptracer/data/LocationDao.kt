package com.karthik.mapuptracer.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LocationDao {

    // Insert one location point
    @Insert
    suspend fun insertLocation(location: LocationEntity)

    // Insert one tracking session
    @Insert
    suspend fun insertSession(session: SessionEntity)

    // Get all sessions
    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    suspend fun getAllSessions(): List<SessionEntity>

    // Get all locations for a session
    @Query("SELECT * FROM locations WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getLocationsForSession(sessionId: Long): List<LocationEntity>

    // ✅ FIX: update end time when tracking stops
    @Query("UPDATE sessions SET endTime = :endTime WHERE sessionId = :sessionId")
    suspend fun updateSessionEndTime(sessionId: Long, endTime: Long)
}