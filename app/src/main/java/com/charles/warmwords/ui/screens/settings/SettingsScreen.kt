package com.charles.warmwords.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.charles.warmwords.R
import com.charles.warmwords.ai.Persona
import com.charles.warmwords.ai.WarmVoice
import com.charles.warmwords.ui.components.DisclaimerBanner
import com.charles.warmwords.util.ModelConfig
import java.io.File

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onPrivacyPolicyClick: () -> Unit,
    onNavigateToPaywall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isPremium = uiState.subscription.isPremium

    var showEditProfile by rememberSaveable { mutableStateOf(false) }
    var showPrivacyPolicy by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    var showPersonaPicker by rememberSaveable { mutableStateOf(false) }
    var showVoicePicker by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isPremium) {
            CardSection(title = "Membership") {
                SettingItem(
                    title = "WarmWord Premium",
                    value = "Ad-free · all personalities unlocked",
                    icon = Icons.Rounded.Lock
                )
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToPaywall() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
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
                            text = "Upgrade to Premium",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Go ad-free & unlock all personalities",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        uiState.profile?.let { profile ->
            CardSection(title = "Profile") {
                SettingItem(
                    title = "Name",
                    value = profile.name.ifBlank { "Not set" },
                    onEdit = { showEditProfile = true }
                )
                SettingItem(
                    title = "Pronouns",
                    value = profile.pronouns.ifBlank { "Not set" },
                    onEdit = { showEditProfile = true }
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        val currentPersona = viewModel.personas.firstOrNull { it.id == uiState.profile?.selectedPersonaId }
            ?: viewModel.personas.first()
        CardSection(title = "AI Personality") {
            val locked = currentPersona.isPremium && !isPremium
            SettingItem(
                title = if (currentPersona.isPremium) "${currentPersona.displayName}  • Premium" else currentPersona.displayName,
                value = if (locked) "Unlock with WarmWord Premium" else currentPersona.tagline,
                icon = if (locked) Icons.Rounded.Lock else Icons.Rounded.Psychology,
                onClick = { if (locked) onNavigateToPaywall() else showPersonaPicker = true }
            )
        }

        Spacer(Modifier.height(16.dp))

        CardSection(title = "Voice") {
            SettingSwitchItem(
                title = "Voice replies",
                subtitle = "WarmWord reads its replies out loud",
                icon = Icons.Rounded.VolumeUp,
                checked = uiState.profile?.voiceRepliesEnabled == true,
                onCheckedChange = { viewModel.setVoiceRepliesEnabled(it) }
            )
            val currentVoice = uiState.availableVoices.firstOrNull { it.name == uiState.profile?.selectedVoiceName }
            SettingItem(
                title = "Reply voice",
                value = currentVoice?.let { "${it.displayName} · ${it.qualityLabel}" } ?: "Automatic",
                icon = Icons.Rounded.RecordVoiceOver,
                onClick = { showVoicePicker = true }
            )
        }

        Spacer(Modifier.height(16.dp))

        CardSection(title = "Model") {
            SettingItem(
                title = "Model",
                value = ModelConfig.MODEL_NAME
            )
            SettingItem(
                title = "Size on disk",
                value = formatFileSize(ModelConfig.MODEL_TOTAL_BYTES)
            )
            SettingItem(
                title = "Stored locally",
                value = if (uiState.modelDownloaded) "Yes" else "No"
            )
        }

        Spacer(Modifier.height(16.dp))

        CardSection(title = "Data") {
            SettingItem(
                title = "Export journal data",
                value = "Share as JSON",
                icon = Icons.Rounded.FileDownload,
                onClick = {
                    viewModel.exportData { uri ->
                        if (uri != null) {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/json"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Export data"))
                        } else {
                            Toast.makeText(context, "Export failed. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
            SettingItem(
                title = "Delete all data",
                value = "Erases chat, journal & profile",
                icon = Icons.Rounded.Delete,
                onClick = { showDeleteConfirm = true },
                isDestructive = true
            )
        }

        Spacer(Modifier.height(16.dp))

        CardSection(title = "About") {
            SettingItem(
                title = "Privacy Policy",
                value = "Ads, analytics & your data explained",
                icon = Icons.Rounded.Lock,
                onClick = { showPrivacyPolicy = true }
            )
            SettingItem(
                title = "App Version",
                value = "1.0.0"
            )
        }

        Spacer(Modifier.height(16.dp))

        CardSection(title = "Ads") {
            if (isPremium) {
                SettingItem(
                    title = "Ad-free",
                    value = "Thanks for being a Premium member",
                    icon = Icons.Rounded.Campaign
                )
            } else {
                var personalizedAds by rememberSaveable { mutableStateOf(viewModel.personalizedAdsEnabled) }
                SettingSwitchItem(
                    title = "Personalized ads",
                    subtitle = "Off (recommended): ads aren't based on your activity",
                    icon = Icons.Rounded.Campaign,
                    checked = personalizedAds,
                    onCheckedChange = {
                        personalizedAds = it
                        viewModel.setPersonalizedAdsEnabled(it)
                    }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        com.charles.warmwords.ui.components.AdBanner(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(24.dp))

        DisclaimerBanner()
    }

    if (showEditProfile) {
        uiState.profile?.let { profile ->
            EditProfileDialog(
                initialName = profile.name,
                initialPronouns = profile.pronouns,
                onDismiss = { showEditProfile = false },
                onSave = { name, pronouns ->
                    viewModel.updateProfile(name, pronouns)
                    showEditProfile = false
                }
            )
        }
    }

    if (showPrivacyPolicy) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyPolicy = false })
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text("Delete all data?") },
            text = { Text("This permanently erases your chat history, journal entries, mood logs, and profile. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAllData {
                        Toast.makeText(context, "All data deleted", Toast.LENGTH_SHORT).show()
                    }
                    showDeleteConfirm = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPersonaPicker) {
        PersonaPickerDialog(
            personas = viewModel.personas,
            selectedId = uiState.profile?.selectedPersonaId ?: viewModel.personas.first().id,
            isPremium = isPremium,
            onSelect = { viewModel.setPersona(it) },
            onNavigateToPaywall = onNavigateToPaywall,
            onDismiss = { showPersonaPicker = false }
        )
    }

    if (showVoicePicker) {
        VoicePickerDialog(
            voices = uiState.availableVoices,
            selectedName = uiState.profile?.selectedVoiceName,
            onSelect = { viewModel.setVoice(it) },
            onPreview = { viewModel.previewVoice(it) },
            onDismiss = { showVoicePicker = false }
        )
    }
}

@Composable
private fun PersonaPickerDialog(
    personas: List<Persona>,
    selectedId: String,
    isPremium: Boolean,
    onSelect: (String) -> Unit,
    onNavigateToPaywall: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = { Text("Choose AI Personality") },
        text = {
            Column {
                personas.forEach { persona ->
                    val locked = persona.isPremium && !isPremium
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { if (locked) onNavigateToPaywall() else onSelect(persona.id) }
                            .padding(vertical = 10.dp)
                    ) {
                        RadioButton(
                            selected = persona.id == selectedId && !locked,
                            onClick = { if (locked) onNavigateToPaywall() else onSelect(persona.id) }
                        )
                        Spacer(Modifier.width(4.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = if (persona.isPremium) "${persona.displayName}  • Premium" else persona.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (locked) "WarmWord Premium" else persona.tagline,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (locked) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "Switching personality starts a fresh conversation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

@Composable
private fun VoicePickerDialog(
    voices: List<WarmVoice>,
    selectedName: String?,
    onSelect: (String) -> Unit,
    onPreview: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = { Text("Choose a Voice") },
        text = {
            if (voices.isEmpty()) {
                Text(
                    "No natural-sounding voices found on this device yet. Try again in a moment.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(modifier = Modifier.height(360.dp)) {
                    items(voices, key = { it.name }) { voice ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(voice.name) }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = voice.name == selectedName,
                                onClick = { onSelect(voice.name) }
                            )
                            Spacer(Modifier.width(4.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = voice.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = voice.qualityLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onPreview(voice.name) }) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayCircle,
                                    contentDescription = "Preview ${voice.displayName}",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

@Composable
private fun EditProfileDialog(
    initialName: String,
    initialPronouns: String,
    onDismiss: () -> Unit,
    onSave: (name: String, pronouns: String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var pronouns by rememberSaveable { mutableStateOf(initialPronouns) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = { Text("Edit Profile") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = pronouns,
                    onValueChange = { pronouns = it },
                    label = { Text("Pronouns") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, pronouns) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = { Text("Privacy Policy") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "WarmWord runs its AI companion entirely on your device. All conversations, " +
                        "journal entries, and mood logs are processed by an on-device AI model and stored " +
                        "locally in an encrypted local database. Your private conversations and journal " +
                        "entries are never uploaded to WarmWord's own servers and are never used to " +
                        "personalize advertising.\n\n" +

                        "Advertising: WarmWord is supported by Google AdMob, a third-party ad network. To " +
                        "show ads, AdMob and its partners may collect and process information such as your " +
                        "advertising ID, IP address, device type, coarse (city-level) location, and general " +
                        "in-app activity (which screens you open). AdMob uses this to serve ads. By default " +
                        "WarmWord requests NON-personalized ads, so ad targeting is not based on your " +
                        "personal information or anything you've written. You can allow personalized ads in " +
                        "Settings > Ads, and you can reset or opt out of ad personalization at any time in " +
                        "your device's Android settings (Ads > Reset advertising ID / Opt out of ads " +
                        "personalization). For details, see Google's advertising privacy policy.\n\n" +

                        "Voice features: if you enable Voice Replies, spoken responses are synthesized " +
                        "on-device or by your phone's text-to-speech engine and are never recorded or sent " +
                        "anywhere. If you use the microphone button to speak a message, your phone's " +
                        "speech-recognition service converts it to text - on many devices this is handled " +
                        "on-device, but some devices/languages route it through your phone manufacturer's " +
                        "or Google's speech recognition servers rather than WarmWord's own servers (WarmWord " +
                        "has none). Review the transcribed text before sending; it's never sent anywhere " +
                        "until you tap Send.\n\n" +

                        "Crash reports & anonymous usage: WarmWord uses Firebase Crashlytics and Analytics " +
                        "to catch bugs and see which features are used. These only ever receive anonymous " +
                        "event names (like \"message sent\" or \"persona changed\") and crash stack traces - " +
                        "never your chat content, journal text, or anything you've typed. You can see exactly " +
                        "what's collected in Google's Firebase privacy documentation.\n\n" +

                        "Subscriptions: if you upgrade to WarmWord Premium, the purchase is handled entirely " +
                        "by Google Play and billed to your Google Play account. Google Play is the merchant of " +
                        "record and processes your payment information; WarmWord does not receive or store your " +
                        "card details. Your subscription status is verified through a Cloudflare Worker that " +
                        "WarmWord's developer controls, which checks the purchase with Google's servers using " +
                        "the Google Play Developer API. You can manage or cancel your subscription anytime in " +
                        "Google Play.\n\n" +

                        "Network access: besides the above, the only network access WarmWord uses is the " +
                        "one-time download of the AI model file, loading advertisements, and opening " +
                        "links/dialer for the resources listed in Find Help. If a future update adds live " +
                        "nearby-provider search, that request is routed through a Cloudflare Worker proxy " +
                        "that WarmWord's developer controls, so no API key for that service is ever stored " +
                        "in the app itself.\n\n" +

                        "You can export or permanently delete all of your data at any time from Settings.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun CardSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(4.dp))
        androidx.compose.material3.Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingSwitchItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp, 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onEdit: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    isDestructive: Boolean = false
) {
    val textColor = if (isDestructive) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.onSurface
    val clickHandler = onClick ?: onEdit

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .let { m -> if (clickHandler != null) m.clickable { clickHandler.invoke() } else m }
            .padding(12.dp, 8.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
            if (value.isNotBlank()) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (onEdit != null && onClick == null) {
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (onClick != null) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1 -> String.format("%.2f GB", gb)
        mb >= 1 -> String.format("%.0f MB", mb)
        kb >= 1 -> String.format("%.0f KB", kb)
        else -> "$bytes B"
    }
}
