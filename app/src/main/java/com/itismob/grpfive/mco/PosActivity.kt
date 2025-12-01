package com.itismob.grpfive.mco

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.itismob.grpfive.mco.adapters.PosAdapter
import com.itismob.grpfive.mco.adapters.ProductSimpleAdapter
import com.itismob.grpfive.mco.databinding.ActivityPosBinding
import com.itismob.grpfive.mco.databinding.DialogAddProductBinding
import com.itismob.grpfive.mco.databinding.DialogScanBarcodeBinding
import com.itismob.grpfive.mco.models.TransactionItem
import com.itismob.grpfive.mco.models.Transaction
import com.itismob.grpfive.mco.models.Product


class PosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPosBinding
    private lateinit var posAdapter: PosAdapter
    private val cartItems = mutableListOf<TransactionItem>()
    private var currentBarcodeScanner: BarcodeScannerHelper? = null
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "User session expired. Please log in again.", Toast.LENGTH_LONG).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        // Set up RecyclerView
        posAdapter = PosAdapter(
            cartItems,
            onDelete = { item ->
                cartItems.remove(item)
                posAdapter.notifyDataSetChanged()
                updateTotal()
            },
            onQuantityChanged = {
                updateTotal()
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = posAdapter

        loadCartState()

        binding.btnAddToCart.setOnClickListener { showAddProductDialog() }
        binding.btnScan.setOnClickListener { showScanBarcodeDialog() }

        binding.btnTotal.setOnClickListener {
            if (cartItems.isNotEmpty()) {
                showConfirmationDialog()
            } else {
                showToast("Cart is empty. Please add something.")
            }
        }

        setupNavigation()
        updateTotal()
    }

    override fun onPause() {
        super.onPause()
        saveCartState()
    }

    private fun setupNavigation() {
        binding.tvNavProfile.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        binding.tvNavDashboard.setOnClickListener { navigateTo(DashboardActivity::class.java) }
        binding.tvNavInventory.setOnClickListener { navigateTo(InventoryActivity::class.java) }
        binding.btnAddProductPage.setOnClickListener { startActivity(Intent(this, AddProductActivity::class.java)) }
        binding.tvNavHistory.setOnClickListener { navigateTo(TransactionHistoryActivity::class.java) }
    }

    private fun navigateTo(destination: Class<*>) {
        val intent = Intent(this, destination)
        // Clear back stack so the user returns to a clean state
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun saveCartState() {
        val sharedPreferences = getSharedPreferences("CartPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(cartItems)
        editor.putString("cartItems", json)
        editor.apply()
    }

    private fun loadCartState() {
        val sharedPreferences = getSharedPreferences("CartPrefs", MODE_PRIVATE)
        val json = sharedPreferences.getString("cartItems", null)

        if (json != null) {
            val type = object : TypeToken<List<TransactionItem>>() {}.type
            cartItems.addAll(Gson().fromJson(json, type))
            posAdapter.notifyDataSetChanged()
            updateTotal()
        }
    }

    private fun updateTotal() {
        val total = cartItems.sumOf { it.subtotal }
        binding.btnTotal.text = String.format("Total: ₱%.2f", total)
        updateEmptyState()
    }

    private fun updateEmptyState() {
        if (cartItems.isEmpty()) {
            binding.recyclerView.visibility = android.view.View.GONE
            binding.emptyStateContainer.visibility = android.view.View.VISIBLE
        } else {
            binding.recyclerView.visibility = android.view.View.VISIBLE
            binding.emptyStateContainer.visibility = android.view.View.GONE
        }
    }

    // Find product within current user's inventory
    private fun findProductByBarcode(barcode: String, onResult: (Product?) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            showToast("User not logged in")
            onResult(null)
            return
        }

        // Query specific user's products
        db.collection("users").document(uid).collection("products")
            .whereEqualTo("productBarcode", barcode)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    onResult(null)
                } else {
                    val product = documents.first().toObject(Product::class.java)
                    product.productID = documents.first().id
                    onResult(product)
                }
            }
            .addOnFailureListener {
                showToast("Error finding product.")
                onResult(null)
            }
    }

    private fun showAddProductDialog() {
        try {
            val dialogBinding = DialogAddProductBinding.inflate(LayoutInflater.from(this))
            val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()

            var selectedProduct: Product? = null
            val allProducts = mutableListOf<Product>()
            val productAdapter = ProductSimpleAdapter(allProducts) { product ->
                // Handle product selection from RecyclerView
                selectedProduct = product
                dialogBinding.etBarcode.setText(product.productBarcode)
                dialogBinding.cvProductInfo.visibility = android.view.View.VISIBLE
                dialogBinding.tvProductName.text = product.productName
                dialogBinding.tvProductPrice.text = String.format("₱%.2f", product.sellingPrice)
                dialogBinding.tvErrorMessage.visibility = android.view.View.GONE
                dialogBinding.tilQuantity.visibility = android.view.View.VISIBLE
                dialogBinding.rvProducts.visibility = android.view.View.GONE
                dialogBinding.etQuantity.setText("1")
            }

            // Setup RecyclerView
            dialogBinding.rvProducts.layoutManager = LinearLayoutManager(this)
            dialogBinding.rvProducts.adapter = productAdapter

            // Load all products initially
            DatabaseHelper.getAllProducts(
                onSuccess = { products ->
                    allProducts.clear()
                    allProducts.addAll(products)
                    productAdapter.notifyDataSetChanged()
                    if (allProducts.isNotEmpty()) {
                        dialogBinding.rvProducts.visibility = android.view.View.VISIBLE
                    }
                },
                onFailure = {
                    showToast("Failed to load products")
                }
            )

            dialogBinding.etBarcode.doAfterTextChanged { text ->
                val barcode = text.toString().trim()
                if (barcode.isNotEmpty()) {
                    findProductByBarcode(barcode) { product ->
                        selectedProduct = product
                        if (selectedProduct != null) {
                            dialogBinding.cvProductInfo.visibility = android.view.View.VISIBLE
                            dialogBinding.tvProductName.text = selectedProduct!!.productName
                            dialogBinding.tvProductPrice.text = String.format("₱%.2f", selectedProduct!!.sellingPrice)
                            dialogBinding.tvErrorMessage.visibility = android.view.View.GONE
                            dialogBinding.tilQuantity.visibility = android.view.View.VISIBLE
                            dialogBinding.rvProducts.visibility = android.view.View.GONE
                        } else {
                            dialogBinding.cvProductInfo.visibility = android.view.View.GONE
                            dialogBinding.tvErrorMessage.visibility = android.view.View.VISIBLE
                            dialogBinding.tilQuantity.visibility = android.view.View.GONE
                            dialogBinding.rvProducts.visibility = android.view.View.GONE
                        }
                    }
                } else {
                    dialogBinding.cvProductInfo.visibility = android.view.View.GONE
                    dialogBinding.tvErrorMessage.visibility = android.view.View.GONE
                    dialogBinding.tilQuantity.visibility = android.view.View.GONE
                    dialogBinding.rvProducts.visibility = android.view.View.VISIBLE
                }
            }

            dialogBinding.btnCancelItem.setOnClickListener { dialog.dismiss() }

            dialogBinding.btnAddtoTransc.setOnClickListener {
                if (selectedProduct != null) {
                    val quantity = dialogBinding.etQuantity.text.toString().toIntOrNull() ?: 1
                    if (quantity > 0) {
                        addProductToCart(selectedProduct!!, quantity)
                        dialog.dismiss()
                    } else {
                        showToast("Please enter a valid quantity.")
                    }
                } else {
                    showToast("Please select a product.")
                }
            }
            dialog.show()
        } catch (e: Exception) {
            showToast("Error creating dialog: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun showScanBarcodeDialog() {
        try {
            val dialogBinding = DialogScanBarcodeBinding.inflate(LayoutInflater.from(this))
            val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()

            var selectedProduct: Product? = null

            currentBarcodeScanner = BarcodeScannerHelper(this, dialogBinding.cameraPreview) { scannedBarcode ->
                runOnUiThread {
                    when {
                        scannedBarcode == "PERMISSION_DENIED" -> {
                            showToast("Camera permission denied.")
                            dialog.dismiss()
                        }
                        scannedBarcode.startsWith("CAMERA_ERROR") -> {
                            showToast("Camera error: $scannedBarcode")
                        }
                        else -> {
                            dialogBinding.tvScannedBarcode.text = "Scanned: $scannedBarcode"
                            findProductByBarcode(scannedBarcode) { product ->
                                selectedProduct = product
                                if (selectedProduct != null) {
                                    dialogBinding.cvProductInfo.visibility = android.view.View.VISIBLE
                                    dialogBinding.tvProductName.text = selectedProduct!!.productName
                                    dialogBinding.tvProductPrice.text = String.format("₱%.2f", selectedProduct!!.sellingPrice)
                                    dialogBinding.tvErrorMessage.visibility = android.view.View.GONE
                                    dialogBinding.tilQuantity.visibility = android.view.View.VISIBLE
                                } else {
                                    dialogBinding.cvProductInfo.visibility = android.view.View.GONE
                                    dialogBinding.tvErrorMessage.visibility = android.view.View.VISIBLE
                                    dialogBinding.tilQuantity.visibility = android.view.View.GONE
                                }
                            }
                        }
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
                if (selectedProduct != null) {
                    val quantity = dialogBinding.etQuantity.text.toString().toIntOrNull() ?: 1
                    if (quantity > 0) {
                        addProductToCart(selectedProduct!!, quantity)
                        dialog.dismiss()
                    } else {
                        showToast("Please enter a valid quantity.")
                    }
                } else {
                    showToast("Please scan a valid barcode.")
                }
            }

            dialog.show()
        } catch (e: Exception) {
            showToast("Error creating scan dialog: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun addProductToCart(product: Product, quantity: Int) {
        val cartItem = cartItems.find { it.productID == product.productID }

        // Check total stock needed
        val totalRequested = (cartItem?.quantity ?: 0) + quantity

        if (totalRequested <= product.stockQuantity) {
            if (cartItem != null) {
                cartItem.quantity += quantity
                showToast("Updated ${product.productName} quantity.")
            } else {
                val newItem = TransactionItem(
                    productID = product.productID,
                    productName = product.productName,
                    productCategory = product.productCategory,
                    productPrice = product.sellingPrice,
                    quantity = quantity,
                    stockQuantity = product.stockQuantity
                )
                cartItems.add(newItem)
                showToast("Added ${product.productName} to cart.")
            }
            posAdapter.notifyDataSetChanged()
            updateTotal()
        } else {
            showToast("Not enough stock. Only ${product.stockQuantity} available.")
        }
    }


    private fun showConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Complete Transaction?")
            .setMessage("Finalize this transaction? This will deduct from inventory.")
            .setPositiveButton("Yes") { _, _ ->
                processTransaction()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun processTransaction() {
        val newTransaction = Transaction(items = cartItems, createdAt = System.currentTimeMillis())

        DatabaseHelper.addTransaction(newTransaction,
            onSuccess = {
                showToast("Transaction Completed Successfully!")
                cartItems.clear()

                // Clear save state of prefs
                getSharedPreferences("CartPrefs", MODE_PRIVATE).edit().remove("cartItems").apply()

                posAdapter.notifyDataSetChanged()
                updateTotal()
            },
            onFailure = { e ->
                showToast("Transaction Failed: ${e.message}")
            }
        )
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}