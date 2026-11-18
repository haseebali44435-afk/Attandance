package com.example.attandance.ui.teacher

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.attandance.R
import com.example.attandance.databinding.ActivityTeacherMainBinding
import com.example.attandance.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class TeacherMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherMainBinding
    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarTeacher)
        setupBottomNavigation()
        setupBackNavigation()

        if (savedInstanceState == null) {
            loadFragment(TeacherDashboardFragment())
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.bottomNavTeacher.selectedItemId != R.id.nav_teacher_dashboard) {
                    binding.bottomNavTeacher.selectedItemId = R.id.nav_teacher_dashboard
                } else {
                    if (backPressedTime + 2000 > System.currentTimeMillis()) {
                        finish()
                    } else {
                        Toast.makeText(this@TeacherMainActivity, "Press back again to exit application", Toast.LENGTH_SHORT).show()
                    }
                    backPressedTime = System.currentTimeMillis()
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar_logout, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_logout) {
            confirmLogout()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out from Teacher Portal?")
            .setPositiveButton("Logout") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavTeacher.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_teacher_dashboard -> TeacherDashboardFragment()
                R.id.nav_teacher_classes -> TeacherDashboardFragment()
                R.id.nav_teacher_sessions -> TeacherDashboardFragment()
                else -> TeacherDashboardFragment()
            }
            loadFragment(fragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.teacherContainer, fragment)
            .commit()
    }
}