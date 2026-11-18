package com.example.attandance.data.repository

import com.example.attandance.data.model.*
import com.example.attandance.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class StudentRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : StudentRepository {

    override fun getStudentSubjectsFlow(departmentId: String, semesterId: String): Flow<Resource<List<Subject>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = db.collection(Constants.COLLECTION_SUBJECTS)
            .whereEqualTo("departmentId", departmentId)
            .whereEqualTo("semesterId", semesterId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error loading subjects"))
                    return@addSnapshotListener
                }
                val subjects = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Subject::class.java)?.copy(id = doc.id)
                }.orEmpty()
                trySend(Resource.Success(subjects))
            }
        awaitClose { listener.remove() }
    }

    override fun getStudentAttendanceHistoryFlow(studentId: String): Flow<Resource<List<AttendanceSession>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = db.collection(Constants.COLLECTION_ATTENDANCE_SESSIONS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error loading student history"))
                    return@addSnapshotListener
                }
                val sessions = snapshot?.documents?.mapNotNull { doc ->
                    val session = doc.toObject(AttendanceSession::class.java)?.copy(id = doc.id)
                    if (session != null && session.records.any { it.studentId == studentId }) {
                        val studentRecord = session.records.filter { it.studentId == studentId }
                        session.copy(records = studentRecord)
                    } else null
                }.orEmpty().sortedByDescending { it.timestamp }
                trySend(Resource.Success(sessions))
            }
        awaitClose { listener.remove() }
    }
}