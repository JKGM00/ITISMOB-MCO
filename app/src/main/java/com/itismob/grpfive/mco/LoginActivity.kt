package com.itismob.grpfive.mco

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.itismob.grpfive.mco.databinding.ActivityLoginBinding
import androidx.core.content.edit

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupLogin()
    }

    // onStart: Check if a user is already logged in (optional auto-login)
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is already logged in
            fetchUserData(currentUser.uid)
        }
    }

    // Called when user leaves the Activity
    override fun onPause() {
        super.onPause()
        val email = binding.etEmail.text.toString().trim()

        // Save the email in SharedPreferences when leaving activity
        val sharedPreferences = getSharedPreferences("LoginPreferences", MODE_PRIVATE)
        sharedPreferences.edit { putString("lastEmail", email) }
    }

    // Called when user returns to the Activity
    override fun onResume() {
        super.onResume()
        binding.etPassword.setText("")

        // Retrieve the email from SharedPreferences when returning
        val sharedPreferences = getSharedPreferences("LoginPreferences", MODE_PRIVATE)
        val savedEmail = sharedPreferences.getString("lastEmail", "")
        binding.etEmail.setText(savedEmail)
    }

    override fun onDestroy() {
        super.onDestroy()
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

        // Use Firebase Authentication to sign in user
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in success -> Fetch user from db
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        fetchUserData(firebaseUser.uid)
                    } else {
                        showToast("Authentication error.")
                    }
                } else {
                    // If Sign-in fails
                    showToast("Invalid email or password.")
                }
            }
    }

    private fun fetchUserData(userId: String) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        // Set the userID property from the document ID -- Important ito unique ID
                        user.userID = document.id

                        // Check if user is active
                        if (!user.isActive) {
                            showToast("This account is currently inactive.")
                            auth.signOut() // Log out the inactive user from Firebase Auth
                            return@addOnSuccessListener
                        }

                        showToast("Welcome, ${user.storeName}!")

                        // AI-generated delay
                        Handler(Looper.getMainLooper()).postDelayed({
                            val intent = Intent(this, DashboardActivity::class.java)
                            intent.putExtra("user", user) // Pass the full User object
                            startActivity(intent)

                            val sharedPrefs = getSharedPreferences("LoginPreferences", MODE_PRIVATE)
                            sharedPrefs.edit { remove("lastEmail") } // Clear saved email

                            finish() // Close LoginActivity
                        }, 1500)
                    } else {
                        showToast("User data parsing failed.")
                        auth.signOut() // Sign out if Firestore data is corrupted
                    }
                } else {
                    showToast("Email not found. Please register.")
                    auth.signOut() // Sign out if Auth user exists but no Firestore profile
                }
            }
            .addOnFailureListener { e ->
                showToast("Failed to fetch user data: ${e.message}")
                auth.signOut() // Sign out on network/db error
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}