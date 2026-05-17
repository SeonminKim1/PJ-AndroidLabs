package com.kimfamily.ledger.domain.model

enum class TransactionType {
    EXPENSE,
    INCOME,
    ;

    companion object {
        fun fromStorage(value: String): TransactionType =
            entries.firstOrNull { it.name == value } ?: EXPENSE
    }
}
