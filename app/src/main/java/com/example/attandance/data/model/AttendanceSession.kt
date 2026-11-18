package com.example.attandance.data.model

data class AttendanceSession(
    val id: String = "",
    val teacherId: String = "",
    val teacherName: String = "",
    val departmentId: String = "",
    val departmentName: String = "",
    val semesterId: String = "",
    val subjectId: String = "",
    val subjectName: String = "",
    val subjectCode: String = "",
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val totalStudents: Int = 0,
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val leaveCount: Int = 0,
    val records: List<AttendanceRecord> = emptyList(),
    val editCount: Int = 0,
    val lastEditedAt: Long? = null
)