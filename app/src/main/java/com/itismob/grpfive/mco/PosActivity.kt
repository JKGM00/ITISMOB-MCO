package com.itismob.grpfive.mco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.itismob.grpfive.mco.databinding.ActivityPosBinding

class PosActivity : ComponentActivity() {
    private lateinit var binding: ActivityPosBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Set up back button click listener
        binding.tvBack2Main.setOnClickListener {
            finish() // Close this activity and return to previous screen
        }
    }
}
