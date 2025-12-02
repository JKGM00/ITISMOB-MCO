package com.itismob.grpfive.mco.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.itismob.grpfive.mco.DashboardTestActivity

import com.itismob.grpfive.mco.dashboard_fragments.LowStockAlertsFragment
import com.itismob.grpfive.mco.dashboard_fragments.RevenueProfitFragment
import com.itismob.grpfive.mco.dashboard_fragments.TopCategoriesFragment

class DashboardPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val periodProvider: () -> String
) : FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        val period = periodProvider()

        return when(position) {
            0 -> RevenueProfitFragment.newInstance(period)
            1 -> TopCategoriesFragment.newInstance(period)
            2 -> LowStockAlertsFragment()
            else -> Fragment()
        }
    }

    override fun getItemCount() = 3
}