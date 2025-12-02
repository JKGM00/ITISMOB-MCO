package com.itismob.grpfive.mco.dashboard_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.itismob.grpfive.mco.databinding.FragmentTopCategoriesLayoutBinding
import com.itismob.grpfive.mco.models.Transaction
import com.itismob.grpfive.mco.DatabaseHelper
import com.itismob.grpfive.mco.utils.TimeUtils
import com.itismob.grpfive.mco.R

class TopCategoriesFragment : Fragment() {

    companion object {
        private const val ARG_PERIOD = "arg_period"

        fun newInstance(period: String): TopCategoriesFragment {
            val frag = TopCategoriesFragment()
            val bundle = Bundle()
            bundle.putString(ARG_PERIOD, period)
            frag.arguments = bundle
            return frag
        }
    }

    private var selectedPeriod: String = "Daily"

    private var _binding: FragmentTopCategoriesLayoutBinding? = null
    private val binding get() = _binding!!

    // Map of category -> color resource
    private val categoryColors = mapOf(
        "Cooking Essentials" to R.color.Category_CookingEssentials,
        "Snacks" to R.color.Category_Snacks,
        "Drinks" to R.color.Category_Drinks,
        "Canned Goods" to R.color.Category_CannedGoods,
        "Instant Food" to R.color.Category_InstantFood,
        "Hygiene" to R.color.Category_Hygiene,
        "Miscellaneous" to R.color.Category_Miscellaneous
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedPeriod = arguments?.getString(ARG_PERIOD) ?: "Daily"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTopCategoriesLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Ensure we load the initial period when the fragment's view is ready
        updateCategoriesForPeriod(selectedPeriod)
    }

    private fun setupPieChart(entries: List<PieEntry>) {
        val dataSet = PieDataSet(entries, "")
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = resources.getColor(android.R.color.white, null)

        // Map each entry to its color
        dataSet.colors = entries.map { entry ->
            resources.getColor(categoryColors[entry.label] ?: R.color.IvoryWhite, null)
        }

        val pieData = PieData(dataSet)
        binding.pieChart.data = pieData
        binding.pieChart.centerText = "Top Categories"
        binding.pieChart.setUsePercentValues(true)
        binding.pieChart.description.isEnabled = false
        binding.pieChart.animateY(800)

        // Setup legend
        val legend = binding.pieChart.legend
        legend.isEnabled = true
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)

        binding.pieChart.invalidate()
    }

    fun updateCategoriesForPeriod(period: String) {
        // Get date range from period
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

                // Get ALL categories (no top 4 slicing)
                val allCategories = DatabaseHelper.getTopCategories(transactions)

                // Views for the 7 cards
                val nameViews = listOf(
                    binding.tvCategoryName1, binding.tvCategoryName2, binding.tvCategoryName3,
                    binding.tvCategoryName4, binding.tvCategoryName5, binding.tvCategoryName6,
                    binding.tvCategoryName7
                )

                val amountViews = listOf(
                    binding.tvCategoryAmount1, binding.tvCategoryAmount2, binding.tvCategoryAmount3,
                    binding.tvCategoryAmount4, binding.tvCategoryAmount5, binding.tvCategoryAmount6,
                    binding.tvCategoryAmount7
                )

                // Loop through 7 cards, fill whatever categories exist
                for (i in nameViews.indices) {
                    if (i < allCategories.size) {
                        val (category, total) = allCategories[i]
                        nameViews[i].text = category
                        amountViews[i].text = "₱${"%.2f".format(total)}"
                    } else {
                        nameViews[i].text = "-"
                        amountViews[i].text = "₱0.00"
                    }
                }

                // Prepare pie chart entries (uses ALL categories)
                val pieEntries = allCategories.map { (category, total) ->
                    PieEntry(total.toFloat(), category)
                }

                setupPieChart(pieEntries)
            },

            onFailure = { e ->
                // Optional: show toast
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}