package com.itismob.grpfive.mco

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import com.itismob.grpfive.mco.databinding.ItemInventoryProductBinding

class ProductInventoryAdapter(
    private val currentProducts: MutableList<Product>,
    private val editProductLauncher: ActivityResultLauncher<Intent>,
    private val onDeleteProduct: (Product) -> Unit
) : RecyclerView.Adapter<ProductViewHolder>() {

    companion object {
        const val PRODUCT_ID_KEY = "PRODUCT_ID"
        const val PRODUCT_NAME_KEY = "PRODUCT_NAME"
        const val PRODUCT_CATEGORY_KEY = "PRODUCT_CATEGORY"
        const val PRODUCT_BARCODE_KEY = "PRODUCT_BARCODE"
        const val PRODUCT_UNIT_COST_KEY = "PRODUCT_UNIT_COST"
        const val PRODUCT_SELLING_PRICE_KEY = "PRODUCT_SELLING_PRICE"
        const val PRODUCT_STOCK_KEY = "PRODUCT_STOCK"
        const val POSITION = "POSITION"

        private val categoryImageMap = mapOf(
            "Cooking Essentials" to R.drawable.cooking_essentials,
            "Snacks" to R.drawable.snack,
            "Drinks" to R.drawable.drinks,
            "Canned Goods" to R.drawable.canned_goods,
            "Instant Food" to R.drawable.instant_food,
            "Hygiene" to R.drawable.hygiene,
            "Miscellaneous" to R.drawable.miscellaneous,
        )

        // Function to get image resource based on category
        fun getCategoryImageResource(category: String): Int {
            return categoryImageMap[category] ?: R.drawable.logo
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemViewBinding =
            ItemInventoryProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(itemViewBinding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = currentProducts[position] // Use currentProducts here
        holder.bindData(product)

        // Edit Button
        holder.itemViewBinding.btnEditProduct.setOnClickListener {
            val intent = Intent(holder.itemView.context, EditProductActivity::class.java).apply {
                putExtra(PRODUCT_ID_KEY, product.productID)
                putExtra(PRODUCT_NAME_KEY, product.productName)
                putExtra(PRODUCT_CATEGORY_KEY, product.productCategory)
                putExtra(PRODUCT_BARCODE_KEY, product.productBarcode)
                putExtra(PRODUCT_UNIT_COST_KEY, product.unitCost.toPlainString())
                putExtra(PRODUCT_SELLING_PRICE_KEY, product.sellingPrice.toPlainString())
                putExtra(PRODUCT_STOCK_KEY, product.stockQuantity)

                putExtra(POSITION, position)
            }
            editProductLauncher.launch(intent)
        }

        // Delete Button
        holder.itemViewBinding.btnDeleteProduct.setOnClickListener {
            onDeleteProduct(product)
        }
    }

    override fun getItemCount(): Int { return currentProducts.size }



}