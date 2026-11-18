package com.example.attandance.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.attandance.data.model.*
import com.example.attandance.data.repository.AdminRepositoryImpl
import com.example.attandance.data.repository.AttendanceRepositoryImpl
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdminViewModel(
    private val adminRepo: AdminRepositoryImpl = AdminRepositoryImpl(),
    private val attendanceRepo: AttendanceRepositoryImpl = AttendanceRepositoryImpl()
) : ViewModel() {

    private val _usersState = MutableLiveData<Resource<List<User>>>()
    val usersState: LiveData<Resource<List<User>>> = _usersState

    private val _deptsState = MutableLiveData<Resource<List<Department>>>()
    val deptsState: LiveData<Resource<List<Department>>> = _deptsState

    private val _subjectsState = MutableLiveData<Resource<List<Subject>>>()
    val subjectsState: LiveData<Resource<List<Subject>>> = _subjectsState

    private val _assignmentsState = MutableLiveData<Resource<List<TeacherAssignment>>>()
    val assignmentsState: LiveData<Resource<List<TeacherAssignment>>> = _assignmentsState

    private val _sessionsState = MutableLiveData<Resource<List<AttendanceSession>>>()
    val sessionsState: LiveData<Resource<List<AttendanceSession>>> = _sessionsState

    private val _actionState = MutableLiveData<Resource<String>?>()
    val actionState: LiveData<Resource<String>?> = _actionState

    init {
        observeUsers()
        observeDepartments()
        observeSubjects()
        observeTeacherAssignments()
        observeTodaySessions()
    }

    private fun observeUsers() {
        viewModelScope.launch {
            adminRepo.getUsersFlow().collectLatest {
                _usersState.value = it
            }
        }
    }

    private fun observeDepartments() {
        viewModelScope.launch {
            adminRepo.getDepartmentsFlow().collectLatest {
                _deptsState.value = it
            }
        }
    }

    private fun observeSubjects() {
        viewModelScope.launch {
            adminRepo.getSubjectsFlow().collectLatest {
                _subjectsState.value = it
            }
        }
    }

    private fun observeTeacherAssignments() {
        viewModelScope.launch {
            adminRepo.getTeacherAssignmentsFlow().collectLatest {
                _assignmentsState.value = it
            }
        }
    }

    private fun observeTodaySessions() {
        viewModelScope.launch {
            attendanceRepo.getTodaySessionsFlow().collectLatest {
                _sessionsState.value = it
            }
        }
    }

    fun createUser(user: User, pass: String) {
        _actionState.value = Resource.Loading
        viewModelScope.launch {
            val res = adminRepo.createUser(user, pass)
            _actionState.value = when (res) {
                is Resource.Success -> Resource.Success("User '${user.name}' created successfully")
                is Resource.Error -> Resource.Error(res.message)
                is Resource.Loading -> Resource.Loading
            }
        }
    }

    fun updateUser(user: User) {
        _actionState.value = Resource.Loading
        viewModelScope.launch {
            val res = adminRepo.updateUser(user)
            _actionState.value = when (res) {
                is Resource.Success -> Resource.Success("User '${user.name}' updated successfully")
                is Resource.Error -> Resource.Error(res.message)
                is Resource.Loading -> Resource.Loading
            }
        }
    }

    fun deleteUser(uid: String) {
        _actionState.value = Resource.Loading
        viewModelScope.launch {
            val res = adminRepo.deleteUser(uid)
            _actionState.value = when (res) {
                is Resource.Success -> Resource.Success("User deleted successfully")
                is Resource.Error -> Resource.Error(res.message)
                is Resource.Loading -> Resource.Loading
            }
        }
    }

    fun addDepartment(dept: Department) {
        _actionState.value = Resource.Loading
        viewModelScope.launch {
            val res = adminRepo.addDepartment(dept)
            _actionState.value = when (res) {
                is Resource.Success -> Resource.Success("Department '${dept.name}' created")
                is Resource.Error -> Resource.Error(res.message)
                is Resource.Loading -> Resource.Loading
            }
        }
    }

    fun updateDepartment(dept: Department) {
        _actionState.value = Resource.Loading
        viewModelScope.launch {
            val res = adminRepo.updateDepartment(dept)
            _actionState.value = when (res) {
                is Resource.Success -> Resource.Success("Department updated")
                is Resource.Error -> Resource.Error(res.message)
                is Resource.Loading -> Resource.Loading
            }
        }
    }

    fun deleteDepartment(deptId: String) {
        _actionState.value = Resource.Loading
        viewModelScope.launch {
            val res = adminRepo.deleteDepartment(deptId)
            _actionState.value = when (res) {
                is Resource.Success -> Resource.Success("Department deleted")
                is Resource.Error -> Resource.Error(res.message)
                is Resource.Loading -> Resource.Loading
            }
        }
    }

    fun addSubject(subject: Subject) {
        _actionState.value = Resource.Loading
        viewModelScope.launch {
            val res = adminRepo.addSubject(subject)
            _actionState.value = when (res) {
                is Resource.Success -> Resource.Success("Subject '${subject.name}' created")
                is Resource.Error -> Resource.Error(res.message)
                is Resource.Loading -> Resource.Loading
            }
        }
    }

    fun updateSubject(subject: Subject) {
        _actionState.value = Resource.Loading
        viewModelScope.launch {
            val res = adminRepo.updateSubject(subject)
            _actionState.value = when (res) {
                is Resource.Success -> Resource.Success("Subject updated")
                is Resource.Error -> Resource.Error(res.message)
                is Resource.Loading -> Resource.Loading
            }
        }
    }

    fun deleteSubject(subjectId: String) {
        _actionState.value = Resource.Loading
        viewModelScope.launch {
            val res = adminRepo.deleteSubject(subjectId)
            _actionState.value = when (res) {
                is Resource.Success -> Resource.Success("Subject deleted")
                is Resource.Error -> Resource.Error(res.message)
                is Resource.Loading -> Resource.Loading
            }
        }
    }

    fun assignTeacher(assignment: TeacherAssignment) {
        _actionState.value = Resource.Loading
        viewModelScope.launch {
            val res = adminRepo.assignTeacher(assignment)
            _actionState.value = when (res) {
                is Resource.Success -> Resource.Success("Teacher assigned successfully")
                is Resource.Error -> Resource.Error(res.message)
                is Resource.Loading -> Resource.Loading
            }
        }
    }

    fun deleteAssignment(assignmentId: String) {
        _actionState.value = Resource.Loading
        viewModelScope.launch {
            val res = adminRepo.deleteAssignment(assignmentId)
            _actionState.value = when (res) {
                is Resource.Success -> Resource.Success("Assignment removed")
                is Resource.Error -> Resource.Error(res.message)
                is Resource.Loading -> Resource.Loading
            }
        }
    }

    fun resetActionState() {
        _actionState.value = null
    }

    fun loadDashboardData() {
        observeUsers()
        observeDepartments()
        observeSubjects()
        observeTeacherAssignments()
        observeTodaySessions()
    }
}