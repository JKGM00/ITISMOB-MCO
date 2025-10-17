package com.itismob.grpfive.mco

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.itismob.grpfive.mco.databinding.ActivityInventoryBinding
import java.math.BigDecimal

class InventoryActivity : AppCompatActivity() {
    private lateinit var viewBinding : ActivityInventoryBinding
    private lateinit var productAdapter: ProductInventoryAdapter
    private lateinit var products: MutableList<Product>


    private val editProductLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val intent = result.data

            if (intent != null) {
                val position = intent.getIntExtra(ProductInventoryAdapter.POSITION, -1)
                if (position != -1) {
                    val productID = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_ID_KEY)
                    val productName = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_NAME_KEY)
                    val productCategory = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_CATEGORY_KEY)
                    val productBarcode = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_BARCODE_KEY)

                    val unitCostString = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_UNIT_COST_KEY)
                    val sellingPriceString = intent.getStringExtra(ProductInventoryAdapter.PRODUCT_SELLING_PRICE_KEY)
                    val stockQuantity = intent.getIntExtra(ProductInventoryAdapter.PRODUCT_STOCK_KEY, 0)

                    val updatedProduct = Product(
                        productID = productID ?: "",
                        productName = productName ?: "",
                        productCategory = productCategory ?: "",
                        productBarcode = productBarcode ?: "",
                        unitCost = BigDecimal(unitCostString ?: "0.00"),
                        sellingPrice = BigDecimal(sellingPriceString ?: "0.00"),
                        stockQuantity = stockQuantity
                    )
                    products[position] = updatedProduct
                    productAdapter.notifyItemChanged(position)
                }
            }
            else {
                Toast.makeText(this, "No changes made / Cancelled Edit.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val addProductLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        result ->
        if (result.resultCode == RESULT_OK) {
            val intent = result.data
            val newProduct = intent?.getSerializableExtra("newProduct") as? Product
            if (newProduct != null) {
                products.add(newProduct)
                productAdapter.notifyItemInserted(products.size - 1)
                Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        products = DataGenerator.sampleProducts() as MutableList<Product>

        // Setup recycler view
        productAdapter = ProductInventoryAdapter(products, editProductLauncher)
        viewBinding.rvInventory.layoutManager = LinearLayoutManager(this)
        viewBinding.rvInventory.adapter = productAdapter

        viewBinding.tvBack2Main.setOnClickListener {
            finish()
        }

        viewBinding.btnAddProduct.setOnClickListener {
            val intent = Intent(this, AddProductActivity::class.java)
            addProductLauncher.launch(intent)
        }
    }
}