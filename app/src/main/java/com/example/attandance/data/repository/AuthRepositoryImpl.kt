package com.example.attandance.data.repository

import android.util.Log
import com.example.attandance.data.model.Resource
import com.example.attandance.data.model.Role
import com.example.attandance.data.model.User
import com.example.attandance.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepositoryImpl"
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return User(uid = firebaseUser.uid, email = firebaseUser.email.orEmpty())
    }

    override fun logout() {
        auth.signOut()
    }

    override suspend fun login(email: String, pass: String): Resource<User> {
        return try {
            Log.d(TAG, "Attempting Firebase Auth login for email: $email")
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val firebaseUser = result.user ?: return Resource.Error("Firebase Auth user is null")
            val uid = firebaseUser.uid
            Log.d(TAG, "Auth successful. UID: $uid")

            val profileRes = getUserProfile(uid)
            if (profileRes is Resource.Success) {
                val user = profileRes.data
                if (user.role == Role.TEACHER) {
                    when (user.status) {
                        "PENDING" -> return Resource.Error("Your teacher account is PENDING approval by Administrator.")
                        "REJECTED" -> return Resource.Error("Your teacher account registration was REJECTED by Administrator.")
                    }
                }
                Resource.Success(user)
            } else {
                Log.w(TAG, "User document missing for UID $uid. Provisioning initial user document.")
                val nameFromEmail = email.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
                val initialUser = User(
                    uid = uid,
                    name = nameFromEmail,
                    email = email,
                    role = Role.ADMIN,
                    status = "APPROVED"
                )
                db.collection(Constants.COLLECTION_USERS).document(uid).set(initialUser).await()
                Resource.Success(initialUser)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login failed: ${e.localizedMessage}", e)
            Resource.Error(e.localizedMessage ?: "Authentication failed", e)
        }
    }

    override suspend fun getUserProfile(uid: String): Resource<User> {
        return try {
            Log.d(TAG, "Fetching user profile document for UID: $uid")
            val doc = db.collection(Constants.COLLECTION_USERS).document(uid).get().await()
            if (doc.exists()) {
                val user = doc.toObject(User::class.java)
                if (user != null) {
                    Log.d(TAG, "User profile loaded successfully. Role: ${user.role}, Status: ${user.status}")
                    Resource.Success(user.copy(uid = uid))
                } else {
                    Log.e(TAG, "Failed to parse User object from document: $uid")
                    Resource.Error("Failed to parse user profile")
                }
            } else {
                Log.w(TAG, "No user profile document found at users/$uid")
                Resource.Error("User profile document does not exist")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user profile for $uid: ${e.localizedMessage}", e)
            Resource.Error(e.localizedMessage ?: "Failed to fetch user profile", e)
        }
    }

    override suspend fun registerTeacher(
        name: String,
        email: String,
        pass: String,
        departmentId: String,
        departmentName: String
    ): Resource<Boolean> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid ?: return Resource.Error("Registration failed: User ID is null")

            val teacherUser = User(
                uid = uid,
                name = name,
                email = email,
                role = Role.TEACHER,
                departmentId = departmentId,
                departmentName = departmentName,
                status = "PENDING"
            )

            db.collection(Constants.COLLECTION_USERS).document(uid).set(teacherUser).await()
            // Sign out after registration so user must log in once approved
            auth.signOut()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Teacher registration failed", e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Resource<Boolean> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to send password reset email", e)
        }
    }
}