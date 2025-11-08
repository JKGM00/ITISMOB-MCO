package com.itismob.grpfive.mco

import com.google.firebase.firestore.Exclude

data class Transaction(
    @get:Exclude @set:Exclude var transactionID: String = "",
    val items: List<TransactionItem> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    // this computed field will help with displaying onscreen
    val totalAmount: Double
        get() {
            return items.sumOf { it.subtotal }
        }
}
