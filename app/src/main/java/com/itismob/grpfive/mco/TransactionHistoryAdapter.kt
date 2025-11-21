package com.itismob.grpfive.mco

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.itismob.grpfive.mco.databinding.ItemTransactionHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class TransactionHistoryAdapter(
    private val transactions: List<Transaction>
) : RecyclerView.Adapter<TransactionHistoryAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size

    class TransactionViewHolder(private val binding: ItemTransactionHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            // Transaction ID
            binding.tvTransactionId.text = "Transaction #${transaction.transactionID.takeLast(4).uppercase()}"
            
            // Date and Time
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
            binding.tvTransactionDate.text = dateFormat.format(Date(transaction.createdAt))
            
            // Item Count
            binding.tvItemCount.text = "${transaction.items.size} item${if (transaction.items.size != 1) "s" else ""}"
            
            // Total Amount
            binding.tvTotalAmount.text = "₱${String.format("%.2f", transaction.totalAmount.toDouble())}"
            
            // Items Preview
            val itemsPreview = transaction.items.joinToString("\n") { item ->
                val price = String.format("%.2f", item.productPrice)
                val subtotal = String.format("%.2f", item.subtotal)
                "${item.productName} (x${item.quantity}) for ₱$price = ₱$subtotal"
            }
            binding.tvItemsPreview.text = itemsPreview
        }
    }
}
