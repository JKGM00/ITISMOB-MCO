package com.itismob.grpfive.mco

import com.google.firebase.firestore.Exclude

data class Transaction(
    @get:Exclude @set:Exclude var transactionID: String = "",
    val items: List<TransactionItem> = emptyList(),
    // change to createdat ...
    val timestampMillis: Long = System.currentTimeMillis() // Timestamp when transaction was created
) {
    // this computed field will help with displaying onscreen
    val totalAmount: Double
        get() {
            return items.sumOf { it.subtotal }
        }
}
