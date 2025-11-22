package com.itismob.grpfive.mco.viewholders

import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.itismob.grpfive.mco.databinding.ItemsPosBinding
import com.itismob.grpfive.mco.models.TransactionItem

class PosViewHolder(private val itemViewBinding: ItemsPosBinding, private val onDelete: (TransactionItem) -> Unit, private val onQuantityChanged: () -> Unit) : RecyclerView.ViewHolder(itemViewBinding.root) {

    fun bindData(itemsTransaction: TransactionItem) {
        itemViewBinding.tvProduct.text = itemsTransaction.productName
        // Display subtotal (price * quantity)
        itemViewBinding.tvPrice.text = String.format("%.2f", itemsTransaction.subtotal)
        itemViewBinding.tvQuantity.text = itemsTransaction.quantity.toString()

        // Delete button
        itemViewBinding.ibtnDelete.setOnClickListener {
            onDelete(itemsTransaction)
        }

        // Increment quantity button
        itemViewBinding.btnAdd.setOnClickListener {
            // Check against current stock
            if (itemsTransaction.quantity < itemsTransaction.stockQuantity) {
                itemsTransaction.quantity++
                itemViewBinding.tvQuantity.text = itemsTransaction.quantity.toString()
                itemViewBinding.tvPrice.text = String.format("%.2f", itemsTransaction.subtotal)
                onQuantityChanged()
            } else {
                Toast.makeText(itemView.context, "Max stock reached.", Toast.LENGTH_SHORT).show()
            }
        }

        // Decrement quantity button
        itemViewBinding.btnMinus.setOnClickListener {
            if (itemsTransaction.quantity > 1) {
                itemsTransaction.quantity--
                itemViewBinding.tvQuantity.text = itemsTransaction.quantity.toString()
                itemViewBinding.tvPrice.text = String.format("%.2f", itemsTransaction.subtotal)
                onQuantityChanged()
            }
        }
    }
}
