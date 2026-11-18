package com.example.attandance.ui.teacher

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.attandance.data.model.Resource
import com.example.attandance.databinding.FragmentTeacherDashboardBinding
import com.example.attandance.utils.Constants
import com.google.firebase.auth.FirebaseAuth

class TeacherDashboardFragment : Fragment() {

    private var _binding: FragmentTeacherDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TeacherViewModel by activityViewModels()

    private lateinit var classAdapter: TeacherClassAdapter
    private lateinit var sessionAdapter: SessionAdapter
    private val currentTeacherId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeacherDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        observeViewModel()

        if (currentTeacherId.isNotBlank()) {
            viewModel.loadAssignedClasses(currentTeacherId)
            viewModel.loadTeacherSessions(currentTeacherId)
        }

        binding.swipeRefreshTeacher.setOnRefreshListener {
            if (currentTeacherId.isNotBlank()) {
                viewModel.loadAssignedClasses(currentTeacherId)
                viewModel.loadTeacherSessions(currentTeacherId)
            }
            binding.swipeRefreshTeacher.isRefreshing = false
        }
    }

    private fun setupRecyclerViews() {
        classAdapter = TeacherClassAdapter { assignment ->
            val intent = Intent(requireContext(), MarkAttendanceActivity::class.java).apply {
                putExtra("teacherId", assignment.teacherId)
                putExtra("teacherName", assignment.teacherName)
                putExtra("departmentId", assignment.departmentId)
                putExtra("departmentName", assignment.departmentName)
                putExtra("semesterId", assignment.semesterId)
                putExtra("subjectId", assignment.subjectId)
                putExtra("subjectName", assignment.subjectName)
                putExtra("subjectCode", assignment.subjectCode)
            }
            startActivity(intent)
        }
        binding.rvAssignedClasses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAssignedClasses.adapter = classAdapter

        sessionAdapter = SessionAdapter { session ->
            val intent = Intent(requireContext(), EditAttendanceActivity::class.java).apply {
                putExtra(Constants.EXTRA_SESSION_ID, session.id)
            }
            startActivity(intent)
        }
        binding.rvTeacherSessions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTeacherSessions.adapter = sessionAdapter
    }

    private fun observeViewModel() {
        viewModel.assignedClassesState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> { }
                is Resource.Success -> {
                    val list = resource.data
                    classAdapter.submitList(list)
                    binding.tvEmptyTeacherClasses.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.teacherSessionsState.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                sessionAdapter.submitList(resource.data)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}