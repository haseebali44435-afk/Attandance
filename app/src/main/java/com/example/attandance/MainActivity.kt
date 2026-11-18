package com.example.attandance

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.attandance.data.model.Role
import com.example.attandance.data.model.User
import com.example.attandance.databinding.ActivityMainBinding
import com.example.attandance.ui.admin.AdminMainActivity
import com.example.attandance.ui.auth.LoginActivity
import com.example.attandance.ui.teacher.TeacherMainActivity
import com.example.attandance.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthAndRoute()
        }, 1500)
    }

    private fun checkAuthAndRoute() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection(Constants.COLLECTION_USERS)
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val user = doc.toObject(User::class.java)
                        if (user != null) {
                            when (user.role) {
                                Role.ADMIN -> {
                                    startActivity(Intent(this, AdminMainActivity::class.java))
                                    finish()
                                    return@addOnSuccessListener
                                }
                                Role.TEACHER -> {
                                    if (user.status == "APPROVED") {
                                        startActivity(Intent(this, TeacherMainActivity::class.java))
                                        finish()
                                        return@addOnSuccessListener
                                    }
                                }
                                else -> { }
                            }
                        }
                    }
                    // Default fallback if profile missing or pending/rejected
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}