package com.kimfamily.ledger.navigation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kimfamily.ledger.LedgerApplication
import com.kimfamily.ledger.R
import com.kimfamily.ledger.domain.model.TransactionType
import com.kimfamily.ledger.ui.LedgerViewModelFactory
import com.kimfamily.ledger.ui.home.HomeScreen
import com.kimfamily.ledger.ui.settings.CategoryListScreen
import com.kimfamily.ledger.ui.settings.SettingsScreen
import com.kimfamily.ledger.ui.stats.StatsScreen
import com.kimfamily.ledger.ui.transaction.TransactionFormScreen

@Composable
fun LedgerNavHost(
    application: LedgerApplication,
    appName: String,
    onAppNameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val activity = LocalContext.current as ComponentActivity
    val repository = application.repository
    val activityFactory = remember(activity, repository) {
        LedgerViewModelFactory(repository, activity)
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route?.substringBefore("?")
    val showBottomBar = currentRoute in Routes.bottomNavRoutes

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    val itemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.HOME,
                        onClick = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = {
                            Text(
                                stringResource(R.string.nav_home),
                                fontWeight = if (currentRoute == Routes.HOME) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                        colors = itemColors,
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.STATS,
                        onClick = {
                            navController.navigate(Routes.STATS) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                        label = {
                            Text(
                                stringResource(R.string.nav_stats),
                                fontWeight = if (currentRoute == Routes.STATS) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                        colors = itemColors,
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.SETTINGS,
                        onClick = {
                            navController.navigate(Routes.SETTINGS) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = {
                            Text(
                                stringResource(R.string.nav_settings),
                                fontWeight = if (currentRoute == Routes.SETTINGS) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                        colors = itemColors,
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    factory = activityFactory,
                    appName = appName,
                    onAddTransaction = { type ->
                        navController.navigate(Routes.transactionForm(type = type))
                    },
                    onEditTransaction = { id ->
                        navController.navigate(Routes.transactionForm(transactionId = id))
                    },
                )
            }
            composable(Routes.STATS) {
                StatsScreen(
                    factory = activityFactory,
                    appName = appName,
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    repository = repository,
                    appName = appName,
                    onAppNameChange = onAppNameChange,
                    onExpenseCategories = {
                        navController.navigate(Routes.categoryList(TransactionType.EXPENSE))
                    },
                    onIncomeCategories = {
                        navController.navigate(Routes.categoryList(TransactionType.INCOME))
                    },
                )
            }
            composable(
                route = Routes.TRANSACTION_FORM,
                arguments = listOf(
                    navArgument("transactionId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    },
                    navArgument("type") {
                        type = NavType.StringType
                        defaultValue = TransactionType.EXPENSE.name
                    },
                ),
            ) { entry ->
                val factory = remember(entry) {
                    LedgerViewModelFactory(repository, entry, entry.arguments)
                }
                TransactionFormScreen(
                    factory = factory,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Routes.CATEGORY_LIST,
                arguments = listOf(
                    navArgument("type") {
                        type = NavType.StringType
                        defaultValue = TransactionType.EXPENSE.name
                    },
                ),
            ) { entry ->
                val factory = remember(entry) {
                    LedgerViewModelFactory(repository, entry, entry.arguments)
                }
                CategoryListScreen(
                    factory = factory,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
