package com.charles.warmwords.app.ui.screens.chat

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.charles.warmwords.app.R
import com.charles.warmwords.app.data.model.ChatMessageModel
import com.charles.warmwords.app.ui.components.Avatar
import com.charles.warmwords.app.ui.components.AvatarState
import com.charles.warmwords.app.ui.components.ChatFooterDisclaimer
import java.util.Locale

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onNavigateToFindHelp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val messages by viewModel.messages.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        ChatHeader(viewModel = viewModel)

        ChatMessageList(
            messages = messages,
            isTyping = uiState.isTyping,
            scrollState = scrollState,
            greeting = uiState.persona.greeting,
            modifier = Modifier.weight(1f)
        )

        ChatInputBar(
            onSendMessage = { text ->
                if (uiState.isModelReady) {
                    viewModel.sendMessage(text)
                }
            },
            isTyping = uiState.isTyping,
            isModelReady = uiState.isModelReady,
            onCancel = { viewModel.cancelResponse() }
        )

        ChatFooterDisclaimer()
    }
}

@Composable
private fun ChatHeader(viewModel: ChatViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Avatar(
            state = when {
                uiState.isTyping -> AvatarState.THINKING
                else -> AvatarState.IDLE
            },
            size = 48.dp,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = "WarmWord",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (uiState.isModelReady) uiState.persona.displayName else "Loading model...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f)
                )
                if (!uiState.isModelReady) {
                    Spacer(Modifier.width(8.dp))
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        if (uiState.isSpeaking) {
            IconButton(onClick = { viewModel.stopSpeaking() }) {
                Icon(
                    imageVector = Icons.Rounded.VolumeUp,
                    contentDescription = "Stop speaking",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        IconButton(onClick = { viewModel.clearChat() }) {
            Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = "Clear chat",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f)
            )
        }
    }
}

@Composable
private fun ChatMessageList(
    messages: List<ChatMessageModel>,
    isTyping: Boolean,
    scrollState: androidx.compose.foundation.ScrollState,
    greeting: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (messages.isEmpty() && !isTyping) {
            EmptyChatState(
                greeting = greeting,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 80.dp)
            ) {
                messages.forEach { message ->
                    MessageBubble(message = message)
                }
                if (isTyping) {
                    TypingIndicator()
                }
            }
        }
    }
}

@Composable
private fun EmptyChatState(greeting: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Avatar(state = AvatarState.IDLE, size = 80.dp)
        Spacer(Modifier.height(16.dp))
        Text(
            text = greeting,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MessageBubble(message: ChatMessageModel) {
    when (message) {
        is ChatMessageModel.User -> UserMessageBubble(message.content)
        is ChatMessageModel.Model -> ModelMessageBubble(
            content = message.content,
            isStreaming = message.isStreaming
        )
    }
}

@Composable
private fun UserMessageBubble(content: String) {
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(18.dp, 4.dp, 18.dp, 18.dp)
                )
                .padding(12.dp, 8.dp)
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun ModelMessageBubble(content: String, isStreaming: Boolean) {
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp)
                )
                .padding(12.dp, 8.dp)
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isStreaming) {
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp)
                )
                .padding(16.dp, 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
                                shape = CircleShape
                            )
                    )
                    if (it < 2) Spacer(Modifier.width(4.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "typing...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
                )
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    onSendMessage: (String) -> Unit,
    isTyping: Boolean,
    isModelReady: Boolean,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var text by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                text = if (text.isBlank()) spoken else "$text $spoken"
            }
        }
    }

    fun launchSpeechRecognizer() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to WarmWord...")
        }
        runCatching { speechLauncher.launch(intent) }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchSpeechRecognizer()
    }

    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp, 8.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = {
                Text(
                    text = if (isModelReady) "Type a message..." else "Loading model...",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            singleLine = false,
            enabled = isModelReady && !isTyping,
            maxLines = 4,
            trailingIcon = {
                IconButton(
                    onClick = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            launchSpeechRecognizer()
                        } else {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    enabled = isModelReady && !isTyping
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Mic,
                        contentDescription = "Speak your message",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f)
                    )
                }
            }
        )
        Spacer(Modifier.width(8.dp))
        if (isTyping) {
            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.errorContainer, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Stop",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(22.dp)
                )
            }
        } else {
            val canSend = text.isNotBlank() && isModelReady && !isTyping
            IconButton(
                onClick = {
                    if (canSend) {
                        onSendMessage(text)
                        text = ""
                    }
                },
                enabled = canSend,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (canSend) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Send,
                    contentDescription = "Send",
                    tint = if (canSend) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
