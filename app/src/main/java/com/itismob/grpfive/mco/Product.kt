package com.itismob.grpfive.mco

import com.google.firebase.firestore.Exclude
import java.io.Serializable

data class Product(
    // Document ID in Firestore
    @get:Exclude @set:Exclude var productID: String = "",
    val productName: String = "",
    val productCategory: String = "",
    val productBarcode: String = "",
    val unitCost: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val stockQuantity: Int = 0,
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),
) : Serializable