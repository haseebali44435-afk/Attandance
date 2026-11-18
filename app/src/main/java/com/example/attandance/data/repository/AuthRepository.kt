package com.example.attandance.data.repository

import com.example.attandance.data.model.Resource
import com.example.attandance.data.model.User

interface AuthRepository {
    fun getCurrentUser(): User?
    fun logout()
    suspend fun login(email: String, pass: String): Resource<User>
    suspend fun getUserProfile(uid: String): Resource<User>
    suspend fun registerTeacher(name: String, email: String, pass: String, departmentId: String, departmentName: String): Resource<Boolean>
    suspend fun sendPasswordResetEmail(email: String): Resource<Boolean>
}