package com.itismob.grpfive.mco


import androidx.recyclerview.widget.RecyclerView
import com.itismob.grpfive.mco.databinding.ItemInventoryProductBinding

class ProductViewHolder(val itemViewBinding: ItemInventoryProductBinding) :
    RecyclerView.ViewHolder(itemViewBinding.root) {

    fun bindData(product: Product) {
        itemViewBinding.tvProductName.text = product.productName
        itemViewBinding.tvProductStock.text = "Stock: ${product.stockQuantity} pcs."

        val imageResource = ProductInventoryAdapter.getCategoryImageResource(product.productCategory)
        itemViewBinding.ivProductImage.setImageResource(imageResource)
    }
}
