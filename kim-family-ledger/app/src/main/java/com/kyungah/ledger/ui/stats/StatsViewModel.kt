package com.kimfamily.ledger.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kimfamily.ledger.data.repository.LedgerRepository
import com.kimfamily.ledger.domain.model.CategorySummary
import com.kimfamily.ledger.domain.model.MonthlySummary
import com.kimfamily.ledger.domain.model.TransactionType
import com.kimfamily.ledger.domain.model.TransactionWithCategory
import com.kimfamily.ledger.ui.util.MonthRange
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class StatsUiState(
    val monthRange: MonthRange = MonthRange.current(),
    val summary: MonthlySummary = MonthlySummary(0, 0),
    val expenseBreakdown: List<CategorySummary> = emptyList(),
    val incomeBreakdown: List<CategorySummary> = emptyList(),
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val selectedCategoryId: Long? = null,
    val groupedSelectedCategoryTransactions: List<StatsTransactionDayGroup> = emptyList(),
)

data class StatsTransactionDayGroup(
    val header: String,
    val items: List<TransactionWithCategory>,
)

class StatsViewModel(
    private val repository: LedgerRepository,
) : ViewModel() {
    private val monthRange = MutableStateFlow(MonthRange.current())
    private val selectedType = MutableStateFlow(TransactionType.EXPENSE)
    private val selectedCategoryId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<StatsUiState> = combine(
        monthRange,
        selectedType,
        selectedCategoryId,
    ) { month, type, categoryId ->
        StatsSelection(month, type, categoryId)
    }
        .flatMapLatest { selection ->
            val selectedTransactionsFlow = selection.categoryId?.let { categoryId ->
                repository.observeTransactionsByCategory(
                    selection.monthRange.year,
                    selection.monthRange.month,
                    selection.type,
                    categoryId,
                )
            } ?: flowOf(emptyList())

            combine(
                repository.observeMonthlySummary(selection.monthRange.year, selection.monthRange.month),
                repository.observeCategorySummaries(
                    selection.monthRange.year,
                    selection.monthRange.month,
                    TransactionType.EXPENSE,
                ),
                repository.observeCategorySummaries(
                    selection.monthRange.year,
                    selection.monthRange.month,
                    TransactionType.INCOME,
                ),
                selectedTransactionsFlow,
            ) { summary, expense, income, selectedTransactions ->
                StatsUiState(
                    monthRange = selection.monthRange,
                    summary = summary,
                    expenseBreakdown = expense,
                    incomeBreakdown = income,
                    selectedType = selection.type,
                    selectedCategoryId = selection.categoryId,
                    groupedSelectedCategoryTransactions = groupByDay(selectedTransactions),
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatsUiState(),
        )

    fun onPreviousMonth() {
        monthRange.update { it.previous() }
        selectedCategoryId.value = null
    }

    fun onNextMonth() {
        monthRange.update { it.next() }
        selectedCategoryId.value = null
    }

    fun onTypeSelected(type: TransactionType) {
        selectedType.value = type
        selectedCategoryId.value = null
    }

    fun onCategorySelected(categoryId: Long) {
        selectedCategoryId.update { selectedId ->
            if (selectedId == categoryId) null else categoryId
        }
    }

    private fun groupByDay(
        transactions: List<TransactionWithCategory>,
    ): List<StatsTransactionDayGroup> {
        if (transactions.isEmpty()) return emptyList()
        return transactions
            .groupBy { com.kimfamily.ledger.ui.util.Formatters.formatGroupHeader(it.transaction.occurredAtMillis) }
            .map { (header, items) -> StatsTransactionDayGroup(header, items) }
    }

    private data class StatsSelection(
        val monthRange: MonthRange,
        val type: TransactionType,
        val categoryId: Long?,
    )
}
