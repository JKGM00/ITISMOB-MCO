package com.itismob.grpfive.mco.models

import com.google.firebase.firestore.Exclude
import java.io.Serializable

data class TransactionItem(
    val productID: String = "",
    val productName: String = "",
    val productCategory: String = "",
    val productPrice: Double = 0.0,
    var quantity: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),

    /*
    This is to 'get' the stock quantity of the product from DB when in the POS system
    To easily implement the input validation of stock quantity in addBtn
    */
    @get:Exclude var stockQuantity: Int = 0
) : Serializable {
    val subtotal: Double
        get() = productPrice * quantity
}