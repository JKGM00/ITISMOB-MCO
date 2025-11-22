package com.itismob.grpfive.mco.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.itismob.grpfive.mco.databinding.ItemsPosBinding
import com.itismob.grpfive.mco.models.TransactionItem
import com.itismob.grpfive.mco.viewholders.PosViewHolder

class PosAdapter(private val itemsTransaction: MutableList<TransactionItem>, private val onDelete: (TransactionItem) -> Unit, private val onQuantityChanged: () -> Unit) : RecyclerView.Adapter<PosViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int ): PosViewHolder {
        val itemViewBinding = ItemsPosBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PosViewHolder(itemViewBinding, onDelete, onQuantityChanged)
    }

    override fun onBindViewHolder(holder: PosViewHolder, position: Int ) {
        val currentItem = itemsTransaction[position]
        holder.bindData(currentItem)
    }

    override fun getItemCount(): Int {
        return itemsTransaction.size
    }
}