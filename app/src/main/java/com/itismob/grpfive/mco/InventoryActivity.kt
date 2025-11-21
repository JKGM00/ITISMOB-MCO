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
import java.util.Locale
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class InventoryActivity : AppCompatActivity() {

    // View Binding & Adapter
    private lateinit var viewBinding : ActivityInventoryBinding
    private lateinit var productAdapter: ProductInventoryAdapter


    // Firebase DB Instance
    private lateinit var db: FirebaseFirestore

    // Database Listener
    private var productListener: ListenerRegistration? = null


    // Product Lists
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
                if (productID == null) {
                    Toast.makeText(this, "Error: Product ID missing.", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }

                // Reconstruct Product Object
                val updatedProduct = Product(
                    productID = productID,
                    productName = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_NAME_KEY) ?: "",
                    productCategory = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_CATEGORY_KEY) ?: "",
                    productBarcode = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_BARCODE_KEY) ?: "",
                    unitCost = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_UNIT_COST_KEY)?.toDoubleOrNull() ?: 0.0,
                    sellingPrice = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_SELLING_PRICE_KEY)?.toDoubleOrNull() ?: 0.0,
                    stockQuantity = intent.getIntExtra(ProductInventoryAdapter.PRODUCT_STOCK_KEY, 0)
                )

                // USE DATABASE HELPER
                DatabaseHelper.updateProduct(updatedProduct,
                    onSuccess = { Toast.makeText(this, "Product updated successfully!", Toast.LENGTH_SHORT).show() },
                    onFailure = { e -> Toast.makeText(this, "Error updating product: ${e.message}", Toast.LENGTH_SHORT).show() })
            } else {
                Toast.makeText(this, "No changes made.", Toast.LENGTH_SHORT).show()
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
                // USE DATABASE HELPER
                DatabaseHelper.addProduct(newProduct,
                    onSuccess = {
                        Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { e ->
                        Toast.makeText(this, "Error adding product: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    private fun applyFilters() {
        var tempFilteredList = products.toList()

        // Apply Search Query Filter
        val currentSearchQuery = searchQuery.lowercase(Locale.ROOT)
        if (currentSearchQuery.isNotBlank()) {
            tempFilteredList = tempFilteredList.filter {
                it.productName.lowercase(Locale.ROOT).contains(currentSearchQuery) ||
                        it.productCategory.lowercase(Locale.ROOT).contains(currentSearchQuery)
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
        productAdapter.notifyDataSetChanged()
    }

    private fun showCategoryFilterDialog() {
        val categories = products.map { it.productCategory }.distinct().sorted().toMutableList()
        categories.add(0, "All Categories")

        val checkedItem = categories.indexOf(filterCategory)

        AlertDialog.Builder(this)
            .setTitle("Filter by Category")
            .setSingleChoiceItems(categories.toTypedArray(), checkedItem) { dialog, which ->
                filterCategory = categories[which]
                applyFilters()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showDeleteConfirmationDialog(productToDelete: Product) {
        AlertDialog.Builder(this)
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete '${productToDelete.productName}'?")
            .setPositiveButton("Yes") { _, _ ->
                if (productToDelete.productID.isEmpty()) {
                    Toast.makeText(this, "Error: Product ID is missing.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // USE DATABASE HELPER
                DatabaseHelper.deleteProduct(productToDelete.productID,
                    onSuccess = {
                        Toast.makeText(this, "'${productToDelete.productName}' deleted successfully!", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { e ->
                        Toast.makeText(this, "Error deleting: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Initialize products list
        products = mutableListOf()

        // Setup recycler view
        productAdapter = ProductInventoryAdapter(filteredProducts, editProductLauncher, this::showDeleteConfirmationDialog)
        viewBinding.rvInventory.layoutManager = LinearLayoutManager(this)
        viewBinding.rvInventory.adapter = productAdapter

        // Back to Main Button
        viewBinding.tvBack2Main.setOnClickListener { finish() }

        // Add Product Button
        viewBinding.btnAddProduct.setOnClickListener {
            val intent = Intent(this, AddProductActivity::class.java)
            addProductLauncher.launch(intent)
        }

        // Search / Filter Functionality
        viewBinding.etSearchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s.toString()
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        viewBinding.btnFilter.setOnClickListener { showCategoryFilterDialog() }
    }

    override fun onStart() {
        super.onStart()
        listenProducts()
    }

    override fun onStop() {
        super.onStop()
        productListener?.remove()
    }

    private fun listenProducts() {
        productListener = DatabaseHelper.listenToProducts(
            onUpdate = { updatedList ->
                products.clear()
                products.addAll(updatedList)
                applyFilters() // Refresh UI
            },
            onError = { e -> Toast.makeText(this, "Error fetching products: ${e.message}", Toast.LENGTH_LONG).show() })
    }

}
