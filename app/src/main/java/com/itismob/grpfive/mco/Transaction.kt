package com.itismob.grpfive.mco

import java.math.BigDecimal

data class Transaction(
    val transactionID: String = "",
    val items: List<TransactionItem> = emptyList(),

) {
    // this computed field will help with displaying onscreen
    val totalAmount: BigDecimal
        get() {
            var total = BigDecimal.ZERO
            for (item in items) {
                total = total.add(item.subtotal)
            }

            return total
        }
}
