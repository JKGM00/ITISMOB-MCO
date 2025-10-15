package com.itismob.grpfive.mco

// CartItem now holds a reference to the Product for easy access to product fields (barcode, price, stock, etc.)
data class CartItem(
    val product: Product,
    var quantity: Int
)
