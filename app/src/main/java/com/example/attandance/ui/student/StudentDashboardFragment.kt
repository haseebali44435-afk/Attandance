package com.example.attandance.ui.student

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.attandance.data.model.AttendanceSession
import com.example.attandance.data.model.Resource
import com.example.attandance.data.model.Subject
import com.example.attandance.databinding.FragmentStudentDashboardBinding
import com.example.attandance.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StudentDashboardFragment : Fragment() {

    private var _binding: FragmentStudentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StudentViewModel by activityViewModels()

    private lateinit var adapter: StudentSubjectAdapter
    private var subjectsList: List<Subject> = emptyList()
    private var historyList: List<AttendanceSession> = emptyList()

    private val currentStudentId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        fetchStudentProfileAndData()

        binding.swipeRefreshStudent.setOnRefreshListener {
            fetchStudentProfileAndData()
            binding.swipeRefreshStudent.isRefreshing = false
        }
    }

    private fun setupRecyclerView() {
        adapter = StudentSubjectAdapter { subject ->
            val intent = Intent(requireContext(), StudentSubjectDetailActivity::class.java).apply {
                putExtra(Constants.EXTRA_SUBJECT_ID, subject.id)
                putExtra("subjectName", subject.name)
                putExtra("subjectCode", subject.code)
            }
            startActivity(intent)
        }
        binding.rvStudentSubjects.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStudentSubjects.adapter = adapter
    }

    private fun fetchStudentProfileAndData() {
        if (currentStudentId.isBlank()) return
        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_USERS)
            .document(currentStudentId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("name").orEmpty()
                    val roll = doc.getString("rollNumber").orEmpty()
                    val deptName = doc.getString("departmentName").orEmpty()
                    val deptId = doc.getString("departmentId").orEmpty()
                    val semesterId = doc.getString("semesterId").orEmpty()

                    binding.tvStudentName.text = name
                    binding.tvStudentMeta.text = "Roll No: $roll | Dept: $deptName | Sem: $semesterId"

                    viewModel.loadStudentDashboard(currentStudentId, deptId, semesterId)
                }
            }
    }

    private fun observeViewModel() {
        viewModel.studentSubjectsState.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                subjectsList = resource.data
                adapter.submitList(subjectsList)
                calculateOverallStats()
            }
        }

        viewModel.attendanceHistoryState.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                historyList = resource.data
                calculateOverallStats()
            }
        }
    }

    private fun calculateOverallStats() {
        var totalClasses = 0
        var totalPresent = 0

        for (session in historyList) {
            val myRecord = session.records.firstOrNull { it.studentId == currentStudentId }
            if (myRecord != null) {
                totalClasses++
                if (myRecord.isPresent) totalPresent++
            }
        }

        val overallPercentage = if (totalClasses > 0) (totalPresent.toDouble() / totalClasses) * 100 else 0.0
        binding.tvOverallPercentage.text = String.format("%.1f%%", overallPercentage)
        binding.progressOverallAttendance.progress = overallPercentage.toInt()

        if (totalClasses > 0 && overallPercentage < Constants.MIN_ATTENDANCE_PERCENTAGE) {
            binding.tvAttendanceStatusWarning.visibility = View.VISIBLE
        } else {
            binding.tvAttendanceStatusWarning.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}