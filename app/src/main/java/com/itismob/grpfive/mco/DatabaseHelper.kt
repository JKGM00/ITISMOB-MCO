package com.itismob.grpfive.mco

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.itismob.grpfive.mco.models.User
import com.itismob.grpfive.mco.models.Product
import com.itismob.grpfive.mco.models.Transaction

object DatabaseHelper {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Get the current User ID
    private val currentUserId: String?
        get() = auth.currentUser?.uid

    // --- CRUD OPERATIONS ---

    fun createUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(user.userID)
            .set(user)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getUser(userId: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    user.userID = document.id
                    onSuccess(user)
                } else {
                    onFailure(Exception("User data is null"))
                }
            }
            .addOnFailureListener { onFailure(it) }
    }


    // Add or Update Product
    fun addProduct(product: Product, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val uid = currentUserId
        if (uid == null) {
            onFailure(Exception("User not logged in"))
            return
        }

        db.collection("users").document(uid).collection("products")
            .document(product.productID)
            .set(product)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun updateProduct(product: Product, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        addProduct(product, onSuccess, onFailure)
    }

    fun deleteProduct(productId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val uid = currentUserId
        if (uid == null) {
            onFailure(Exception("User not logged in"))
            return
        }

        db.collection("users").document(uid).collection("products")
            .document(productId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // Creates connection to app and DB and keeps watch for any changes
    fun listenToProducts(onUpdate: (List<Product>) -> Unit, onError: (Exception) -> Unit): ListenerRegistration? {
        val uid = currentUserId
        if (uid == null) {
            onError(Exception("User not logged in"))
            return null
        }

        // Ensures that users -> uid -> view their products only
        return db.collection("users").document(uid).collection("products")
            .addSnapshotListener { snapshots, err ->
                if (err != null) {
                    onError(err)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val productList = snapshots.documents.mapNotNull { doc ->
                        // Set the productID to ensure it matches the document ID
                        doc.toObject(Product::class.java)?.apply {
                            productID = doc.id
                        }
                    }
                    onUpdate(productList)
                }
            }
    }

    fun addTransaction(transaction: Transaction, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val uid = currentUserId
        if (uid == null) {
            onFailure(Exception("No user logged in"))
            return
        }

        // 1. Generate a new ID for the transaction
        val newRef = db.collection("users").document(uid).collection("transactions").document()
        transaction.transactionID = newRef.id

        // 2. Save transaction
        newRef.set(transaction)
            .addOnSuccessListener {
                updateStock(transaction)
                onSuccess()
            }
            .addOnFailureListener { onFailure(it) }
    }


    // Pass productID to exclude self when editing barcode
    fun checkProductDuplicates(barcode: String, name: String, productId: String? = null, onResult: (barcodeExists: Boolean, nameExists: Boolean) -> Unit, onFailure: (Exception) -> Unit) {
        val uid = currentUserId
        if (uid == null) {
            onFailure(Exception("User not logged in"))
            return
        }

        val productsRef = db.collection("users").document(uid).collection("products")

        productsRef.whereEqualTo("productBarcode", barcode).get()
            .addOnSuccessListener { barcodeSnap ->
                // If editing, ignore the document if it matches own ID
                val barcodeExists = barcodeSnap.documents.any { doc ->
                    productId == null || doc.id != productId
                }

                /* When EDITING, check if the product
                    is same as the one being edited.
                    If it is same, not marked as duplicate.
                    (Error when if it is another from the list)
                */

                productsRef.whereEqualTo("productName", name).get()
                    .addOnSuccessListener { nameSnap ->
                        val nameExists = nameSnap.documents.any { doc ->
                            productId == null || doc.id != productId
                        }
                        onResult(barcodeExists, nameExists)
                    }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun listenToTransactions(onUpdate: (List<Transaction>) -> Unit, onError: (Exception) -> Unit): ListenerRegistration? {
        val uid = currentUserId
        if (uid == null) {
            onError(Exception("User not logged in"))
            return null
        }

        return db.collection("users").document(uid).collection("transactions")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, err ->
                if (err != null) {
                    onError(err)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val transactionList = snapshots.documents.mapNotNull { doc ->
                        doc.toObject(Transaction::class.java)?.apply {
                            transactionID = doc.id
                        }
                    }
                    onUpdate(transactionList)
                }
            }
    }

    private fun updateStock(transaction: Transaction) {
        val uid = currentUserId ?: return
        val batch = db.batch()

        transaction.items.forEach { item ->
            val productRef =
                db.collection("users").document(uid).collection("products").document(item.productID)
            // Decrement stock
            batch.update(
                productRef,
                "stockQuantity",
                com.google.firebase.firestore.FieldValue.increment(-item.quantity.toLong())
            )
        }
        batch.commit()
    }

    fun getTransactionsForPeriod(start: Long, end: Long, onSuccess: (List<Transaction>) -> Unit, onFailure: (Exception) -> Unit) {
        val uid = currentUserId ?: return onFailure(Exception("User is not logged in."))

        db.collection("users").document(uid).collection("transactions")
            .whereGreaterThanOrEqualTo("createdAt", start)
            .whereLessThanOrEqualTo("createdAt", end)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Transaction::class.java)?.apply {
                        transactionID = doc.id
                    }
                }
                onSuccess(list)
            }
            .addOnFailureListener(onFailure)
    }

    fun calculateTotalRevenue(transactions: List<Transaction>): Double {
        return transactions.sumOf { it.totalAmount }
    }

    fun getAllProducts(onSuccess: (List<Product>) -> Unit, onFailure: (Exception) -> Unit) {
        val uid = currentUserId
        if (uid == null) {
            onFailure(Exception("User not logged in"))
            return
        }

        db.collection("users").document(uid).collection("products")
            .get()
            .addOnSuccessListener { snapshot ->
                val products = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.apply {
                        productID = doc.id
                    }
                }
                onSuccess(products)
            }
            .addOnFailureListener(onFailure)
    }

    fun getTopCategories(transactions: List<Transaction>): List<Pair<String, Double>> {
        // Map to hold total sales per category
        val categoryTotals = mutableMapOf<String, Double>()

        transactions.forEach { transaction ->
            transaction.items.forEach { item ->
                val category = item.productCategory
                categoryTotals[category] = (categoryTotals[category] ?: 0.0) + (item.subtotal ?: 0.0)
            }
        }

        // Sort descending by total
        return categoryTotals.entries
            .sortedByDescending { it.value }
            .map { it.key to it.value }  // Convert to Pair<String, Double>
    }
}
