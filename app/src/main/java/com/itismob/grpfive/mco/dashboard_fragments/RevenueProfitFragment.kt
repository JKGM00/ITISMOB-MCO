package com.itismob.grpfive.mco.dashboard_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.itismob.grpfive.mco.databinding.FragmentRevenueProfitLayoutBinding
import com.itismob.grpfive.mco.models.Transaction
import com.itismob.grpfive.mco.DatabaseHelper
import com.itismob.grpfive.mco.R
import com.itismob.grpfive.mco.utils.TimeUtils
import java.util.Calendar

class RevenueProfitFragment : Fragment() {
    companion object {
        private const val ARG_PERIOD = "arg_period"

        fun newInstance(period: String): RevenueProfitFragment {
            val frag = RevenueProfitFragment()
            val bundle = Bundle()
            bundle.putString(ARG_PERIOD, period)
            frag.arguments = bundle
            return frag
        }
    }

    private var _binding: FragmentRevenueProfitLayoutBinding? = null
    private val binding get() = _binding!!
    private var selectedPeriod: String = "Daily"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedPeriod = arguments?.getString(ARG_PERIOD) ?: "Daily"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRevenueProfitLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initial load (daily)
        val period = arguments?.getString(ARG_PERIOD) ?: "Daily"
        updateRevenueForPeriod(period)
    }

    fun updateRevenueForPeriod(period: String) {
        val (start, end) = when(period) {
            "Daily" -> TimeUtils.dayRange()
            "Weekly" -> TimeUtils.weekRange()
            "Monthly" -> TimeUtils.monthRange()
            "Quarterly" -> TimeUtils.quarterRange()
            "Yearly" -> TimeUtils.yearRange()
            else -> TimeUtils.dayRange()
        }

        DatabaseHelper.getTransactionsForPeriod(start, end,
            onSuccess = { transactions ->
                updateStatsCard(transactions, period)
                updateLineChart(transactions, period)
            },
            onFailure = {
                // You might show a Toast or log the error
            }
        )
    }

    private fun updateStatsCard(transactions: List<Transaction>, period: String) {
        val totalRevenue = transactions.sumOf { it.items.sumOf { item -> item.subtotal ?: 0.0 } }
        val totalProfit = transactions.sumOf { it.items.sumOf { item -> item.totalProfit } }

        binding.tvRevenueAmount.text = "₱${"%.2f".format(totalRevenue)}"
        binding.tvRevenueAmount3.text = "₱${"%.2f".format(totalProfit)}"
        binding.tvTransactionCount.text = "${transactions.size} transactions"

        // Update the label based on the period
        val periodLabel = when(period) {
            "Daily" -> "for Today"
            "Weekly" -> "for This Week"
            "Monthly" -> "for This Month"
            "Quarterly" -> "for This Quarter"
            "Yearly" -> "for This Year"
            else -> ""
        }

        binding.tvStatsLabel.text = "Total Revenue and Profit $periodLabel"
    }

    // implement updateLineChart
    private fun updateLineChart(transactions: List<Transaction>, period: String) {
        val chart = binding.lineChartStats

        val revenueEntries = mutableListOf<Entry>()
        val profitEntries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()

        // Determine time range
        val (periodStart, _) = when (period) {
            "Daily" -> TimeUtils.dayRange()
            "Weekly" -> TimeUtils.weekRange()
            "Monthly" -> TimeUtils.monthRange()
            "Quarterly" -> TimeUtils.quarterRange()
            "Yearly" -> TimeUtils.yearRange()
            else -> TimeUtils.dayRange()
        }

        // Determine buckets based on selected period
        val buckets: List<Pair<Long, String>> = when (period) {
            "Daily" -> TimeUtils.dailyHourBuckets(periodStart)
            "Weekly" -> TimeUtils.weeklyDayBuckets(periodStart)
            "Monthly" -> TimeUtils.monthlyWeekBuckets(periodStart)
            "Quarterly" -> TimeUtils.quarterlyMonthBuckets(periodStart)
            "Yearly" -> TimeUtils.yearlyMonthBuckets(periodStart)
            else -> emptyList()
        }

        // Fill data for each bucket
        buckets.forEachIndexed { index, (bucketStart, label) ->
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = bucketStart }

            // Each bucket spans until the next bucket (or ends naturally)
            val bucketEnd = if (index < buckets.lastIndex) {
                buckets[index + 1].first
            } else {
                // Last bucket: estimate end (month or period edge)
//                when (period) {
//                    "Daily" -> bucketStart + 60 * 60 * 1000L
//                    "Weekly" -> bucketStart + 24 * 60 * 60 * 1000L
//                    "Monthly" -> bucketStart + 7 * 24 * 60 * 60 * 1000L
//                    "Quarterly" -> bucketStart + 30L * 24 * 60 * 60 * 1000L
//                    "Yearly" -> bucketStart + 30L * 24 * 60 * 60 * 1000L
//                    else -> bucketStart
//                }

                Calendar.getInstance().apply {
                    timeInMillis = bucketStart
                    when (period) {
                        "Daily" -> add(Calendar.HOUR_OF_DAY, 1)
                        "Weekly" -> add(Calendar.DAY_OF_MONTH, 1)
                        "Monthly" -> add(Calendar.WEEK_OF_MONTH, 1)
                        "Quarterly", "Yearly" -> add(Calendar.MONTH, 1)
                    }
                }.timeInMillis
            }

            // Filter transactions inside this bucket
            val bucketTransactions = transactions.filter {
                it.createdAt in bucketStart until bucketEnd
            }

            val revenue = bucketTransactions.sumOf { it.items.sumOf { item -> item.subtotal ?: 0.0 } }
            val profit = bucketTransactions.sumOf { it.items.sumOf { item -> item.totalProfit } }

            revenueEntries.add(Entry(index.toFloat(), revenue.toFloat()))
            profitEntries.add(Entry(index.toFloat(), profit.toFloat()))
            labels.add(label)
        }

        // DataSets
        val revenueDataSet = LineDataSet(revenueEntries, "Revenue").apply {
            color = requireContext().getColor(R.color.Dark_Blue)
            setCircleColor(requireContext().getColor(R.color.Dark_Blue))
            lineWidth = 2f
            circleRadius = 4f
            valueTextSize = 10f
        }

        val profitDataSet = LineDataSet(profitEntries, "Profit").apply {
            color = requireContext().getColor(R.color.Dark_Green)
            setCircleColor(requireContext().getColor(R.color.Dark_Green))
            lineWidth = 2f
            circleRadius = 4f
            valueTextSize = 10f
        }

        chart.data = LineData(revenueDataSet, profitDataSet)

        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            valueFormatter = IndexAxisValueFormatter(labels)
        }

        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false
        chart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}