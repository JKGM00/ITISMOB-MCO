package com.itismob.grpfive.mco.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.itismob.grpfive.mco.databinding.ItemLowStockBinding
import com.itismob.grpfive.mco.models.Product

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
