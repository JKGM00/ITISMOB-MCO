package com.itismob.grpfive.mco

import java.math.BigDecimal

data class Transaction(
    val transactionID: String = "",
    val items: List<TransactionItem> = emptyList(),
    val timestampMillis: Long = System.currentTimeMillis() // Timestamp when transaction was created
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
