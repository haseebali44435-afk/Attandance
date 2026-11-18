package com.example.attandance.data.model

enum class Role {
    ADMIN,
    TEACHER,
    STUDENT;

    companion object {
        fun fromString(roleStr: String?): Role {
            return when (roleStr?.uppercase()) {
                "ADMIN" -> ADMIN
                "TEACHER" -> TEACHER
                "STUDENT" -> STUDENT
                else -> STUDENT
            }
        }
    }
}