package com.kimfamily.ledger.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kimfamily.ledger.domain.model.CategorySummary
import com.kimfamily.ledger.ui.util.Formatters
import com.kimfamily.ledger.ui.util.categoryIcon

@Composable
fun CategoryBreakdownChart(
    items: List<CategorySummary>,
    modifier: Modifier = Modifier,
    selectedCategoryId: Long? = null,
    onCategoryClick: (CategorySummary) -> Unit = {},
) {
    if (items.isEmpty()) {
        Text(
            text = "표시할 내역이 없어요",
            modifier = modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    val grandTotal = items.sumOf { it.totalAmount }.coerceAtLeast(1L)
    val maxTotal = items.maxOf { it.totalAmount }.coerceAtLeast(1L)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            items.forEachIndexed { index, item ->
                val percent = item.totalAmount * 100f / grandTotal
                val isSelected = item.category.id == selectedCategoryId
                Surface(
                    onClick = { onCategoryClick(item) },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (isSelected) {
                        Color(item.category.colorArgb).copy(alpha = 0.08f)
                    } else {
                        Color.Transparent
                    },
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(item.category.colorArgb).copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = categoryIcon(item.category.iconKey),
                                    contentDescription = null,
                                    modifier = Modifier.size(19.dp),
                                    tint = Color(item.category.colorArgb),
                                )
                            }
                            Text(
                                text = item.category.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 10.dp),
                                maxLines = 1,
                            )
                            Text(
                                text = "${percent.toInt()}%",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp),
                            )
                            Text(
                                text = Formatters.formatAmount(item.totalAmount),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                            )
                        }
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .padding(top = 7.dp),
                        ) {
                            val trackColor = Color(item.category.colorArgb).copy(alpha = 0.14f)
                            drawRoundRect(
                                color = trackColor,
                                size = size,
                                cornerRadius = CornerRadius(7f, 7f),
                            )
                            val fraction = item.totalAmount.toFloat() / maxTotal.toFloat()
                            drawRoundRect(
                                color = Color(item.category.colorArgb),
                                topLeft = Offset.Zero,
                                size = Size(size.width * fraction, size.height),
                                cornerRadius = CornerRadius(7f, 7f),
                            )
                        }
                    }
                }
                if (index < items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                    )
                }
            }
        }
    }
}
