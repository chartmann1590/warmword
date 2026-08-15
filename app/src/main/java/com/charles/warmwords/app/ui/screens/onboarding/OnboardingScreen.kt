package com.charles.warmwords.app.ui.screens.onboarding

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charles.warmwords.app.R
import com.charles.warmwords.app.ui.components.Avatar
import com.charles.warmwords.app.ui.components.AvatarState
import com.charles.warmwords.app.ui.components.DisclaimerBanner
import com.charles.warmwords.app.util.getAvailableRamMb
import com.charles.warmwords.app.util.isLowRamDevice

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onOnboardingComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val availableRam = context.getAvailableRamMb()
    val isLowRam = availableRam < 3000

    LaunchedEffect(Unit) {
        if (isLowRam) {
            viewModel.setShowLowRamWarning(true)
        }
    }

    AnimatedContent(
        targetState = uiState.currentPage,
        contentKey = { it }
    ) { page ->
        when (page) {
            0 -> WelcomePage(viewModel)
            1 -> DownloadPage(viewModel)
            2 -> ProfilePage(viewModel, onOnboardingComplete)
        }
    }
}

@Composable
private fun WelcomePage(viewModel: OnboardingViewModel) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        Avatar(
            state = AvatarState.HAPPY,
            size = 180.dp,
            modifier = Modifier.size(180.dp)
        )

        Spacer(Modifier.height(32.dp))

        Text(
            text = "Welcome to WarmWord",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Your warm, supportive AI companion for emotional support and journaling.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(Modifier.height(16.dp))

        DisclaimerBanner(
            text = "WarmWord is an AI companion, not a doctor or therapist. It cannot provide medical advice, diagnosis, or treatment."
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { viewModel.nextPage() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Get Started",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DownloadPage(viewModel: OnboardingViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        Text(
            text = "Downloading AI Model",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "WarmWord runs entirely on-device. The first download (~2.5GB) prepares your personal AI companion.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        if (uiState.showLowRamWarning) {
            Text(
                text = "Low RAM detected. Using smaller model for better performance.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(12.dp))
        }

        if (uiState.downloadError != null) {
            Text(
                text = uiState.downloadError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
        }

        LinearProgressIndicator(
            progress = { uiState.downloadProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Text(
            text = "${(uiState.downloadProgress * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = if (uiState.isDownloading) "Downloading..." else "Ready!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                if (!uiState.isDownloading && uiState.downloadProgress >= 1.0f) {
                    viewModel.nextPage()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            enabled = !uiState.isDownloading && uiState.downloadProgress >= 1.0f
        ) {
            Text(
                text = "Continue",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }

        TextButton(
            onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(text = "Open App Settings", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ProfilePage(
    viewModel: OnboardingViewModel,
    onOnboardingComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var name by rememberSaveable { mutableStateOf(uiState.name) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        Avatar(
            state = AvatarState.SUPPORTIVE,
            size = 120.dp,
            modifier = Modifier.size(120.dp)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Your Name",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("Enter your name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                viewModel.saveProfile(name)
                onOnboardingComplete()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            enabled = name.isNotBlank()
        ) {
            Text(
                text = "Finish",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}
