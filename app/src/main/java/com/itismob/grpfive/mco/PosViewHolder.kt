package com.itismob.grpfive.mco

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
        itemViewBinding.tvPrice.text = itemsTransaction.subtotal.setScale(2).toPlainString()
        itemViewBinding.tvQuantity.text = itemsTransaction.quantity.toString()

        // Delete button
        itemViewBinding.ibtnDelete.setOnClickListener {
            onDelete(itemsTransaction)
        }
        
        // Increment quantity button
        itemViewBinding.btnAdd.setOnClickListener {
            itemsTransaction.quantity++
            itemViewBinding.tvQuantity.text = itemsTransaction.quantity.toString()
            itemViewBinding.tvPrice.text = itemsTransaction.subtotal.setScale(2).toPlainString()
            onQuantityChanged()
        }
        
        // Decrement quantity button
        itemViewBinding.btnMinus.setOnClickListener {
            if (itemsTransaction.quantity > 1) {
                itemsTransaction.quantity--
                itemViewBinding.tvQuantity.text = itemsTransaction.quantity.toString()
                itemViewBinding.tvPrice.text = itemsTransaction.subtotal.setScale(2).toPlainString()
                onQuantityChanged()
            }
        }
    }
}