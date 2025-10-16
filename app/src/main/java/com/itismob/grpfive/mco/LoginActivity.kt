package com.itismob.grpfive.mco

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import android.widget.Toast
import com.itismob.grpfive.mco.databinding.ActivityLoginBinding

class LoginActivity : ComponentActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val usersList = DataGenerator.sampleUsers()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLogin()
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

    private fun checkLogin(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            showToast("Please fill in both fields.")
            return
        }

        val matchedUser = usersList.firstOrNull {
            it.userEmail == email && it.userHashedPw == password
        }

        if (matchedUser != null) {
            showToast("Welcome, ${matchedUser.storeName}!")

            // Delays the login by a bit to show successful login (this bit is AI-generated)
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("user", matchedUser)
                startActivity(intent)
                finish()
            }, 1500) // 1.5s delay for “Welcome!” message to show
        } else {
            showToast("Invalid email or password.")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}