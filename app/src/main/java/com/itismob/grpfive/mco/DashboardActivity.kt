package com.itismob.grpfive.mco

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.itismob.grpfive.mco.adapters.LowStockAdapter
import com.itismob.grpfive.mco.databinding.ActivityDashboardBinding
import com.itismob.grpfive.mco.models.Transaction
import com.itismob.grpfive.mco.utils.TimeUtils

class DashboardActivity : ComponentActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var lowStockAdapter: LowStockAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "User data missing. Please log in again.", Toast.LENGTH_LONG).show()
            val loginIntent = Intent(this, LoginActivity::class.java)
            // Clear stack to prevent going back
            loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(loginIntent)
            finish()
            return
        }

        // Create adapter with the string array
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.revenue_period_options,
            android.R.layout.simple_spinner_item
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRevenuePeriod.adapter = adapter
        binding.spinnerRevenuePeriod.setSelection(0)

        binding.spinnerRevenuePeriod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedPeriod = parent?.getItemAtPosition(position).toString()
                updateRevenueDisplay(selectedPeriod)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        lowStockAdapter = LowStockAdapter(emptyList())
        binding.rvLowStock.layoutManager = LinearLayoutManager(this@DashboardActivity)
        binding.rvLowStock.adapter = lowStockAdapter

        setupNavigation()

        updateRevenueDisplay("Daily")
        updateLowStockDisplay()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning from other tabs
        val selectedPeriod = binding.spinnerRevenuePeriod.selectedItem.toString()
        updateRevenueDisplay(selectedPeriod)
        updateLowStockDisplay()
    }

    private fun setupNavigation() {
        binding.tvNavProfile.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        binding.tvNavHistory.setOnClickListener { navigateTo(TransactionHistoryActivity::class.java) }
        binding.tvNavInventory.setOnClickListener { navigateTo(InventoryActivity::class.java) }
        binding.btnAddProductPage.setOnClickListener { startActivity(Intent(this, AddProductActivity::class.java)) }
        binding.tvNavPos.setOnClickListener { navigateTo(PosActivity::class.java) }
    }

    private fun navigateTo(destination: Class<*>) {
        val intent = Intent(this, destination)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    // ... Keep updateCategorySales, updateLowStockDisplay, updateRevenueDisplay as they are ...
    // (Ensure DatabaseHelper inside these functions uses FirebaseAuth.getInstance().currentUser?.uid internally)

    private fun updateCategorySales(transactions: List<Transaction>) {
        val topCategories = DatabaseHelper.getTopCategories(transactions, 4)
        val nameViews = listOf(binding.tvCategoryName1, binding.tvCategoryName2, binding.tvCategoryName3, binding.tvCategoryName4)
        val amountViews = listOf(binding.tvCategoryAmount1, binding.tvCategoryAmount2, binding.tvCategoryAmount3, binding.tvCategoryAmount4)

        for (i in nameViews.indices) {
            if (i < topCategories.size) {
                val (category, total) = topCategories[i]
                nameViews[i].text = category
                amountViews[i].text = "₱${String.format("%.2f", total)}"
            } else {
                nameViews[i].text = "-"
                amountViews[i].text = "₱0.00"
            }
        }
    }

    private fun updateLowStockDisplay() {
        val lowStockThreshold = 5
        DatabaseHelper.getAllProducts({ products ->
            val lowStockProducts = products
                .filter { it.stockQuantity <= lowStockThreshold }
                .sortedBy { it.stockQuantity }
            lowStockAdapter.updateProducts(lowStockProducts)
        }, { error ->
            Toast.makeText(this, "Failed to load products: ${error.message}", Toast.LENGTH_SHORT).show()
        })
    }

    private fun updateRevenueDisplay(period: String) {
        val (start, end) = when (period) {
            "Daily" -> TimeUtils.dayRange()
            "Weekly" -> TimeUtils.weekRange()
            "Monthly" -> TimeUtils.monthRange()
            "Quarterly" -> TimeUtils.quarterRange()
            "Yearly" -> TimeUtils.yearRange()
            else -> TimeUtils.dayRange()
        }

        DatabaseHelper.getTransactionsForPeriod(start, end,
            onSuccess = { transactions ->
                val totalRevenue = DatabaseHelper.calculateTotalRevenue(transactions)
                binding.tvRevenueAmount.text = "₱${String.format("%.2f", totalRevenue)}"
                val transactionCount = transactions.size
                binding.tvTransactionCount.text = "$transactionCount transaction${if (transactionCount != 1) "s" else ""}"
                updateCategorySales(transactions)
            },
            onFailure = { error ->
                Toast.makeText(this, "Failed to load transactions: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
