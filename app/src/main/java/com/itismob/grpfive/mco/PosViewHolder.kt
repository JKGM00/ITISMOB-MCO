package com.itismob.grpfive.mco

import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.itismob.grpfive.mco.databinding.ItemsPosBinding


class PosViewHolder(
    private val itemViewBinding: ItemsPosBinding,
    private val onDelete: (TransactionItem) -> Unit,
    private val onQuantityChanged: () -> Unit
) : RecyclerView.ViewHolder(itemViewBinding.root) {

    fun bindData(itemsTransaction: TransactionItem) {
        itemViewBinding.tvProduct.text = itemsTransaction.productName
        // Display subtotal (price * quantity) instead of unit price
        itemViewBinding.tvPrice.text = String.format("%.2f", itemsTransaction.subtotal)
        itemViewBinding.tvQuantity.text = itemsTransaction.quantity.toString()

        // Delete button
        itemViewBinding.ibtnDelete.setOnClickListener {
            onDelete(itemsTransaction)
        }
        
        // Increment quantity button
        itemViewBinding.btnAdd.setOnClickListener {
            // Stock Quantity field of itemTransaction is now used here to validate the quantity
            if (itemsTransaction.quantity < itemsTransaction.stockQuantity) {
                itemsTransaction.quantity++
                itemViewBinding.tvQuantity.text = itemsTransaction.quantity.toString()
                itemViewBinding.tvPrice.text = String.format("%.2f", itemsTransaction.subtotal)
                onQuantityChanged()
            } else {
                Toast.makeText(itemView.context, "Can't add more. Not enough stock.", Toast.LENGTH_SHORT).show()
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