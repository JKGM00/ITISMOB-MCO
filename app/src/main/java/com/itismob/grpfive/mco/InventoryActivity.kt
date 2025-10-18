package com.itismob.grpfive.mco


import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.TextWatcher
import androidx.appcompat.app.AlertDialog
import com.itismob.grpfive.mco.databinding.ActivityInventoryBinding
import java.math.BigDecimal
import java.util.Locale

class InventoryActivity : AppCompatActivity() {
    private lateinit var viewBinding : ActivityInventoryBinding
    private lateinit var productAdapter: ProductInventoryAdapter
    private lateinit var products: MutableList<Product> // Master list of all products
    private val filteredProducts: MutableList<Product> = mutableListOf() // List displayed by adapter

    // State Variables for Search & Filter
    private var searchQuery: String = ""
    private var filterCategory: String = "All Categories"

    private val editProductLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val intent = result.data

            if (intent != null) {
                val productID = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_ID_KEY)
                val productName = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_NAME_KEY)
                val productCategory = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_CATEGORY_KEY)
                val productBarcode = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_BARCODE_KEY)
                val unitCostString = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_UNIT_COST_KEY)
                val sellingPriceString = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_SELLING_PRICE_KEY)
                val stockQuantity = intent.getIntExtra(ProductInventoryAdapter.PRODUCT_STOCK_KEY, 0)

                val updatedProduct = Product(
                    productID = productID ?: "",
                    productName = productName ?: "",
                    productCategory = productCategory ?: "",
                    productBarcode = productBarcode ?: "",
                    unitCost = BigDecimal(unitCostString ?: "0.00"),
                    sellingPrice = BigDecimal(sellingPriceString ?: "0.00"),
                    stockQuantity = stockQuantity
                )

                // Update the product based on its ID
                val originalIndex = products.indexOfFirst { it.productID == productID }
                if (originalIndex != -1) {
                    products[originalIndex] = updatedProduct
                    applyFilters() // Re-apply filters to refresh the RecyclerView after update
                    Toast.makeText(this, "Product updated successfully!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No changes made / Cancelled Edit.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val addProductLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val intent = result.data
            val newProduct = intent?.getSerializableExtra("newProduct") as? Product
            if (newProduct != null) {
                products.add(0, newProduct) // Add to master list
                applyFilters() // Re-apply filters to show the new product
                Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applyFilters() {
        var tempFilteredList = products.toList()

        // Apply Search Query Filter
        val currentSearchQuery = searchQuery.toLowerCase(Locale.ROOT)
        if (currentSearchQuery.isNotBlank()) {
            tempFilteredList = tempFilteredList.filter {
                // Filter by name or Category
                it.productName.toLowerCase(Locale.ROOT).contains(currentSearchQuery) || it.productCategory.toLowerCase(Locale.ROOT).contains(currentSearchQuery)
            }
        }

        // Apply Category Filters
        if (filterCategory != "All Categories") {
            tempFilteredList = tempFilteredList.filter {
                it.productCategory == filterCategory
            }
        }

        // Update Adapter List
        filteredProducts.clear()
        filteredProducts.addAll(tempFilteredList)
        productAdapter.notifyDataSetChanged() // Notify adapter that its data has changed
    }

    private fun showCategoryFilterDialog() {
        // Ensure categories are based on the master 'products' list
        val categories = products.map { it.productCategory }.distinct().sorted().toMutableList()
        categories.add(0, "All Categories")

        val checkedItem = categories.indexOf(filterCategory)

        AlertDialog.Builder(this)
            .setTitle("Filter by Category")
            .setSingleChoiceItems(categories.toTypedArray(), checkedItem) { dialog, which ->
                filterCategory = categories[which]
                applyFilters() // Apply filters after category selection
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


    private fun showDeleteConfirmationDialog(productToDelete: Product) {
        AlertDialog.Builder(this)
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete '${productToDelete.productName}'?")
            .setPositiveButton("Yes") { dialog, which ->
                // User confirmed, find and remove from master list
                val originalIndex = products.indexOfFirst { it.productID == productToDelete.productID }
                if (originalIndex != -1) {
                    products.removeAt(originalIndex)
                    applyFilters() // Re-apply filters to refresh the RecyclerView
                    Toast.makeText(this, "${productToDelete.productName} deleted!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error: Product not found in master list for deletion.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
                Toast.makeText(this, "Deletion cancelled.", Toast.LENGTH_SHORT).show()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Initialize products (OG list)
        products = DataGenerator.sampleProducts()
        // Initialize filteredProducts for adaptive display
        filteredProducts.addAll(products)

        // Setup recycler view
        // filteredProducts is passed to the adapter (to change when filters are used) instead of the OG product list, and also the deleteConfirmation
        productAdapter = ProductInventoryAdapter(filteredProducts, editProductLauncher, this::showDeleteConfirmationDialog)
        viewBinding.rvInventory.layoutManager = LinearLayoutManager(this)
        viewBinding.rvInventory.adapter = productAdapter

        // Back to Main Button
        viewBinding.tvBack2Main.setOnClickListener {
            finish()
        }

        // Add Product Button (AddProductActivity)
        viewBinding.btnAddProduct.setOnClickListener {
            val intent = Intent(this, AddProductActivity::class.java)
            addProductLauncher.launch(intent)
        }

        // Search / Filter Functionality
        viewBinding.etSearchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s.toString() // Update current search query
                applyFilters() // Re-apply filters whenever text changes
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        viewBinding.btnFilter.setOnClickListener {
            showCategoryFilterDialog()
        }
    }
}