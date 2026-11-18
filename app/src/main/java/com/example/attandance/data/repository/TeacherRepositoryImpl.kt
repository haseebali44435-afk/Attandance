package com.example.attandance.data.repository

import com.example.attandance.data.model.*
import com.example.attandance.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class TeacherRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : TeacherRepository {

    override fun getAssignedClassesFlow(teacherId: String): Flow<Resource<List<TeacherAssignment>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = db.collection(Constants.COLLECTION_TEACHER_ASSIGNMENTS)
            .whereEqualTo("teacherId", teacherId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error fetching assigned classes"))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(TeacherAssignment::class.java)?.copy(id = doc.id)
                }.orEmpty()
                trySend(Resource.Success(list))
            }
        awaitClose { listener.remove() }
    }

    override fun getStudentsForClassFlow(departmentId: String, semesterId: String): Flow<Resource<List<Student>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = db.collection(Constants.COLLECTION_STUDENTS)
            .whereEqualTo("departmentId", departmentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error fetching students"))
                    return@addSnapshotListener
                }
                val students = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Student::class.java)?.copy(uid = doc.id)
                }?.filter { it.semesterId == semesterId || semesterId.isBlank() }.orEmpty()
                trySend(Resource.Success(students))
            }
        awaitClose { listener.remove() }
    }

    override fun getTeacherSessionsFlow(teacherId: String): Flow<Resource<List<AttendanceSession>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = db.collection(Constants.COLLECTION_ATTENDANCE_SESSIONS)
            .whereEqualTo("teacherId", teacherId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error fetching sessions"))
                    return@addSnapshotListener
                }
                val sessions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(AttendanceSession::class.java)?.copy(id = doc.id)
                }.orEmpty().sortedByDescending { it.timestamp }
                trySend(Resource.Success(sessions))
            }
        awaitClose { listener.remove() }
    }
}