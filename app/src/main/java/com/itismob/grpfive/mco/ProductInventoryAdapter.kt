package com.itismob.grpfive.mco

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.itismob.grpfive.mco.databinding.ItemInventoryProductBinding

class ProductInventoryAdapter(
    private val products: MutableList<Product>,
    private val editProductLauncher: ActivityResultLauncher<Intent>
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
        val product = products[position]
        holder.bindData(product)

        // Edit Button
        holder.itemViewBinding.btnEditProduct.setOnClickListener {
            val intent = Intent(holder.itemView.context, EditProductActivity::class.java)

            intent.putExtra(PRODUCT_ID_KEY, product.productID)
            intent.putExtra(PRODUCT_NAME_KEY, product.productName)
            intent.putExtra(PRODUCT_CATEGORY_KEY, product.productCategory)
            intent.putExtra(PRODUCT_BARCODE_KEY, product.productBarcode)

            intent.putExtra(PRODUCT_UNIT_COST_KEY, product.unitCost.toPlainString())
            intent.putExtra(PRODUCT_SELLING_PRICE_KEY, product.sellingPrice.toPlainString())

            intent.putExtra(PRODUCT_STOCK_KEY, product.stockQuantity)
            intent.putExtra(POSITION, position)
            editProductLauncher.launch(intent)
        }

        // Delete Button
        holder.itemViewBinding.btnDeleteProduct.setOnClickListener {
            val context = holder.itemView.context
            showDeleteConfirmationDialog(context, product, position)
        }

    }

    override fun getItemCount(): Int { return products.size }

    private fun showDeleteConfirmationDialog(context: Context, product: Product, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete '${product.productName}'?")
            .setPositiveButton("Yes") { dialog, which ->
                // User confirmed, proceed with deletion
                products.removeAt(position)
                notifyItemRemoved(position)

                Toast.makeText(context, "${product.productName} deleted!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No") { dialog, which ->
                // User cancelled, close dialog
                dialog.dismiss()

                Toast.makeText(context, "Deletion cancelled.", Toast.LENGTH_SHORT).show()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }


}