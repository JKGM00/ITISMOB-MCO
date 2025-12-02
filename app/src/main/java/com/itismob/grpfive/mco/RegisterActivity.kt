package com.itismob.grpfive.mco

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.view.MotionEvent
import android.widget.EditText
import com.itismob.grpfive.mco.databinding.ActivityRegisterBinding
import com.itismob.grpfive.mco.models.User
import com.itismob.grpfive.mco.utils.Validator

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

        setupPasswordToggle(binding.etPassword)
        setupPasswordToggle(binding.etConfirmPassword)
    }


    private fun handleRegistration() {
        val storeName = binding.tvStoreName.text.toString().trim()
        val email = binding.tvEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        // Validate store name length
        Validator.validateLength(storeName, min = 1, max = 50)?.let {
            Toast.makeText(this, "Store name: $it", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate email
        Validator.validateEmail(email)?.let {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            return
        }

        // Validate password strength
        Validator.validatePassword(password)?.let {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            return
        }

        // Lastly, validate password match
        Validator.validatePasswordMatch(password, confirmPassword)?.let {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
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
                            profilePic = "",
                            userEmail = email,
                            createdAt = currentTime,
                            updatedAt = currentTime,
                            isActive = true
                        )

                        DatabaseHelper.createUser(newUser,
                            onSuccess = {
                                // Registration successful
                                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                                finish()
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
