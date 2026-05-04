package com.haftabook.app.presentation.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.haftabook.app.data.remote.PinType
import com.haftabook.app.di.AppContainer
import com.haftabook.app.platform.PlatformBackHandler
import com.haftabook.app.presentation.customer.CustomerDetailScreen
import com.haftabook.app.presentation.customer.CustomerDetailViewModel
import com.haftabook.app.presentation.home.HomeScreen
import com.haftabook.app.presentation.home.HomeViewModel
import com.haftabook.app.presentation.analytics.AnalyticsScreen
import com.haftabook.app.presentation.analytics.AnalyticsViewModel
import com.haftabook.app.presentation.components.CustomerPhotoZoomScreen
import com.haftabook.app.presentation.settings.SettingsScreen
import com.haftabook.app.presentation.auth.AuthFlow
import com.haftabook.app.platform.onSessionUnlocked
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private sealed interface AppDestination {
    data object Home : AppDestination
    data class CustomerDetail(val customerId: Long) : AppDestination
    data class CustomerPhoto(val photoPath: String) : AppDestination
    data object Settings : AppDestination
    data object Analytics : AppDestination
}

private sealed interface AppLaunchStage {
    data object Splash : AppLaunchStage
    data object Dashboard : AppLaunchStage
    data object Lock : AppLaunchStage
    data object Home : AppLaunchStage
}

/**
 * App navigation without androidx Navigation Compose (avoids SavedState/NavController JVM binary issues on Desktop).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    container: AppContainer,
    isDarkTheme: Boolean,
    isShowMonthlyEnabled: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    onShowMonthlyChange: (Boolean) -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    fun openDrawer() {
        scope.launch { drawerState.open() }
    }

    var stack by remember { mutableStateOf(listOf<AppDestination>(AppDestination.Home)) }
    val current = stack.last()

    var launchStage by remember { mutableStateOf<AppLaunchStage>(AppLaunchStage.Splash) }
    var selectedPinType by remember { mutableStateOf<PinType?>(null) }

    LaunchedEffect(Unit) {
        delay(1200L)
        if (launchStage == AppLaunchStage.Splash) {
            launchStage = AppLaunchStage.Dashboard
        }
    }

    when (launchStage) {
        AppLaunchStage.Splash -> {
            SplashScreen()
            return
        }
        AppLaunchStage.Dashboard -> Unit
        AppLaunchStage.Lock -> {
            PlatformBackHandler(enabled = true) {
                launchStage = AppLaunchStage.Dashboard
            }
            val pinType = selectedPinType ?: PinType.MONTHLY
            AuthFlow(
                pinType = pinType,
                onUnlocked = {
                    launchStage = AppLaunchStage.Home
                    onSessionUnlocked()
                    scope.launch {
                        drawerState.close()
                    }
                    stack = listOf(AppDestination.Home)
                }
            )
            return
        }
        AppLaunchStage.Home -> Unit
    }

    PlatformBackHandler(
        enabled = launchStage == AppLaunchStage.Home &&
            current == AppDestination.Home &&
            stack.size == 1 &&
            drawerState.currentValue == DrawerValue.Closed
    ) {
        selectedPinType = null
        launchStage = AppLaunchStage.Dashboard
        stack = listOf(AppDestination.Home)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = launchStage == AppLaunchStage.Dashboard || current == AppDestination.Home,
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
            if (launchStage == AppLaunchStage.Dashboard) {
                DashboardScreen(
                    onOpenDrawer = { openDrawer() },
                    isShowMonthlyEnabled = isShowMonthlyEnabled,
                    onSelectMode = { pinType ->
                        selectedPinType = pinType
                        launchStage = AppLaunchStage.Lock
                    }
                )
            } else {
                val activeLoanType = if (selectedPinType == PinType.DAILY) "DAILY" else "MONTHLY"
                val keyedViewModel: HomeViewModel = viewModel(key = "home_$activeLoanType") {
                    HomeViewModel(
                        loanTypeFilter = activeLoanType,
                        getCustomersUseCase = container.getCustomersUseCase,
                        addCustomerUseCase = container.addCustomerUseCase,
                        updateCustomerPhotoUseCase = container.updateCustomerPhotoUseCase,
                        deleteCustomerUseCase = container.deleteCustomerUseCase,
                        requestSyncNow = { container.requestSyncNow() },
                    )
                }
                HomeScreen(
                    viewModel = keyedViewModel,
                    loanTypeFilter = activeLoanType,
                    onCustomerClick = { customerId ->
                        stack = stack + AppDestination.CustomerDetail(customerId)
                    },
                    onCustomerPhotoClick = { path ->
                        stack = stack + AppDestination.CustomerPhoto(path)
                    }
                )
            }
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
                    markEmiSlotPaidUseCase = container.markEmiSlotPaidUseCase,
                    updateCustomerPhotoUseCase = container.updateCustomerPhotoUseCase,
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

        is AppDestination.CustomerPhoto -> {
            PlatformBackHandler(enabled = stack.size > 1) {
                stack = stack.dropLast(1)
            }
            CustomerPhotoZoomScreen(
                photoPath = current.photoPath,
                onBack = {
                    if (stack.size > 1) stack = stack.dropLast(1)
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
                isShowMonthlyEnabled = isShowMonthlyEnabled,
                onShowMonthlyChange = onShowMonthlyChange,
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
