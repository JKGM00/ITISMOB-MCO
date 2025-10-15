package com.itismob.grpfive.mco

import androidx.recyclerview.widget.RecyclerView
import com.itismob.grpfive.mco.databinding.ItemsPosBinding


class PosViewHolder(private val itemViewBinding: ItemsPosBinding) :
    RecyclerView.ViewHolder(itemViewBinding.root) {

    fun bindData(itemsTransaction: TransactionItem){
        itemViewBinding.tvProduct.text = itemsTransaction.productName
        itemViewBinding.tvPrice.text = (itemsTransaction.productPrice as CharSequence?).toString()
        itemViewBinding.tvQuantity.text = itemsTransaction.quantity.toString()

    }
}