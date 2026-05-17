package com.kimfamily.ledger.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kimfamily.ledger.data.repository.LedgerRepository
import com.kimfamily.ledger.domain.model.Category
import com.kimfamily.ledger.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryListUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val categories: List<Category> = emptyList(),
    val showAddDialog: Boolean = false,
    val editingCategory: Category? = null,
    val newName: String = "",
    val selectedIconKey: String = "more_horiz",
    val errorMessage: String? = null,
)

class CategoryListViewModel(
    private val repository: LedgerRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val type = TransactionType.fromStorage(
        savedStateHandle.get<String>("type") ?: TransactionType.EXPENSE.name,
    )

    private val _uiState = MutableStateFlow(CategoryListUiState(type = type))
    val uiState: StateFlow<CategoryListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeCategories(type).collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun showAddDialog() {
        val defaultIconKey = if (type == TransactionType.INCOME) "savings" else "shopping_bag"
        _uiState.update {
            it.copy(
                showAddDialog = true,
                editingCategory = null,
                newName = "",
                selectedIconKey = defaultIconKey,
                errorMessage = null,
            )
        }
    }

    fun showEditDialog(category: Category) {
        _uiState.update {
            it.copy(
                showAddDialog = true,
                editingCategory = category,
                newName = category.name,
                selectedIconKey = category.iconKey,
                errorMessage = null,
            )
        }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(showAddDialog = false, editingCategory = null, newName = "") }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(newName = name, errorMessage = null) }
    }

    fun onIconSelected(iconKey: String) {
        _uiState.update { it.copy(selectedIconKey = iconKey, errorMessage = null) }
    }

    fun saveCategory() {
        val state = _uiState.value
        val name = state.newName.trim()
        if (name.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "이름을 입력해 주세요") }
            return
        }
        viewModelScope.launch {
            val existing = state.editingCategory
            repository.saveCategory(
                Category(
                    id = existing?.id ?: 0L,
                    type = type,
                    name = name,
                    iconKey = state.selectedIconKey,
                    colorArgb = existing?.colorArgb ?: 0xFF90A4AE.toInt(),
                    isDefault = existing?.isDefault ?: false,
                ),
            )
            dismissDialog()
        }
    }

    fun deleteCategory(category: Category) {
        if (category.isDefault) return
        viewModelScope.launch {
            val deleted = repository.deleteCategory(category)
            if (!deleted) {
                _uiState.update { it.copy(errorMessage = "사용 중인 카테고리는 삭제할 수 없습니다") }
            }
        }
    }
}
