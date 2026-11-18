package com.example.attandance.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.attandance.data.model.*
import com.example.attandance.databinding.DialogAddAssignmentBinding
import com.example.attandance.databinding.FragmentManageAssignmentsBinding

class ManageAssignmentsFragment : Fragment() {

    private var _binding: FragmentManageAssignmentsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()

    private lateinit var adapter: AssignmentAdapter
    private var teachers: List<User> = emptyList()
    private var departments: List<Department> = emptyList()
    private var subjects: List<Subject> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageAssignmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        binding.fabAddAssignment.setOnClickListener {
            showAssignmentDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = AssignmentAdapter(
            onDeleteClick = { assignment -> confirmDeleteAssignment(assignment) }
        )
        binding.rvAssignments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAssignments.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.assignmentsState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> { }
                is Resource.Success -> {
                    val list = resource.data
                    adapter.submitList(list)
                    binding.tvEmptyAssignments.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.usersState.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                teachers = resource.data.filter { it.role == Role.TEACHER }
            }
        }

        viewModel.deptsState.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                departments = resource.data
            }
        }

        viewModel.subjectsState.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                subjects = resource.data
            }
        }
    }

    private fun showAssignmentDialog() {
        val dialogBinding = DialogAddAssignmentBinding.inflate(layoutInflater)

        val teacherNames = teachers.map { it.name }
        val teacherAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, teacherNames)
        dialogBinding.actvDlgAssignTeacher.setAdapter(teacherAdapter)

        val deptNames = departments.map { it.name }
        val deptAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, deptNames)
        dialogBinding.actvDlgAssignDept.setAdapter(deptAdapter)

        dialogBinding.actvDlgAssignDept.setOnItemClickListener { _, _, _, _ ->
            val selDeptName = dialogBinding.actvDlgAssignDept.text.toString()
            val deptObj = departments.find { it.name == selDeptName }
            val filteredSubjects = subjects.filter { it.departmentId == deptObj?.id }
            val subNames = filteredSubjects.map { "${it.name} (${it.code})" }
            val subAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, subNames)
            dialogBinding.actvDlgAssignSubject.setAdapter(subAdapter)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Assign Teacher to Subject")
            .setView(dialogBinding.root)
            .setPositiveButton("Assign", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val assignBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            assignBtn.setOnClickListener {
                val teacherName = dialogBinding.actvDlgAssignTeacher.text.toString()
                val deptName = dialogBinding.actvDlgAssignDept.text.toString()
                val subSelected = dialogBinding.actvDlgAssignSubject.text.toString()

                val teacherObj = teachers.find { it.name == teacherName }
                val deptObj = departments.find { it.name == deptName }
                val subObj = subjects.find { subSelected.contains(it.name) }

                if (teacherObj == null) {
                    Toast.makeText(requireContext(), "Please select a teacher", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (subObj == null) {
                    Toast.makeText(requireContext(), "Please select a subject", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val assignment = TeacherAssignment(
                    teacherId = teacherObj.uid,
                    teacherName = teacherObj.name,
                    departmentId = deptObj?.id.orEmpty(),
                    departmentName = deptName,
                    semesterId = subObj.semesterId,
                    subjectId = subObj.id,
                    subjectName = subObj.name,
                    subjectCode = subObj.code
                )
                viewModel.assignTeacher(assignment)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun confirmDeleteAssignment(assignment: TeacherAssignment) {
        AlertDialog.Builder(requireContext())
            .setTitle("Remove Assignment")
            .setMessage("Are you sure you want to unassign Professor '${assignment.teacherName}' from subject '${assignment.subjectName}'?")
            .setPositiveButton("Remove") { _, _ ->
                viewModel.deleteAssignment(assignment.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}