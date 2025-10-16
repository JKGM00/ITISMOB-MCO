package com.itismob.grpfive.mco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.itismob.grpfive.mco.databinding.ActivityProfileBinding

class ProfileActivity : ComponentActivity() {
    private lateinit var binding: ActivityProfileBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Set up back button click listener
        binding.tvBack2Main.setOnClickListener {
            finish() // Close this activity and return to previous screen
        }
    }
}
