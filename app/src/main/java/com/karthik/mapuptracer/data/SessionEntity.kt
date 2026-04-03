package com.karthik.mapuptracer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey
    val sessionId: Long,
    val startTime: Long,
    val endTime: Long? = null
)