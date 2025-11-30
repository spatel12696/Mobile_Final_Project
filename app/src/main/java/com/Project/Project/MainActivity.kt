package com.Project.Project

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // If user is already logged in → go to Home
        if (currentUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
        } else {
            // Otherwise → go to Login
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Close MainActivity so users can’t come back with back button
        finish()
    }
}
