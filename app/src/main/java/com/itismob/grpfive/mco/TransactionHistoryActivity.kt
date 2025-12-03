package com.itismob.grpfive.mco

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.itismob.grpfive.mco.adapters.TransactionHistoryAdapter
import com.itismob.grpfive.mco.databinding.ActivityTransactionHistoryBinding
import com.itismob.grpfive.mco.models.Transaction
import java.text.SimpleDateFormat
import java.util.*

class TransactionHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionHistoryBinding
    private lateinit var transactionAdapter: TransactionHistoryAdapter
    private val transactions = mutableListOf<Transaction>()
    private val filteredTransactions = mutableListOf<Transaction>()
    
    private var startDateMillis: Long? = null
    private var endDateMillis: Long? = null
    private var sortOrder = SortOrder.NONE
    private val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    private var tempStartDateMillis: Long? = null
    private var tempEndDateMillis: Long? = null
    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var isInitialLoadComplete = false
    
    private enum class SortOrder {
        NONE, NEWEST, OLDEST, HIGHEST_AMOUNT, LOWEST_AMOUNT
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "User session expired. Please log in again.", Toast.LENGTH_LONG).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }
        
        // Load transactions from Firebase
        loadTransactionsFromFirebase()
        
        // Set up RecyclerView
        transactionAdapter = TransactionHistoryAdapter(filteredTransactions)
        binding.recyclerViewTransactions.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewTransactions.adapter = transactionAdapter
        
        // Set up Sort Spinner (Date)
        val sortOptions = arrayOf("None", "Newest First", "Oldest First")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSort.adapter = spinnerAdapter
        binding.spinnerSort.setSelection(0)
        binding.spinnerSort.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (isInitialLoadComplete) {
                    sortOrder = when (position) {
                        0 -> SortOrder.NONE
                        1 -> SortOrder.NEWEST
                        2 -> SortOrder.OLDEST
                        else -> SortOrder.NONE
                    }
                    applyFiltersAndSort()
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
        
        // Set up Total Amount Sort Spinner
        val amountSortOptions = arrayOf("None", "Highest Amount", "Lowest Amount")
        val amountSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, amountSortOptions)
        amountSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPriceSort.adapter = amountSpinnerAdapter
        binding.spinnerPriceSort.setSelection(0)
        binding.spinnerPriceSort.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (isInitialLoadComplete) {
                    sortOrder = when (position) {
                        0 -> SortOrder.NONE
                        1 -> SortOrder.HIGHEST_AMOUNT
                        2 -> SortOrder.LOWEST_AMOUNT
                        else -> SortOrder.NONE
                    }
                    applyFiltersAndSort()
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
        
        // Filter button listener
        binding.btnCalendarFilt.setOnClickListener {
            showDateRangePicker()
        }
        binding.btnCalendarFilt.setOnLongClickListener {
            Toast.makeText(this, "Filter by Date", Toast.LENGTH_SHORT).show()
            true
        }
        
        // Reset button listener
        binding.btnResetFilter.setOnClickListener {
            resetFilters()
        }
        binding.btnResetFilter.setOnLongClickListener {
            Toast.makeText(this, "Clear all filters", Toast.LENGTH_SHORT).show()
            true
        }
        
        // Spinners long-press listeners
        binding.spinnerSort.setOnLongClickListener {
            Toast.makeText(this, "Sort by Date", Toast.LENGTH_SHORT).show()
            true
        }
        
        binding.spinnerPriceSort.setOnLongClickListener {
            Toast.makeText(this, "Sort by Amount", Toast.LENGTH_SHORT).show()
            true
        }

        setupNavigation()
        // Mark initial load as complete to enable spinner listeners
        isInitialLoadComplete = true
    }

    private fun setupNavigation() {
        binding.tvNavProfile.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        binding.tvNavDashboard.setOnClickListener { navigateTo(DashboardActivity::class.java) }
        binding.tvNavInventory.setOnClickListener { navigateTo(InventoryActivity::class.java) }
        binding.btnAddProductPage.setOnClickListener { startActivity(Intent(this, AddProductActivity::class.java)) }
        binding.tvNavPos.setOnClickListener { navigateTo(PosActivity::class.java) }
    }

    private fun navigateTo(destination: Class<*>) {
        val intent = Intent(this, destination)
        // Clear back stack so the user returns to a clean state
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
    
    private fun showDateRangePicker() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_date_filter)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        val etStartDate = dialog.findViewById<EditText>(R.id.etStartDate)
        val etEndDate = dialog.findViewById<EditText>(R.id.etEndDate)
        val btnSelectStartDate = dialog.findViewById<ImageButton>(R.id.btnSelectStartDate)
        val btnSelectEndDate = dialog.findViewById<ImageButton>(R.id.btnSelectEndDate)
        val btnDialogReset = dialog.findViewById<Button>(R.id.btnDialogReset)
        val btnDialogCancel = dialog.findViewById<Button>(R.id.btnDialogCancel)
        val btnDialogConfirm = dialog.findViewById<Button>(R.id.btnDialogConfirm)
        
        // Initialize temp dates with current filter values
        tempStartDateMillis = startDateMillis
        tempEndDateMillis = endDateMillis
        
        // Display current dates if set
        if (startDateMillis != null) {
            etStartDate.setText(dateFormat.format(Date(startDateMillis!!)))
        }
        if (endDateMillis != null) {
            etEndDate.setText(dateFormat.format(Date(endDateMillis!!)))
        }
        
        // Start date picker
        btnSelectStartDate.setOnClickListener {
            showDatePicker { selectedDateMillis ->
                tempStartDateMillis = selectedDateMillis
                etStartDate.setText(dateFormat.format(Date(selectedDateMillis)))
            }
        }
        
        etStartDate.setOnClickListener {
            showDatePicker { selectedDateMillis ->
                tempStartDateMillis = selectedDateMillis
                etStartDate.setText(dateFormat.format(Date(selectedDateMillis)))
            }
        }
        
        // End date picker
        btnSelectEndDate.setOnClickListener {
            showDatePicker { selectedDateMillis ->
                tempEndDateMillis = selectedDateMillis
                etEndDate.setText(dateFormat.format(Date(selectedDateMillis)))
            }
        }
        
        etEndDate.setOnClickListener {
            showDatePicker { selectedDateMillis ->
                tempEndDateMillis = selectedDateMillis
                etEndDate.setText(dateFormat.format(Date(selectedDateMillis)))
            }
        }
        
        // Reset button
        btnDialogReset.setOnClickListener {
            tempStartDateMillis = null
            tempEndDateMillis = null
            etStartDate.setText("")
            etEndDate.setText("")
        }
        
        // Cancel button
        btnDialogCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        // Confirm button
        btnDialogConfirm.setOnClickListener {
            if (tempStartDateMillis != null && tempEndDateMillis != null) {
                if (tempStartDateMillis!! > tempEndDateMillis!!) {
                    Toast.makeText(this, "Start date must be before end date", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }
            
            startDateMillis = tempStartDateMillis
            endDateMillis = tempEndDateMillis
            applyFiltersAndSort()
            
            if (startDateMillis != null && endDateMillis != null) {
                val startDate = dateFormat.format(Date(startDateMillis!!))
                val endDate = dateFormat.format(Date(endDateMillis!!))
                Toast.makeText(this, "Filtered: $startDate - $endDate", Toast.LENGTH_SHORT).show()
            } else if (startDateMillis != null) {
                Toast.makeText(this, "Filtered from: ${dateFormat.format(Date(startDateMillis!!))}", Toast.LENGTH_SHORT).show()
            } else if (endDateMillis != null) {
                Toast.makeText(this, "Filtered until: ${dateFormat.format(Date(endDateMillis!!))}", Toast.LENGTH_SHORT).show()
            }
            
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun showDatePicker(onDateSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth, 0, 0, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                onDateSelected(calendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
    
    private fun resetFilters() {
        startDateMillis = null
        endDateMillis = null
        sortOrder = SortOrder.NONE
        binding.spinnerSort.setSelection(0)
        binding.spinnerPriceSort.setSelection(0)
        applyFiltersAndSort()
        Toast.makeText(this, "Filters reset", Toast.LENGTH_SHORT).show()
    }

    private fun loadTransactionsFromFirebase() {
        DatabaseHelper.listenToTransactions(
            onUpdate = { fetchedTransactions ->
                transactions.clear()
                transactions.addAll(fetchedTransactions)
                // Apply default filters after transactions are loaded
                if (isInitialLoadComplete) {
                    applyFiltersAndSort()
                }
            },
            onError = { error ->
                Toast.makeText(this, "Error loading transactions: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ).let { registration ->
            listenerRegistration = registration
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
    }

    
    private fun applyFiltersAndSort() {
        // Filter by date range
        filteredTransactions.clear()
        
        for (transaction in transactions) {
            val transactionTime = transaction.createdAt
            
            val isAfterStartDate = startDateMillis == null || transactionTime >= startDateMillis!!
            val isBeforeEndDate = endDateMillis == null || transactionTime <= (endDateMillis!! + 86400000) // Add 1 day to include end date
            
            if (isAfterStartDate && isBeforeEndDate) {
                filteredTransactions.add(transaction)
            }
        }
        
        // Sort
        when (sortOrder) {
            SortOrder.NONE -> {
                // No sorting, keep original order
            }
            SortOrder.NEWEST -> {
                filteredTransactions.sortByDescending { it.createdAt }
            }
            SortOrder.OLDEST -> {
                filteredTransactions.sortBy { it.createdAt }
            }
            SortOrder.HIGHEST_AMOUNT -> {
                filteredTransactions.sortByDescending { it.totalAmount }
            }
            SortOrder.LOWEST_AMOUNT -> {
                filteredTransactions.sortBy { it.totalAmount }
            }
        }
        
        // Update UI
        transactionAdapter.notifyDataSetChanged()
        updateEmptyState()
    }
    
    private fun updateEmptyState() {
        if (filteredTransactions.isEmpty()) {
            binding.recyclerViewTransactions.visibility = android.view.View.GONE
            binding.llEmptyState.visibility = android.view.View.VISIBLE
        } else {
            binding.recyclerViewTransactions.visibility = android.view.View.VISIBLE
            binding.llEmptyState.visibility = android.view.View.GONE
        }
    }
}
