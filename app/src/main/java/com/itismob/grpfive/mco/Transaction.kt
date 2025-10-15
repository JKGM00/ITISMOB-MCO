package com.itismob.grpfive.mco

data class Transaction(
    val id: String,
    val items: List<CartItem>,
    val total: Int,
    val timestampMillis: Long
)
