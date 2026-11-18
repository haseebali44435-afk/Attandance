package com.example.attandance.data.model

data class AttendanceRecord(
    val studentId: String = "",
    val studentName: String = "",
    val rollNumber: String = "",
    val status: String = "PRESENT", // "PRESENT", "ABSENT", "ON_LEAVE"
    val isPresent: Boolean = true
)