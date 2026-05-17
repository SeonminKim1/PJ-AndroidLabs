package com.kimfamily.ledger.navigation

import com.kimfamily.ledger.domain.model.TransactionType

object Routes {
    const val HOME = "home"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val TRANSACTION_FORM = "transaction_form?transactionId={transactionId}&type={type}"
    const val CATEGORY_LIST = "category_list/{type}"

    fun transactionForm(transactionId: Long = 0L, type: TransactionType = TransactionType.EXPENSE): String =
        "transaction_form?transactionId=$transactionId&type=${type.name}"

    fun categoryList(type: TransactionType): String = "category_list/${type.name}"

    val bottomNavRoutes = setOf(HOME, STATS, SETTINGS)
}
