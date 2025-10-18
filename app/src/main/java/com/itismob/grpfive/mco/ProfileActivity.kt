package com.itismob.grpfive.mco

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.itismob.grpfive.mco.databinding.ActivityEditProfileBinding


class ProfileActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityEditProfileBinding
    private lateinit var currentUser: User

    companion object {
        const val USER_KEY = "USER_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewBinding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

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
        viewBinding.etPassword.setText("")
        viewBinding.tvEmail.text = currentUser.userEmail
        viewBinding.ivStoreImage.setImageResource(currentUser.profilePic)

        // Back Button
        viewBinding.tvBack2Main.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        // Store Image / Profile Picture
        viewBinding.ivStoreImage.setOnClickListener {
            // TODO: DIALOG BOX PROFILE PICTURE
        }

        // Save Button
        viewBinding.btnSave.setOnClickListener {
            saveProfileData()
        }

        // Logout Button
        viewBinding.tvLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun saveProfileData() {
        val newStoreName = viewBinding.etStoreName.text.toString().trim()
        val newPassword = viewBinding.etPassword.text.toString().trim()

        // Create updated User object
        val updatedUser = currentUser.copy(
            storeName = newStoreName,
            userHashedPw = newPassword.ifEmpty { currentUser.userHashedPw }
            // TODO : IMPLEMENT IMAGE SELECTION / UPLOAD
        )

        // Return updated User to DashboardActivity
        val resultIntent = Intent().apply {
            putExtra(USER_KEY, updatedUser)
        }

        setResult(RESULT_OK, resultIntent)
        finish()
    }
}