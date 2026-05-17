package com.kimfamily.ledger.ui.util

import com.kimfamily.ledger.domain.model.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Formatters {
    private val koreanLocale = Locale.KOREA
    private val currencyFormat = NumberFormat.getNumberInstance(koreanLocale)
    private val dateFormat = SimpleDateFormat("M월 d일", koreanLocale)
    private val weekdayFormat = SimpleDateFormat("E", koreanLocale)
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", koreanLocale)
    private val formDateFormat = SimpleDateFormat("yyyy년 M월 d일 (E)", koreanLocale)
    private val formTimeFormat = SimpleDateFormat("HH:mm", koreanLocale)

    fun formatAmount(amount: Long): String = "${currencyFormat.format(amount)}원"

    fun formatAmountNumber(amount: Long): String = currencyFormat.format(amount)

    fun formatSignedNetAmount(net: Long): String {
        val prefix = if (net >= 0) "+" else "−"
        return "$prefix${currencyFormat.format(kotlin.math.abs(net))}원"
    }

    fun formatSignedAmount(type: TransactionType, amount: Long): String {
        val prefix = when (type) {
            TransactionType.INCOME -> "+"
            TransactionType.EXPENSE -> "−"
        }
        return "$prefix${currencyFormat.format(amount)}원"
    }

    fun formatDateTime(millis: Long): String = dateTimeFormat.format(Date(millis))

    fun formatFormDate(millis: Long): String = formDateFormat.format(Date(millis))

    fun formatFormTime(millis: Long): String = formTimeFormat.format(Date(millis))

    fun formatGroupHeader(millis: Long): String {
        val today = startOfDay(Calendar.getInstance())
        val target = startOfDay(Calendar.getInstance().apply { timeInMillis = millis })
        val yesterday = startOfDay(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) })

        val weekday = weekdayFormat.format(Date(millis))
        return when (target.timeInMillis) {
            today.timeInMillis -> "오늘 ($weekday)"
            yesterday.timeInMillis -> "어제 ($weekday)"
            else -> "${dateFormat.format(Date(millis))} ($weekday)"
        }
    }

    fun formatShortDateTime(millis: Long): String = formTimeFormat.format(Date(millis))

    fun parseAmount(input: String): Long? {
        val digits = input.filter { it.isDigit() }
        if (digits.isEmpty()) return null
        return digits.toLongOrNull()
    }

    private fun startOfDay(calendar: Calendar): Calendar =
        Calendar.getInstance().apply {
            timeInMillis = calendar.timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
}
