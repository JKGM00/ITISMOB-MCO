package com.itismob.grpfive.mco.utils

import java.util.Calendar

object TimeUtils {

    fun dayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.add(Calendar.DAY_OF_MONTH, 1)
        val end = cal.timeInMillis

        return start to end
    }

    fun weekRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.add(Calendar.WEEK_OF_YEAR, 1)
        val end = cal.timeInMillis

        return start to end
    }

    fun monthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis

        return start to end
    }

    fun quarterRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH)

        val quarterStartMonth = when (month) {
            in 0..2 -> 0   // Q1
            in 3..5 -> 3   // Q2
            in 6..8 -> 6   // Q3
            else -> 9      // Q4
        }

        cal.set(Calendar.MONTH, quarterStartMonth)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.add(Calendar.MONTH, 3)
        val end = cal.timeInMillis

        return start to end
    }

    fun yearRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, 0)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.add(Calendar.YEAR, 1)
        val end = cal.timeInMillis

        return start to end
    }

    // Had to add ways to separate buckets per week (for month), and per month (for quarterly and yearly)
    fun monthlyWeekBuckets(start: Long): List<Pair<Long, String>> {
        val cal = Calendar.getInstance()
        cal.timeInMillis = start

        val buckets = mutableListOf<Pair<Long, String>>()

        repeat(4) { week ->
            buckets += cal.timeInMillis to "Week ${week + 1}"
            cal.add(Calendar.WEEK_OF_MONTH, 1)
        }

        return buckets
    }

    fun quarterlyMonthBuckets(start: Long): List<Pair<Long, String>> {
        val cal = Calendar.getInstance()
        cal.timeInMillis = start

        val names = listOf("Month 1", "Month 2", "Month 3")
        val buckets = mutableListOf<Pair<Long, String>>()

        repeat(3) { i ->
            buckets += cal.timeInMillis to names[i]
            cal.add(Calendar.MONTH, 1)
        }

        return buckets
    }

    fun yearlyMonthBuckets(start: Long): List<Pair<Long, String>> {
        val cal = Calendar.getInstance()
        cal.timeInMillis = start

        val monthNames = listOf(
            "Jan","Feb","Mar","Apr","May","Jun",
            "Jul","Aug","Sep","Oct","Nov","Dec"
        )

        return (0..11).map { i ->
            val monthStart = cal.timeInMillis
            val label = monthNames[cal.get(Calendar.MONTH)]
            cal.add(Calendar.MONTH, 1)
            monthStart to label
        }
    }
}