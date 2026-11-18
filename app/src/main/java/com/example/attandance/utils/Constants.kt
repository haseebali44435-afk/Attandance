package com.example.attandance.utils

object Constants {
    // Firestore Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_DEPARTMENTS = "departments"
    const val COLLECTION_SEMESTERS = "semesters"
    const val COLLECTION_SUBJECTS = "subjects"
    const val COLLECTION_STUDENTS = "students"
    const val COLLECTION_TEACHER_ASSIGNMENTS = "teacherAssignments"
    const val COLLECTION_ATTENDANCE = "attendance"
    const val COLLECTION_ATTENDANCE_SESSIONS = "attendanceSessions"

    // Preferences & Intent Keys
    const val PREF_NAME = "attendance_pref"
    const val KEY_USER_ROLE = "user_role"
    const val KEY_USER_ID = "user_id"
    const val EXTRA_ASSIGNMENT_ID = "extra_assignment_id"
    const val EXTRA_SUBJECT_ID = "extra_subject_id"
    const val EXTRA_SESSION_ID = "extra_session_id"

    // Attendance Policy Rules
    const val MIN_ATTENDANCE_PERCENTAGE = 75.0
    const val MAX_ALLOWED_EDITS = 1
}