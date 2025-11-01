package com.itismob.grpfive.mco



data class TransactionItem(
    val productID: String = "",
    val productName: String = "",
    val productPrice: Double = 0.0,
    var quantity: Int = 0
) {
    val subtotal: Double
        get() = productPrice * quantity
}

