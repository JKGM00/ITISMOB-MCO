package com.itismob.grpfive.mco

import java.math.BigDecimal

data class TransactionItem(
    val productID: String = "",
    val productName: String = "",
    val productPrice: BigDecimal = BigDecimal.ZERO,
    var quantity: Int = 0
) {
    val subtotal: BigDecimal
        get() = productPrice.multiply(BigDecimal(quantity))
}

