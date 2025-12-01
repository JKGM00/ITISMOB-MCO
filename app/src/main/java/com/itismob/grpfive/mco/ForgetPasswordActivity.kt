package com.itismob.grpfive.mco

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.itismob.grpfive.mco.databinding.ActivityForgetPasswordBinding

class ForgetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgetPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Handle Reset Button Click
        binding.btnResetPassword.setOnClickListener {
            val email = binding.etResetEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendPasswordReset(email)
        }

        // Handle Back to Login Click
        binding.tvBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun sendPasswordReset(email: String) {
        binding.btnResetPassword.isEnabled = false

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                binding.btnResetPassword.isEnabled = true
                binding.btnResetPassword.text = "Send Reset Link"

                if (task.isSuccessful) {
                    Toast.makeText(this, "Reset link sent to your email!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val errorMessage = task.exception?.message ?: "An error occurred."
                    Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
    }
}
