package com.kimfamily.ledger.ui

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.kimfamily.ledger.data.repository.LedgerRepository
import com.kimfamily.ledger.ui.home.HomeViewModel
import com.kimfamily.ledger.ui.settings.CategoryListViewModel
import com.kimfamily.ledger.ui.stats.StatsViewModel
import com.kimfamily.ledger.ui.transaction.TransactionFormViewModel

class LedgerViewModelFactory(
    private val repository: LedgerRepository,
    owner: SavedStateRegistryOwner,
    defaultArgs: android.os.Bundle? = null,
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle,
    ): T = when {
        modelClass.isAssignableFrom(HomeViewModel::class.java) ->
            HomeViewModel(repository) as T
        modelClass.isAssignableFrom(StatsViewModel::class.java) ->
            StatsViewModel(repository) as T
        modelClass.isAssignableFrom(TransactionFormViewModel::class.java) ->
            TransactionFormViewModel(repository, handle) as T
        modelClass.isAssignableFrom(CategoryListViewModel::class.java) ->
            CategoryListViewModel(repository, handle) as T
        else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
