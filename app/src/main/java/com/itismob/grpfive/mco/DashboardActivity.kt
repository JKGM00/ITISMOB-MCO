package com.itismob.grpfive.mco

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.itismob.grpfive.mco.adapters.LowStockAdapter
import com.itismob.grpfive.mco.databinding.ActivityDashboardBinding
import com.itismob.grpfive.mco.models.Transaction
import com.itismob.grpfive.mco.models.User
import com.itismob.grpfive.mco.utils.TimeUtils

class DashboardActivity : ComponentActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var lowStockAdapter: LowStockAdapter
    private lateinit var currentUser : User

    private val profileActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            if (data != null) {
                val updatedUser: User? = data.getSerializableExtra(ProfileActivity.USER_KEY) as? User
                if (updatedUser != null) {
                    currentUser = updatedUser // Update the current user data
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (result.resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Profile edit cancelled.", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        binding.tvNavPos.setOnLongClickListener {
            Toast.makeText(this, "Point of sale", Toast.LENGTH_SHORT).show()
            true
        }
        
        binding.tvNavInventory.setOnClickListener {
            val intent = Intent(this, InventoryActivity::class.java)
            startActivity(intent)
        }
        binding.tvNavInventory.setOnLongClickListener {
            Toast.makeText(this, "Inventory", Toast.LENGTH_SHORT).show()
            true
        }

        binding.tvNavHistory.setOnClickListener {
            val intent = Intent(this, TransactionHistoryActivity::class.java)
            startActivity(intent)
        }
        binding.tvNavHistory.setOnLongClickListener {
            Toast.makeText(this, "History", Toast.LENGTH_SHORT).show()
            true
        }

        binding.tvNavProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java).apply {
                putExtra(ProfileActivity.USER_KEY, currentUser)
            }
            profileActivityLauncher.launch(intent)
        }
        binding.tvNavProfile.setOnLongClickListener {
            Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
            true
        }


        // Load initial data
        updateRevenueDisplay("Daily")
        updateLowStockDisplay()
    }

    override fun onResume() {
        super.onResume()
        val selectedPeriod = binding.spinnerRevenuePeriod.selectedItem.toString()
        updateRevenueDisplay(selectedPeriod)
        updateLowStockDisplay()
    }

    
    private fun updateCategorySales(transactions: List<Transaction>) {
        // Group sales by product category
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
        // Get all products and filter for low stock (10 or fewer units)
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
        // Get the timestamp range based on the selected period
        val (start, end) = when (period) {
            "Daily" -> TimeUtils.dayRange()
            "Weekly" -> TimeUtils.weekRange()
            "Monthly" -> TimeUtils.monthRange()
            "Quarterly" -> TimeUtils.quarterRange()
            "Yearly" -> TimeUtils.yearRange()
            else -> TimeUtils.dayRange()
        }

        // Fetch transactions from Firestore for the given period
        DatabaseHelper.getTransactionsForPeriod(start, end,
            onSuccess = { transactions ->
                // 1️⃣ Calculate total revenue
                val totalRevenue = DatabaseHelper.calculateTotalRevenue(transactions)
                binding.tvRevenueAmount.text = "₱${String.format("%.2f", totalRevenue)}"

                // 2️⃣ Count transactions
                val transactionCount = transactions.size
                binding.tvTransactionCount.text =
                    "$transactionCount transaction${if (transactionCount != 1) "s" else ""}"

                // 3️⃣ Calculate top 4 categories
                updateCategorySales(transactions)
            },
            onFailure = { error ->
                Toast.makeText(this, "Failed to load transactions: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }


}
