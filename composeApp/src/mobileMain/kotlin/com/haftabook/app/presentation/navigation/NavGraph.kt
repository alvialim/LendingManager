package com.haftabook.app.presentation.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.haftabook.app.di.AppContainer
import com.haftabook.app.platform.PlatformBackHandler
import com.haftabook.app.presentation.customer.CustomerDetailScreen
import com.haftabook.app.presentation.customer.CustomerDetailViewModel
import com.haftabook.app.presentation.home.HomeScreen
import com.haftabook.app.presentation.home.HomeViewModel
import com.haftabook.app.presentation.analytics.AnalyticsScreen
import com.haftabook.app.presentation.analytics.AnalyticsViewModel
import com.haftabook.app.presentation.settings.SettingsScreen
import kotlinx.coroutines.launch

private sealed interface AppDestination {
    data object Home : AppDestination
    data class CustomerDetail(val customerId: Long) : AppDestination
    data object Settings : AppDestination
    data object Analytics : AppDestination
}

/**
 * App navigation without androidx Navigation Compose (avoids SavedState/NavController JVM binary issues on Desktop).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    container: AppContainer,
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    fun openDrawer() {
        scope.launch { drawerState.open() }
    }

    var stack by remember { mutableStateOf(listOf<AppDestination>(AppDestination.Home)) }
    val current = stack.last()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = current == AppDestination.Home,
        drawerContent = {
            AppDrawerContent(
                onSettingsClick = {
                    scope.launch {
                        drawerState.close()
                        if (stack.last() != AppDestination.Settings) {
                            stack = stack + AppDestination.Settings
                        }
                    }
                }
            )
        }
    ) {
        when (current) {
        AppDestination.Home -> {
            val viewModel: HomeViewModel = viewModel {
                HomeViewModel(
                    getCustomersUseCase = container.getCustomersUseCase,
                    addCustomerUseCase = container.addCustomerUseCase,
                    deleteCustomerUseCase = container.deleteCustomerUseCase,
                    requestSyncNow = { container.requestSyncNow() },
                )
            }
            HomeScreen(
                viewModel = viewModel,
                onOpenDrawer = { openDrawer() },
                onCustomerClick = { customerId ->
                    stack = stack + AppDestination.CustomerDetail(customerId)
                }
            )
        }

        is AppDestination.CustomerDetail -> {
            PlatformBackHandler(enabled = stack.size > 1) {
                stack = stack.dropLast(1)
            }
            val customerId = current.customerId
            val viewModel: CustomerDetailViewModel = viewModel(key = "customer_$customerId") {
                CustomerDetailViewModel(
                    customerId = customerId,
                    getCustomerDetailsUseCase = container.getCustomerDetailsUseCase,
                    getLoansUseCase = container.getLoansUseCase,
                    getEmisUseCase = container.getEmisUseCase,
                    addLoanUseCase = container.addLoanUseCase,
                    addEmiUseCase = container.addEmiUseCase,
                    deleteLoanUseCase = container.deleteLoanUseCase
                )
            }
            CustomerDetailScreen(
                viewModel = viewModel,
                onBack = {
                    if (stack.size > 1) {
                        stack = stack.dropLast(1)
                    }
                }
            )
        }

        AppDestination.Settings -> {
            PlatformBackHandler(enabled = stack.size > 1) {
                stack = stack.dropLast(1)
            }
            SettingsScreen(
                isDarkTheme = isDarkTheme,
                onDarkThemeChange = onDarkThemeChange,
                onAnalyticsClick = {
                    if (stack.last() != AppDestination.Analytics) {
                        stack = stack + AppDestination.Analytics
                    }
                },
                onBack = {
                    if (stack.size > 1) {
                        stack = stack.dropLast(1)
                    }
                }
            )
        }

        AppDestination.Analytics -> {
            PlatformBackHandler(enabled = stack.size > 1) {
                stack = stack.dropLast(1)
            }
            val analyticsViewModel: AnalyticsViewModel = viewModel(key = "analytics") {
                AnalyticsViewModel(container.getAnalyticsUseCase)
            }
            AnalyticsScreen(
                viewModel = analyticsViewModel,
                onBack = {
                    if (stack.size > 1) {
                        stack = stack.dropLast(1)
                    }
                }
            )
        }
        }
        PlatformBackHandler(enabled = drawerState.currentValue == DrawerValue.Open) {
            scope.launch { drawerState.close() }
        }
    }
}
