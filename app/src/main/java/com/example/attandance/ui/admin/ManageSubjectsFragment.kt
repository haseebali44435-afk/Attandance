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
import com.example.attandance.data.model.Department
import com.example.attandance.data.model.Resource
import com.example.attandance.data.model.Subject
import com.example.attandance.databinding.DialogAddSubjectBinding
import com.example.attandance.databinding.FragmentManageSubjectsBinding

class ManageSubjectsFragment : Fragment() {

    private var _binding: FragmentManageSubjectsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()

    private lateinit var adapter: SubjectAdapter
    private var allSubjects: List<Subject> = emptyList()
    private var availableDepts: List<Department> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageSubjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilters()
        observeViewModel()

        binding.fabAddSubject.setOnClickListener {
            showSubjectDialog(null)
        }
    }

    private fun setupRecyclerView() {
        adapter = SubjectAdapter(
            onEditClick = { subject -> showSubjectDialog(subject) },
            onDeleteClick = { subject -> confirmDeleteSubject(subject) }
        )
        binding.rvSubjects.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSubjects.adapter = adapter
    }

    private fun setupFilters() {
        val semesters = listOf("All Semesters", "1st Semester", "2nd Semester", "3rd Semester", "4th Semester", "5th Semester", "6th Semester", "7th Semester", "8th Semester")
        val semAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, semesters)
        binding.actvFilterSemester.setAdapter(semAdapter)

        binding.actvFilterDept.setOnItemClickListener { _, _, _, _ -> filterSubjects() }
        binding.actvFilterSemester.setOnItemClickListener { _, _, _, _ -> filterSubjects() }
    }

    private fun filterSubjects() {
        val selectedDept = binding.actvFilterDept.text.toString()
        val selectedSem = binding.actvFilterSemester.text.toString()

        val filtered = allSubjects.filter { subject ->
            val deptObj = availableDepts.find { it.id == subject.departmentId }
            val matchesDept = selectedDept.isBlank() || selectedDept == "All Departments" || deptObj?.name == selectedDept
            val matchesSem = selectedSem.isBlank() || selectedSem == "All Semesters" || subject.semesterId == selectedSem
            matchesDept && matchesSem
        }
        adapter.submitList(filtered)
        binding.tvEmptySubjects.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun observeViewModel() {
        viewModel.subjectsState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> { }
                is Resource.Success -> {
                    allSubjects = resource.data
                    filterSubjects()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.deptsState.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                availableDepts = resource.data
                val deptNames = mutableListOf("All Departments")
                deptNames.addAll(availableDepts.map { it.name })
                val deptAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, deptNames)
                binding.actvFilterDept.setAdapter(deptAdapter)
            }
        }
    }

    private fun showSubjectDialog(subjectToEdit: Subject?) {
        val dialogBinding = DialogAddSubjectBinding.inflate(layoutInflater)

        val deptNames = availableDepts.map { it.name }
        val deptAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, deptNames)
        dialogBinding.actvDlgSubDept.setAdapter(deptAdapter)

        val semesters = listOf("1st Semester", "2nd Semester", "3rd Semester", "4th Semester", "5th Semester", "6th Semester", "7th Semester", "8th Semester")
        val semAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, semesters)
        dialogBinding.actvDlgSubSemester.setAdapter(semAdapter)

        if (subjectToEdit != null) {
            dialogBinding.etDlgSubjectName.setText(subjectToEdit.name)
            dialogBinding.etDlgSubjectCode.setText(subjectToEdit.code)
            val deptObj = availableDepts.find { it.id == subjectToEdit.departmentId }
            dialogBinding.actvDlgSubDept.setText(deptObj?.name.orEmpty(), false)
            dialogBinding.actvDlgSubSemester.setText(subjectToEdit.semesterId, false)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (subjectToEdit == null) "Add Subject" else "Edit Subject")
            .setView(dialogBinding.root)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val saveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveBtn.setOnClickListener {
                val name = dialogBinding.etDlgSubjectName.text?.toString()?.trim().orEmpty()
                val code = dialogBinding.etDlgSubjectCode.text?.toString()?.trim().orEmpty()
                val deptName = dialogBinding.actvDlgSubDept.text.toString()
                val semId = dialogBinding.actvDlgSubSemester.text.toString()

                if (name.isBlank()) {
                    dialogBinding.etDlgSubjectName.error = "Subject name is required"
                    return@setOnClickListener
                }
                if (code.isBlank()) {
                    dialogBinding.etDlgSubjectCode.error = "Subject code is required"
                    return@setOnClickListener
                }

                val deptObj = availableDepts.find { it.name == deptName }

                if (subjectToEdit == null) {
                    val newSubject = Subject(
                        name = name,
                        code = code,
                        departmentId = deptObj?.id.orEmpty(),
                        semesterId = semId
                    )
                    viewModel.addSubject(newSubject)
                } else {
                    val updatedSubject = subjectToEdit.copy(
                        name = name,
                        code = code,
                        departmentId = deptObj?.id ?: subjectToEdit.departmentId,
                        semesterId = semId
                    )
                    viewModel.updateSubject(updatedSubject)
                }
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun confirmDeleteSubject(subject: Subject) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Subject")
            .setMessage("Are you sure you want to delete '${subject.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteSubject(subject.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}