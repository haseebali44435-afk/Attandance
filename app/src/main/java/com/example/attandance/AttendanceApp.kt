package com.example.attandance

import android.app.Application
import com.google.firebase.FirebaseApp

class AttendanceApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}