package com.itismob.grpfive.mco

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.itismob.grpfive.mco.databinding.ActivityRegisterBinding
import java.util.UUID

class RegisterActivity : ComponentActivity() {
    private lateinit var binding: ActivityRegisterBinding

    // Temporary list to simulate a database or existing users
    private val usersList = DataGenerator.sampleUsers().toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            handleRegistration()
        }
    }

    private fun handleRegistration() {
        val storeName = binding.tvStoreName.text.toString().trim()
        val email = binding.tvEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Basic input validation
        if (storeName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if user already exists
        if (usersList.any { it.userEmail == email }) {
            Toast.makeText(this, "An account with this email already exists.", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a new user (simulated)
        val newUser = User(
            userID = UUID.randomUUID().toString(),
            storeName = storeName,
            profilePic = 0, // default for now
            userEmail = email,
            userHashedPw = password // we'll hash later once Firebase/DB is integrated
        )

        usersList.add(newUser)

        Toast.makeText(this, "Registration successful! Please log in.", Toast.LENGTH_LONG).show()

        // Go back to login screen
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("newUser", newUser)
        startActivity(intent)
        finish()
    }
}
