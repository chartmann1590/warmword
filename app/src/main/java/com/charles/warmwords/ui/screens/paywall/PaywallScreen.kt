package com.charles.warmwords.ui.screens.paywall

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import com.charles.warmwords.ui.components.Text
import com.charles.warmwords.ui.components.DisclaimerBanner
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.billingclient.api.ProductDetails
import com.charles.warmwords.ui.components.DisclaimerBanner

private val PREMIUM_BENEFITS = listOf(
    "Ad-free — no banners or interruptions, ever",
    "All 5 AI personalities, including CBT Coach, Mindful Guide, Motivator & Quiet Listener",
    "Cancel anytime through Google Play"
)

@Composable
fun PaywallScreen(
    viewModel: PaywallViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val products by viewModel.productDetails.collectAsState()
    val subscription by viewModel.subscription.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            PaywallTopBar(onNavigateBack = onNavigateBack)
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "WarmWord Premium",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Unlock a calmer, more personal companion.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))

            PREMIUM_BENEFITS.forEach { benefit ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp).padding(top = 2.dp)
                    )
                    Spacer(Modifier.size(12.dp))
                    Text(
                        text = benefit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            if (subscription.isPremium) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = "You're a Premium member",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/account/subscriptions")
                                )
                            )
                        }) {
                            Text("Manage subscription")
                        }
                    }
                }
            } else {
                if (products.isEmpty()) {
                    Text(
                        text = "Loading plans…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                products.forEach { product ->
                    PlanCard(
                        productDetails = product,
                        onSubscribe = { viewModel.purchase(context as Activity, product) }
                    )
                    Spacer(Modifier.height(12.dp))
                }

                OutlinedButton(
                    onClick = { viewModel.restore() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Restore purchases")
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = "Subscriptions auto-renew via Google Play and can be cancelled anytime in your Play Store account settings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            DisclaimerBanner()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaywallTopBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = { Text("Upgrade") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
            }
        }
    )
}

@Composable
private fun PlanCard(
    productDetails: ProductDetails,
    onSubscribe: () -> Unit
) {
    val offer = productDetails.subscriptionOfferDetails?.firstOrNull()
    val price = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: ""
    val title = productDetails.title.substringBefore(" (").ifBlank { productDetails.name }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSubscribe() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (price.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = price,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            TextButton(onClick = onSubscribe) {
                Text("Subscribe")
            }
        }
    }
}
