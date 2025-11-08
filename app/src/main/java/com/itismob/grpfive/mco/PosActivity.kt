package com.itismob.grpfive.mco

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.toObject
import com.itismob.grpfive.mco.databinding.ActivityPosBinding
import com.itismob.grpfive.mco.databinding.DialogAddProductBinding
import com.itismob.grpfive.mco.databinding.DialogScanBarcodeBinding
import java.util.UUID

class PosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPosBinding
    private lateinit var posAdapter: PosAdapter
    private val cartItems = mutableListOf<TransactionItem>()
    private var currentBarcodeScanner: BarcodeScannerHelper? = null
    
    // Firebase Tables
    private val db = FirebaseFirestore.getInstance()
    private val products = db.collection("products")
    private val transactions = db.collection("transactions")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPosBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        
        binding.tvBack2Main.setOnClickListener {
            finish()
        }
        
        binding.btnAddToCart.setOnClickListener {
            showAddProductDialog()
        }
        
        binding.btnScan.setOnClickListener {
            showScanBarcodeDialog()
        }

        binding.btnTotal.setOnClickListener {
            if (cartItems.isNotEmpty()) {
                showConfirmationDialog()
            } else {
                showToast("Cart is empty.")
            }
        }
        
        updateTotal()
    }
    
    private fun findProductByBarcode(barcode: String, onResult: (Product?) -> Unit) {
        products.whereEqualTo("productBarcode", barcode).limit(1).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    onResult(null)
                } else {
                    val product = documents.first().toObject<Product>()
                    product.productID = documents.first().id // Set document ID
                    onResult(product)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("PosActivity", "Error getting product by barcode", exception)
                showToast("Error finding product.")
                onResult(null)
            }
    }

    private fun showAddProductDialog() {
        try {
            val dialogBinding = DialogAddProductBinding.inflate(LayoutInflater.from(this))
            val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()
            
            var selectedProduct: Product? = null
            
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
                        } else {
                            dialogBinding.cvProductInfo.visibility = android.view.View.GONE
                            dialogBinding.tvErrorMessage.visibility = android.view.View.VISIBLE
                            dialogBinding.tilQuantity.visibility = android.view.View.GONE
                        }
                    }
                } else {
                    dialogBinding.cvProductInfo.visibility = android.view.View.GONE
                    dialogBinding.tvErrorMessage.visibility = android.view.View.GONE
                    dialogBinding.tilQuantity.visibility = android.view.View.GONE
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
                    showToast("Please enter a valid barcode.")
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
        if (cartItem != null) {
            if (cartItem.quantity + quantity <= product.stockQuantity) {
                cartItem.quantity += quantity
                showToast("Updated ${product.productName} quantity.")
            } else {
                showToast("Not enough stock for ${product.productName}.")
                return
            }
        } else {
            if (quantity <= product.stockQuantity) {
                val newItem = TransactionItem(
                    productID = product.productID,
                    productName = product.productName,
                    productPrice = product.sellingPrice,
                    quantity = quantity,
                    stockQuantity = product.stockQuantity // Stock Quantity for addBtn to validate
                )
                cartItems.add(newItem)
                showToast("Added ${product.productName} to cart.")
            } else {
                showToast("Not enough stock for ${product.productName}.")
                return
            }
        }
        posAdapter.notifyDataSetChanged()
        updateTotal()
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
        val newTransaction = Transaction(
            transactionID = UUID.randomUUID().toString(),
            items = cartItems,
            createdAt = System.currentTimeMillis()
        )

        db.runTransaction { firestoreTransaction ->
            // 1. Must Read first according to DOCU before write
            val productDetails = cartItems.map { item ->
                val ref = products.document(item.productID)
                val snapshot = firestoreTransaction.get(ref)
                Triple(item, ref, snapshot)
            }

            // 2. If all checks pass, perform all writes.
            val newTimestamp = System.currentTimeMillis()

            for ((item, ref, snapshot) in productDetails) {
                val currentStock = snapshot.getLong("stockQuantity") ?: 0
                if (currentStock < item.quantity) {
                    throw FirebaseFirestoreException(
                        "Not enough stock for ${item.productName}. Only $currentStock left.",
                        FirebaseFirestoreException.Code.ABORTED
                    )
                }
                
                // Calculate new stock level -- update products table
                val newStock = currentStock - item.quantity
                firestoreTransaction.update(ref, "stockQuantity", newStock, "updatedAt", newTimestamp)
            }

            // Record the transaction
            val transactionRef = transactions.document(newTransaction.transactionID)
            // Put / Write 'transaction' into the database
            firestoreTransaction.set(transactionRef, newTransaction)

            null // Return null to indicate success
        }.addOnSuccessListener {
            showToast("Transaction completed successfully!")
            cartItems.clear()
            posAdapter.notifyDataSetChanged()
            updateTotal()
            finish()
        }.addOnFailureListener { e ->
            showToast("Transaction failed: ${e.message}")
            Log.e("PosActivity", "Transaction failed", e)
        }
    }

    private fun updateTotal() {
        val total = cartItems.sumOf { it.subtotal }
        binding.btnTotal.text = "₱${String.format("%.2f", total)}"
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == BarcodeScannerHelper.CAMERA_PERMISSION_REQUEST_CODE) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED
            currentBarcodeScanner?.handlePermissionResult(granted)
        }
    }
}