package com.example.attandance.data.repository

import com.example.attandance.data.model.*
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    fun getUsersFlow(): Flow<Resource<List<User>>>
    fun getDepartmentsFlow(): Flow<Resource<List<Department>>>
    fun getSubjectsFlow(): Flow<Resource<List<Subject>>>
    fun getTeacherAssignmentsFlow(): Flow<Resource<List<TeacherAssignment>>>
    suspend fun createUser(user: User, pass: String): Resource<Boolean>
    suspend fun addDepartment(dept: Department): Resource<Boolean>
    suspend fun addSubject(subject: Subject): Resource<Boolean>
    suspend fun assignTeacher(assignment: TeacherAssignment): Resource<Boolean>
}