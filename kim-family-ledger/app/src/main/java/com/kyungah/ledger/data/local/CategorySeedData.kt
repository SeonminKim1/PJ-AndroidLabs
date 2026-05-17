package com.kimfamily.ledger.data.local

import com.kimfamily.ledger.data.local.entity.CategoryEntity
import com.kimfamily.ledger.domain.model.TransactionType

object CategorySeedData {
    val defaults: List<CategoryEntity> = listOf(
        category(TransactionType.EXPENSE, "식비", "restaurant", 0xFFE57373.toInt()),
        category(TransactionType.EXPENSE, "교통", "directions_bus", 0xFF64B5F6.toInt()),
        category(TransactionType.EXPENSE, "쇼핑", "shopping_bag", 0xFFBA68C8.toInt()),
        category(TransactionType.EXPENSE, "주거/공과금", "home", 0xFF4DB6AC.toInt()),
        category(TransactionType.EXPENSE, "문화/여가", "movie", 0xFFFFB74D.toInt()),
        category(TransactionType.EXPENSE, "의료", "local_hospital", 0xFF81C784.toInt()),
        category(TransactionType.EXPENSE, "교육", "school", 0xFF7986CB.toInt()),
        category(TransactionType.EXPENSE, "기타", "more_horiz", 0xFF90A4AE.toInt()),
        category(TransactionType.INCOME, "급여", "payments", 0xFF3D7C59.toInt()),
        category(TransactionType.INCOME, "부수입", "work", 0xFF5E8C61.toInt()),
        category(TransactionType.INCOME, "용돈", "card_giftcard", 0xFF7E6BAF.toInt()),
        category(TransactionType.INCOME, "이자/배당", "savings", 0xFF4F8CA8.toInt()),
        category(TransactionType.INCOME, "기타", "more_horiz", 0xFF8A7A5C.toInt()),
    )

    private fun category(
        type: TransactionType,
        name: String,
        iconKey: String,
        colorArgb: Int,
    ): CategoryEntity = CategoryEntity(
        type = type.name,
        name = name,
        iconKey = iconKey,
        colorArgb = colorArgb,
        isDefault = true,
    )
}
