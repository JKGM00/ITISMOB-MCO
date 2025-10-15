package com.itismob.grpfive.mco

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.itismob.grpfive.mco.databinding.ItemsPosBinding

class PosAdapter(
    private val itemsTransaction: List<TransactionItem>,
    private val onDelete: (TransactionItem) -> Unit
) : RecyclerView.Adapter<PosViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PosViewHolder {
        val itemViewBinding = ItemsPosBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PosViewHolder(itemViewBinding, onDelete)
    }


    override fun onBindViewHolder(
        holder: PosViewHolder,
        position: Int
    ) {
        val currentItem = itemsTransaction[position]
        holder.bindData(currentItem)
    }

    override fun getItemCount(): Int {
        return itemsTransaction.size
    }
}