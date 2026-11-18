package com.example.attandance.ui.student

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.attandance.data.model.AttendanceSession
import com.example.attandance.data.model.Resource
import com.example.attandance.data.model.Subject
import com.example.attandance.data.repository.StudentRepositoryImpl
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StudentViewModel(
    private val studentRepo: StudentRepositoryImpl = StudentRepositoryImpl()
) : ViewModel() {

    private val _studentSubjectsState = MutableLiveData<Resource<List<Subject>>>()
    val studentSubjectsState: LiveData<Resource<List<Subject>>> = _studentSubjectsState

    private val _attendanceHistoryState = MutableLiveData<Resource<List<AttendanceSession>>>()
    val attendanceHistoryState: LiveData<Resource<List<AttendanceSession>>> = _attendanceHistoryState

    fun loadStudentDashboard(studentId: String, deptId: String, semesterId: String) {
        viewModelScope.launch {
            studentRepo.getStudentSubjectsFlow(deptId, semesterId).collectLatest {
                _studentSubjectsState.value = it
            }
        }
        viewModelScope.launch {
            studentRepo.getStudentAttendanceHistoryFlow(studentId).collectLatest {
                _attendanceHistoryState.value = it
            }
        }
    }
}