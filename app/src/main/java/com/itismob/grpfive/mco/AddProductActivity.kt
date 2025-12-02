package com.itismob.grpfive.mco

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.itismob.grpfive.mco.databinding.ActivityAddProductBinding
import com.itismob.grpfive.mco.databinding.DialogScanBarcodeBinding
import com.itismob.grpfive.mco.models.Product
import java.util.UUID

class AddProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddProductBinding
    private var currentBarcodeScanner: BarcodeScannerHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "User session expired. Please log in again.", Toast.LENGTH_LONG).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        val categories = listOf("Cooking Essentials", "Snacks", "Drinks", "Canned Goods", "Instant Food", "Hygiene", "Miscellaneous")

        // DROPDOWN SETUP
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spnProductCategory.adapter = spinnerAdapter

        // Cancel Add Product
        binding.btnCancel.setOnClickListener { finish() }
        // Save / Add New Product
        binding.btnAddProduct.setOnClickListener { saveNewProduct() }
        // Scan Barcode
        binding.btnScanBarcode.setOnClickListener { showScanBarcodeDialog() }

        setupNavigation()
    }

    private fun setupNavigation() {
        binding.tvNavProfile.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        binding.tvNavDashboard.setOnClickListener { navigateTo(DashboardTestActivity::class.java) }
        binding.tvNavInventory.setOnClickListener { navigateTo(InventoryActivity::class.java) }
        binding.tvNavHistory.setOnClickListener { navigateTo(TransactionHistoryActivity::class.java) }
        binding.tvNavPos.setOnClickListener { navigateTo(PosActivity::class.java) }
    }

    private fun navigateTo(destination: Class<*>) {
        val intent = Intent(this, destination)
        // Clear back stack so the user returns to a clean state
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun saveNewProduct() {
        val productName = binding.etProductName.text.toString().trim()
        val productCategory = binding.spnProductCategory.selectedItem.toString().trim()
        val productBarcode = binding.etBarcodeEntry.text.toString().trim()
        val productUnitCost = binding.etUnitCost.text.toString().trim().toDoubleOrNull()
        val productSellingPrice = binding.etSellingPrice.text.toString().trim().toDoubleOrNull()
        val productStock = binding.etStockQuantity.text.toString().trim().toIntOrNull() ?: 0

        if (productName.isEmpty()) {
            showToast("Please enter a product name.")
            return
        }
        if (productUnitCost == null || productSellingPrice == null || productUnitCost <= 0 || productSellingPrice <= 0) {
            showToast("Please enter valid numeric values.")
            return
        }
        if (productStock < 0) {
            showToast("Stock quantity cannot be negative.")
            return
        }
        if (productBarcode.isEmpty()) {
            showToast("Please enter a barcode.")
            return
        }
        if (productCategory.isEmpty()) {
            showToast("Please select a category.")
            return
        }

        // Disable button
        binding.btnAddProduct.isEnabled = false

        DatabaseHelper.checkProductDuplicates(productBarcode, productName, null,
            onResult = { barcodeExists, nameExists ->
                if (barcodeExists) {
                    binding.btnAddProduct.isEnabled = true
                    showToast("A product with this barcode already exists.")
                } else if (nameExists) {
                    binding.btnAddProduct.isEnabled = true
                    showToast("A product with this name already exists.")
                } else {
                    saveProduct(productName, productCategory, productBarcode, productUnitCost, productSellingPrice, productStock)
                }
            },
            onFailure = { e ->
                binding.btnAddProduct.isEnabled = true
                showToast("Error checking validation: ${e.message}")
            }
        )
    }

    private fun saveProduct(name: String, category: String, barcode: String, cost: Double, price: Double, stock: Int) {
        val newProduct = Product(
            productID = UUID.randomUUID().toString(),
            productName = name,
            productCategory = category,
            productBarcode = barcode,
            unitCost = cost,
            sellingPrice = price,
            stockQuantity = stock
        )

        DatabaseHelper.addProduct(newProduct,
            onSuccess = {
                showToast("Product added successfully!")
                val newProductIntent = Intent().apply { putExtra("newProduct", newProduct) }
                setResult(RESULT_OK, newProductIntent)
                finish()
            },
            onFailure = { e ->
                binding.btnAddProduct.isEnabled = true
                showToast("Error adding product: ${e.message}")
            }
        )
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showScanBarcodeDialog() {
        try {
            val dialogBinding = DialogScanBarcodeBinding.inflate(LayoutInflater.from(this))
            val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()

            dialogBinding.cvProductInfo.visibility = android.view.View.GONE
            dialogBinding.tilQuantity.visibility = android.view.View.GONE
            dialogBinding.tvErrorMessage.visibility = android.view.View.GONE
            dialogBinding.btnAddToCart.text = "Use Barcode"

            var scannedBarcode: String? = null

            currentBarcodeScanner = BarcodeScannerHelper(this, dialogBinding.cameraPreview) { barcode ->
                runOnUiThread {
                    if (!barcode.startsWith("CAMERA_ERROR") && barcode != "PERMISSION_DENIED") {
                        scannedBarcode = barcode
                        dialogBinding.tvScannedBarcode.text = "Scanned: $barcode"
                    }
                }
            }

            dialog.setOnShowListener { currentBarcodeScanner?.checkCameraPermissionAndStart() }
            dialog.setOnDismissListener {
                currentBarcodeScanner?.shutdown()
                currentBarcodeScanner = null
            }
            dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }
            dialogBinding.btnAddToCart.setOnClickListener {
                if (scannedBarcode != null) {
                    binding.etBarcodeEntry.setText(scannedBarcode)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Please scan a barcode first", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == BarcodeScannerHelper.CAMERA_PERMISSION_REQUEST_CODE) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED
            currentBarcodeScanner?.handlePermissionResult(granted)
        }
    }
}
