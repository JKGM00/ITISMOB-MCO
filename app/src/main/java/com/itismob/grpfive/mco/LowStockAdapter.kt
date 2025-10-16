package com.itismob.grpfive.mco

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.itismob.grpfive.mco.databinding.ItemLowStockBinding

class LowStockAdapter(private var products: List<Product>) :
    RecyclerView.Adapter<LowStockViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LowStockViewHolder {
        val binding = ItemLowStockBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LowStockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LowStockViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}
