package com.example.attandance.data.repository

import com.example.attandance.data.model.*
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {
    suspend fun submitAttendanceSession(session: AttendanceSession): Resource<Boolean>
    suspend fun updateAttendanceSession(session: AttendanceSession): Resource<Boolean>
    fun getTodaySessionsFlow(): Flow<Resource<List<AttendanceSession>>>
    fun getSessionDetailsFlow(sessionId: String): Flow<Resource<AttendanceSession>>
}