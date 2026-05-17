package com.kimfamily.ledger.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kimfamily.ledger.data.repository.LedgerRepository
import com.kimfamily.ledger.domain.model.MonthlySummary
import com.kimfamily.ledger.domain.model.TransactionWithCategory
import com.kimfamily.ledger.ui.util.MonthRange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class HomeUiState(
    val monthRange: MonthRange = MonthRange.current(),
    val searchQuery: String = "",
    val summary: MonthlySummary = MonthlySummary(0, 0),
    val transactions: List<TransactionWithCategory> = emptyList(),
    val groupedTransactions: List<TransactionDayGroup> = emptyList(),
)

data class TransactionDayGroup(
    val header: String,
    val items: List<TransactionWithCategory>,
)

class HomeViewModel(
    private val repository: LedgerRepository,
) : ViewModel() {
    private val monthRange = MutableStateFlow(MonthRange.current())
    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<HomeUiState> = combine(
        monthRange,
        searchQuery,
    ) { month, query -> month to query }
        .flatMapLatest { (month, query) ->
            combine(
                repository.observeMonthlySummary(month.year, month.month),
                repository.observeTransactions(month.year, month.month, query),
            ) { summary, transactions ->
                HomeUiState(
                    monthRange = month,
                    searchQuery = query,
                    summary = summary,
                    transactions = transactions,
                    groupedTransactions = groupByDay(transactions),
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )

    fun onPreviousMonth() {
        monthRange.update { it.previous() }
    }

    fun onNextMonth() {
        monthRange.update { it.next() }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    private fun groupByDay(
        transactions: List<TransactionWithCategory>,
    ): List<TransactionDayGroup> {
        if (transactions.isEmpty()) return emptyList()
        return transactions
            .groupBy { com.kimfamily.ledger.ui.util.Formatters.formatGroupHeader(it.transaction.occurredAtMillis) }
            .map { (header, items) -> TransactionDayGroup(header, items) }
    }
}
