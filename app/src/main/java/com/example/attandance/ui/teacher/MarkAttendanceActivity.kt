package com.example.attandance.ui.teacher

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.attandance.data.model.AttendanceRecord
import com.example.attandance.data.model.AttendanceSession
import com.example.attandance.data.model.Resource
import com.example.attandance.databinding.ActivityMarkAttendanceBinding
import com.example.attandance.utils.DateUtils

class MarkAttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMarkAttendanceBinding
    private val viewModel: TeacherViewModel by viewModels()

    private lateinit var adapter: MarkAttendanceAdapter
    private var records: MutableList<AttendanceRecord> = mutableListOf()

    private var teacherId: String = ""
    private var teacherName: String = ""
    private var departmentId: String = ""
    private var departmentName: String = ""
    private var semesterId: String = ""
    private var subjectId: String = ""
    private var subjectName: String = ""
    private var subjectCode: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarkAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        extractIntentExtras()
        setupHeaderUI()
        setupRecyclerView()
        setupQuickMarkButtons()
        observeViewModel()

        if (departmentId.isNotBlank()) {
            viewModel.loadStudentsForClass(departmentId, semesterId)
        }
    }

    private fun extractIntentExtras() {
        teacherId = intent.getStringExtra("teacherId").orEmpty()
        teacherName = intent.getStringExtra("teacherName").orEmpty()
        departmentId = intent.getStringExtra("departmentId").orEmpty()
        departmentName = intent.getStringExtra("departmentName").orEmpty()
        semesterId = intent.getStringExtra("semesterId").orEmpty()
        subjectId = intent.getStringExtra("subjectId").orEmpty()
        subjectName = intent.getStringExtra("subjectName").orEmpty()
        subjectCode = intent.getStringExtra("subjectCode").orEmpty()
    }

    private fun setupHeaderUI() {
        binding.toolbarMarkAttendance.setNavigationOnClickListener { finish() }
        binding.tvMarkSubjectTitle.text = "$subjectName ($subjectCode)"
        binding.tvMarkDetails.text = "$departmentName | Semester: $semesterId | ${DateUtils.getCurrentDate()}"
    }

    private fun setupRecyclerView() {
        adapter = MarkAttendanceAdapter(records) { index, isPresent ->
            if (index in records.indices) {
                records[index] = records[index].copy(isPresent = isPresent)
                updateCounters()
            }
        }
        binding.rvMarkStudents.layoutManager = LinearLayoutManager(this)
        binding.rvMarkStudents.adapter = adapter
    }

    private fun setupQuickMarkButtons() {
        binding.btnMarkAllPresent.setOnClickListener {
            for (i in records.indices) {
                records[i] = records[i].copy(isPresent = true)
            }
            adapter.submitList(records.toList())
            updateCounters()
        }

        binding.btnMarkAllAbsent.setOnClickListener {
            for (i in records.indices) {
                records[i] = records[i].copy(isPresent = false)
            }
            adapter.submitList(records.toList())
            updateCounters()
        }

        binding.btnSubmitAttendance.setOnClickListener {
            if (records.isEmpty()) {
                Toast.makeText(this, "No students in this class to mark attendance", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val presentCount = records.count { it.isPresent }
            val absentCount = records.count { !it.isPresent }

            val session = AttendanceSession(
                teacherId = teacherId,
                teacherName = teacherName,
                departmentId = departmentId,
                departmentName = departmentName,
                semesterId = semesterId,
                subjectId = subjectId,
                subjectName = subjectName,
                subjectCode = subjectCode,
                date = DateUtils.getCurrentDate(),
                timestamp = System.currentTimeMillis(),
                totalStudents = records.size,
                presentCount = presentCount,
                absentCount = absentCount,
                records = records.toList()
            )
            viewModel.submitAttendance(session)
        }
    }

    private fun updateCounters() {
        val total = records.size
        val present = records.count { it.isPresent }
        val absent = total - present

        binding.tvTotalCount.text = "Total: $total"
        binding.tvPresentCount.text = "Present: $present"
        binding.tvAbsentCount.text = "Absent: $absent"
    }

    private fun observeViewModel() {
        viewModel.studentsState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> { }
                is Resource.Success -> {
                    records = resource.data.map { student ->
                        AttendanceRecord(
                            studentId = student.uid,
                            studentName = student.name,
                            rollNumber = student.rollNumber,
                            isPresent = true
                        )
                    }.toMutableList()
                    adapter.submitList(records.toList())
                    updateCounters()
                }
                is Resource.Error -> {
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.submitState.observe(this) { resource ->
            resource?.let {
                when (it) {
                    is Resource.Loading -> {
                        binding.btnSubmitAttendance.isEnabled = false
                    }
                    is Resource.Success -> {
                        binding.btnSubmitAttendance.isEnabled = true
                        Toast.makeText(this, it.data, Toast.LENGTH_SHORT).show()
                        viewModel.resetSubmitState()
                        finish()
                    }
                    is Resource.Error -> {
                        binding.btnSubmitAttendance.isEnabled = true
                        Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                        viewModel.resetSubmitState()
                    }
                }
            }
        }
    }
}