package com.kimfamily.ledger.ui.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.kimfamily.ledger.R
import com.kimfamily.ledger.data.repository.LedgerRepository
import com.kimfamily.ledger.domain.model.TransactionType
import com.kimfamily.ledger.ui.components.LedgerTopAppBar
import com.kimfamily.ledger.ui.util.MonthRange
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: LedgerRepository,
    appName: String,
    onAppNameChange: (String) -> Unit,
    onExpenseCategories: () -> Unit,
    onIncomeCategories: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val month = MonthRange.current()
    var showAppNameDialog by remember { mutableStateOf(false) }
    var appNameDraft by remember(appName) { mutableStateOf(appName) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(repository.exportBackupJson().toByteArray())
                    } ?: error("파일을 열 수 없습니다")
                }.onSuccess {
                    Toast.makeText(context, "백업 파일을 만들었어요", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(context, "백업에 실패했어요", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) pendingImportUri = uri
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LedgerTopAppBar(
                appName = appName,
                title = stringResource(R.string.nav_settings),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            SettingsSection(title = "카테고리") {
                SettingsItem(
                    title = "지출 카테고리",
                    description = "식비, 교통, 쇼핑 같은 지출 분류",
                    icon = Icons.Default.Category,
                    onClick = onExpenseCategories,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                SettingsItem(
                    title = "수입 카테고리",
                    description = "급여, 부수입, 이자 같은 수입 분류",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    onClick = onIncomeCategories,
                )
            }
            SettingsSection(
                title = "데이터",
                modifier = Modifier.padding(top = 14.dp),
            ) {
                SettingsItem(
                    title = "이번 달 텍스트로 내보내기",
                    description = "${month.label} 거래 내역을 공유합니다",
                    icon = Icons.Default.Share,
                    onClick = {
                        scope.launch {
                            val text = repository.exportCsv(month.year, month.month)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "${month.label} $appName")
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(intent, "텍스트로 내보내기"))
                        }
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                SettingsItem(
                    title = "백업 파일 만들기",
                    description = "모든 카테고리와 거래를 파일로 저장합니다",
                    icon = Icons.Default.Share,
                    onClick = {
                        createBackupLauncher.launch("kim-family-ledger-backup.json")
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                SettingsItem(
                    title = "백업 가져오기",
                    description = "백업 파일로 현재 데이터를 복원합니다",
                    icon = Icons.Default.Restore,
                    onClick = {
                        importBackupLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                    },
                )
            }
            SettingsSection(
                title = "앱 정보",
                modifier = Modifier.padding(top = 14.dp),
            ) {
                SettingsItem(
                    title = "앱 이름",
                    description = appName,
                    icon = Icons.Default.Edit,
                    onClick = {
                        appNameDraft = appName
                        showAppNameDialog = true
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                ListItem(
                    leadingContent = {
                        Icon(Icons.Default.Info, contentDescription = null)
                    },
                    headlineContent = { Text(appName) },
                    supportingContent = { Text("버전 1.0 · 오프라인 전용") },
                )
            }
        }
    }

    pendingImportUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingImportUri = null },
            title = { Text("백업을 가져올까요?") },
            text = { Text("현재 앱의 카테고리와 거래가 백업 파일 내용으로 바뀝니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingImportUri = null
                        scope.launch {
                            runCatching {
                                val json = context.contentResolver.openInputStream(uri)
                                    ?.bufferedReader()
                                    ?.use { it.readText() }
                                    ?: error("파일을 열 수 없습니다")
                                repository.importBackupJson(json)
                            }.onSuccess {
                                Toast.makeText(context, "백업을 가져왔어요", Toast.LENGTH_SHORT).show()
                            }.onFailure {
                                Toast.makeText(context, "백업 파일을 읽지 못했어요", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                ) {
                    Text("가져오기")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportUri = null }) {
                    Text("취소")
                }
            },
        )
    }

    if (showAppNameDialog) {
        AlertDialog(
            onDismissRequest = { showAppNameDialog = false },
            title = { Text("앱 이름 바꾸기") },
            text = {
                Column {
                    OutlinedTextField(
                        value = appNameDraft,
                        onValueChange = { appNameDraft = it },
                        label = { Text("앱 이름") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = "헤더, 시작 화면, 앱 정보, 공유 제목에 적용됩니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val nextName = appNameDraft.trim()
                        if (nextName.isNotEmpty()) {
                            onAppNameChange(nextName)
                            showAppNameDialog = false
                        }
                    },
                ) {
                    Text("저장")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAppNameDialog = false }) {
                    Text("취소")
                }
            },
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
            ),
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    ListItem(
        leadingContent = {
            Icon(icon, contentDescription = null)
        },
        headlineContent = { Text(title) },
        supportingContent = { Text(description) },
        trailingContent = {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    )
}
