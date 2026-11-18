package com.example.attandance.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.attandance.R
import com.example.attandance.data.model.Resource
import com.example.attandance.data.model.Role
import com.example.attandance.databinding.ActivityLoginBinding
import com.example.attandance.ui.admin.AdminMainActivity
import com.example.attandance.ui.teacher.TeacherMainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    private var selectedRole: Role = Role.TEACHER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRoleToggle()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRoleToggle() {
        binding.toggleRoleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                selectedRole = when (checkedId) {
                    R.id.btnRoleAdmin -> Role.ADMIN
                    else -> Role.TEACHER
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text?.toString()?.trim().orEmpty()
            val password = binding.etPassword.text?.toString()?.trim().orEmpty()
            viewModel.login(email, password, selectedRole)
        }

        binding.btnGoRegisterTeacher.setOnClickListener {
            startActivity(Intent(this, RegisterTeacherActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun showForgotPasswordDialog() {
        val input = EditText(this).apply {
            hint = "Enter registered email address"
            setPadding(50, 40, 50, 40)
        }

        AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setMessage("We will send a password reset link to your email.")
            .setView(input)
            .setPositiveButton("Send Link") { _, _ ->
                val email = input.text.toString().trim()
                viewModel.sendPasswordResetEmail(email)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBarLogin.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                    binding.tvError.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBarLogin.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    val user = resource.data
                    navigateToRoleDashboard(user.role)
                }
                is Resource.Error -> {
                    binding.progressBarLogin.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    binding.tvError.text = resource.message
                    binding.tvError.visibility = View.VISIBLE
                }
            }
        }

        viewModel.resetState.observe(this) { resource ->
            resource?.let {
                when (it) {
                    is Resource.Success -> {
                        Toast.makeText(this, it.data, Toast.LENGTH_LONG).show()
                        viewModel.resetResetState()
                    }
                    is Resource.Error -> {
                        Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                        viewModel.resetResetState()
                    }
                    is Resource.Loading -> { }
                }
            }
        }
    }

    private fun navigateToRoleDashboard(role: Role) {
        val intent = when (role) {
            Role.ADMIN -> Intent(this, AdminMainActivity::class.java)
            Role.TEACHER, Role.STUDENT -> Intent(this, TeacherMainActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}