package com.itismob.grpfive.mco

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.itismob.grpfive.mco.adapters.ProductInventoryAdapter
import com.itismob.grpfive.mco.databinding.ActivityEditProductBinding
import com.itismob.grpfive.mco.databinding.DialogScanBarcodeBinding
import com.itismob.grpfive.mco.models.Product

class EditProductActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityEditProductBinding
    private var position : Int = -1
    private var currentBarcodeScanner: BarcodeScannerHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityEditProductBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Back button
        viewBinding.tvBack2Main.setOnClickListener { finish() }

        // Retrieve Intent / Info
        val intent = intent
        val productID = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_ID_KEY)
        val productName = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_NAME_KEY) ?: ""
        val productCategory = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_CATEGORY_KEY) ?: ""
        val productBarcode = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_BARCODE_KEY) ?: ""
        val unitCost = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_UNIT_COST_KEY) ?: "0.00"
        val sellingPrice = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_SELLING_PRICE_KEY) ?: "0.00"
        val stockQuantity = intent.getIntExtra(ProductInventoryAdapter.PRODUCT_STOCK_KEY, 0)
        position = intent.getIntExtra(ProductInventoryAdapter.POSITION, -1)

        // Populate Edit Text fields with text
        viewBinding.etProductName.setText(productName)
        viewBinding.etUnitCost.setText(unitCost)
        viewBinding.etSellingPrice.setText(sellingPrice)
        viewBinding.etStockQuantity.setText(stockQuantity.toString())
        viewBinding.etBarcodeEntry.setText(productBarcode)

        // Setup Spinner / Drop-down (Adapter and View Binding)
        val categories = listOf(
            "Cooking Essentials",
            "Snacks",
            "Drinks",
            "Canned Goods",
            "Instant Food",
            "Hygiene",
            "Miscellaneous"
        )

        // Dropdown Adapter
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        viewBinding.spProductCategory.adapter = adapter

        // Set the selected category in the spinner
        viewBinding.spProductCategory.setSelection(adapter.getPosition(productCategory))

        // Save
        viewBinding.btnSave.setOnClickListener { saveEditedProduct(productID) }

        // Cancel
        viewBinding.btnCancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        // Barcode Scanning
        viewBinding.btnScanBarcode.setOnClickListener { showScanBarcodeDialog() }
    }

    private fun saveEditedProduct(productID: String?) {
        if (productID == null) return

        val name = viewBinding.etProductName.text.toString().trim()
        val unitCost = viewBinding.etUnitCost.text.toString().trim().toDoubleOrNull()
        val sellingPrice = viewBinding.etSellingPrice.text.toString().trim().toDoubleOrNull()
        val stockQty = viewBinding.etStockQuantity.text.toString().trim()
        val barcode = viewBinding.etBarcodeEntry.text.toString().trim()
        val category = viewBinding.spProductCategory.selectedItem.toString()

        if (name.isEmpty() || stockQty.isEmpty() || barcode.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }
        if (unitCost == null || sellingPrice == null || unitCost <= 0 || sellingPrice <= 0) {
            Toast.makeText(this, "Please enter valid numeric values", Toast.LENGTH_SHORT).show()
            return
        }

        viewBinding.btnSave.isEnabled = false

        DatabaseHelper.checkProductDuplicates(barcode, name, productID,
            onResult = { barcodeExists, nameExists ->
                if (barcodeExists) {
                    viewBinding.btnSave.isEnabled = true
                    Toast.makeText(this, "Barcode already used by another product.", Toast.LENGTH_SHORT).show()
                } else if (nameExists) {
                    viewBinding.btnSave.isEnabled = true
                    Toast.makeText(this, "Product Name already exists.", Toast.LENGTH_SHORT).show()
                } else {
                    performUpdate(productID, name, category, barcode, unitCost, sellingPrice, stockQty)
                }
            },
            onFailure = { e ->
                viewBinding.btnSave.isEnabled = true
                Toast.makeText(this, "Error checking validation: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun performUpdate(
        productID: String,
        name: String,
        category: String,
        barcode: String,
        unitCost: Double,
        sellingPrice: Double,
        stockQty: String
    ) {
        val unitCostNumber = unitCost.toDouble()
        val sellingPriceNumber = sellingPrice.toDouble()
        val stockQtyNumber = stockQty.toInt()

        val updatedProduct = Product(
            productID = productID,
            productName = name,
            productCategory = category,
            productBarcode = barcode,
            unitCost = unitCostNumber,
            sellingPrice = sellingPriceNumber,
            stockQuantity = stockQtyNumber
        )

        // Update in Database
        DatabaseHelper.updateProduct(updatedProduct,
            onSuccess = {
                // Send back as Intent to InventoryActivity
                val resultIntent = Intent()
                resultIntent.putExtra(ProductInventoryAdapter.PRODUCT_ID_KEY, updatedProduct.productID)
                resultIntent.putExtra(ProductInventoryAdapter.PRODUCT_NAME_KEY, updatedProduct.productName)
                resultIntent.putExtra(ProductInventoryAdapter.PRODUCT_CATEGORY_KEY, updatedProduct.productCategory)
                resultIntent.putExtra(ProductInventoryAdapter.PRODUCT_BARCODE_KEY, updatedProduct.productBarcode)
                resultIntent.putExtra(ProductInventoryAdapter.PRODUCT_UNIT_COST_KEY, updatedProduct.unitCost.toString())
                resultIntent.putExtra(ProductInventoryAdapter.PRODUCT_SELLING_PRICE_KEY, updatedProduct.sellingPrice.toString())
                resultIntent.putExtra(ProductInventoryAdapter.PRODUCT_STOCK_KEY, updatedProduct.stockQuantity)
                resultIntent.putExtra(ProductInventoryAdapter.POSITION, position)

                setResult(RESULT_OK, resultIntent)
                finish()
            },
            onFailure = { e ->
                viewBinding.btnSave.isEnabled = true
                Toast.makeText(this, "Error updating product: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        )
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
                    viewBinding.etBarcodeEntry.setText(scannedBarcode)
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