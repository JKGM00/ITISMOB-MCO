package com.itismob.grpfive.mco

import com.google.firebase.firestore.Exclude
import java.io.Serializable

data class Transaction(
    @get:Exclude @set:Exclude var transactionID: String = "",
    val items: List<TransactionItem> = emptyList(),
    val totalAmount: Double = items.sumOf { it.subtotal },
    val createdAt: Long = System.currentTimeMillis()
) : Serializable
