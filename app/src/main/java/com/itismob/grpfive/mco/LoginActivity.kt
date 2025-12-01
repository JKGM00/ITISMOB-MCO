package com.itismob.grpfive.mco

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.itismob.grpfive.mco.databinding.ActivityLoginBinding
import androidx.core.content.edit
import com.itismob.grpfive.mco.utils.Validator

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        setupPasswordToggle(binding.etPassword)
        setupLogin()
    }

    // onStart: Check if a user is already logged in (auto-login)
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

        binding.tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgetPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkLogin(email: String, password: String) {
        Validator.validateLogin(email, password)?.let {
            showToast(it)
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
        DatabaseHelper.getUser(userId,
            onSuccess = { user ->
                // Check if user is active
                if (!user.isActive) {
                    showToast("This account is currently inactive.")
                    auth.signOut() // Log out the inactive user from Firebase Auth
                    return@getUser
                }

                showToast("Welcome, ${user.storeName}!")


                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("user", user) // Pass the full User object
                startActivity(intent)

                val sharedPrefs = getSharedPreferences("LoginPreferences", MODE_PRIVATE)
                sharedPrefs.edit { remove("lastEmail") } // Clear saved email
                finish() // Close LoginActivity
            },
            onFailure = { e ->
                showToast("Failed to fetch user data: ${e.message}")
                auth.signOut()
            }
        )
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setupPasswordToggle(editText: EditText) {
        editText.setOnTouchListener { _, event ->
            if (event.action != MotionEvent.ACTION_UP) return@setOnTouchListener false

            val drawableEnd = editText.compoundDrawables[2] ?: return@setOnTouchListener false
            if (event.rawX < editText.right - drawableEnd.bounds.width()) return@setOnTouchListener false

            // Toggle password visibility
            val isPasswordHidden = editText.transformationMethod is android.text.method.PasswordTransformationMethod
            editText.transformationMethod = if (isPasswordHidden)
                android.text.method.HideReturnsTransformationMethod.getInstance()
            else
                android.text.method.PasswordTransformationMethod.getInstance()

            editText.setCompoundDrawablesWithIntrinsicBounds(
                0, 0,
                if (isPasswordHidden) R.drawable.ic_visibility_on else R.drawable.ic_visibility_off,
                0
            )
            editText.setSelection(editText.text.length) // keep cursor at end
            true
        }
    }
}