package com.kimfamily.ledger.ui.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kimfamily.ledger.R
import com.kimfamily.ledger.domain.model.TransactionType
import com.kimfamily.ledger.ui.LedgerViewModelFactory
import com.kimfamily.ledger.ui.components.CategoryBreakdownChart
import com.kimfamily.ledger.ui.components.LedgerTopAppBar
import com.kimfamily.ledger.ui.components.MonthSelector
import com.kimfamily.ledger.ui.components.StatsTypeTabs
import com.kimfamily.ledger.ui.components.SummaryCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    factory: LedgerViewModelFactory,
    appName: String,
    modifier: Modifier = Modifier,
) {
    val viewModel: StatsViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val breakdown = when (uiState.selectedType) {
        TransactionType.EXPENSE -> uiState.expenseBreakdown
        TransactionType.INCOME -> uiState.incomeBreakdown
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LedgerTopAppBar(
                appName = appName,
                title = stringResource(R.string.nav_stats),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            MonthSelector(
                label = uiState.monthRange.label,
                isCurrentMonth = uiState.monthRange.isCurrentMonth,
                onPrevious = viewModel::onPreviousMonth,
                onNext = viewModel::onNextMonth,
            )
            SummaryCard(
                summary = uiState.summary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            StatsTypeTabs(
                selectedType = uiState.selectedType,
                onTypeSelected = viewModel::onTypeSelected,
            )
            Text(
                text = if (uiState.selectedType == TransactionType.EXPENSE) {
                    "카테고리별 지출"
                } else {
                    "카테고리별 수입"
                },
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )
            CategoryBreakdownChart(
                items = breakdown,
                modifier = Modifier.padding(bottom = 88.dp),
            )
        }
    }
}
