package com.kimfamily.ledger.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kimfamily.ledger.domain.model.TransactionType
import com.kimfamily.ledger.ui.LedgerViewModelFactory
import com.kimfamily.ledger.ui.util.categoryIcon
import com.kimfamily.ledger.ui.util.categoryIconOptions

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoryListScreen(
    factory: LedgerViewModelFactory,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: CategoryListViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val title = if (uiState.type == TransactionType.INCOME) "수입 카테고리" else "지출 카테고리"

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::showAddDialog) {
                Icon(Icons.Default.Add, contentDescription = "카테고리 추가")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            items(uiState.categories, key = { it.id }) { category ->
                ListItem(
                    leadingContent = {
                        Icon(
                            imageVector = categoryIcon(category.iconKey),
                            contentDescription = null,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(category.colorArgb).copy(alpha = 0.16f))
                                .padding(8.dp),
                            tint = Color(category.colorArgb),
                        )
                    },
                    headlineContent = { Text(category.name) },
                    supportingContent = {
                        if (category.isDefault) Text("기본")
                    },
                    trailingContent = {
                        Row {
                            IconButton(onClick = { viewModel.showEditDialog(category) }) {
                                Icon(Icons.Default.Edit, contentDescription = "수정")
                            }
                            IconButton(onClick = { viewModel.showDeleteDialog(category) }) {
                                Icon(Icons.Default.Delete, contentDescription = "삭제")
                            }
                        }
                    },
                )
            }
        }
    }

    if (uiState.showAddDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDialog,
            title = { Text(if (uiState.editingCategory == null) "카테고리 추가" else "카테고리 수정") },
            text = {
                Column {
                    OutlinedTextField(
                        value = uiState.newName,
                        onValueChange = viewModel::onNameChange,
                        label = { Text("이름") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = "아이콘",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                    )
                    FlowRow(
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                    ) {
                        categoryIconOptions.forEach { option ->
                            FilterChip(
                                selected = uiState.selectedIconKey == option.key,
                                onClick = { viewModel.onIconSelected(option.key) },
                                label = { Text(option.label) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = option.imageVector,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                },
                            )
                        }
                    }
                    uiState.errorMessage?.let { Text(it) }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::saveCategory) { Text("저장") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDialog) { Text("취소") }
            },
        )
    }

    uiState.deletePreview?.takeUnless { uiState.showCannotDeleteLastDialog }?.let { preview ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteDialog,
            title = { Text("${preview.category.name} 삭제") },
            text = {
                Column {
                    Text(
                        text = if (preview.transactionCount > 0) {
                            "${preview.category.name} 카테고리의 내역 ${preview.transactionCount}개를 어느 카테고리로 옮길까요?"
                        } else {
                            "${preview.category.name} 카테고리를 삭제할까요?"
                        },
                    )
                    if (preview.transactionCount > 0) {
                        Spacer(modifier = Modifier.size(12.dp))
                        Column {
                            preview.replacementCategories.forEach { category ->
                                val selected = category.id == uiState.selectedReplacementCategoryId
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            viewModel.onReplacementCategorySelected(category.id)
                                        },
                                    color = if (selected) {
                                        Color(category.colorArgb).copy(alpha = 0.12f)
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    },
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            imageVector = categoryIcon(category.iconKey),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Color(category.colorArgb).copy(alpha = 0.16f))
                                                .padding(7.dp),
                                            tint = Color(category.colorArgb),
                                        )
                                        Text(
                                            text = category.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(start = 10.dp),
                                            maxLines = 1,
                                        )
                                        if (selected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "선택됨",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::deleteCategory) {
                    Text(if (preview.transactionCount > 0) "옮기고 삭제" else "삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteDialog) {
                    Text("취소")
                }
            },
        )
    }

    if (uiState.showCannotDeleteLastDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteDialog,
            title = { Text("삭제할 수 없어요") },
            text = { Text("카테고리는 최소 1개가 있어야 해요. 새 카테고리를 먼저 추가한 뒤 삭제해 주세요.") },
            confirmButton = {
                TextButton(onClick = viewModel::dismissDeleteDialog) {
                    Text("확인")
                }
            },
        )
    }
}
