package com.example.attandance.ui.teacher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.attandance.data.model.*
import com.example.attandance.data.repository.AttendanceRepositoryImpl
import com.example.attandance.data.repository.TeacherRepositoryImpl
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TeacherViewModel(
    private val teacherRepo: TeacherRepositoryImpl = TeacherRepositoryImpl(),
    private val attendanceRepo: AttendanceRepositoryImpl = AttendanceRepositoryImpl()
) : ViewModel() {

    private val _assignedClassesState = MutableLiveData<Resource<List<TeacherAssignment>>>()
    val assignedClassesState: LiveData<Resource<List<TeacherAssignment>>> = _assignedClassesState

    private val _studentsState = MutableLiveData<Resource<List<Student>>>()
    val studentsState: LiveData<Resource<List<Student>>> = _studentsState

    private val _teacherSessionsState = MutableLiveData<Resource<List<AttendanceSession>>>()
    val teacherSessionsState: LiveData<Resource<List<AttendanceSession>>> = _teacherSessionsState

    private val _submitState = MutableLiveData<Resource<String>?>()
    val submitState: LiveData<Resource<String>?> = _submitState

    private val _sessionDetailsState = MutableLiveData<Resource<AttendanceSession>>()
    val sessionDetailsState: LiveData<Resource<AttendanceSession>> = _sessionDetailsState

    fun loadAssignedClasses(teacherId: String) {
        viewModelScope.launch {
            teacherRepo.getAssignedClassesFlow(teacherId).collectLatest {
                _assignedClassesState.value = it
            }
        }
    }

    fun loadStudentsForClass(departmentId: String, semesterId: String) {
        viewModelScope.launch {
            teacherRepo.getStudentsForClassFlow(departmentId, semesterId).collectLatest {
                _studentsState.value = it
            }
        }
    }

    fun loadTeacherSessions(teacherId: String) {
        viewModelScope.launch {
            teacherRepo.getTeacherSessionsFlow(teacherId).collectLatest {
                _teacherSessionsState.value = it
            }
        }
    }

    fun loadSessionDetails(sessionId: String) {
        viewModelScope.launch {
            attendanceRepo.getSessionDetailsFlow(sessionId).collectLatest {
                _sessionDetailsState.value = it
            }
        }
    }

    fun submitAttendance(session: AttendanceSession) {
        _submitState.value = Resource.Loading
        viewModelScope.launch {
            val res = attendanceRepo.submitAttendanceSession(session)
            _submitState.value = when (res) {
                is Resource.Success -> Resource.Success("Attendance submitted successfully")
                is Resource.Error -> Resource.Error(res.message)
                is Resource.Loading -> Resource.Loading
            }
        }
    }

    fun updateAttendance(session: AttendanceSession) {
        _submitState.value = Resource.Loading
        viewModelScope.launch {
            val res = attendanceRepo.updateAttendanceSession(session)
            _submitState.value = when (res) {
                is Resource.Success -> Resource.Success("Attendance corrected successfully")
                is Resource.Error -> Resource.Error(res.message)
                is Resource.Loading -> Resource.Loading
            }
        }
    }

    fun resetSubmitState() {
        _submitState.value = null
    }
}