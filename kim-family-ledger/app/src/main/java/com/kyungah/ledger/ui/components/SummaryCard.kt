package com.kimfamily.ledger.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kimfamily.ledger.domain.model.MonthlySummary
import com.kimfamily.ledger.ui.theme.ExpenseRed
import com.kimfamily.ledger.ui.theme.IncomeGreen
import com.kimfamily.ledger.ui.util.Formatters

@Composable
fun SummaryCard(
    summary: MonthlySummary,
    modifier: Modifier = Modifier,
) {
    val netColor = when {
        summary.netTotal > 0 -> IncomeGreen
        summary.netTotal < 0 -> ExpenseRed
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
        ),
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 68.dp)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SummaryChip(
                label = "수입",
                amount = summary.incomeTotal,
                color = IncomeGreen,
                positive = true,
                modifier = Modifier.weight(1f),
            )
            SummaryChip(
                label = "지출",
                amount = summary.expenseTotal,
                color = ExpenseRed,
                positive = false,
                modifier = Modifier.weight(1f),
            )
            SummaryChip(
                label = "차액",
                amountText = Formatters.formatSignedNetAmount(summary.netTotal),
                color = netColor,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SummaryChip(
    label: String,
    amount: Long,
    color: androidx.compose.ui.graphics.Color,
    positive: Boolean,
    modifier: Modifier = Modifier,
) {
    SummaryChip(
        label = label,
        amountText = if (positive) {
            "+${Formatters.formatAmountNumber(amount)}원"
        } else {
            "−${Formatters.formatAmountNumber(amount)}원"
        },
        color = color,
        modifier = modifier,
    )
}

@Composable
private fun SummaryChip(
    label: String,
    amountText: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        Text(
            text = amountText,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum",
            ),
            color = color,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp),
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
