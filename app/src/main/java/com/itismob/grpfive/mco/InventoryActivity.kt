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

class InventoryActivity : AppCompatActivity() {

    // View Binding & Adapter
    private lateinit var viewBinding : ActivityInventoryBinding
    private lateinit var productAdapter: ProductInventoryAdapter

    // Firebase DB Instance
    private lateinit var db: FirebaseFirestore


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

                val productName = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_NAME_KEY)
                val productCategory = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_CATEGORY_KEY)
                val productBarcode = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_BARCODE_KEY)
                val unitCostString = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_UNIT_COST_KEY)
                val sellingPriceString = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_SELLING_PRICE_KEY)
                val stockQuantity = intent.getIntExtra(ProductInventoryAdapter.PRODUCT_STOCK_KEY, 0)

                val updatedProduct = Product(
                    productID = productID,
                    productName = productName ?: "",
                    productCategory = productCategory ?: "",
                    productBarcode = productBarcode ?: "",
                    unitCost = unitCostString?.toDoubleOrNull() ?: 0.0,
                    sellingPrice = sellingPriceString?.toDoubleOrNull() ?: 0.0,
                    stockQuantity = stockQuantity
                )

                // Update the product in Firestore
                db.collection("products").document(productID)
                    .set(updatedProduct)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Product updated successfully!", Toast.LENGTH_SHORT).show()
                        // The snapshot listener in fetchProducts() auto refresh UI
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error updating product: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "No changes made / Edit Cancelled.", Toast.LENGTH_SHORT).show()
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
                // Add the new product to Firestore. The productID is already generated in AddProductActivity.
                db.collection("products").document(newProduct.productID)
                    .set(newProduct)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show()
                        // The snapshot listener in fetchProducts() auto refresh UI
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error adding product: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun applyFilters() {
        var tempFilteredList = products.toList()

        // Apply Search Query Filter
        val currentSearchQuery = searchQuery.lowercase(Locale.ROOT)
        if (currentSearchQuery.isNotBlank()) {
            tempFilteredList = tempFilteredList.filter {
                // Filter by name or Category
                it.productName.lowercase(Locale.ROOT).contains(currentSearchQuery) || it.productCategory.lowercase(Locale.ROOT).contains(currentSearchQuery)
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
            .setPositiveButton("Yes") { _, _ ->
                if (productToDelete.productID.isEmpty()) {
                    Toast.makeText(this, "Error: Product ID is missing.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                // Delete the document from Firestore
                db.collection("products").document(productToDelete.productID)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "'${productToDelete.productName}' deleted successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error deleting product: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        db = FirebaseFirestore.getInstance()

        // Initialize products list
        products = mutableListOf()

        // Setup recycler view
        productAdapter = ProductInventoryAdapter(filteredProducts, editProductLauncher, this::showDeleteConfirmationDialog)
        viewBinding.rvInventory.layoutManager = LinearLayoutManager(this)
        viewBinding.rvInventory.adapter = productAdapter

        // Fetch products from DB after binding
        fetchProducts()

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

    private fun fetchProducts() {
        // Get all 'products' from Firestore
        db.collection("products")
            // Attach listener to collection
            .addSnapshotListener { snapshots, err ->
                if (err != null) {
                    Toast.makeText(this, "Error fetching products: ${err.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    // Temp list for new data
                    val updatedProducts = mutableListOf<Product>()
                    for (document in snapshots.documents) {
                        val product = document.toObject(Product::class.java)
                        // If object is created, set productID and add to list
                        if (product != null) {
                            product.productID = document.id // Set the Firestore document ID to productID
                            updatedProducts.add(product)
                        }
                    }
                    products.clear()
                    products.addAll(updatedProducts)
                    applyFilters() // Re-apply filters and notify adapter with the new data
                }
            }
    }

}
