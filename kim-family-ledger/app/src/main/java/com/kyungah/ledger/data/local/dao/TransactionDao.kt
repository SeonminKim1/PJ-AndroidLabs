package com.kimfamily.ledger.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kimfamily.ledger.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

data class TypeTotalRow(
    val type: String,
    val total: Long,
)

data class CategoryTotalRow(
    val categoryId: Long,
    val total: Long,
)

@Dao
interface TransactionDao {
    @Query(
        """
        SELECT * FROM transactions
        WHERE occurredAtMillis >= :startMillis AND occurredAtMillis < :endMillis
        ORDER BY occurredAtMillis DESC
        """,
    )
    fun observeByMonth(startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE occurredAtMillis >= :startMillis AND occurredAtMillis < :endMillis
        AND (memo LIKE '%' || :query || '%' OR :query = '')
        ORDER BY occurredAtMillis DESC
        """,
    )
    fun observeByMonthWithQuery(
        startMillis: Long,
        endMillis: Long,
        query: String,
    ): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE occurredAtMillis >= :startMillis AND occurredAtMillis < :endMillis
        AND type = :type
        AND categoryId = :categoryId
        ORDER BY occurredAtMillis DESC
        """,
    )
    fun observeByMonthAndCategory(
        startMillis: Long,
        endMillis: Long,
        type: String,
        categoryId: Long,
    ): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query(
        """
        SELECT type, SUM(amount) AS total FROM transactions
        WHERE occurredAtMillis >= :startMillis AND occurredAtMillis < :endMillis
        GROUP BY type
        """,
    )
    fun observeTypeTotals(startMillis: Long, endMillis: Long): Flow<List<TypeTotalRow>>

    @Query(
        """
        SELECT categoryId, SUM(amount) AS total FROM transactions
        WHERE occurredAtMillis >= :startMillis AND occurredAtMillis < :endMillis
        AND type = :type
        GROUP BY categoryId
        ORDER BY total DESC
        """,
    )
    fun observeCategoryTotals(
        startMillis: Long,
        endMillis: Long,
        type: String,
    ): Flow<List<CategoryTotalRow>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE occurredAtMillis >= :startMillis AND occurredAtMillis < :endMillis
        ORDER BY occurredAtMillis ASC
        """,
    )
    suspend fun getAllInMonth(startMillis: Long, endMillis: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions ORDER BY occurredAtMillis ASC")
    suspend fun getAll(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :categoryId")
    suspend fun countByCategory(categoryId: Long): Int

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun count(): Int

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(transaction: TransactionEntity)
}
