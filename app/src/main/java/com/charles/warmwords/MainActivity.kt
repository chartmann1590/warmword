package com.charles.warmwords

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.charles.warmwords.data.local.entity.UserProfile
import com.charles.warmwords.ads.AdsManager
import com.charles.warmwords.domain.usecase.UserProfileUseCases
import com.charles.warmwords.translation.TranslationDownloadState
import com.charles.warmwords.translation.TranslationManager
import com.charles.warmwords.ui.components.LocalAppTranslation
import com.charles.warmwords.ui.components.TranslationContext
import com.charles.warmwords.ui.navigation.WarmWordBottomBar
import com.charles.warmwords.ui.navigation.WarmWordNavGraph
import com.charles.warmwords.ui.navigation.Screen
import com.charles.warmwords.ui.navigation.bottomNavItems
import com.charles.warmwords.ui.theme.WarmWordTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userProfileUseCases: UserProfileUseCases

    @Inject
    lateinit var adsManager: AdsManager

    @Inject
    lateinit var translationManager: TranslationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            WarmWordTheme {
                WarmWordApp(
                    userProfileUseCases = userProfileUseCases,
                    translationManager = translationManager,
                    onBeforeNavigate = { target, current ->
                        adsManager.maybeShowInterstitial(this, target.route, current)
                    }
                )
            }
        }
    }
}

@Composable
fun WarmWordApp(
    userProfileUseCases: UserProfileUseCases,
    translationManager: TranslationManager,
    onBeforeNavigate: (Screen, String) -> Unit = { _, _ -> }
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()
    var startDestination by rememberSaveable { mutableStateOf(Screen.Onboarding.route) }
    var showBottomBar by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val profile = userProfileUseCases.getProfile()
        if (profile?.onboardingComplete == true) {
            startDestination = Screen.Chat.route
            showBottomBar = true
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Screen.Onboarding.route

    val navItems = bottomNavItems.filter { !it.screen.route.startsWith("onboarding") }

    WarmWordTheme {
        val targetLanguage by translationManager.targetLanguage.collectAsState()
        val downloadState by translationManager.downloadState.collectAsState()
        CompositionLocalProvider(
            LocalAppTranslation provides TranslationContext(
                manager = translationManager,
                targetCode = targetLanguage,
                downloadState = downloadState
            )
        ) {
            Scaffold(
                bottomBar = {
                    if (showBottomBar && currentRoute != Screen.Onboarding.route) {
                        WarmWordBottomBar(
                            items = navItems,
                            currentRoute = currentRoute,
                            onNavigate = { screen ->
                                onBeforeNavigate(screen, currentRoute)
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(padding)
                ) {
                    WarmWordNavGraph(
                        navController = navController,
                        startDestination = startDestination,
                        onOnboardingComplete = {
                            showBottomBar = true
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    LaunchedEffect(currentRoute) {
        showBottomBar = currentRoute != Screen.Onboarding.route
    }
}
