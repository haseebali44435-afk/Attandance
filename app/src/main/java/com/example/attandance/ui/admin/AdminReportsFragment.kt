package com.example.attandance.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.attandance.data.model.*
import com.example.attandance.databinding.FragmentAdminReportsBinding

class AdminReportsFragment : Fragment() {

    private var _binding: FragmentAdminReportsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()

    private var departments: List<Department> = emptyList()
    private var subjects: List<Subject> = emptyList()
    private var sessions: List<AttendanceSession> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvReportStudents.layoutManager = LinearLayoutManager(requireContext())
        observeViewModel()

        binding.btnGenerateReport.setOnClickListener {
            generateReport()
        }

        binding.btnExportReport.setOnClickListener {
            exportReportCsv()
        }
    }

    private fun observeViewModel() {
        viewModel.deptsState.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                departments = resource.data
                val deptNames = departments.map { it.name }
                val deptAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, deptNames)
                binding.actvReportDept.setAdapter(deptAdapter)
            }
        }

        viewModel.subjectsState.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                subjects = resource.data
                val subNames = subjects.map { "${it.name} (${it.code})" }
                val subAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, subNames)
                binding.actvReportSubject.setAdapter(subAdapter)
            }
        }

        viewModel.sessionsState.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                sessions = resource.data
            }
        }
    }

    private fun generateReport() {
        val selectedDeptName = binding.actvReportDept.text.toString()
        val selectedSubName = binding.actvReportSubject.text.toString()

        val deptObj = departments.find { it.name == selectedDeptName }
        val subObj = subjects.find { selectedSubName.contains(it.name) }

        val filteredSessions = sessions.filter { session ->
            val matchesDept = deptObj == null || session.departmentId == deptObj.id
            val matchesSub = subObj == null || session.subjectId == subObj.id
            matchesDept && matchesSub
        }

        if (filteredSessions.isEmpty()) {
            Toast.makeText(requireContext(), "No attendance records found for selection", Toast.LENGTH_SHORT).show()
            binding.cardReportSummary.visibility = View.GONE
            return
        }

        var totalRecords = 0
        var totalPresent = 0
        val studentStatsMap = mutableMapOf<String, Pair<String, Pair<Int, Int>>>() // studentId -> (Name, (Present, Total))

        for (session in filteredSessions) {
            for (rec in session.records) {
                totalRecords++
                if (rec.isPresent) totalPresent++

                val current = studentStatsMap[rec.studentId]
                val currentPresent = (current?.second?.first ?: 0) + (if (rec.isPresent) 1 else 0)
                val currentTotal = (current?.second?.second ?: 0) + 1
                studentStatsMap[rec.studentId] = Pair(rec.studentName, Pair(currentPresent, currentTotal))
            }
        }

        val overallPercentage = if (totalRecords > 0) (totalPresent.toDouble() / totalRecords) * 100 else 0.0
        binding.tvReportAverage.text = String.format("%.1f%%", overallPercentage)
        binding.cardReportSummary.visibility = View.VISIBLE
    }

    private fun exportReportCsv() {
        val selectedDeptName = binding.actvReportDept.text.toString()
        val selectedSubName = binding.actvReportSubject.text.toString()
        val avgText = binding.tvReportAverage.text.toString()

        val csvContent = StringBuilder()
        csvContent.append("College Attendance Management System - Report\n")
        csvContent.append("Department: $selectedDeptName\n")
        csvContent.append("Subject: $selectedSubName\n")
        csvContent.append("Overall Average Attendance: $avgText\n\n")

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, csvContent.toString())
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Export Attendance Report")
        startActivity(shareIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}