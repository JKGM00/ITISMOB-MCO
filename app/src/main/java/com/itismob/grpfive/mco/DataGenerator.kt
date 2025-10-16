package com.itismob.grpfive.mco

import java.math.BigDecimal
import java.util.UUID

object DataGenerator {

	fun sampleUsers(): MutableList<User> = mutableListOf(
		User(
			userID = "u001",
			storeName = "Aaron's Store",
			profilePic = R.drawable.profile,
			userEmail = "ajc_barcelita@gmail.com",
			userHashedPw = "DLSU1234!"
		),
		User(
			userID = "u002",
			storeName = "JK's Store",
			profilePic = R.drawable.profile,
			userEmail = "jk_mendoza@gmail.com",
			userHashedPw = "DLSU1234!"
		),
		User(
			userID = "u003",
			storeName = "Enrico's Store",
			profilePic = R.drawable.profile,
			userEmail = "enrico_cariaga@gmail.com",
			userHashedPw = "DLSU1234!"
		)
	)

	// 🛒 1️⃣ Sample products — sari-sari store style with cost + selling price
	fun sampleProducts(): MutableList<Product> = mutableListOf(
		Product(
			productID = "p100",
			productName = "Toyo Cooking Oil 1L",
			productCategory = "Cooking Essentials",
			productBarcode = "8901000001000",
			unitCost = BigDecimal("120.00"),          // store buys at ₱120
			sellingPrice = BigDecimal("160.00"),      // sells at ₱160 (33% markup)
			stockQuantity = 3  // LOW STOCK!
		),
		Product(
			productID = "p101",
			productName = "Biscuit Pack",
			productCategory = "Snacks",
			productBarcode = "8901000001001",
			unitCost = BigDecimal("10.00"),
			sellingPrice = BigDecimal("15.00"),
			stockQuantity = 120
		),
		Product(
			productID = "p102",
			productName = "Canned Sardines",
			productCategory = "Canned Goods",
			productBarcode = "8901000001002",
			unitCost = BigDecimal("18.00"),
			sellingPrice = BigDecimal("25.00"),
			stockQuantity = 7  // LOW STOCK!
		),
		Product(
			productID = "p103",
			productName = "Instant Noodles",
			productCategory = "Instant Food",
			productBarcode = "8901000001003",
			unitCost = BigDecimal("10.00"),
			sellingPrice = BigDecimal("15.00"),
			stockQuantity = 200
		),
		Product(
			productID = "p104",
			productName = "Soap Bar",
			productCategory = "Hygiene",
			productBarcode = "8901000001004",
			unitCost = BigDecimal("30.00"),
			sellingPrice = BigDecimal("45.00"),
			stockQuantity = 10  // LOW STOCK! (at threshold)
		),
		Product(
			productID = "p105",
			productName = "Cigarettes (Pack)",
			productCategory = "Miscellaneous",
			productBarcode = "8901000001005",
			unitCost = BigDecimal("150.00"),
			sellingPrice = BigDecimal("200.00"),
			stockQuantity = 40
		),
		Product(
			productID = "p106",
			productName = "Bottled Water 500ml",
			productCategory = "Drinks",
			productBarcode = "8901000001006",
			unitCost = BigDecimal("10.00"),
			sellingPrice = BigDecimal("20.00"),
			stockQuantity = 300
		),
		Product(
			productID = "p107",
			productName = "Soft Drink 330ml",
			productCategory = "Drinks",
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
	fun sampleTransactions(): MutableList<Transaction> {
		val now = System.currentTimeMillis()
		val oneHourAgo = now - (60 * 60 * 1000)
		val oneDayAgo = now - (24 * 60 * 60 * 1000)
		val oneWeekAgo = now - (7 * 24 * 60 * 60 * 1000)
		val oneMonthAgo = now - (30L * 24 * 60 * 60 * 1000)

		val items1 = listOf(
			makeTransactionItem("p106", 2), // Water
			makeTransactionItem("p103", 1)  // Noodles
		)

		val items2 = listOf(
			makeTransactionItem("p105", 1), // Cigarettes
			makeTransactionItem("p101", 2), // Biscuits
			makeTransactionItem("p104", 1)  // Soap
		)

		val items3 = listOf(
			makeTransactionItem("p100", 1), // Cooking Oil
			makeTransactionItem("p107", 3)  // Soft Drink
		)

		val items4 = listOf(
			makeTransactionItem("p102", 2), // Sardines
			makeTransactionItem("p106", 5)  // Water
		)

		val items5 = listOf(
			makeTransactionItem("p103", 5), // Noodles
			makeTransactionItem("p101", 3)  // Biscuits
		)

		return mutableListOf(
			// Today's transactions
			Transaction(
				transactionID = "tx-${UUID.randomUUID()}",
				items = items1,
				timestampMillis = now
			),
			Transaction(
				transactionID = "tx-${UUID.randomUUID()}",
				items = items2,
				timestampMillis = oneHourAgo
			),
			// Yesterday's transaction
			Transaction(
				transactionID = "tx-${UUID.randomUUID()}",
				items = items3,
				timestampMillis = oneDayAgo
			),
			// Last week's transaction
			Transaction(
				transactionID = "tx-${UUID.randomUUID()}",
				items = items4,
				timestampMillis = oneWeekAgo
			),
			// Last month's transaction
			Transaction(
				transactionID = "tx-${UUID.randomUUID()}",
				items = items5,
				timestampMillis = oneMonthAgo
			)
		)
	}

	// 📷 5️⃣ Simulate scanner lookup by barcode
	fun findByBarcode(barcode: String): Product? =
		sampleProducts().firstOrNull { it.productBarcode == barcode }
}


