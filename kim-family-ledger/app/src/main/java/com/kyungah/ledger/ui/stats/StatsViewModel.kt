package com.kimfamily.ledger.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kimfamily.ledger.data.repository.LedgerRepository
import com.kimfamily.ledger.domain.model.CategorySummary
import com.kimfamily.ledger.domain.model.MonthlySummary
import com.kimfamily.ledger.domain.model.TransactionType
import com.kimfamily.ledger.ui.util.MonthRange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class StatsUiState(
    val monthRange: MonthRange = MonthRange.current(),
    val summary: MonthlySummary = MonthlySummary(0, 0),
    val expenseBreakdown: List<CategorySummary> = emptyList(),
    val incomeBreakdown: List<CategorySummary> = emptyList(),
    val selectedType: TransactionType = TransactionType.EXPENSE,
)

class StatsViewModel(
    private val repository: LedgerRepository,
) : ViewModel() {
    private val monthRange = MutableStateFlow(MonthRange.current())
    private val selectedType = MutableStateFlow(TransactionType.EXPENSE)

    val uiState: StateFlow<StatsUiState> = combine(monthRange, selectedType) { month, type ->
        month to type
    }
        .flatMapLatest { (month, type) ->
            combine(
                repository.observeMonthlySummary(month.year, month.month),
                repository.observeCategorySummaries(month.year, month.month, TransactionType.EXPENSE),
                repository.observeCategorySummaries(month.year, month.month, TransactionType.INCOME),
            ) { summary, expense, income ->
                StatsUiState(
                    monthRange = month,
                    summary = summary,
                    expenseBreakdown = expense,
                    incomeBreakdown = income,
                    selectedType = type,
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
    }

    fun onNextMonth() {
        monthRange.update { it.next() }
    }

    fun onTypeSelected(type: TransactionType) {
        selectedType.value = type
    }
}
