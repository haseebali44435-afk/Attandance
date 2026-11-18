package com.example.attandance.ui.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.attandance.R
import com.example.attandance.data.model.Department
import com.example.attandance.data.model.Resource
import com.example.attandance.data.model.Role
import com.example.attandance.data.model.User
import com.example.attandance.databinding.DialogAddUserBinding
import com.example.attandance.databinding.FragmentManageUsersBinding

class ManageUsersFragment : Fragment() {

    private var _binding: FragmentManageUsersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()

    private lateinit var adapter: UserAdapter
    private var allUsers: List<User> = emptyList()
    private var availableDepts: List<Department> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchAndFilter()
        observeViewModel()

        binding.fabAddUser.setOnClickListener {
            showUserDialog(null)
        }
    }

    private fun setupRecyclerView() {
        adapter = UserAdapter(
            onEditClick = { user -> showUserDialog(user) },
            onDeleteClick = { user -> confirmDeleteUser(user) },
            onApproveClick = { user -> approveTeacher(user) },
            onRejectClick = { user -> rejectTeacher(user) }
        )
        binding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsers.adapter = adapter
    }

    private fun setupSearchAndFilter() {
        binding.etSearchUser.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterUsers()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.chipGroupRole.setOnCheckedStateChangeListener { _, _ ->
            filterUsers()
        }
    }

    private fun filterUsers() {
        val query = binding.etSearchUser.text?.toString()?.lowercase()?.trim().orEmpty()
        val filtered = allUsers.filter { user ->
            val matchesQuery = user.name.lowercase().contains(query) ||
                    user.email.lowercase().contains(query) ||
                    user.rollNumber.lowercase().contains(query)

            val matchesRole = when (binding.chipGroupRole.checkedChipId) {
                R.id.chipTeachers -> user.role == Role.TEACHER && user.status == "APPROVED"
                R.id.chipStudents -> user.role == Role.STUDENT
                R.id.chipPending -> user.status == "PENDING"
                else -> user.status != "REJECTED"
            }

            matchesQuery && matchesRole
        }
        adapter.submitList(filtered)
        binding.tvEmptyUsers.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun approveTeacher(user: User) {
        AlertDialog.Builder(requireContext())
            .setTitle("Approve Teacher Account")
            .setMessage("Approve teacher registration for '${user.name}' (${user.email})?")
            .setPositiveButton("Approve") { _, _ ->
                val approvedUser = user.copy(status = "APPROVED")
                viewModel.updateUser(approvedUser)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun rejectTeacher(user: User) {
        AlertDialog.Builder(requireContext())
            .setTitle("Reject Teacher Registration")
            .setMessage("Reject teacher registration for '${user.name}'?")
            .setPositiveButton("Reject") { _, _ ->
                val rejectedUser = user.copy(status = "REJECTED")
                viewModel.updateUser(rejectedUser)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.usersState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> { }
                is Resource.Success -> {
                    allUsers = resource.data
                    filterUsers()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.deptsState.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                availableDepts = resource.data
            }
        }

        viewModel.actionState.observe(viewLifecycleOwner) { resource ->
            resource?.let {
                when (it) {
                    is Resource.Success -> {
                        Toast.makeText(requireContext(), it.data, Toast.LENGTH_SHORT).show()
                        viewModel.resetActionState()
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                        viewModel.resetActionState()
                    }
                    is Resource.Loading -> { }
                }
            }
        }
    }

    private fun showUserDialog(userToEdit: User?) {
        val dialogBinding = DialogAddUserBinding.inflate(layoutInflater)
        val roles = listOf(Role.STUDENT.name, Role.TEACHER.name, Role.ADMIN.name)
        val roleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, roles)
        dialogBinding.actvDlgRole.setAdapter(roleAdapter)

        val deptNames = availableDepts.map { it.name }
        val deptAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, deptNames)
        dialogBinding.actvDlgDept.setAdapter(deptAdapter)

        if (userToEdit != null) {
            dialogBinding.etDlgName.setText(userToEdit.name)
            dialogBinding.etDlgEmail.setText(userToEdit.email)
            dialogBinding.etDlgPassword.setText("******")
            dialogBinding.etDlgPassword.isEnabled = false
            dialogBinding.actvDlgRole.setText(userToEdit.role.name, false)
            dialogBinding.actvDlgDept.setText(userToEdit.departmentName, false)
            dialogBinding.etDlgRollNo.setText(userToEdit.rollNumber)
        } else {
            dialogBinding.actvDlgRole.setText(Role.STUDENT.name, false)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (userToEdit == null) "Create User Account" else "Edit User Profile")
            .setView(dialogBinding.root)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val saveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveBtn.setOnClickListener {
                val name = dialogBinding.etDlgName.text?.toString()?.trim().orEmpty()
                val email = dialogBinding.etDlgEmail.text?.toString()?.trim().orEmpty()
                val password = dialogBinding.etDlgPassword.text?.toString()?.trim().orEmpty()
                val roleStr = dialogBinding.actvDlgRole.text.toString()
                val deptName = dialogBinding.actvDlgDept.text.toString()
                val rollNo = dialogBinding.etDlgRollNo.text?.toString()?.trim().orEmpty()

                if (name.isBlank()) {
                    dialogBinding.etDlgName.error = "Name is required"
                    return@setOnClickListener
                }
                if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    dialogBinding.etDlgEmail.error = "Valid email is required"
                    return@setOnClickListener
                }
                if (userToEdit == null && password.length < 6) {
                    dialogBinding.etDlgPassword.error = "Password must be at least 6 characters"
                    return@setOnClickListener
                }

                val role = Role.fromString(roleStr)
                val deptObj = availableDepts.find { it.name == deptName }

                if (userToEdit == null) {
                    val newUser = User(
                        name = name,
                        email = email,
                        role = role,
                        departmentId = deptObj?.id.orEmpty(),
                        departmentName = deptName,
                        rollNumber = if (role == Role.STUDENT) rollNo else "",
                        status = "APPROVED"
                    )
                    viewModel.createUser(newUser, password)
                } else {
                    val updatedUser = userToEdit.copy(
                        name = name,
                        email = email,
                        role = role,
                        departmentId = deptObj?.id ?: userToEdit.departmentId,
                        departmentName = deptName,
                        rollNumber = if (role == Role.STUDENT) rollNo else ""
                    )
                    viewModel.updateUser(updatedUser)
                }
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun confirmDeleteUser(user: User) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete User")
            .setMessage("Are you sure you want to delete user '${user.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteUser(user.uid)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}