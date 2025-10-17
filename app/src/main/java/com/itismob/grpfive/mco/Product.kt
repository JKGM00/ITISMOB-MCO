package com.itismob.grpfive.mco

import java.math.BigDecimal
import java.io.Serializable

data class Product(
    val productID: String = "",
    val productName: String = "",
    val productCategory: String = "",
    val productBarcode: String = "",
    val unitCost: BigDecimal = BigDecimal.ZERO,
    val sellingPrice: BigDecimal = BigDecimal.ZERO,
    val stockQuantity: Int = 0
) : Serializable
