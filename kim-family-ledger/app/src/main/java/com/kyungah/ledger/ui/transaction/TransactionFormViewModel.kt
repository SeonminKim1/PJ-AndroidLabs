package com.kimfamily.ledger.ui.transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kimfamily.ledger.data.repository.LedgerRepository
import com.kimfamily.ledger.domain.model.Category
import com.kimfamily.ledger.domain.model.Transaction
import com.kimfamily.ledger.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TransactionFormUiState(
    val isLoading: Boolean = true,
    val isNew: Boolean = true,
    val type: TransactionType = TransactionType.EXPENSE,
    val amountText: String = "",
    val selectedCategoryId: Long? = null,
    val categories: List<Category> = emptyList(),
    val memo: String = "",
    val occurredAtMillis: Long = System.currentTimeMillis(),
    val errorMessage: String? = null,
    val saved: Boolean = false,
    val deleted: Boolean = false,
)

class TransactionFormViewModel(
    private val repository: LedgerRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val transactionId: Long = savedStateHandle.get<Long>("transactionId") ?: 0L
    private val initialType: TransactionType = TransactionType.fromStorage(
        savedStateHandle.get<String>("type") ?: TransactionType.EXPENSE.name,
    )

    private val _uiState = MutableStateFlow(
        TransactionFormUiState(
            isNew = transactionId == 0L,
            type = initialType,
            isLoading = transactionId != 0L,
        ),
    )
    val uiState: StateFlow<TransactionFormUiState> = _uiState.asStateFlow()

    private var categoriesJob: Job? = null

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            if (transactionId != 0L) {
                val existing = repository.getTransaction(transactionId)
                if (existing == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "거래를 찾을 수 없습니다") }
                    return@launch
                }
                observeCategories(existing.type)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isNew = false,
                        type = existing.type,
                        amountText = existing.amount.toString(),
                        selectedCategoryId = existing.categoryId,
                        memo = existing.memo.orEmpty(),
                        occurredAtMillis = existing.occurredAtMillis,
                    )
                }
            } else {
                observeCategories(initialType)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun observeCategories(type: TransactionType) {
        categoriesJob?.cancel()
        categoriesJob = viewModelScope.launch {
            repository.observeCategories(type).collect { categories ->
                _uiState.update { state ->
                    val selected = state.selectedCategoryId
                        ?: categories.firstOrNull()?.id
                    state.copy(
                        categories = categories,
                        selectedCategoryId = selected,
                    )
                }
            }
        }
    }

    fun onTypeChange(type: TransactionType) {
        if (_uiState.value.type == type) return
        _uiState.update {
            it.copy(type = type, selectedCategoryId = null, categories = emptyList())
        }
        observeCategories(type)
    }

    fun onAmountChange(text: String) {
        _uiState.update { it.copy(amountText = text.filter { c -> c.isDigit() }, errorMessage = null) }
    }

    fun onCategorySelected(id: Long) {
        _uiState.update { it.copy(selectedCategoryId = id) }
    }

    fun onMemoChange(memo: String) {
        _uiState.update { it.copy(memo = memo) }
    }

    fun onDateTimeChange(millis: Long) {
        _uiState.update { it.copy(occurredAtMillis = millis) }
    }

    fun save() {
        val state = _uiState.value
        val amount = com.kimfamily.ledger.ui.util.Formatters.parseAmount(state.amountText)
        val categoryId = state.selectedCategoryId
        when {
            amount == null || amount <= 0 ->
                _uiState.update { it.copy(errorMessage = "금액을 입력해 주세요") }
            categoryId == null ->
                _uiState.update { it.copy(errorMessage = "카테고리를 선택해 주세요") }
            else -> viewModelScope.launch {
                val now = System.currentTimeMillis()
                repository.saveTransaction(
                    Transaction(
                        id = if (state.isNew) 0L else transactionId,
                        type = state.type,
                        amount = amount,
                        categoryId = categoryId,
                        memo = state.memo.takeIf { it.isNotBlank() },
                        occurredAtMillis = state.occurredAtMillis,
                        createdAtMillis = if (state.isNew) now else {
                            repository.getTransaction(transactionId)?.createdAtMillis ?: now
                        },
                    ),
                )
                _uiState.update { it.copy(saved = true, errorMessage = null) }
            }
        }
    }

    fun delete() {
        if (transactionId == 0L) return
        viewModelScope.launch {
            repository.deleteTransaction(transactionId)
            _uiState.update { it.copy(deleted = true) }
        }
    }
}
