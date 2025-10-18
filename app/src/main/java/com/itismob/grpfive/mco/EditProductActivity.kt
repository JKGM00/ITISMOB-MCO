package com.itismob.grpfive.mco

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.itismob.grpfive.mco.databinding.ActivityEditProductBinding
import com.itismob.grpfive.mco.databinding.DialogScanBarcodeBinding
import java.math.BigDecimal

class EditProductActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityEditProductBinding
    private var position : Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityEditProductBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.tvBack2Main.setOnClickListener {
            finish()
        }

        // Retrieve Intent / Information
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
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        viewBinding.spProductCategory.adapter = adapter

        // Set the selected category in the spinner
        viewBinding.spProductCategory.setSelection(adapter.getPosition(productCategory))

        // Save
        viewBinding.btnSave.setOnClickListener {
            saveEditedProduct(productID)
        }

        // Cancel
        viewBinding.btnCancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        // Barcode Scanning
        viewBinding.btnScanBarcode.setOnClickListener {
            showScanBarcodeDialog()
        }

    }

    private fun saveEditedProduct(productID: String?) {
        val name = viewBinding.etProductName.text.toString().trim()
        val unitCost = viewBinding.etUnitCost.text.toString().trim()
        val sellingPrice = viewBinding.etSellingPrice.text.toString().trim()
        val stockQty = viewBinding.etStockQuantity.text.toString().trim()
        val barcode = viewBinding.etBarcodeEntry.text.toString().trim()
        val category = viewBinding.spProductCategory.selectedItem.toString()

        if (name.isEmpty() || unitCost.isEmpty() || sellingPrice.isEmpty() || stockQty.isEmpty() || barcode.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Number Validation
        val unitCostNumber : BigDecimal
        val sellingPriceNumber : BigDecimal
        val stockQtyNumber : Int
        try {
            unitCostNumber = BigDecimal(unitCost)
            sellingPriceNumber = BigDecimal(sellingPrice)
            stockQtyNumber = stockQty.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show()
            return
        }

        // Create Updated Object
        val updatedProduct = Product(
            productID = productID ?: "",
            productName = name,
            productCategory = category,
            productBarcode = barcode,
            unitCost = unitCostNumber,
            sellingPrice = sellingPriceNumber,
            stockQuantity = stockQtyNumber
        )

        // Send back as Intent to InventoryActivity (EditProductLauncher)
        val resultIntent = Intent()
        resultIntent.putExtra(ProductInventoryAdapter.PRODUCT_ID_KEY, updatedProduct.productID)
        resultIntent.putExtra(ProductInventoryAdapter.PRODUCT_NAME_KEY, updatedProduct.productName)
        resultIntent.putExtra(ProductInventoryAdapter.PRODUCT_CATEGORY_KEY, updatedProduct.productCategory)
        resultIntent.putExtra(ProductInventoryAdapter.PRODUCT_BARCODE_KEY, updatedProduct.productBarcode)

        // Send Cost back as string values
        resultIntent.putExtra(ProductInventoryAdapter.PRODUCT_UNIT_COST_KEY, updatedProduct.unitCost.toPlainString())
        resultIntent.putExtra(ProductInventoryAdapter.PRODUCT_SELLING_PRICE_KEY, updatedProduct.sellingPrice.toPlainString())

        resultIntent.putExtra(ProductInventoryAdapter.PRODUCT_STOCK_KEY, updatedProduct.stockQuantity)
        resultIntent.putExtra(ProductInventoryAdapter.POSITION, position)

        setResult(RESULT_OK, resultIntent)
        finish()
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
            
            // Simulate barcode scan when clicking camera placeholder
            dialogBinding.tvCameraPlaceholder.setOnClickListener {
                // Simulate scanning - in real app, this would be camera result
                val simulatedBarcode = "8901000001008" // Example new barcode
                scannedBarcode = simulatedBarcode
                dialogBinding.tvScannedBarcode.text = "Scanned: $simulatedBarcode"
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

}