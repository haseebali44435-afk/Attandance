package com.example.attandance.data.repository

import com.example.attandance.data.model.*
import com.example.attandance.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AttendanceRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AttendanceRepository {

    override suspend fun submitAttendanceSession(session: AttendanceSession): Resource<Boolean> {
        return try {
            val docRef = db.collection(Constants.COLLECTION_ATTENDANCE_SESSIONS).document()
            val finalSession = session.copy(id = docRef.id)
            
            db.runTransaction { transaction ->
                transaction.set(docRef, finalSession)
                
                // Increment subject total classes held
                val subjectRef = db.collection(Constants.COLLECTION_SUBJECTS).document(session.subjectId)
                val subjectDoc = transaction.get(subjectRef)
                if (subjectDoc.exists()) {
                    val currentHeld = subjectDoc.getLong("totalClassesHeld") ?: 0L
                    transaction.update(subjectRef, "totalClassesHeld", currentHeld + 1)
                }
            }.await()
            
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to submit attendance session", e)
        }
    }

    override suspend fun updateAttendanceSession(session: AttendanceSession): Resource<Boolean> {
        return try {
            val sessionRef = db.collection(Constants.COLLECTION_ATTENDANCE_SESSIONS).document(session.id)
            val existingDoc = sessionRef.get().await()
            if (!existingDoc.exists()) {
                return Resource.Error("Attendance session does not exist")
            }
            
            val currentEditCount = existingDoc.getLong("editCount") ?: 0L
            if (currentEditCount >= Constants.MAX_ALLOWED_EDITS) {
                return Resource.Error("Policy violation: Attendance session has already been edited 1 time!")
            }

            val updatedSession = session.copy(
                editCount = (currentEditCount + 1).toInt(),
                lastEditedAt = System.currentTimeMillis()
            )
            sessionRef.set(updatedSession).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update attendance session", e)
        }
    }

    override fun getTodaySessionsFlow(): Flow<Resource<List<AttendanceSession>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = db.collection(Constants.COLLECTION_ATTENDANCE_SESSIONS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error loading sessions"))
                    return@addSnapshotListener
                }
                val sessions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(AttendanceSession::class.java)?.copy(id = doc.id)
                }.orEmpty().sortedByDescending { it.timestamp }
                trySend(Resource.Success(sessions))
            }
        awaitClose { listener.remove() }
    }

    override fun getSessionDetailsFlow(sessionId: String): Flow<Resource<AttendanceSession>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = db.collection(Constants.COLLECTION_ATTENDANCE_SESSIONS)
            .document(sessionId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error loading session details"))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val session = snapshot.toObject(AttendanceSession::class.java)?.copy(id = snapshot.id)
                    if (session != null) {
                        trySend(Resource.Success(session))
                    } else {
                        trySend(Resource.Error("Parse error"))
                    }
                } else {
                    trySend(Resource.Error("Session not found"))
                }
            }
        awaitClose { listener.remove() }
    }
}