package com.kimfamily.ledger.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryIconOption(
    val key: String,
    val label: String,
    val imageVector: ImageVector,
)

val categoryIconOptions: List<CategoryIconOption> = listOf(
    CategoryIconOption("restaurant", "식비", Icons.Filled.Restaurant),
    CategoryIconOption("directions_bus", "교통", Icons.Filled.DirectionsBus),
    CategoryIconOption("shopping_bag", "쇼핑", Icons.Filled.ShoppingBag),
    CategoryIconOption("home", "주거", Icons.Filled.Home),
    CategoryIconOption("movie", "여가", Icons.Filled.Movie),
    CategoryIconOption("local_hospital", "의료", Icons.Filled.LocalHospital),
    CategoryIconOption("school", "교육", Icons.Filled.School),
    CategoryIconOption("payments", "급여", Icons.Filled.Payments),
    CategoryIconOption("work", "업무", Icons.Filled.Work),
    CategoryIconOption("card_giftcard", "선물", Icons.Filled.CardGiftcard),
    CategoryIconOption("savings", "저축", Icons.Filled.Savings),
    CategoryIconOption("more_horiz", "기타", Icons.Filled.MoreHoriz),
)

fun categoryIcon(iconKey: String): ImageVector = when (iconKey) {
    "restaurant" -> Icons.Filled.Restaurant
    "directions_bus" -> Icons.Filled.DirectionsBus
    "shopping_bag" -> Icons.Filled.ShoppingBag
    "home" -> Icons.Filled.Home
    "movie" -> Icons.Filled.Movie
    "local_hospital" -> Icons.Filled.LocalHospital
    "school" -> Icons.Filled.School
    "payments" -> Icons.Filled.Payments
    "work" -> Icons.Filled.Work
    "card_giftcard" -> Icons.Filled.CardGiftcard
    "savings" -> Icons.Filled.Savings
    "more_horiz" -> Icons.Filled.MoreHoriz
    else -> Icons.Filled.Category
}
