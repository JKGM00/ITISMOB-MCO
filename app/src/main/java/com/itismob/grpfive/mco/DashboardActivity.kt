package com.itismob.grpfive.mco

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

import com.google.android.material.tabs.TabLayoutMediator

import com.itismob.grpfive.mco.adapters.DashboardPagerAdapter
import com.itismob.grpfive.mco.dashboard_fragments.TopCategoriesFragment
import com.itismob.grpfive.mco.dashboard_fragments.RevenueProfitFragment
import com.itismob.grpfive.mco.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding

    private var selectedPeriod: String = "Daily"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check user login
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

        // Setup spinner for time period selection
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.revenue_period_options,
            android.R.layout.simple_spinner_item
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRevenuePeriod.adapter = adapter
        binding.spinnerRevenuePeriod.setSelection(0)

        selectedPeriod = binding.spinnerRevenuePeriod.selectedItem.toString()

        binding.spinnerRevenuePeriod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedPeriod = parent?.getItemAtPosition(position).toString()

                // Update TopCategories
                val topCategoriesFragment = supportFragmentManager.findFragmentByTag("f1")
                if (topCategoriesFragment is TopCategoriesFragment) {
                    topCategoriesFragment.updateCategoriesForPeriod(selectedPeriod)
                }

                // Update RevenueProfit
                val revenueFragment = supportFragmentManager.findFragmentByTag("f0")
                if (revenueFragment is RevenueProfitFragment) {
                    revenueFragment.updateRevenueForPeriod(selectedPeriod)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        // Setup ViewPager2 and tabs
        val dashboardAdapter = DashboardPagerAdapter(this) {
            selectedPeriod // return selected period
        }
        binding.viewPager.adapter = dashboardAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> "Revenue & Profit"
                1 -> "Top Categories"
                2 -> "Low Stock Alerts"
                else -> ""
            }
        }.attach()

        binding.tabLayout.tabMode = com.google.android.material.tabs.TabLayout.MODE_FIXED

        setupNavigation()
    }

    override fun onResume() {
        super.onResume()
        selectedPeriod = binding.spinnerRevenuePeriod.selectedItem.toString()
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
}