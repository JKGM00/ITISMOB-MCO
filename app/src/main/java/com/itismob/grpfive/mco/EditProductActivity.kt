package com.itismob.grpfive.mco

import android.R
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.itismob.grpfive.mco.databinding.ActivityEditProductBinding
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
            // TODO : Barcode Scanning to be implemented
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

        // Send result back as String
        resultIntent.putExtra(ProductInventoryAdapter.PRODUCT_UNIT_COST_KEY, updatedProduct.unitCost.toPlainString())
        resultIntent.putExtra(ProductInventoryAdapter.PRODUCT_SELLING_PRICE_KEY, updatedProduct.sellingPrice.toPlainString())

        resultIntent.putExtra(ProductInventoryAdapter.PRODUCT_STOCK_KEY, updatedProduct.stockQuantity)
        resultIntent.putExtra(ProductInventoryAdapter.POSITION, position)

        setResult(RESULT_OK, resultIntent)
        finish()
    }


}