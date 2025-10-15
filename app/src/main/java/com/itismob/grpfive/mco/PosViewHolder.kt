package com.itismob.grpfive.mco

import androidx.recyclerview.widget.RecyclerView
import com.itismob.grpfive.mco.databinding.ItemsPosBinding


class PosViewHolder(
    private val itemViewBinding: ItemsPosBinding,
    private val onDelete: (TransactionItem) -> Unit
) : RecyclerView.ViewHolder(itemViewBinding.root) {

    fun bindData(itemsTransaction: TransactionItem) {
        itemViewBinding.tvProduct.text = itemsTransaction.productName
        // format price (BigDecimal) to 2 decimal places
        itemViewBinding.tvPrice.text = itemsTransaction.productPrice.setScale(2).toPlainString()
        itemViewBinding.tvQuantity.text = itemsTransaction.quantity.toString()

        itemViewBinding.ibtnDelete.setOnClickListener {
            onDelete(itemsTransaction)
        }
    }
}