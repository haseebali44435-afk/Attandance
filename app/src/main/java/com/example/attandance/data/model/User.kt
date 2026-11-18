package com.example.attandance.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: Role = Role.STUDENT,
    val departmentId: String = "",
    val departmentName: String = "",
    val semesterId: String = "",
    val rollNumber: String = "",
    val phone: String = "",
    val status: String = "APPROVED", // "APPROVED", "PENDING", "REJECTED"
    val createdAt: Long = System.currentTimeMillis()
)