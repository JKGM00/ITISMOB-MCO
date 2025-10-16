package com.itismob.grpfive.mco

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.itismob.grpfive.mco.databinding.ActivityDashboardBinding
import java.math.BigDecimal
import java.util.Calendar

class DashboardActivity : ComponentActivity() {
    private lateinit var binding: ActivityDashboardBinding
    
    // Category image mapping
    private val categoryImageMap = mapOf(
        "Cooking Essentials" to R.drawable.cooking_essentials,
        "Snacks" to R.drawable.snack,
        "Drinks" to R.drawable.drinks,
        "Canned Goods" to R.drawable.canned_goods,
        "Instant Food" to R.drawable.instant_food,
        "Hygiene" to R.drawable.hygiene,
        "Miscellaneous" to R.drawable.miscellaneous
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Create adapter with the string array
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.revenue_period_options,
            android.R.layout.simple_spinner_item
        )
        
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        // Apply the adapter to the spinner
        binding.spinner2.adapter = adapter
        
        // Set default selection to "Daily" (position 0)
        binding.spinner2.setSelection(0)
        
        // Set up listener for spinner selection changes
        binding.spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedPeriod = parent?.getItemAtPosition(position).toString()
                // Handle the selection change here
                // position 0 = Daily, 1 = Weekly, 2 = Monthly, 3 = Quarterly, 4 = Yearly
                updateRevenueDisplay(selectedPeriod)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Optional: handle case when nothing is selected
            }
        }
        
        // Load initial data
        updateRevenueDisplay("Daily")
        updateLowStockDisplay()
    }
    
    private fun updateRevenueDisplay(period: String) {
        // Get all transactions (from DataGenerator or database)
        val transactions = DataGenerator.sampleTransactions()
        val now = System.currentTimeMillis()
        
        // Filter transactions based on selected period
        val filteredTransactions = when (period) {
            "Daily" -> filterByDay(transactions, now)
            "Weekly" -> filterByWeek(transactions, now)
            "Monthly" -> filterByMonth(transactions, now)
            "Quarterly" -> filterByQuarter(transactions, now)
            "Yearly" -> filterByYear(transactions, now)
            else -> transactions
        }
        
        // Calculate total revenue
        val totalRevenue = filteredTransactions.sumOf { transaction ->
            transaction.items.sumOf { it.subtotal }
        }
        
        // Calculate transaction count
        val transactionCount = filteredTransactions.size
        
        // Update the TextViews with formatted revenue using ViewBinding
        binding.textView6.text = "₱${totalRevenue.setScale(2).toPlainString()}"
        binding.tvTransactionCount.text = "$transactionCount transaction${if (transactionCount != 1) "s" else ""}"
        
        // Update category sales
        updateCategorySales(filteredTransactions)
    }
    
    private fun updateCategorySales(transactions: List<Transaction>) {
        // Group sales by product category
        val categoryTotals = mutableMapOf<String, BigDecimal>()
        
        transactions.forEach { transaction ->
            transaction.items.forEach { item ->
                // Get product category from DataGenerator
                val product = DataGenerator.sampleProducts().find { 
                    it.productName == item.productName 
                }
                val category = product?.productCategory ?: "Other"
                
                categoryTotals[category] = categoryTotals.getOrDefault(category, BigDecimal.ZERO)
                    .add(item.subtotal)
            }
        }
        
        // Sort categories by revenue (highest first)
        val sortedCategories = categoryTotals.entries.sortedByDescending { it.value }
        
        // Get category amount TextViews from binding
        val category1Amount = binding.llCategory1.getChildAt(1) as android.widget.TextView
        val category2Amount = binding.llCategory2.getChildAt(1) as android.widget.TextView
        val category3Amount = binding.llCategory3.getChildAt(1) as android.widget.TextView
        val category4Amount = binding.llCategory4.getChildAt(1) as android.widget.TextView
        
        // Update category displays (show top 4 categories)
        if (sortedCategories.isNotEmpty()) {
            category1Amount.text = "₱${sortedCategories[0].value.setScale(2).toPlainString()}"
            (category1Amount.parent as android.widget.LinearLayout).getChildAt(0).let {
                (it as android.widget.TextView).text = sortedCategories[0].key
            }
        }
        
        if (sortedCategories.size > 1) {
            category2Amount.text = "₱${sortedCategories[1].value.setScale(2).toPlainString()}"
            (category2Amount.parent as android.widget.LinearLayout).getChildAt(0).let {
                (it as android.widget.TextView).text = sortedCategories[1].key
            }
        }
        
        if (sortedCategories.size > 2) {
            category3Amount.text = "₱${sortedCategories[2].value.setScale(2).toPlainString()}"
            (category3Amount.parent as android.widget.LinearLayout).getChildAt(0).let {
                (it as android.widget.TextView).text = sortedCategories[2].key
            }
        }
        
        if (sortedCategories.size > 3) {
            category4Amount.text = "₱${sortedCategories[3].value.setScale(2).toPlainString()}"
            (category4Amount.parent as android.widget.LinearLayout).getChildAt(0).let {
                (it as android.widget.TextView).text = sortedCategories[3].key
            }
        }
    }
    
    private fun updateLowStockDisplay() {
        // Get all products and filter for low stock (10 or fewer units)
        val lowStockThreshold = 10
        val lowStockProducts = DataGenerator.sampleProducts()
            .filter { it.stockQuantity <= lowStockThreshold }
            .sortedBy { it.stockQuantity } // Sort by quantity ascending (lowest first)
        
        // Hide all low stock items by default using ViewBinding
        binding.llLowStock1.visibility = View.GONE
        binding.llLowStock2.visibility = View.GONE
        binding.llLowStock3.visibility = View.GONE
        
        // Display up to 3 low stock items
        if (lowStockProducts.isNotEmpty()) {
            binding.llLowStock1.visibility = View.VISIBLE
            binding.tvLowStockProduct1.text = lowStockProducts[0].productName
            binding.tvLowStockCategory1.text = lowStockProducts[0].productCategory
            binding.tvLowStockQuantity1.text = "${lowStockProducts[0].stockQuantity} left"
        }
        
        if (lowStockProducts.size > 1) {
            binding.llLowStock2.visibility = View.VISIBLE
            binding.tvLowStockProduct2.text = lowStockProducts[1].productName
            binding.tvLowStockCategory2.text = lowStockProducts[1].productCategory
            binding.tvLowStockQuantity2.text = "${lowStockProducts[1].stockQuantity} left"
        }
        
        if (lowStockProducts.size > 2) {
            binding.llLowStock3.visibility = View.VISIBLE
            binding.tvLowStockProduct3.text = lowStockProducts[2].productName
            binding.tvLowStockCategory3.text = lowStockProducts[2].productCategory
            binding.tvLowStockQuantity3.text = "${lowStockProducts[2].stockQuantity} left"
        }
    }
    
    private fun filterByDay(transactions: List<Transaction>, currentTime: Long): List<Transaction> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTime
        val currentDay = calendar.get(Calendar.DAY_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)
        
        return transactions.filter { transaction ->
            calendar.timeInMillis = transaction.timestampMillis
            calendar.get(Calendar.DAY_OF_YEAR) == currentDay && 
            calendar.get(Calendar.YEAR) == currentYear
        }
    }
    
    private fun filterByWeek(transactions: List<Transaction>, currentTime: Long): List<Transaction> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTime
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)
        
        return transactions.filter { transaction ->
            calendar.timeInMillis = transaction.timestampMillis
            calendar.get(Calendar.WEEK_OF_YEAR) == currentWeek && 
            calendar.get(Calendar.YEAR) == currentYear
        }
    }
    
    private fun filterByMonth(transactions: List<Transaction>, currentTime: Long): List<Transaction> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTime
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        return transactions.filter { transaction ->
            calendar.timeInMillis = transaction.timestampMillis
            calendar.get(Calendar.MONTH) == currentMonth && 
            calendar.get(Calendar.YEAR) == currentYear
        }
    }
    
    private fun filterByQuarter(transactions: List<Transaction>, currentTime: Long): List<Transaction> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTime
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentQuarter = currentMonth / 3 // 0-2=Q1, 3-5=Q2, 6-8=Q3, 9-11=Q4
        val currentYear = calendar.get(Calendar.YEAR)
        
        return transactions.filter { transaction ->
            calendar.timeInMillis = transaction.timestampMillis
            val transactionQuarter = calendar.get(Calendar.MONTH) / 3
            transactionQuarter == currentQuarter && 
            calendar.get(Calendar.YEAR) == currentYear
        }
    }
    
    private fun filterByYear(transactions: List<Transaction>, currentTime: Long): List<Transaction> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTime
        val currentYear = calendar.get(Calendar.YEAR)
        
        return transactions.filter { transaction ->
            calendar.timeInMillis = transaction.timestampMillis
            calendar.get(Calendar.YEAR) == currentYear
        }
    }
}
