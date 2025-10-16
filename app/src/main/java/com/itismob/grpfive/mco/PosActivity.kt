package com.itismob.grpfive.mco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import com.itismob.grpfive.mco.databinding.ActivityPosBinding
import java.math.BigDecimal

class PosActivity : ComponentActivity() {
    private lateinit var binding: ActivityPosBinding
    private lateinit var posAdapter: PosAdapter
    private val cartItems = mutableListOf<TransactionItem>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Load sample cart data from DataGenerator
        cartItems.addAll(DataGenerator.sampleCart())
        
        // Set up RecyclerView
        posAdapter = PosAdapter(cartItems) { item ->
            // Handle delete
            cartItems.remove(item)
            posAdapter.notifyDataSetChanged()
            updateTotal()
        }
        
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = posAdapter
        
        // Set up back button click listener
        binding.tvBack2Main.setOnClickListener {
            finish() // Close this activity and return to previous screen
        }
        
        // Update total on initial load
        updateTotal()
    }
    
    private fun updateTotal() {
        val total = cartItems.sumOf { it.subtotal }
        binding.btnTotal.text = "₱${total.setScale(2).toPlainString()}"
    }
}
