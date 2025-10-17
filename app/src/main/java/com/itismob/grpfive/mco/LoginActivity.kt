package com.itismob.grpfive.mco

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.itismob.grpfive.mco.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val usersList = DataGenerator.sampleUsers() // Mock user data (simulates a database for now)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLogin()
    }

    // Called every time the Activity becomes visible (even after RegisterActivity)
    override fun onStart() {
        super.onStart()

        // Check if a new user was passed from RegisterActivity
        val newUser = intent.getSerializableExtra("newUser") as? User
        if (newUser != null) {
            // Add the new user temporarily to the mock list (for demo purposes)
            usersList.add(newUser)
            showToast("Registration successful! You can now log in as ${newUser.storeName}.")
        }
    }

    // Called when user leaves the Activity (e.g., opens Register screen or switches app)
    override fun onPause() {
        super.onPause()

        // Save current email input to SharedPreferences
        // so that the user doesn’t have to retype it later
        val email = binding.etEmail.text.toString().trim()
        val sharedPreferences = getSharedPreferences("LoginPreferences", MODE_PRIVATE)
        sharedPreferences.edit().putString("lastEmail", email).apply()
    }

    // Called when user returns to the Activity (e.g., back from Register)
    override fun onResume() {
        super.onResume()

        // Always clear password for security reasons
        binding.etPassword.setText("")

        // Retrieve and restore last typed email (if any)
        val sharedPreferences = getSharedPreferences("LoginPreferences", MODE_PRIVATE)
        val savedEmail = sharedPreferences.getString("lastEmail", "")
        binding.etEmail.setText(savedEmail)
    }

    // Called when the Activity is destroyed (e.g., app closed, screen finished)
    override fun onDestroy() {
        super.onDestroy()
        // Nothing special here for now, but this is where you’d clean up resources (like listeners or threads)
    }

    private fun setupLogin() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            checkLogin(email, password)
        }

        binding.tvRegisterLink2.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    // Validates login credentials against mock user list
    private fun checkLogin(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            showToast("Please fill in both fields.")
            return
        }

        // Try to find a user with matching email and password
        val matchedUser = usersList.firstOrNull {
            it.userEmail == email && it.userHashedPw == password
        }

        if (matchedUser != null) {
            // Login successful
            showToast("Welcome, ${matchedUser.storeName}!")

            // Delay slightly for UI feedback before redirecting to Dashboard
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("user", matchedUser)
                startActivity(intent)

                // Clear saved email since login succeeded
                val sharedPrefs = getSharedPreferences("LoginPreferences", MODE_PRIVATE)
                sharedPrefs.edit().remove("lastEmail").apply()

                // Close LoginActivity so user can’t go “back” to it
                finish()
            }, 1500)
        } else {
            // Login failed — wrong credentials
            showToast("Invalid email or password.")
        }
    }

    // Helper function to display short Toast messages
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
