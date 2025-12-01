package com.itismob.grpfive.mco.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.itismob.grpfive.mco.databinding.ItemProductSimpleBinding
import com.itismob.grpfive.mco.models.Product

class ProductSimpleViewHolder(private val binding: ItemProductSimpleBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(product: Product, imageResource: Int) {
        binding.apply {
            tvProductName.text = product.productName
            tvProductStock.text = "Stock: ${product.stockQuantity} pcs."
            ivProductImage.setImageResource(imageResource)
        }
    }
}
