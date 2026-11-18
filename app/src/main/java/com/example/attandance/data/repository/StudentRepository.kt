package com.example.attandance.data.repository

import com.example.attandance.data.model.*
import kotlinx.coroutines.flow.Flow

interface StudentRepository {
    fun getStudentSubjectsFlow(departmentId: String, semesterId: String): Flow<Resource<List<Subject>>>
    fun getStudentAttendanceHistoryFlow(studentId: String): Flow<Resource<List<AttendanceSession>>>
}