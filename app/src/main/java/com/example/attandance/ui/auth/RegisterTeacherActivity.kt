package com.example.attandance.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.attandance.data.model.Department
import com.example.attandance.data.model.Resource
import com.example.attandance.databinding.ActivityRegisterTeacherBinding
import com.example.attandance.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore

class RegisterTeacherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterTeacherBinding
    private val viewModel: AuthViewModel by viewModels()

    private var availableDepts: List<Department> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterTeacherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarRegister.setNavigationOnClickListener { finish() }

        loadDepartments()
        setupClickListeners()
        observeViewModel()
    }

    private fun loadDepartments() {
        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_DEPARTMENTS)
            .get()
            .addOnSuccessListener { snapshot ->
                availableDepts = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Department::class.java)?.copy(id = doc.id)
                }
                val deptNames = availableDepts.map { it.name }
                val deptAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, deptNames)
                binding.actvRegDept.setAdapter(deptAdapter)
            }
    }

    private fun setupClickListeners() {
        binding.btnSubmitRegister.setOnClickListener {
            val name = binding.etRegName.text?.toString()?.trim().orEmpty()
            val email = binding.etRegEmail.text?.toString()?.trim().orEmpty()
            val password = binding.etRegPassword.text?.toString()?.trim().orEmpty()
            val confirmPass = binding.etRegConfirmPassword.text?.toString()?.trim().orEmpty()
            val deptName = binding.actvRegDept.text.toString()

            if (name.isBlank()) {
                binding.etRegName.error = "Name is required"
                return@setOnClickListener
            }
            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etRegEmail.error = "Valid email address is required"
                return@setOnClickListener
            }
            if (deptName.isBlank()) {
                Toast.makeText(this, "Please select a department", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                binding.etRegPassword.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }
            if (password != confirmPass) {
                binding.etRegConfirmPassword.error = "Passwords do not match"
                return@setOnClickListener
            }

            val deptObj = availableDepts.find { it.name == deptName }
            viewModel.registerTeacher(name, email, password, deptObj?.id.orEmpty(), deptName)
        }
    }

    private fun observeViewModel() {
        viewModel.registerState.observe(this) { resource ->
            resource?.let {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressBarReg.visibility = View.VISIBLE
                        binding.btnSubmitRegister.isEnabled = false
                    }
                    is Resource.Success -> {
                        binding.progressBarReg.visibility = View.GONE
                        binding.btnSubmitRegister.isEnabled = true
                        Toast.makeText(this, it.data, Toast.LENGTH_LONG).show()
                        viewModel.resetRegisterState()
                        finish()
                    }
                    is Resource.Error -> {
                        binding.progressBarReg.visibility = View.GONE
                        binding.btnSubmitRegister.isEnabled = true
                        Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                        viewModel.resetRegisterState()
                    }
                }
            }
        }
    }
}