package com.kimfamily.ledger.ui.util

import java.util.Calendar

data class MonthRange(
    val year: Int,
    val month: Int,
    val startMillis: Long,
    val endMillis: Long,
) {
    val label: String get() = "${year}년 ${month}월"

    val isCurrentMonth: Boolean
        get() {
            val now = current()
            return year == now.year && month == now.month
        }

    companion object {
        fun of(year: Int, month: Int): MonthRange {
            val start = Calendar.getInstance().apply {
                set(year, month - 1, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val end = Calendar.getInstance().apply {
                set(year, month - 1, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.MONTH, 1)
            }
            return MonthRange(year, month, start.timeInMillis, end.timeInMillis)
        }

        fun current(): MonthRange {
            val cal = Calendar.getInstance()
            return of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        }
    }

    fun previous(): MonthRange {
        val cal = Calendar.getInstance().apply {
            set(year, month - 1, 1)
            add(Calendar.MONTH, -1)
        }
        return of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    fun next(): MonthRange {
        val cal = Calendar.getInstance().apply {
            set(year, month - 1, 1)
            add(Calendar.MONTH, 1)
        }
        return of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }
}
