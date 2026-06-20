package com.kimfamily.ledger.data.repository

import com.kimfamily.ledger.data.local.dao.CategoryDao
import com.kimfamily.ledger.data.local.dao.TransactionDao
import com.kimfamily.ledger.data.mapper.toDomain
import com.kimfamily.ledger.data.mapper.toEntity
import com.kimfamily.ledger.domain.model.Category
import com.kimfamily.ledger.domain.model.CategorySummary
import com.kimfamily.ledger.domain.model.MonthlySummary
import com.kimfamily.ledger.domain.model.Transaction
import com.kimfamily.ledger.domain.model.TransactionType
import com.kimfamily.ledger.domain.model.TransactionWithCategory
import com.kimfamily.ledger.ui.util.MonthRange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class LedgerRepository(
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
) {
    fun observeCategories(type: TransactionType): Flow<List<Category>> =
        categoryDao.observeByType(type.name).map { list -> list.map { it.toDomain() } }

    fun observeAllCategories(): Flow<List<Category>> =
        categoryDao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeTransactions(
        year: Int,
        month: Int,
        query: String = "",
    ): Flow<List<TransactionWithCategory>> {
        val range = MonthRange.of(year, month)
        val transactionsFlow = if (query.isBlank()) {
            transactionDao.observeByMonth(range.startMillis, range.endMillis)
        } else {
            transactionDao.observeByMonthWithQuery(range.startMillis, range.endMillis, query)
        }
        return combine(transactionsFlow, categoryDao.observeAll()) { transactions, categories ->
            val categoryMap = categories.associateBy { it.id }
            transactions.mapNotNull { entity ->
                val category = categoryMap[entity.categoryId]?.toDomain() ?: return@mapNotNull null
                TransactionWithCategory(entity.toDomain(), category)
            }
        }
    }

    fun observeTransactionsByCategory(
        year: Int,
        month: Int,
        type: TransactionType,
        categoryId: Long,
    ): Flow<List<TransactionWithCategory>> {
        val range = MonthRange.of(year, month)
        return combine(
            transactionDao.observeByMonthAndCategory(
                range.startMillis,
                range.endMillis,
                type.name,
                categoryId,
            ),
            categoryDao.observeAll(),
        ) { transactions, categories ->
            val categoryMap = categories.associateBy { it.id }
            transactions.mapNotNull { entity ->
                val category = categoryMap[entity.categoryId]?.toDomain() ?: return@mapNotNull null
                TransactionWithCategory(entity.toDomain(), category)
            }
        }
    }

    fun observeMonthlySummary(year: Int, month: Int): Flow<MonthlySummary> {
        val range = MonthRange.of(year, month)
        return transactionDao.observeTypeTotals(range.startMillis, range.endMillis).map { rows ->
            var income = 0L
            var expense = 0L
            rows.forEach { row ->
                when (TransactionType.fromStorage(row.type)) {
                    TransactionType.INCOME -> income = row.total
                    TransactionType.EXPENSE -> expense = row.total
                }
            }
            MonthlySummary(incomeTotal = income, expenseTotal = expense)
        }
    }

    fun observeCategorySummaries(
        year: Int,
        month: Int,
        type: TransactionType,
    ): Flow<List<CategorySummary>> {
        val range = MonthRange.of(year, month)
        return combine(
            transactionDao.observeCategoryTotals(range.startMillis, range.endMillis, type.name),
            categoryDao.observeByType(type.name),
        ) { totals, categories ->
            val categoryMap = categories.associateBy { it.id }
            totals.mapNotNull { row ->
                val category = categoryMap[row.categoryId]?.toDomain() ?: return@mapNotNull null
                CategorySummary(category, row.total)
            }
        }
    }

    suspend fun getTransaction(id: Long): Transaction? =
        transactionDao.getById(id)?.toDomain()

    suspend fun getCategory(id: Long): Category? =
        categoryDao.getById(id)?.toDomain()

    suspend fun saveTransaction(transaction: Transaction): Long {
        val entity = transaction.toEntity()
        return if (transaction.id == 0L) {
            transactionDao.insert(entity.copy(id = 0))
        } else {
            transactionDao.update(entity)
            transaction.id
        }
    }

    suspend fun deleteTransaction(id: Long) {
        transactionDao.getById(id)?.let { transactionDao.delete(it) }
    }

    suspend fun saveCategory(category: Category): Long {
        val entity = category.toEntity()
        return if (category.id == 0L) {
            categoryDao.insert(entity.copy(id = 0, isDefault = false))
        } else {
            categoryDao.update(entity)
            category.id
        }
    }

    suspend fun deleteCategory(category: Category): Boolean {
        if (category.isDefault) return false
        if (transactionDao.countByCategory(category.id) > 0) return false
        categoryDao.delete(category.toEntity())
        return true
    }

    suspend fun exportCsv(year: Int, month: Int): String {
        val range = MonthRange.of(year, month)
        val transactions = transactionDao.getAllInMonth(range.startMillis, range.endMillis)
        val header = "날짜,유형,카테고리,금액,메모\n"
        val sb = StringBuilder(header)
        transactions.forEach { tx ->
            val cat = categoryDao.getById(tx.categoryId)
            val typeLabel = if (tx.type == TransactionType.INCOME.name) "수입" else "지출"
            val date = com.kimfamily.ledger.ui.util.Formatters.formatDateTime(tx.occurredAtMillis)
            val catName = cat?.name ?: "?"
            val memo = tx.memo?.replace(",", " ") ?: ""
            sb.append("$date,$typeLabel,$catName,${tx.amount},$memo\n")
        }
        return sb.toString()
    }

    suspend fun exportBackupJson(): String {
        val categories = categoryDao.getAll()
        val transactions = transactionDao.getAll()
        val root = JSONObject()
            .put("version", 1)
            .put("exportedAtMillis", System.currentTimeMillis())
            .put(
                "categories",
                JSONArray().apply {
                    categories.forEach { category ->
                        put(
                            JSONObject()
                                .put("id", category.id)
                                .put("type", category.type)
                                .put("name", category.name)
                                .put("iconKey", category.iconKey)
                                .put("colorArgb", category.colorArgb)
                                .put("isDefault", category.isDefault),
                        )
                    }
                },
            )
            .put(
                "transactions",
                JSONArray().apply {
                    transactions.forEach { transaction ->
                        put(
                            JSONObject()
                                .put("id", transaction.id)
                                .put("type", transaction.type)
                                .put("amount", transaction.amount)
                                .put("categoryId", transaction.categoryId)
                                .put("memo", transaction.memo)
                                .put("occurredAtMillis", transaction.occurredAtMillis)
                                .put("createdAtMillis", transaction.createdAtMillis),
                        )
                    }
                },
            )
        return root.toString(2)
    }

    suspend fun importBackupJson(json: String) {
        val root = JSONObject(json)
        val categoriesJson = root.getJSONArray("categories")
        val transactionsJson = root.getJSONArray("transactions")

        val categories = List(categoriesJson.length()) { index ->
            val item = categoriesJson.getJSONObject(index)
            com.kimfamily.ledger.data.local.entity.CategoryEntity(
                id = item.getLong("id"),
                type = item.getString("type"),
                name = item.getString("name"),
                iconKey = item.getString("iconKey"),
                colorArgb = item.getInt("colorArgb"),
                isDefault = item.getBoolean("isDefault"),
            )
        }
        val transactions = List(transactionsJson.length()) { index ->
            val item = transactionsJson.getJSONObject(index)
            com.kimfamily.ledger.data.local.entity.TransactionEntity(
                id = item.getLong("id"),
                type = item.getString("type"),
                amount = item.getLong("amount"),
                categoryId = item.getLong("categoryId"),
                memo = item.optString("memo").takeUnless { it == "null" || it.isBlank() },
                occurredAtMillis = item.getLong("occurredAtMillis"),
                createdAtMillis = item.getLong("createdAtMillis"),
            )
        }

        transactionDao.deleteAll()
        categoryDao.deleteAll()
        categoryDao.insertAll(categories)
        transactionDao.insertAll(transactions)
    }
}
