package com.itismob.grpfive.mco

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding



class PosViewHolder(private val itemViewBinding: ViewBinding) :
    RecyclerView.ViewHolder(itemViewBinding.root) {

    fun bindData(itemsTransaction: TransactionItem){
        itemViewBinding.tv_product.text = itemsTransaction.productName
        itemViewBinding.etn_price.text = itemsTransaction.productPrice.toString()
    }
}