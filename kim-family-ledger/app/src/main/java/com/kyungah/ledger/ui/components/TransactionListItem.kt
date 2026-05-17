package com.kimfamily.ledger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kimfamily.ledger.domain.model.TransactionType
import com.kimfamily.ledger.domain.model.TransactionWithCategory
import com.kimfamily.ledger.ui.theme.ExpenseRed
import com.kimfamily.ledger.ui.theme.IncomeGreen
import com.kimfamily.ledger.ui.util.Formatters
import com.kimfamily.ledger.ui.util.categoryIcon

@Composable
fun TransactionListItem(
    item: TransactionWithCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accentColor = when (item.transaction.type) {
        TransactionType.INCOME -> IncomeGreen
        TransactionType.EXPENSE -> ExpenseRed
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 58.dp)
                    .background(
                        color = accentColor,
                        shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                    ),
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(Color(item.category.colorArgb).copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = categoryIcon(item.category.iconKey),
                        contentDescription = item.category.name,
                        modifier = Modifier.size(22.dp),
                        tint = Color(item.category.colorArgb),
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp),
                ) {
                    Text(
                        text = item.category.name,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                    )
                    item.transaction.memo?.let { memo ->
                        Text(
                            text = memo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Formatters.formatSignedAmount(
                            item.transaction.type,
                            item.transaction.amount,
                        ),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum",
                        ),
                        color = accentColor,
                        maxLines = 1,
                    )
                    Text(
                        text = Formatters.formatShortDateTime(item.transaction.occurredAtMillis),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
