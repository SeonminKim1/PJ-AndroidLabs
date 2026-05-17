package com.kimfamily.ledger.data.mapper

import com.kimfamily.ledger.data.local.entity.CategoryEntity
import com.kimfamily.ledger.data.local.entity.TransactionEntity
import com.kimfamily.ledger.domain.model.Category
import com.kimfamily.ledger.domain.model.Transaction
import com.kimfamily.ledger.domain.model.TransactionType

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    type = TransactionType.fromStorage(type),
    name = name,
    iconKey = iconKey,
    colorArgb = colorArgb,
    isDefault = isDefault,
)

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    type = TransactionType.fromStorage(type),
    amount = amount,
    categoryId = categoryId,
    memo = memo,
    occurredAtMillis = occurredAtMillis,
    createdAtMillis = createdAtMillis,
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    type = type.name,
    amount = amount,
    categoryId = categoryId,
    memo = memo,
    occurredAtMillis = occurredAtMillis,
    createdAtMillis = createdAtMillis,
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    type = type.name,
    name = name,
    iconKey = iconKey,
    colorArgb = colorArgb,
    isDefault = isDefault,
)
