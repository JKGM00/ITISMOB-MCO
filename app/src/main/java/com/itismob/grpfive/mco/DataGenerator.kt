package com.itismob.grpfive.mco

import java.math.BigDecimal
import java.util.UUID

object DataGenerator {

	// 🛒 1️⃣ Sample products — sari-sari store style with cost + selling price
	fun sampleProducts(): List<Product> = listOf(
		Product(
			productID = "p100",
			productName = "Toyo Cooking Oil 1L",
			productCategory = "Cooking Oil",
			productImage = 0,
			productBarcode = "8901000001000",
			unitCost = BigDecimal("120.00"),          // store buys at ₱120
			sellingPrice = BigDecimal("160.00"),      // sells at ₱160 (33% markup)
			stockQuantity = 20
		),
		Product(
			productID = "p101",
			productName = "Biscuit Pack",
			productCategory = "Snacks",
			productImage = 0,
			productBarcode = "8901000001001",
			unitCost = BigDecimal("10.00"),
			sellingPrice = BigDecimal("15.00"),
			stockQuantity = 120
		),
		Product(
			productID = "p102",
			productName = "Canned Sardines",
			productCategory = "Canned Goods",
			productImage = 0,
			productBarcode = "8901000001002",
			unitCost = BigDecimal("18.00"),
			sellingPrice = BigDecimal("25.00"),
			stockQuantity = 60
		),
		Product(
			productID = "p103",
			productName = "Instant Noodles",
			productCategory = "Instant Food",
			productImage = 0,
			productBarcode = "8901000001003",
			unitCost = BigDecimal("10.00"),
			sellingPrice = BigDecimal("15.00"),
			stockQuantity = 200
		),
		Product(
			productID = "p104",
			productName = "Soap Bar",
			productCategory = "Hygiene",
			productImage = 0,
			productBarcode = "8901000001004",
			unitCost = BigDecimal("30.00"),
			sellingPrice = BigDecimal("45.00"),
			stockQuantity = 90
		),
		Product(
			productID = "p105",
			productName = "Cigarettes (Pack)",
			productCategory = "Miscellaneous",
			productImage = 0,
			productBarcode = "8901000001005",
			unitCost = BigDecimal("150.00"),
			sellingPrice = BigDecimal("200.00"),
			stockQuantity = 40
		),
		Product(
			productID = "p106",
			productName = "Bottled Water 500ml",
			productCategory = "Drinks",
			productImage = 0,
			productBarcode = "8901000001006",
			unitCost = BigDecimal("10.00"),
			sellingPrice = BigDecimal("20.00"),
			stockQuantity = 300
		),
		Product(
			productID = "p107",
			productName = "Soft Drink 330ml",
			productCategory = "Drinks",
			productImage = 0,
			productBarcode = "8901000001007",
			unitCost = BigDecimal("25.00"),
			sellingPrice = BigDecimal("40.00"),
			stockQuantity = 180
		)
	)

	// 🧺 2️⃣ Helper: make a TransactionItem (cart item)
	fun makeTransactionItem(productID: String, quantity: Int): TransactionItem {
		val product = sampleProducts().first { it.productID == productID }
		return TransactionItem(
			productID = product.productID,
			productName = product.productName,
			productPrice = product.sellingPrice, // use sellingPrice now
			quantity = quantity
		)
	}

	// 🛍️ 3️⃣ Sample "cart" for UI demo
	fun sampleCart(): List<TransactionItem> = listOf(
		makeTransactionItem("p103", 3), // Instant noodles x3
		makeTransactionItem("p106", 2), // Bottled water x2
		makeTransactionItem("p101", 1)  // Biscuit pack x1
	)

	// 💵 4️⃣ Sample transactions (for reports or history)
	fun sampleTransactions(): List<Transaction> {

		val items1 = listOf(
			makeTransactionItem("p106", 2),
			makeTransactionItem("p103", 1)
		)

		val items2 = listOf(
			makeTransactionItem("p105", 1),
			makeTransactionItem("p101", 2),
			makeTransactionItem("p104", 1)
		)

		return listOf(
			Transaction(
				transactionID = "tx-${UUID.randomUUID()}",
				items = items1
			),
			Transaction(
				transactionID = "tx-${UUID.randomUUID()}",
				items = items2
			)
		)
	}

	// 📷 5️⃣ Simulate scanner lookup by barcode
	fun findByBarcode(barcode: String): Product? =
		sampleProducts().firstOrNull { it.productBarcode == barcode }
}


