package com.itismob.grpfive.mco

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.itismob.grpfive.mco.databinding.ActivityPosBinding
import com.itismob.grpfive.mco.databinding.DialogAddProductBinding
import com.itismob.grpfive.mco.databinding.DialogScanBarcodeBinding

class PosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPosBinding
    private lateinit var posAdapter: PosAdapter
    private val cartItems = mutableListOf<TransactionItem>()
    private var currentBarcodeScanner: BarcodeScannerHelper? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Load sample cart data from DataGenerator
        cartItems.addAll(DataGenerator.sampleCart())
        
        // Set up RecyclerView
        posAdapter = PosAdapter(
            cartItems,
            onDelete = { item ->
                // Handle delete
                cartItems.remove(item)
                posAdapter.notifyDataSetChanged()
                updateTotal()
            },
            onQuantityChanged = {
                // Update total when quantity changes
                updateTotal()
            }
        )
        
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

        binding.btnTotal.setOnClickListener {
            showConfirmationDialog()
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
                        dialogBinding.tvProductPrice.text = String.format("₱%.2f", selectedProduct!!.sellingPrice)
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
        binding.btnTotal.text = "₱${String.format("%.2f", total)}"
    }
    
    private fun showScanBarcodeDialog() {
        try {
            val dialogBinding = DialogScanBarcodeBinding.inflate(LayoutInflater.from(this))
            val dialog = AlertDialog.Builder(this)
                .setView(dialogBinding.root)
                .create()
            
            var selectedProduct: Product? = null
            
            // Initialize barcode scanner with camera
            currentBarcodeScanner = BarcodeScannerHelper(
                this,
                dialogBinding.cameraPreview
            ) { scannedBarcode ->
                // Handle scanned barcode
                runOnUiThread {
                    when {
                        scannedBarcode == "PERMISSION_DENIED" -> {
                            Toast.makeText(this, "Camera permission denied. Please grant permission in settings.", Toast.LENGTH_LONG).show()
                            dialog.dismiss()
                        }
                        scannedBarcode.startsWith("CAMERA_ERROR") -> {
                            Toast.makeText(this, "Camera error: $scannedBarcode", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            // Valid barcode scanned
                            dialogBinding.tvScannedBarcode.text = "Scanned: $scannedBarcode"
                            
                            // Look up product by barcode
                            selectedProduct = DataGenerator.findByBarcode(scannedBarcode)
                            if (selectedProduct != null) {
                                // Show product info card
                                dialogBinding.cvProductInfo.visibility = android.view.View.VISIBLE
                                dialogBinding.tvProductName.text = selectedProduct!!.productName
                                dialogBinding.tvProductPrice.text = String.format("₱%.2f", selectedProduct!!.sellingPrice)
                                dialogBinding.tvErrorMessage.visibility = android.view.View.GONE
                                dialogBinding.tilQuantity.visibility = android.view.View.VISIBLE
                            } else {
                                // Show error message
                                dialogBinding.cvProductInfo.visibility = android.view.View.GONE
                                dialogBinding.tvErrorMessage.visibility = android.view.View.VISIBLE
                                dialogBinding.tilQuantity.visibility = android.view.View.GONE
                            }
                        }
                    }
                }
            }
            
            // Start camera when dialog is shown
            dialog.setOnShowListener {
                currentBarcodeScanner?.checkCameraPermissionAndStart()
            }
            
            // Clean up camera when dialog is dismissed
            dialog.setOnDismissListener {
                currentBarcodeScanner?.shutdown()
                currentBarcodeScanner = null
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

    private fun showConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Complete Transaction?")
            .setMessage("Are you sure you want to finalize this transaction?")
            .setPositiveButton("Yes") { _, _ ->
                // Clear cart items
                cartItems.clear()
                posAdapter.notifyDataSetChanged()
                updateTotal()

                Toast.makeText(this, "Transaction completed!", Toast.LENGTH_SHORT).show()

                // Navigate Back
                finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "Transaction cancelled.", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == BarcodeScannerHelper.CAMERA_PERMISSION_REQUEST_CODE) {
            val granted = grantResults.isNotEmpty() && 
                         grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED
            currentBarcodeScanner?.handlePermissionResult(granted)
        }
    }

}
