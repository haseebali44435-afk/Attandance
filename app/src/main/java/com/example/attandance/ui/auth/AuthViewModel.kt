package com.example.attandance.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.attandance.data.model.Resource
import com.example.attandance.data.model.Role
import com.example.attandance.data.model.User
import com.example.attandance.data.repository.AuthRepositoryImpl
import com.example.attandance.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val authRepo: AuthRepositoryImpl = AuthRepositoryImpl(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _loginState = MutableLiveData<Resource<User>>()
    val loginState: LiveData<Resource<User>> = _loginState

    private val _registerState = MutableLiveData<Resource<String>?>()
    val registerState: LiveData<Resource<String>?> = _registerState

    private val _resetState = MutableLiveData<Resource<String>?>()
    val resetState: LiveData<Resource<String>?> = _resetState

    fun getCurrentUser(): User? = authRepo.getCurrentUser()

    fun login(email: String, pass: String, selectedRole: Role) {
        if (email.isBlank() || pass.isBlank()) {
            _loginState.value = Resource.Error("Email and Password cannot be empty")
            return
        }
        _loginState.value = Resource.Loading
        viewModelScope.launch {
            val res = authRepo.login(email, pass)
            if (res is Resource.Success) {
                val user = res.data
                if (user.role != selectedRole) {
                    _loginState.value = Resource.Error("Account role mismatch. Your account is registered as '${user.role.name}'.")
                } else {
                    _loginState.value = res
                }
            } else {
                _loginState.value = res
            }
        }
    }

    fun registerTeacher(name: String, email: String, pass: String, deptId: String, deptName: String) {
        _registerState.value = Resource.Loading
        viewModelScope.launch {
            val res = authRepo.registerTeacher(name, email, pass, deptId, deptName)
            _registerState.value = when (res) {
                is Resource.Success -> Resource.Success("Registration submitted! Your teacher account is PENDING administrator approval.")
                is Resource.Error -> Resource.Error(res.message)
                is Resource.Loading -> Resource.Loading
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _resetState.value = Resource.Error("Please enter a valid email address.")
            return
        }
        _resetState.value = Resource.Loading
        viewModelScope.launch {
            val res = authRepo.sendPasswordResetEmail(email)
            _resetState.value = when (res) {
                is Resource.Success -> Resource.Success("Password reset link sent to $email. Please check your inbox.")
                is Resource.Error -> Resource.Error(res.message)
                is Resource.Loading -> Resource.Loading
            }
        }
    }

    fun resetRegisterState() {
        _registerState.value = null
    }

    fun resetResetState() {
        _resetState.value = null
    }

    fun logout() {
        authRepo.logout()
    }
}