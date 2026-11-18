package com.example.attandance.ui.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.attandance.data.model.Department
import com.example.attandance.data.model.Resource
import com.example.attandance.databinding.DialogAddDepartmentBinding
import com.example.attandance.databinding.FragmentManageDepartmentsBinding

class ManageDepartmentsFragment : Fragment() {

    private var _binding: FragmentManageDepartmentsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()

    private lateinit var adapter: DepartmentAdapter
    private var allDepts: List<Department> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageDepartmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        observeViewModel()

        binding.fabAddDept.setOnClickListener {
            showDepartmentDialog(null)
        }
    }

    private fun setupRecyclerView() {
        adapter = DepartmentAdapter(
            onEditClick = { dept -> showDepartmentDialog(dept) },
            onDeleteClick = { dept -> confirmDeleteDept(dept) }
        )
        binding.rvDepartments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDepartments.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearchDept.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterDepts()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterDepts() {
        val query = binding.etSearchDept.text?.toString()?.lowercase()?.trim().orEmpty()
        val filtered = allDepts.filter { dept ->
            dept.name.lowercase().contains(query) || dept.code.lowercase().contains(query)
        }
        adapter.submitList(filtered)
        binding.tvEmptyDepts.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun observeViewModel() {
        viewModel.deptsState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> { }
                is Resource.Success -> {
                    allDepts = resource.data
                    filterDepts()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDepartmentDialog(deptToEdit: Department?) {
        val dialogBinding = DialogAddDepartmentBinding.inflate(layoutInflater)

        if (deptToEdit != null) {
            dialogBinding.etDlgDeptName.setText(deptToEdit.name)
            dialogBinding.etDlgDeptCode.setText(deptToEdit.code)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (deptToEdit == null) "Add Department" else "Edit Department")
            .setView(dialogBinding.root)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val saveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveBtn.setOnClickListener {
                val name = dialogBinding.etDlgDeptName.text?.toString()?.trim().orEmpty()
                val code = dialogBinding.etDlgDeptCode.text?.toString()?.trim().orEmpty()

                if (name.isBlank()) {
                    dialogBinding.etDlgDeptName.error = "Department name is required"
                    return@setOnClickListener
                }
                if (code.isBlank()) {
                    dialogBinding.etDlgDeptCode.error = "Department code is required"
                    return@setOnClickListener
                }

                if (deptToEdit == null) {
                    viewModel.addDepartment(Department(name = name, code = code))
                } else {
                    viewModel.updateDepartment(deptToEdit.copy(name = name, code = code))
                }
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun confirmDeleteDept(dept: Department) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Department")
            .setMessage("Are you sure you want to delete '${dept.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteDepartment(dept.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}