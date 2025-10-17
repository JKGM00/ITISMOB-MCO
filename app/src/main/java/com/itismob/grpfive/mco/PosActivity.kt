package com.itismob.grpfive.mco

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.itismob.grpfive.mco.databinding.ActivityPosBinding
import com.itismob.grpfive.mco.databinding.DialogAddProductBinding
import com.itismob.grpfive.mco.databinding.DialogScanBarcodeBinding
import java.math.BigDecimal

class PosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPosBinding
    private lateinit var posAdapter: PosAdapter
    private val cartItems = mutableListOf<TransactionItem>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        
        // Set up Add button to show dialog
        binding.btnAddToCart.setOnClickListener {
            showAddProductDialog()
        }
        
        // Set up Scan button to show scan dialog
        binding.btnScan.setOnClickListener {
            showScanBarcodeDialog()
        }
        
        // Update total on initial load
        updateTotal()
    }
    
    private fun showAddProductDialog() {
        try {
            Toast.makeText(this, "Opening Add Product Dialog", Toast.LENGTH_SHORT).show()
            
            val dialogBinding = DialogAddProductBinding.inflate(LayoutInflater.from(this))
            val dialog = AlertDialog.Builder(this)
                .setView(dialogBinding.root)
                .create()
            
            // Don't set transparent background - this might be causing visibility issues
            // dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            
            var selectedProduct: Product? = null
            
            // Listen for barcode input changes
            dialogBinding.etBarcode.doAfterTextChanged { text ->
                val barcode = text.toString()
                if (barcode.isNotEmpty()) {
                    // Look up product by barcode
                    selectedProduct = DataGenerator.findByBarcode(barcode)
                    if (selectedProduct != null) {
                        // Show product info card
                        dialogBinding.cvProductInfo.visibility = android.view.View.VISIBLE
                        dialogBinding.tvProductName.text = selectedProduct!!.productName
                        dialogBinding.tvProductPrice.text = "₱${selectedProduct!!.sellingPrice.setScale(2).toPlainString()}"
                        dialogBinding.tvErrorMessage.visibility = android.view.View.GONE
                        dialogBinding.tilQuantity.visibility = android.view.View.VISIBLE
                    } else {
                        // Show error message
                        dialogBinding.cvProductInfo.visibility = android.view.View.GONE
                        dialogBinding.tvErrorMessage.visibility = android.view.View.VISIBLE
                        dialogBinding.tilQuantity.visibility = android.view.View.GONE
                    }
                } else {
                    // Hide all info when barcode is empty
                    dialogBinding.cvProductInfo.visibility = android.view.View.GONE
                    dialogBinding.tvErrorMessage.visibility = android.view.View.GONE
                    dialogBinding.tilQuantity.visibility = android.view.View.GONE
                }
            }
            
            // Cancel button
            dialogBinding.btnCancelItem.setOnClickListener {
                dialog.dismiss()
            }
            
            // Add to Cart button
            dialogBinding.btnAddtoTransc.setOnClickListener {
                if (selectedProduct != null) {
                    val quantityText = dialogBinding.etQuantity.text.toString()
                    val quantity = quantityText.toIntOrNull() ?: 1
                    
                    if (quantity > 0) {
                        // Check if product already in cart
                        val existingItem = cartItems.find { it.productID == selectedProduct!!.productID }
                        if (existingItem != null) {
                            // Update quantity
                            existingItem.quantity += quantity
                        } else {
                            // Add new item
                            val newItem = TransactionItem(
                                productID = selectedProduct!!.productID,
                                productName = selectedProduct!!.productName,
                                productPrice = selectedProduct!!.sellingPrice,
                                quantity = quantity
                            )
                            cartItems.add(newItem)
                        }
                        
                        posAdapter.notifyDataSetChanged()
                        updateTotal()
                        dialog.dismiss()
                        Toast.makeText(this, "Added ${selectedProduct!!.productName} to cart", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please enter a valid barcode", Toast.LENGTH_SHORT).show()
                }
            }
            
            dialog.show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error creating dialog: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
    
    private fun updateTotal() {
        val total = cartItems.sumOf { it.subtotal }
        binding.btnTotal.text = "₱${total.setScale(2).toPlainString()}"
    }
    
    private fun showScanBarcodeDialog() {
        try {
            val dialogBinding = DialogScanBarcodeBinding.inflate(LayoutInflater.from(this))
            val dialog = AlertDialog.Builder(this)
                .setView(dialogBinding.root)
                .create()
            
            var selectedProduct: Product? = null
            
            // TODO: Implement actual camera scanning here
            // For now, simulate a barcode scan when user clicks on camera placeholder
            dialogBinding.tvCameraPlaceholder.setOnClickListener {
                // Simulate scanning - in real app, this would be camera result
                val simulatedBarcode = "8901000001003" // Example barcode
                dialogBinding.tvScannedBarcode.text = "Scanned: $simulatedBarcode"
                
                // Look up product by barcode
                selectedProduct = DataGenerator.findByBarcode(simulatedBarcode)
                if (selectedProduct != null) {
                    // Show product info card
                    dialogBinding.cvProductInfo.visibility = android.view.View.VISIBLE
                    dialogBinding.tvProductName.text = selectedProduct!!.productName
                    dialogBinding.tvProductPrice.text = "₱${selectedProduct!!.sellingPrice.setScale(2).toPlainString()}"
                    dialogBinding.tvErrorMessage.visibility = android.view.View.GONE
                    dialogBinding.tilQuantity.visibility = android.view.View.VISIBLE
                } else {
                    // Show error message
                    dialogBinding.cvProductInfo.visibility = android.view.View.GONE
                    dialogBinding.tvErrorMessage.visibility = android.view.View.VISIBLE
                    dialogBinding.tilQuantity.visibility = android.view.View.GONE
                }
            }
            
            // Close button
            dialogBinding.btnClose.setOnClickListener {
                dialog.dismiss()
            }
            
            // Add to Cart button
            dialogBinding.btnAddToCart.setOnClickListener {
                if (selectedProduct != null) {
                    val quantityText = dialogBinding.etQuantity.text.toString()
                    val quantity = quantityText.toIntOrNull() ?: 1
                    
                    if (quantity > 0) {
                        // Check if product already in cart
                        val existingItem = cartItems.find { it.productID == selectedProduct!!.productID }
                        if (existingItem != null) {
                            // Update quantity
                            existingItem.quantity += quantity
                        } else {
                            // Add new item
                            val newItem = TransactionItem(
                                productID = selectedProduct!!.productID,
                                productName = selectedProduct!!.productName,
                                productPrice = selectedProduct!!.sellingPrice,
                                quantity = quantity
                            )
                            cartItems.add(newItem)
                        }
                        
                        posAdapter.notifyDataSetChanged()
                        updateTotal()
                        dialog.dismiss()
                        Toast.makeText(this, "Added ${selectedProduct!!.productName} to cart", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please scan a barcode first", Toast.LENGTH_SHORT).show()
                }
            }
            
            dialog.show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error creating scan dialog: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}
