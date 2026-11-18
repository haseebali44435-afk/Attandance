package com.example.attandance.ui.admin

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
import com.example.attandance.databinding.ActivityAdminMainBinding
import com.example.attandance.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class AdminMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminMainBinding
    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarAdmin)
        setupBottomNavigation()
        setupBackNavigation()

        if (savedInstanceState == null) {
            loadFragment(AdminDashboardFragment())
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.bottomNavAdmin.selectedItemId != R.id.nav_admin_dashboard) {
                    // Navigate back to Dashboard tab first
                    binding.bottomNavAdmin.selectedItemId = R.id.nav_admin_dashboard
                } else {
                    // Double tap back to exit application
                    if (backPressedTime + 2000 > System.currentTimeMillis()) {
                        finish()
                    } else {
                        Toast.makeText(this@AdminMainActivity, "Press back again to exit application", Toast.LENGTH_SHORT).show()
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
            .setMessage("Are you sure you want to log out from Admin Console?")
            .setPositiveButton("Logout") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavAdmin.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_admin_dashboard -> AdminDashboardFragment()
                R.id.nav_manage_users -> ManageUsersFragment()
                R.id.nav_manage_departments -> ManageDepartmentsFragment()
                R.id.nav_manage_subjects -> ManageSubjectsFragment()
                R.id.nav_admin_reports -> AdminReportsFragment()
                else -> AdminDashboardFragment()
            }
            loadFragment(fragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.adminContainer, fragment)
            .commit()
    }
}