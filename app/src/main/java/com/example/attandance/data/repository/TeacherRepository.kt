package com.example.attandance.data.repository

import com.example.attandance.data.model.*
import kotlinx.coroutines.flow.Flow

interface TeacherRepository {
    fun getAssignedClassesFlow(teacherId: String): Flow<Resource<List<TeacherAssignment>>>
    fun getStudentsForClassFlow(departmentId: String, semesterId: String): Flow<Resource<List<Student>>>
    fun getTeacherSessionsFlow(teacherId: String): Flow<Resource<List<AttendanceSession>>>
}