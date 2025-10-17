package com.itismob.grpfive.mco

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.itismob.grpfive.mco.databinding.ActivityAddProductBinding
import java.util.UUID

class AddProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddProductBinding

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

        val productUnitCost = productUnitCostString.toBigDecimalOrNull()
        val productSellingPrice = productSellingPriceString.toBigDecimalOrNull()

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
}