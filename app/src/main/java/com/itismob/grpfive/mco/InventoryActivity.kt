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
import com.google.firebase.auth.FirebaseAuth
import com.itismob.grpfive.mco.adapters.ProductInventoryAdapter
import com.itismob.grpfive.mco.databinding.ActivityInventoryBinding
import com.itismob.grpfive.mco.models.Product
import java.util.Locale
import com.google.firebase.firestore.ListenerRegistration

class InventoryActivity : AppCompatActivity() {

    // View Binding & Adapter
    private lateinit var binding : ActivityInventoryBinding
    private lateinit var productAdapter: ProductInventoryAdapter

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
        updateEmptyState()
    }

    private fun updateEmptyState() {
        if (filteredProducts.isEmpty()) {
            binding.rvInventory.visibility = android.view.View.GONE
            binding.emptyStateContainer.visibility = android.view.View.VISIBLE
        } else {
            binding.rvInventory.visibility = android.view.View.VISIBLE
            binding.emptyStateContainer.visibility = android.view.View.GONE
        }
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
        binding = ActivityInventoryBinding.inflate(layoutInflater)
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

        // Initialize products list
        products = mutableListOf()

        // Setup recycler view
        productAdapter = ProductInventoryAdapter(filteredProducts, editProductLauncher, this::showDeleteConfirmationDialog)
        binding.rvInventory.layoutManager = LinearLayoutManager(this)
        binding.rvInventory.adapter = productAdapter

        // Search / Filter Functionality
        binding.etSearchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s.toString()
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnFilter.setOnClickListener { showCategoryFilterDialog() }
        binding.btnFilter.setOnLongClickListener {
            Toast.makeText(this, "Filter by Category", Toast.LENGTH_SHORT).show()
            true
        }
        
        setupNavigation()
    }

    override fun onStart() {
        super.onStart()
        listenProducts()
    }

    override fun onStop() {
        super.onStop()
        productListener?.remove()
    }

    private fun setupNavigation() {
        binding.tvNavProfile.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        binding.tvNavDashboard.setOnClickListener { navigateTo(DashboardActivity::class.java) }
        binding.btnAddProductPage.setOnClickListener { startActivity(Intent(this, AddProductActivity::class.java)) }
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
