package com.example.attandance.ui.student

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.attandance.R
import com.example.attandance.databinding.ActivityStudentMainBinding

class StudentMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()

        if (savedInstanceState == null) {
            loadFragment(StudentDashboardFragment())
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavStudent.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_student_dashboard -> StudentDashboardFragment()
                R.id.nav_student_subjects -> StudentDashboardFragment()
                R.id.nav_student_history -> StudentDashboardFragment()
                else -> StudentDashboardFragment()
            }
            loadFragment(fragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.studentContainer, fragment)
            .commit()
    }
}