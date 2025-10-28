package com.itismob.grpfive.mco

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.itismob.grpfive.mco.databinding.ActivityEditProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityEditProfileBinding
    private lateinit var currentUser: User
    private lateinit var db: FirebaseFirestore

    companion object {
        const val USER_KEY = "USER_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewBinding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Initialize Firestore db
        db = FirebaseFirestore.getInstance()

        // Retrieve CurrentUser from DashboardActivity
        val currUser: User? = intent.getSerializableExtra(USER_KEY) as? User
        if (currUser == null) {
            Toast.makeText(this, "User data not found!", Toast.LENGTH_LONG).show()
            setResult(RESULT_CANCELED)
            finish()
            return
        }
        currentUser = currUser

        // Populate Edit Text Fields
        viewBinding.etStoreName.setText(currentUser.storeName)
        viewBinding.tvEmail.text = currentUser.userEmail
        viewBinding.ivStoreImage.setImageResource(currentUser.profilePic)

        // Back Button
        viewBinding.tvBack2Main.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        // Store Image / Profile Picture
        viewBinding.ivStoreImage.setOnClickListener {
            // TODO: DIALOG BOX PROFILE PICTURE (Integrate Firebase Storage for this)
            // Baka di na
        }

        // Save Button
        viewBinding.btnSave.setOnClickListener {
            saveProfileData()
        }

        // Logout Button
        viewBinding.tvLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut() // Sign out from Firebase Auth
            val intent = Intent(this, LoginActivity::class.java)
            // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun saveProfileData() {
        val newStoreName = viewBinding.etStoreName.text.toString().trim()

        if (newStoreName.isEmpty()) {
            Toast.makeText(this, "Store name cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }

        // Create updated User object with new store name and updated timestamp
        val updatedUser = currentUser.copy(
            storeName = newStoreName,
            updatedAt = System.currentTimeMillis() // Update timestamp
            // Profile kung itutuloy ko pa
        )

        // Save updated data to Firestore
        db.collection("users").document(updatedUser.userID)
            .set(updatedUser)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                // Return updated user to DashboardActivity
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