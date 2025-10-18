package com.itismob.grpfive.mco

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.itismob.grpfive.mco.databinding.ActivityRegisterBinding
import java.util.UUID

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    // Temporary list to simulate a database or existing users
    companion object {
        private val usersList = DataGenerator.sampleUsers().toMutableList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()

        binding.btnRegister.setOnClickListener {
            handleRegistration()
        }

        binding.tvLoginLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
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
            profilePic = R.drawable.account_profile, // default for now
            userEmail = email,
            userHashedPw = password // we'll hash later once Firebase/DB is integrated
        )

        usersList.add(newUser)

        Toast.makeText(this, "Registration successful! Please log in.", Toast.LENGTH_LONG).show()

        // Delays the move to login screen by a bit to show successful registration (this bit is AI-generated)
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("newUser", newUser)
            startActivity(intent)
            finish()
        }, 1500) // delay by 1.5s
    }
}
