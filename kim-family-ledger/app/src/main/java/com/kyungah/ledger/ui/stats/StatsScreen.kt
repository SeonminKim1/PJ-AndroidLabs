package com.kimfamily.ledger.ui.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.kimfamily.ledger.ui.components.TransactionListItem
import com.kimfamily.ledger.ui.util.Formatters
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    factory: LedgerViewModelFactory,
    appName: String,
    onEditTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: StatsViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val breakdown = when (uiState.selectedType) {
        TransactionType.EXPENSE -> uiState.expenseBreakdown
        TransactionType.INCOME -> uiState.incomeBreakdown
    }
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.selectedCategoryId) {
        if (uiState.selectedCategoryId != null) {
            delay(120)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
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
                .verticalScroll(scrollState),
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
                selectedCategoryId = uiState.selectedCategoryId,
                onCategoryClick = { viewModel.onCategorySelected(it.category.id) },
            )
            CategoryDetailTransactions(
                uiState = uiState,
                onEditTransaction = onEditTransaction,
                modifier = Modifier.padding(bottom = 88.dp),
            )
        }
    }
}

@Composable
private fun CategoryDetailTransactions(
    uiState: StatsUiState,
    onEditTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedCategory = when (uiState.selectedType) {
        TransactionType.EXPENSE -> uiState.expenseBreakdown
        TransactionType.INCOME -> uiState.incomeBreakdown
    }.firstOrNull { it.category.id == uiState.selectedCategoryId }

    if (selectedCategory == null) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = "${selectedCategory.category.name} 세부 내역",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            Text(
                text = Formatters.formatAmount(selectedCategory.totalAmount),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
        ) {
            uiState.groupedSelectedCategoryTransactions.forEach { group ->
                Text(
                    text = group.header,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
                )
                group.items.forEachIndexed { index, item ->
                    TransactionListItem(
                        item = item,
                        onClick = { onEditTransaction(item.transaction.id) },
                    )
                    if (index < group.items.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }
    }
}
