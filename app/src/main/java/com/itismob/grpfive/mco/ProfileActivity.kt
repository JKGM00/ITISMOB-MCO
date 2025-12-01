package com.itismob.grpfive.mco

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.itismob.grpfive.mco.databinding.ActivityEditProfileBinding
import com.itismob.grpfive.mco.models.User

class ProfileActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityEditProfileBinding
    private var currentUser: User? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    companion object {
        const val USER_KEY = "USER_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Check if user is logged in via Auth
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "User session expired. Please log in.", Toast.LENGTH_LONG).show()
            goToLogin()
            return
        }

        // Fetch User Data
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    currentUser = document.toObject(User::class.java)

                    currentUser?.userID = document.id

                    currentUser?.let { user ->
                        viewBinding.etStoreName.setText(user.storeName)
                        viewBinding.tvEmail.text = user.userEmail
                    }
                } else {
                    Toast.makeText(this, "User profile not found in database.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        // Back Button
        viewBinding.tvBack2Main.setOnClickListener {
            finish()
        }

        // Save Button
        viewBinding.btnSave.setOnClickListener {
            saveProfileData()
        }

        // Logout Button
        viewBinding.tvLogout.setOnClickListener {
            auth.signOut()
            goToLogin()
        }
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        // Clear stack so user can't go back
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun saveProfileData() {
        val user = currentUser ?: return // If user data hasn't loaded yet

        val newStoreName = viewBinding.etStoreName.text.toString().trim()

        if (newStoreName.isEmpty()) {
            Toast.makeText(this, "Store name cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }

        // Create updated User object
        val updatedUser = user.copy(
            storeName = newStoreName,
            updatedAt = System.currentTimeMillis()
        )

        // Save to Firestore
        db.collection("users").document(updatedUser.userID)
            .set(updatedUser)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                currentUser = updatedUser

                val resultIntent = Intent().apply {
                    putExtra(USER_KEY, updatedUser)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
