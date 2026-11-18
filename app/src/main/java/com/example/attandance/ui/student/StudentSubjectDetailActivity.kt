package com.example.attandance.ui.student

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.attandance.data.model.Resource
import com.example.attandance.databinding.ActivityStudentSubjectDetailBinding
import com.example.attandance.utils.Constants
import com.google.firebase.auth.FirebaseAuth

class StudentSubjectDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentSubjectDetailBinding
    private val viewModel: StudentViewModel by viewModels()
    private lateinit var adapter: StudentHistoryAdapter

    private var subjectId: String = ""
    private var subjectName: String = ""
    private var subjectCode: String = ""

    private val currentStudentId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentSubjectDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subjectId = intent.getStringExtra(Constants.EXTRA_SUBJECT_ID).orEmpty()
        subjectName = intent.getStringExtra("subjectName").orEmpty()
        subjectCode = intent.getStringExtra("subjectCode").orEmpty()

        binding.toolbarSubjectDetail.setNavigationOnClickListener { finish() }
        binding.tvDetailSubjectTitle.text = subjectName
        binding.tvDetailSubjectCode.text = "Code: $subjectCode"

        adapter = StudentHistoryAdapter()
        binding.rvSubjectHistory.layoutManager = LinearLayoutManager(this)
        binding.rvSubjectHistory.adapter = adapter

        observeViewModel()
        if (currentStudentId.isNotBlank()) {
            viewModel.loadStudentDashboard(currentStudentId, "", "")
        }
    }

    private fun observeViewModel() {
        viewModel.attendanceHistoryState.observe(this) { resource ->
            if (resource is Resource.Success) {
                val subjectSessions = resource.data.filter { it.subjectId == subjectId || subjectId.isBlank() }
                adapter.submitList(subjectSessions)

                var attended = 0
                val total = subjectSessions.size
                for (session in subjectSessions) {
                    val myRec = session.records.firstOrNull { it.studentId == currentStudentId }
                    if (myRec?.isPresent == true) {
                        attended++
                    }
                }
                val percent = if (total > 0) (attended.toDouble() / total) * 100 else 0.0
                binding.tvDetailAttended.text = "Attended: $attended/$total"
                binding.tvDetailPercent.text = String.format("%.1f%%", percent)
            }
        }
    }
}