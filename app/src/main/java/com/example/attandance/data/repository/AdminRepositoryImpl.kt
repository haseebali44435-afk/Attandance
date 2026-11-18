package com.example.attandance.data.repository

import com.example.attandance.data.model.*
import com.example.attandance.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AdminRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AdminRepository {

    override fun getUsersFlow(): Flow<Resource<List<User>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = db.collection(Constants.COLLECTION_USERS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error loading users"))
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.copy(uid = doc.id)
                }.orEmpty()
                trySend(Resource.Success(users))
            }
        awaitClose { listener.remove() }
    }

    override fun getDepartmentsFlow(): Flow<Resource<List<Department>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = db.collection(Constants.COLLECTION_DEPARTMENTS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error loading departments"))
                    return@addSnapshotListener
                }
                val depts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Department::class.java)?.copy(id = doc.id)
                }.orEmpty()
                trySend(Resource.Success(depts))
            }
        awaitClose { listener.remove() }
    }

    override fun getSubjectsFlow(): Flow<Resource<List<Subject>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = db.collection(Constants.COLLECTION_SUBJECTS)
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

    override fun getTeacherAssignmentsFlow(): Flow<Resource<List<TeacherAssignment>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = db.collection(Constants.COLLECTION_TEACHER_ASSIGNMENTS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error loading assignments"))
                    return@addSnapshotListener
                }
                val assignments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(TeacherAssignment::class.java)?.copy(id = doc.id)
                }.orEmpty()
                trySend(Resource.Success(assignments))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun createUser(user: User, pass: String): Resource<Boolean> {
        return try {
            if (user.role == Role.STUDENT && user.rollNumber.isNotBlank()) {
                val existingRoll = db.collection(Constants.COLLECTION_USERS)
                    .whereEqualTo("departmentId", user.departmentId)
                    .whereEqualTo("rollNumber", user.rollNumber)
                    .get().await()
                if (!existingRoll.isEmpty) {
                    return Resource.Error("Roll number '${user.rollNumber}' already exists in this department!")
                }
            }

            val docRef = db.collection(Constants.COLLECTION_USERS).document()
            val userWithId = user.copy(uid = docRef.id)
            docRef.set(userWithId).await()

            if (user.role == Role.STUDENT) {
                val student = Student(
                    uid = docRef.id,
                    name = user.name,
                    email = user.email,
                    rollNumber = user.rollNumber,
                    departmentId = user.departmentId,
                    semesterId = user.semesterId
                )
                db.collection(Constants.COLLECTION_STUDENTS).document(docRef.id).set(student).await()
            }
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to create user account", e)
        }
    }

    suspend fun updateUser(user: User): Resource<Boolean> {
        return try {
            db.collection(Constants.COLLECTION_USERS).document(user.uid).set(user).await()
            if (user.role == Role.STUDENT) {
                val student = Student(
                    uid = user.uid,
                    name = user.name,
                    email = user.email,
                    rollNumber = user.rollNumber,
                    departmentId = user.departmentId,
                    semesterId = user.semesterId
                )
                db.collection(Constants.COLLECTION_STUDENTS).document(user.uid).set(student).await()
            }
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update user", e)
        }
    }

    suspend fun deleteUser(uid: String): Resource<Boolean> {
        return try {
            db.collection(Constants.COLLECTION_USERS).document(uid).delete().await()
            db.collection(Constants.COLLECTION_STUDENTS).document(uid).delete().await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete user", e)
        }
    }

    override suspend fun addDepartment(dept: Department): Resource<Boolean> {
        return try {
            val docRef = db.collection(Constants.COLLECTION_DEPARTMENTS).document()
            docRef.set(dept.copy(id = docRef.id)).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to save department", e)
        }
    }

    suspend fun updateDepartment(dept: Department): Resource<Boolean> {
        return try {
            db.collection(Constants.COLLECTION_DEPARTMENTS).document(dept.id).set(dept).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update department", e)
        }
    }

    suspend fun deleteDepartment(deptId: String): Resource<Boolean> {
        return try {
            db.collection(Constants.COLLECTION_DEPARTMENTS).document(deptId).delete().await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete department", e)
        }
    }

    override suspend fun addSubject(subject: Subject): Resource<Boolean> {
        return try {
            val docRef = db.collection(Constants.COLLECTION_SUBJECTS).document()
            docRef.set(subject.copy(id = docRef.id)).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to save subject", e)
        }
    }

    suspend fun updateSubject(subject: Subject): Resource<Boolean> {
        return try {
            db.collection(Constants.COLLECTION_SUBJECTS).document(subject.id).set(subject).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update subject", e)
        }
    }

    suspend fun deleteSubject(subjectId: String): Resource<Boolean> {
        return try {
            db.collection(Constants.COLLECTION_SUBJECTS).document(subjectId).delete().await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete subject", e)
        }
    }

    override suspend fun assignTeacher(assignment: TeacherAssignment): Resource<Boolean> {
        return try {
            val docRef = db.collection(Constants.COLLECTION_TEACHER_ASSIGNMENTS).document()
            docRef.set(assignment.copy(id = docRef.id)).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to assign teacher", e)
        }
    }

    suspend fun deleteAssignment(assignmentId: String): Resource<Boolean> {
        return try {
            db.collection(Constants.COLLECTION_TEACHER_ASSIGNMENTS).document(assignmentId).delete().await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete assignment", e)
        }
    }
}