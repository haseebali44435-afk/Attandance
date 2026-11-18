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
import com.example.attandance.databinding.ActivityEditAttendanceBinding
import com.example.attandance.utils.Constants

class EditAttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditAttendanceBinding
    private val viewModel: TeacherViewModel by viewModels()

    private lateinit var adapter: MarkAttendanceAdapter
    private var currentSession: AttendanceSession? = null
    private var records: MutableList<AttendanceRecord> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarEditAttendance.setNavigationOnClickListener { finish() }
        setupRecyclerView()
        observeViewModel()

        val sessionId = intent.getStringExtra(Constants.EXTRA_SESSION_ID).orEmpty()
        if (sessionId.isNotBlank()) {
            viewModel.loadSessionDetails(sessionId)
        }

        binding.btnUpdateAttendance.setOnClickListener {
            val session = currentSession ?: return@setOnClickListener
            if (session.editCount >= Constants.MAX_ALLOWED_EDITS) {
                Toast.makeText(this, "Policy Error: Session has already been edited 1 time!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val presentCount = records.count { it.isPresent }
            val absentCount = records.count { !it.isPresent }

            val updatedSession = session.copy(
                presentCount = presentCount,
                absentCount = absentCount,
                records = records.toList()
            )
            viewModel.updateAttendance(updatedSession)
        }
    }

    private fun setupRecyclerView() {
        adapter = MarkAttendanceAdapter(records) { index, isPresent ->
            if (index in records.indices) {
                records[index] = records[index].copy(isPresent = isPresent)
            }
        }
        binding.rvEditStudents.layoutManager = LinearLayoutManager(this)
        binding.rvEditStudents.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.sessionDetailsState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> { }
                is Resource.Success -> {
                    val session = resource.data
                    currentSession = session
                    records = session.records.toMutableList()
                    adapter.submitList(records.toList())

                    if (session.editCount >= Constants.MAX_ALLOWED_EDITS) {
                        binding.tvEditWarning.text = "POLICY LOCKED: This session has reached the 1-time edit limit."
                        binding.btnUpdateAttendance.isEnabled = false
                    }
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
                        binding.btnUpdateAttendance.isEnabled = false
                    }
                    is Resource.Success -> {
                        binding.btnUpdateAttendance.isEnabled = true
                        Toast.makeText(this, it.data, Toast.LENGTH_SHORT).show()
                        viewModel.resetSubmitState()
                        finish()
                    }
                    is Resource.Error -> {
                        binding.btnUpdateAttendance.isEnabled = true
                        Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                        viewModel.resetSubmitState()
                    }
                }
            }
        }
    }
}