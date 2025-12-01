package com.itismob.grpfive.mco.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.itismob.grpfive.mco.databinding.ItemProductSimpleBinding
import com.itismob.grpfive.mco.models.Product
import com.itismob.grpfive.mco.viewholders.ProductSimpleViewHolder

class ProductSimpleAdapter(
    private val products: List<Product>,
    private val onProductSelected: (Product) -> Unit
) : RecyclerView.Adapter<ProductSimpleViewHolder>() {

    companion object {
        private val categoryImageMap = mapOf(
            "Cooking Essentials" to com.itismob.grpfive.mco.R.drawable.cooking_essentials,
            "Snacks" to com.itismob.grpfive.mco.R.drawable.snack,
            "Drinks" to com.itismob.grpfive.mco.R.drawable.drinks,
            "Canned Goods" to com.itismob.grpfive.mco.R.drawable.canned_goods,
            "Instant Food" to com.itismob.grpfive.mco.R.drawable.instant_food,
            "Hygiene" to com.itismob.grpfive.mco.R.drawable.hygiene,
            "Miscellaneous" to com.itismob.grpfive.mco.R.drawable.miscellaneous,
        )

        fun getCategoryImageResource(category: String): Int {
            return categoryImageMap[category] ?: com.itismob.grpfive.mco.R.drawable.logo
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductSimpleViewHolder {
        val binding = ItemProductSimpleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductSimpleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductSimpleViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product, getCategoryImageResource(product.productCategory))
        
        // Only allow click if product has stock
        if (product.stockQuantity > 0) {
            holder.itemView.setOnClickListener {
                onProductSelected(product)
            }
        } else {
            holder.itemView.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = products.size
}
