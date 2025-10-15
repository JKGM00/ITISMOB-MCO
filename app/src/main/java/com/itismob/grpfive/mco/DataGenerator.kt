package com.itismob.grpfive.mco

import java.util.UUID

object DataGenerator {
	// A small set of products typical for a sari-sari store.
	// Barcodes are simplified EAN-like strings for simulation; replace with real codes from supplier if needed.
	fun sampleProducts(): List<Product> = listOf(
	Product(id = "p100", name = "Toyo Oil 1L", barcode = "8901000001000", price = 1800, stock = 20),
	Product(id = "p101", name = "Biscuit Pack", barcode = "8901000001001", price = 120, stock = 120),
	Product(id = "p102", name = "Canned Sardines", barcode = "8901000001002", price = 550, stock = 60),
	Product(id = "p103", name = "Instant Noodles", barcode = "8901000001003", price = 95, stock = 200),
	Product(id = "p104", name = "Soap Bar", barcode = "8901000001004", price = 85, stock = 90),
	Product(id = "p105", name = "Cigarettes (Pack)", barcode = "8901000001005", price = 1200, stock = 40),
	Product(id = "p106", name = "Bottled Water 500ml", barcode = "8901000001006", price = 75, stock = 300),
	Product(id = "p107", name = "Softdrink 330ml", barcode = "8901000001007", price = 150, stock = 180)
	)

	// Helper to create a CartItem from a product id and quantity
	fun makeCartItem(productId: String, qty: Int): CartItem {
		val p = sampleProducts().first { it.id == productId }
		return CartItem(product = p, quantity = qty)
	}

	fun sampleCart(): List<CartItem> = listOf(
		makeCartItem("p103", 3),
		makeCartItem("p106", 2),
		makeCartItem("p101", 1)
	)

	fun sampleTransactions(): List<Transaction> {
		val now = System.currentTimeMillis()

		val items1 = listOf(makeCartItem("p106", 2), makeCartItem("p103", 1))
	val total1 = items1.sumOf { it.product.price * it.quantity }

		val items2 = listOf(makeCartItem("p105", 1), makeCartItem("p101", 2), makeCartItem("p104", 1))
	val total2 = items2.sumOf { it.product.price * it.quantity }

		return listOf(
			Transaction(id = "tx-${UUID.randomUUID()}", items = items1, total = total1, timestampMillis = now - 86_400_000L),
			Transaction(id = "tx-${UUID.randomUUID()}", items = items2, total = total2, timestampMillis = now - 3_600_000L)
		)
	}

	// Lookup by barcode (simulate scanner lookup)
	fun findByBarcode(barcode: String): Product? = sampleProducts().firstOrNull { it.barcode == barcode }
}
