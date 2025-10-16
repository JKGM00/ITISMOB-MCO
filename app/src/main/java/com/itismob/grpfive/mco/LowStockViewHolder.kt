package com.itismob.grpfive.mco

import androidx.recyclerview.widget.RecyclerView
import com.itismob.grpfive.mco.databinding.ItemLowStockBinding

class LowStockViewHolder(private val binding: ItemLowStockBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(product: Product) {
        binding.apply {
            tvProductName.text = product.productName
            tvProductCategory.text = product.productCategory
            tvStockQuantity.text = "${product.stockQuantity} left"
        }
    }
}
