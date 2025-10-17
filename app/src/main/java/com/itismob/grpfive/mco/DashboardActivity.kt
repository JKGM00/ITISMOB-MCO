package com.itismob.grpfive.mco

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.itismob.grpfive.mco.databinding.ActivityDashboardBinding
import java.math.BigDecimal
import java.util.Calendar

class DashboardActivity : ComponentActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var lowStockAdapter: LowStockAdapter
    private lateinit var currentUser : User
    
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

    private val profileActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                val updatedUser: User? = data.getSerializableExtra(ProfileActivity.USER_KEY) as? User
                if (updatedUser != null) {
                    currentUser = updatedUser // Update the current user data
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, "Profile edit cancelled.", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve CurrentUser from LOGIN
        val userFromLogin: User? = intent.getSerializableExtra("user") as? User
        if (userFromLogin != null) {
            currentUser = userFromLogin // Initialize currentUser here
        } else {
            Toast.makeText(this, "User data missing. Please log in again.", Toast.LENGTH_LONG).show()
            val loginIntent = Intent(this, LoginActivity::class.java)
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
        
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        // Apply the adapter to the spinner
        binding.spinnerRevenuePeriod.adapter = adapter
        
        // Set default selection to "Daily" (position 0)
        binding.spinnerRevenuePeriod.setSelection(0)
        
        // Set up listener for spinner selection changes
        binding.spinnerRevenuePeriod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
        
        // Set up RecyclerView for low stock items
        lowStockAdapter = LowStockAdapter(emptyList())
        binding.rvLowStock.layoutManager = LinearLayoutManager(this@DashboardActivity)
        binding.rvLowStock.adapter = lowStockAdapter
        
        // Set up navigation click listeners
        binding.tvNavPos.setOnClickListener {
            val intent = Intent(this, PosActivity::class.java)
            startActivity(intent)
        }
        
        binding.tvNavInventory.setOnClickListener {
            val intent = Intent(this, InventoryActivity::class.java)
            startActivity(intent)
        }

        binding.tvNavProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java).apply {
                putExtra(ProfileActivity.USER_KEY, currentUser)
            }
            profileActivityLauncher.launch(intent)
        }

//        binding.tvNavLogOut.setOnClickListener {
//            val intent = Intent(this, LoginActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(intent)
//            finish()
//        }
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
        binding.tvRevenueAmount.text = "₱${totalRevenue.setScale(2).toPlainString()}"
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
        
        // Update category displays (show top 4 categories)
        if (sortedCategories.isNotEmpty()) {
            binding.tvCategoryName1.text = sortedCategories[0].key
            binding.tvCategoryAmount1.text = "₱${sortedCategories[0].value.setScale(2).toPlainString()}"
        }
        
        if (sortedCategories.size > 1) {
            binding.tvCategoryName2.text = sortedCategories[1].key
            binding.tvCategoryAmount2.text = "₱${sortedCategories[1].value.setScale(2).toPlainString()}"
        }
        
        if (sortedCategories.size > 2) {
            binding.tvCategoryName3.text = sortedCategories[2].key
            binding.tvCategoryAmount3.text = "₱${sortedCategories[2].value.setScale(2).toPlainString()}"
        }
        
        if (sortedCategories.size > 3) {
            binding.tvCategoryName4.text = sortedCategories[3].key
            binding.tvCategoryAmount4.text = "₱${sortedCategories[3].value.setScale(2).toPlainString()}"
        }
    }
    
    private fun updateLowStockDisplay() {
        // Get all products and filter for low stock (10 or fewer units)
        val lowStockThreshold = 10
        val lowStockProducts = DataGenerator.sampleProducts()
            .filter { it.stockQuantity <= lowStockThreshold }
            .sortedBy { it.stockQuantity } // Sort by quantity ascending (lowest first)
        
        // Update RecyclerView with low stock products
        lowStockAdapter.updateProducts(lowStockProducts)
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
