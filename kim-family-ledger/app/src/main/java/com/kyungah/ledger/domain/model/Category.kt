package com.kimfamily.ledger.domain.model

data class Category(
    val id: Long,
    val type: TransactionType,
    val name: String,
    val iconKey: String,
    val colorArgb: Int,
    val isDefault: Boolean,
)
