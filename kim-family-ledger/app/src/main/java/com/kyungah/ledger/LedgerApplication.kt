package com.kimfamily.ledger

import android.app.Application
import com.kimfamily.ledger.data.AppPreferences
import com.kimfamily.ledger.data.local.LedgerDatabase
import com.kimfamily.ledger.data.repository.LedgerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LedgerApplication : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val database by lazy { LedgerDatabase.getInstance(this) }
    val appPreferences: AppPreferences by lazy { AppPreferences(this) }

    val repository: LedgerRepository by lazy {
        LedgerRepository(
            categoryDao = database.categoryDao(),
            transactionDao = database.transactionDao(),
        )
    }

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            if (database.categoryDao().count() == 0) {
                database.categoryDao().insertAll(
                    com.kimfamily.ledger.data.local.CategorySeedData.defaults,
                )
            }
        }
    }
}
