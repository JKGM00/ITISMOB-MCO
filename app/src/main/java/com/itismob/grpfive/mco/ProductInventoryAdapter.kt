package com.itismob.grpfive.mco

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
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
        const val PRODUCT_IMAGE_KEY = "PRODUCT_IMAGE"
        const val PRODUCT_BARCODE_KEY = "PRODUCT_BARCODE"
        const val PRODUCT_UNIT_COST_KEY = "PRODUCT_UNIT_COST"
        const val PRODUCT_SELLING_PRICE_KEY = "PRODUCT_SELLING_PRICE"
        const val PRODUCT_STOCK_KEY = "PRODUCT_STOCK"
        const val POSITION = "POSITION"
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemViewBinding =
            ItemInventoryProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(itemViewBinding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.bindData(product)


        holder.itemViewBinding.btnEditProduct.setOnClickListener {
            val intent = Intent(holder.itemView.context, EditProductActivity::class.java)
            intent.putExtra(PRODUCT_ID_KEY, product.productID)
            intent.putExtra(PRODUCT_NAME_KEY, product.productName)
            intent.putExtra(PRODUCT_CATEGORY_KEY, product.productCategory)
            intent.putExtra(PRODUCT_IMAGE_KEY, product.productImage)
            intent.putExtra(PRODUCT_BARCODE_KEY, product.productBarcode)
            intent.putExtra(PRODUCT_UNIT_COST_KEY, product.unitCost)
            intent.putExtra(PRODUCT_SELLING_PRICE_KEY, product.sellingPrice)
            intent.putExtra(PRODUCT_STOCK_KEY, product.stockQuantity)
            intent.putExtra(POSITION, position)
            editProductLauncher.launch(intent)
        }

        holder.itemViewBinding.btnDeleteProduct.setOnClickListener {
            // Put Pop-up conditional
            products.removeAt(position)
            notifyItemRemoved(position)
        }

    }

    override fun getItemCount(): Int { return products.size }

    // Helper Functions

}