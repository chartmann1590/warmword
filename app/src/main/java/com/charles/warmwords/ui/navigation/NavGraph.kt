package com.charles.warmwords.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.charles.warmwords.ui.screens.chat.ChatScreen
import com.charles.warmwords.ui.screens.findhelp.FindHelpScreen
import com.charles.warmwords.ui.screens.insights.InsightsScreen
import com.charles.warmwords.ui.screens.journal.JournalScreen
import com.charles.warmwords.ui.screens.onboarding.OnboardingScreen
import com.charles.warmwords.ui.screens.onboarding.OnboardingViewModel
import com.charles.warmwords.ui.screens.paywall.PaywallScreen
import com.charles.warmwords.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Chat : Screen("chat")
    object Journal : Screen("journal")
    object Insights : Screen("insights")
    object FindHelp : Screen("findhelp")
    object Settings : Screen("settings")
    object Paywall : Screen("paywall")
}

@Composable
fun WarmWordNavGraph(
    navController: NavHostController,
    startDestination: String,
    onOnboardingComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Onboarding.route) {
            val onboardingViewModel: OnboardingViewModel = hiltViewModel()
            OnboardingScreen(
                viewModel = onboardingViewModel,
                onOnboardingComplete = {
                    navController.navigate(Screen.Chat.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                    onOnboardingComplete()
                }
            )
        }

        composable(Screen.Chat.route) {
            val chatViewModel: com.charles.warmwords.ui.screens.chat.ChatViewModel = hiltViewModel()
            ChatScreen(
                viewModel = chatViewModel,
                onNavigateToFindHelp = {
                    navController.navigate(Screen.FindHelp.route)
                }
            )
        }

        composable(Screen.Journal.route) {
            val journalViewModel: com.charles.warmwords.ui.screens.journal.JournalViewModel = hiltViewModel()
            JournalScreen(viewModel = journalViewModel)
        }

        composable(Screen.Insights.route) {
            val insightsViewModel: com.charles.warmwords.ui.screens.insights.InsightsViewModel = hiltViewModel()
            InsightsScreen(viewModel = insightsViewModel)
        }

        composable(Screen.FindHelp.route) {
            val findHelpViewModel: com.charles.warmwords.ui.screens.findhelp.FindHelpViewModel = hiltViewModel()
            FindHelpScreen(viewModel = findHelpViewModel)
        }

        composable(Screen.Settings.route) {
            val settingsViewModel: com.charles.warmwords.ui.screens.settings.SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = settingsViewModel,
                onPrivacyPolicyClick = { },
                onNavigateToPaywall = {
                    navController.navigate(Screen.Paywall.route)
                }
            )
        }

        composable(Screen.Paywall.route) {
            val paywallViewModel: com.charles.warmwords.ui.screens.paywall.PaywallViewModel = hiltViewModel()
            PaywallScreen(
                viewModel = paywallViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
