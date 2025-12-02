package com.itismob.grpfive.mco

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.itismob.grpfive.mco.databinding.ActivityEditProfileBinding
import com.itismob.grpfive.mco.models.User
import java.util.HashMap

class ProfileActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityEditProfileBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var currentUser: User? = null
    private var selectedImageUri: Uri? = null

    // Image Picker
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            // Show image
            Glide.with(this).load(uri).circleCrop().into(viewBinding.ivStoreImage)
            viewBinding.ivStoreImage.setPadding(0, 0, 0, 0)
            viewBinding.ivStoreImage.imageTintList = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        initCloudinary()
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val uid = auth.currentUser?.uid
        if (uid == null) {
            goToLogin()
            return
        }

        loadUserData(uid)

        viewBinding.ivStoreImage.setOnClickListener { pickImage.launch("image/*") }
        viewBinding.tvBack2Main.setOnClickListener { finish() }
        viewBinding.tvLogout.setOnClickListener {
            auth.signOut()
            goToLogin()
        }

        viewBinding.btnSave.setOnClickListener {
            saveProfileData()
        }
    }

    private fun initCloudinary() {
        try {
            MediaManager.get()
        } catch (e: IllegalStateException) {
            val config = HashMap<String, String>()
            config["cloud_name"] = BuildConfig.CLOUDINARY_CLOUD_NAME
            config["api_key"] = BuildConfig.CLOUDINARY_API_KEY
            config["api_secret"] = BuildConfig.CLOUDINARY_API_SECRET
            MediaManager.init(this, config)
        }
    }

    private fun loadUserData(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    currentUser = document.toObject(User::class.java)
                    currentUser?.userID = document.id

                    // Update UI
                    currentUser?.let { user ->
                        viewBinding.etStoreName.setText(user.storeName)
                        viewBinding.tvEmail.text = user.userEmail

                        if (user.profilePic.isNotEmpty()) {
                            Glide.with(this).load(user.profilePic).circleCrop().into(viewBinding.ivStoreImage)
                            viewBinding.ivStoreImage.setPadding(0, 0, 0, 0)
                            viewBinding.ivStoreImage.imageTintList = null
                        }
                    }
                }
            }
    }

    private fun saveProfileData() {
        if (!viewBinding.btnSave.isEnabled) return

        val user = currentUser ?: return
        val newName = viewBinding.etStoreName.text.toString().trim()

        if (newName.isEmpty()) {
            Toast.makeText(this, "Store name required", Toast.LENGTH_SHORT).show()
            return
        }

        // Disable button
        viewBinding.btnSave.isEnabled = false

        if (selectedImageUri != null) {
            uploadImage(user.userID, newName)
        } else {
            updateFirestore(user.profilePic, newName)
        }
    }

    private fun uploadImage(userId: String, name: String) {
        // Filename is userId
        MediaManager.get().upload(selectedImageUri)
            .option("public_id", userId)
            .option("folder", "paninda_pos_profiles")
            .option("overwrite", true)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, totalBytes: Long, bytesUploaded: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val downloadUrl = resultData["secure_url"].toString()

                    runOnUiThread {
                        updateFirestore(downloadUrl, name)
                    }
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    runOnUiThread {
                        Toast.makeText(this@ProfileActivity, "Upload Error: ${error.description}", Toast.LENGTH_SHORT).show()
                        viewBinding.btnSave.isEnabled = true
                    }
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }

    private fun updateFirestore(photoUrl: String, name: String) {
        val user = currentUser ?: return

        // Create updated object
        val updatedUser = user.copy(
            storeName = name,
            profilePic = photoUrl,
            updatedAt = System.currentTimeMillis()
        )

        db.collection("users").document(updatedUser.userID).set(updatedUser)
            .addOnSuccessListener {
                Toast.makeText(this, "Saved successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "DB Error: ${e.message}", Toast.LENGTH_SHORT).show()
                viewBinding.btnSave.isEnabled = true
            }
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
