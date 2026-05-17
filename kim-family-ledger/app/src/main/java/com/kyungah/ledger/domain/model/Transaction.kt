package com.kimfamily.ledger.domain.model

data class Transaction(
    val id: Long,
    val type: TransactionType,
    val amount: Long,
    val categoryId: Long,
    val memo: String?,
    val occurredAtMillis: Long,
    val createdAtMillis: Long,
)

data class TransactionWithCategory(
    val transaction: Transaction,
    val category: Category,
)

data class CategorySummary(
    val category: Category,
    val totalAmount: Long,
)

data class MonthlySummary(
    val incomeTotal: Long,
    val expenseTotal: Long,
) {
    val netTotal: Long get() = incomeTotal - expenseTotal
}
