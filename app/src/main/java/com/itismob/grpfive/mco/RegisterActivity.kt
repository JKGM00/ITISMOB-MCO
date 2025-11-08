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
    private lateinit var db: FirebaseFirestore // Firebase DB for user data

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Authentication and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
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

        // Firebase Auth req for min password length
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT)
                .show()
            return
        }

        // Create a new user with Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // User Registered successfully
                    val user = auth.currentUser

                    if (user != null) {
                        val currentTime = System.currentTimeMillis()

                        // Save user data in User object
                        val newUser = User(
                            userID = user.uid,
                            storeName = storeName,
                            userEmail = email,
                            createdAt = currentTime,
                            updatedAt = currentTime,
                            isActive = true
                        )

                        // Save User in 'users' table
                        db.collection("users").document(newUser.userID)
                            .set(newUser)
                            .addOnSuccessListener {
                                // Registration successful
                                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()

                                Handler(Looper.getMainLooper()).postDelayed({
                                    val intent = Intent(this, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }, 1500) // Delay by 1.5s
                            }
                            .addOnFailureListener { e ->
                                // Registration failed -- Delete user from Firebase Auth
                                user.delete()
                                Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "User creation failed.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}
