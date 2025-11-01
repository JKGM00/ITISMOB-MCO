package com.itismob.grpfive.mco

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.itismob.grpfive.mco.databinding.ActivityAddProductBinding
import com.itismob.grpfive.mco.databinding.DialogScanBarcodeBinding
import java.util.UUID

class AddProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddProductBinding
    private var currentBarcodeScanner: BarcodeScannerHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // set up the ProductCategory spinner
        val categories = listOf(
            "Cooking Essentials",
            "Snacks",
            "Drinks",
            "Canned Goods",
            "Instant Food",
            "Hygiene",
            "Miscellaneous"
        )

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spnProductCategory.adapter = spinnerAdapter

        // Set up tvBack2Main to go back to main
        binding.tvBack2Main.setOnClickListener {
            finish()
        }

        // Set up btnCancel to go back to main
        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnAddProduct.setOnClickListener {
            saveNewProduct()
        }
        
        // Set up btnScanBarcode to open scan dialog
        binding.btnScanBarcode.setOnClickListener {
            showScanBarcodeDialog()
        }
    }

    private fun saveNewProduct() {
        val productName = binding.etProductName.text.toString().trim()
        val productCategory = binding.spnProductCategory.selectedItem.toString().trim()
        val productBarcode = binding.etBarcodeEntry.text.toString().trim()
        val productUnitCostString = binding.etUnitCost.text.toString().trim()
        val productSellingPriceString = binding.etSellingPrice.text.toString().trim()
        val productStockString = binding.etStockQuantity.text.toString().trim()

        // Do input validation, and prepare for product creation
        if (productName.isEmpty()) {
            showToast("Please enter a product name.")
            return
        }

        val productUnitCost = productUnitCostString.toDoubleOrNull()
        val productSellingPrice = productSellingPriceString.toDoubleOrNull()

        if (productUnitCost == null || productSellingPrice == null) {
            showToast("Please enter valid numeric values for cost and price.")
            return
        }

        val productStock = productStockString.toIntOrNull() ?: 0

        // If valid, make a new product
        val newProduct = Product(
            productID = UUID.randomUUID().toString(),
            productName = productName,
            productCategory = productCategory,
            productBarcode = productBarcode,
            unitCost = productUnitCost,
            sellingPrice = productSellingPrice,
            stockQuantity = productStock
        )

        //  Send new product back to InventoryActivity (add logic there to accept the new product)
        val newProductIntent = Intent().apply {
            putExtra("newProduct", newProduct)
        }

        setResult(RESULT_OK, newProductIntent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun showScanBarcodeDialog() {
        try {
            val dialogBinding = DialogScanBarcodeBinding.inflate(LayoutInflater.from(this))
            val dialog = AlertDialog.Builder(this)
                .setView(dialogBinding.root)
                .create()
            
            // Hide product info and quantity fields (we only need barcode)
            dialogBinding.cvProductInfo.visibility = android.view.View.GONE
            dialogBinding.tilQuantity.visibility = android.view.View.GONE
            dialogBinding.tvErrorMessage.visibility = android.view.View.GONE
            dialogBinding.btnAddToCart.text = "Use Barcode"
            
            var scannedBarcode: String? = null
            
            // Initialize barcode scanner with camera
            currentBarcodeScanner = BarcodeScannerHelper(
                this,
                dialogBinding.cameraPreview
            ) { barcode ->
                runOnUiThread {
                    when {
                        barcode == "PERMISSION_DENIED" -> {
                            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show()
                            dialog.dismiss()
                        }
                        barcode.startsWith("CAMERA_ERROR") -> {
                            Toast.makeText(this, "Camera error: $barcode", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            scannedBarcode = barcode
                            dialogBinding.tvScannedBarcode.text = "Scanned: $barcode"
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
            
            // Use barcode button
            dialogBinding.btnAddToCart.setOnClickListener {
                if (scannedBarcode != null) {
                    // Set the barcode in the edit text field
                    binding.etBarcodeEntry.setText(scannedBarcode)
                    dialog.dismiss()
                    Toast.makeText(this, "Barcode set to: $scannedBarcode", Toast.LENGTH_SHORT).show()
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