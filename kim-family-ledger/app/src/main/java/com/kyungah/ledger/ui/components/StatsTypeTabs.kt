package com.kimfamily.ledger.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kimfamily.ledger.domain.model.TransactionType
import com.kimfamily.ledger.ui.theme.ExpenseRed
import com.kimfamily.ledger.ui.theme.IncomeGreen

@Composable
fun StatsTypeTabs(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier,
) {
    RowTabs(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        selectedType = selectedType,
        onTypeSelected = onTypeSelected,
    )
}

@Composable
private fun RowTabs(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Row(modifier = modifier) {
        TransactionType.entries.forEach { type ->
            val selected = selectedType == type
            val accent = when (type) {
                TransactionType.EXPENSE -> ExpenseRed
                TransactionType.INCOME -> IncomeGreen
            }
            val label = if (type == TransactionType.INCOME) "수입" else "지출"
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    onClick = { onTypeSelected(type) },
                    color = if (selected) {
                        accent.copy(alpha = 0.12f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    },
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (selected) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.45f)
                            .padding(top = 6.dp)
                            .height(3.dp),
                        color = accent,
                        shape = MaterialTheme.shapes.small,
                    ) {}
                } else {
                    Spacer(modifier = Modifier.height(9.dp))
                }
            }
        }
    }
}
