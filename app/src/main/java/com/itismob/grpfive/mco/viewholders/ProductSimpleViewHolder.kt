package com.itismob.grpfive.mco.viewholders

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.itismob.grpfive.mco.databinding.ItemProductSimpleBinding
import com.itismob.grpfive.mco.models.Product

class ProductSimpleViewHolder(private val binding: ItemProductSimpleBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(product: Product, imageResource: Int) {
        binding.apply {
            tvProductName.text = product.productName
            tvProductStock.text = "Stock: ${product.stockQuantity} pcs."
            ivProductImage.setImageResource(imageResource)
            
            // Show/hide Out of Stock badge
            if (product.stockQuantity == 0) {
                tvOutOfStock.visibility = View.VISIBLE
                // Disable the item
                root.alpha = 0.6f
                root.isEnabled = false
            } else {
                tvOutOfStock.visibility = View.GONE
                // Enable the item
                root.alpha = 1f
                root.isEnabled = true
            }
        }
    }
}
