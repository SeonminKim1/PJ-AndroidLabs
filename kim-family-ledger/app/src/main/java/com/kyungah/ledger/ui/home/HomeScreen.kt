package com.kimfamily.ledger.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kimfamily.ledger.domain.model.TransactionType
import com.kimfamily.ledger.ui.LedgerViewModelFactory
import com.kimfamily.ledger.ui.components.AddTransactionSheetContent
import com.kimfamily.ledger.ui.components.EmptyTransactionsState
import com.kimfamily.ledger.ui.components.LedgerTopAppBar
import com.kimfamily.ledger.ui.components.MonthSelector
import com.kimfamily.ledger.ui.components.SummaryCard
import com.kimfamily.ledger.ui.components.TransactionListItem
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    factory: LedgerViewModelFactory,
    appName: String,
    onAddTransaction: (TransactionType) -> Unit,
    onEditTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: HomeViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchExpanded by remember { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf("") }
    var showAddSheet by remember { mutableStateOf(false) }
    val addSheetState = rememberModalBottomSheetState()

    LaunchedEffect(searchText) {
        delay(200)
        viewModel.onSearchQueryChange(searchText)
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LedgerTopAppBar(
                appName = appName,
                actions = {
                    IconButton(onClick = { showAddSheet = true }) {
                        Icon(Icons.Default.Add, contentDescription = "거래 추가")
                    }
                    IconButton(
                        onClick = {
                            if (searchExpanded) {
                                searchText = ""
                                viewModel.onSearchQueryChange("")
                            }
                            searchExpanded = !searchExpanded
                        },
                    ) {
                        Icon(
                            imageVector = if (searchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (searchExpanded) "검색 닫기" else "검색",
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            MonthSelector(
                label = uiState.monthRange.label,
                isCurrentMonth = uiState.monthRange.isCurrentMonth,
                onPrevious = viewModel::onPreviousMonth,
                onNext = viewModel::onNextMonth,
            )
            SummaryCard(
                summary = uiState.summary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
            AnimatedVisibility(
                visible = searchExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .heightIn(min = 52.dp),
                    placeholder = {
                        Text(
                            text = "메모 검색",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                )
            }
            if (uiState.groupedTransactions.isEmpty()) {
                EmptyTransactionsState(
                    onAddClick = { showAddSheet = true },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.16f)),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
                ) {
                    uiState.groupedTransactions.forEach { group ->
                        item(key = "header_${group.header}") {
                            Text(
                                text = group.header,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 7.dp,
                                ),
                            )
                        }
                        itemsIndexed(group.items, key = { _, item -> item.transaction.id }) { index, item ->
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
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = addSheetState,
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            AddTransactionSheetContent(
                onExpense = {
                    showAddSheet = false
                    onAddTransaction(TransactionType.EXPENSE)
                },
                onIncome = {
                    showAddSheet = false
                    onAddTransaction(TransactionType.INCOME)
                },
            )
        }
    }
}
