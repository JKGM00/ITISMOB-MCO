package com.itismob.grpfive.mco

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

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
}
