package com.itismob.grpfive.mco

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.itismob.grpfive.mco.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth // Firebase Authentication


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()
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
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        // Basic input validation
        if (storeName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        // Firebase Auth req for min password length
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT)
                .show()
            return
        }

        // Check if passwords match
        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a new user with Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // User Registered successfully in Auth
                    val user = auth.currentUser

                    if (user != null) {
                        val currentTime = System.currentTimeMillis()

                        // Create User Object
                        val newUser = User(
                            userID = user.uid,
                            storeName = storeName,
                            userEmail = email,
                            createdAt = currentTime,
                            updatedAt = currentTime,
                            isActive = true
                        )

                        DatabaseHelper.createUser(newUser,
                            onSuccess = {
                                // Registration successful
                                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = { e ->
                                // Database save failed -- Delete user from Firebase Auth to keep clean state
                                user.delete()
                                Toast.makeText(this, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            })
                    } else { Toast.makeText(this, "User creation failed.", Toast.LENGTH_SHORT).show() }
                } else {
                    // Auth failure
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
