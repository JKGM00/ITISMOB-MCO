package com.itismob.grpfive.mco.dashboard_fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.itismob.grpfive.mco.adapters.LowStockAdapter
import com.itismob.grpfive.mco.databinding.FragmentLowStockLayoutBinding
import com.itismob.grpfive.mco.DatabaseHelper

class LowStockAlertsFragment : Fragment() {
    private var _binding: FragmentLowStockLayoutBinding? = null
    private val binding get() = _binding!!
    private lateinit var lowStockAdapter: LowStockAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLowStockLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize adapter with empty list
        lowStockAdapter = LowStockAdapter(emptyList())
        binding.rvLowStock.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLowStock.adapter = lowStockAdapter

        // Load products
        loadLowStockProducts()
    }

    private fun loadLowStockProducts() {
        val lowStockThreshold = 5
        DatabaseHelper.getAllProducts({ products ->
            val lowStockProducts = products
                .filter { it.stockQuantity <= lowStockThreshold }
                .sortedBy { it.stockQuantity }
            lowStockAdapter.updateProducts(lowStockProducts)
        }, { error ->
            Toast.makeText(requireContext(), "Failed to load products: ${error.message}", Toast.LENGTH_SHORT).show()
        })
    }

    override fun onResume() {
        super.onResume()
        loadLowStockProducts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}