package com.itismob.grpfive.mco

data class Product(
    val id: String,
    val name: String,
    val barcode: String,
    val price: Int,
    val stock: Int = 0
)
