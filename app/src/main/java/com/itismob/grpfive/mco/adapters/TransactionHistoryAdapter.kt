package com.itismob.grpfive.mco.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.itismob.grpfive.mco.databinding.ItemTransactionHistoryBinding
import com.itismob.grpfive.mco.models.Transaction
import com.itismob.grpfive.mco.viewholders.TransactionHistoryViewHolder

class TransactionHistoryAdapter(private val transactions: List<Transaction>) : RecyclerView.Adapter<TransactionHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionHistoryViewHolder {
        val binding = ItemTransactionHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionHistoryViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size
}
