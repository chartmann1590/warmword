package com.charles.warmwords.app.ui.screens.chat

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charles.warmwords.app.ai.LiteRtLmManager
import com.charles.warmwords.app.ai.Persona
import com.charles.warmwords.app.ai.SystemPrompt
import com.charles.warmwords.app.ai.TextToSpeechManager
import com.charles.warmwords.app.analytics.AnalyticsManager
import com.charles.warmwords.app.billing.SubscriptionState
import com.charles.warmwords.app.data.model.ChatMessageModel
import com.charles.warmwords.app.domain.repository.SubscriptionRepository
import com.charles.warmwords.app.domain.usecase.ChatUseCases
import com.charles.warmwords.app.domain.usecase.UserProfileUseCases
import com.charles.warmwords.app.util.ModelConfig
import com.charles.warmwords.app.util.isLowRamDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessageModel> = emptyList(),
    val isTyping: Boolean = false,
    val isModelReady: Boolean = false,
    val showLowRamError: Boolean = false,
    val voiceRepliesEnabled: Boolean = false,
    val isSpeaking: Boolean = false,
    val persona: Persona = SystemPrompt.DEFAULT_PERSONA
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatUseCases: ChatUseCases,
    private val liteRtLmManager: LiteRtLmManager,
    private val userProfileUseCases: UserProfileUseCases,
    private val textToSpeechManager: TextToSpeechManager,
    private val analyticsManager: AnalyticsManager,
    private val subscriptionRepository: SubscriptionRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val messages = chatUseCases.allMessages
        .map { it }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    private var lastKnownPersonaId: String? = null

    init {
        viewModelScope.launch {
            chatUseCases.deleteOrphanedStreamingMessages()
        }
        viewModelScope.launch {
            userProfileUseCases.profile.collect { profile ->
                val personaId = profile?.selectedPersonaId
                val requestedPersona = SystemPrompt.byId(personaId)
                // If a premium persona was selected but the subscription is no longer active
                // (e.g. lapsed), fall back to the free companion and persist the reset.
                val persona = if (requestedPersona.isPremium && !subscriptionRepository.getState().isPremium) {
                    if (personaId != SystemPrompt.DEFAULT_PERSONA.id) {
                        profile?.let {
                            userProfileUseCases.updateProfile(
                                it.copy(selectedPersonaId = SystemPrompt.DEFAULT_PERSONA.id)
                            )
                        }
                    }
                    SystemPrompt.DEFAULT_PERSONA
                } else {
                    requestedPersona
                }
                _uiState.value = _uiState.value.copy(
                    voiceRepliesEnabled = profile?.voiceRepliesEnabled == true,
                    persona = persona
                )

                val previousPersonaId = lastKnownPersonaId
                lastKnownPersonaId = personaId
                if (previousPersonaId != null && previousPersonaId != personaId && liteRtLmManager.isInitialized()) {
                    liteRtLmManager.switchPersona(persona.systemPrompt)
                    chatUseCases.deleteAll()
                    Log.d("ChatViewModel", "Switched persona to ${persona.id}, conversation reset")
                    analyticsManager.logEvent(
                        AnalyticsManager.EVENT_PERSONA_CHANGED,
                        mapOf(AnalyticsManager.PARAM_PERSONA_ID to persona.id)
                    )
                }

                textToSpeechManager.applyVoice(profile?.selectedVoiceName)
            }
        }
        viewModelScope.launch {
            textToSpeechManager.isSpeaking.collect { speaking ->
                _uiState.value = _uiState.value.copy(isSpeaking = speaking)
            }
        }
        if (liteRtLmManager.isInitialized()) {
            // LiteRtLmManager is a Hilt singleton, so if it's already initialized (e.g. this
            // ChatViewModel was recreated by Compose Navigation while the underlying engine
            // stayed alive), the existing Conversation still holds the model's in-context
            // memory of everything said so far. Re-running initializeModel() here would call
            // LiteRtLmManager.initialize(), which always release()s and recreates the
            // Conversation from scratch - silently wiping that memory while the DB-backed
            // message list on screen still shows the old messages, making the AI look like it
            // "forgot" the conversation even though nothing else changed. Just sync UI state.
            Log.d("ChatViewModel", "Engine already initialized, reusing existing conversation")
            viewModelScope.launch {
                val persona = SystemPrompt.byId(userProfileUseCases.getProfile()?.selectedPersonaId)
                lastKnownPersonaId = persona.id
                _uiState.value = _uiState.value.copy(isModelReady = true, persona = persona)
            }
        } else {
            val modelFile = File(
                context.getExternalFilesDir(null),
                "models/${ModelConfig.MODEL_VERSION}/${ModelConfig.MODEL_FILE_NAME}"
            )
            if (modelFile.exists()) {
                initializeModel(modelFile.absolutePath) { success ->
                    Log.d("ChatViewModel", "Model initialization result: $success")
                    if (success) analyticsManager.logEvent(AnalyticsManager.EVENT_SESSION_STARTED)
                }
            } else {
                Log.e("ChatViewModel", "Model file not found at ${modelFile.absolutePath}")
                _uiState.value = _uiState.value.copy(
                    isModelReady = false
                )
            }
        }
    }

    fun sendMessage(text: String) {
        Log.d("ChatViewModel", "sendMessage called: text='${text.take(50)}', isModelReady=${liteRtLmManager.isInitialized()}")
        if (text.isBlank()) {
            Log.d("ChatViewModel", "sendMessage: text is blank, returning")
            return
        }

        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()

            val userMessage = ChatMessageModel.User(
                id = 0,
                timestamp = currentTime,
                content = text
            )
            chatUseCases.saveMessage(userMessage)
            analyticsManager.logEvent(AnalyticsManager.EVENT_MESSAGE_SENT)
            Log.d("ChatViewModel", "sendMessage: user message saved")

            val modelMessageId = chatUseCases.saveMessage(
                ChatMessageModel.Model(
                    id = 0,
                    timestamp = currentTime,
                    content = "",
                    isStreaming = true
                )
            )
            Log.d("ChatViewModel", "sendMessage: model message saved, id=$modelMessageId")

            var currentModelMessage = ChatMessageModel.Model(
                id = modelMessageId,
                timestamp = currentTime,
                content = "",
                isStreaming = true
            )

            _uiState.value = _uiState.value.copy(isTyping = true)
            Log.d("ChatViewModel", "sendMessage: isTyping set to true, calling streamMessage")

            try {
                liteRtLmManager.streamMessage(text).collect { chunk ->
                    currentModelMessage = currentModelMessage.copy(
                        content = currentModelMessage.content + chunk,
                        isStreaming = true
                    )
                    chatUseCases.saveMessage(currentModelMessage)
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "sendMessage: streamMessage error", e)
                analyticsManager.recordNonFatal(e)
                Toast.makeText(context, "Sorry, I encountered an error. Please try again.", Toast.LENGTH_SHORT).show()
            } finally {
                chatUseCases.saveMessage(
                    currentModelMessage.copy(isStreaming = false)
                )
                _uiState.value = _uiState.value.copy(isTyping = false)
                if (_uiState.value.voiceRepliesEnabled && currentModelMessage.content.isNotBlank()) {
                    textToSpeechManager.speak(currentModelMessage.content)
                }
                Log.d("ChatViewModel", "sendMessage: finally block completed")
            }
        }
    }

    fun cancelResponse() {
        liteRtLmManager.cancelResponse()
        textToSpeechManager.stop()
    }

    fun stopSpeaking() {
        textToSpeechManager.stop()
    }

    fun clearChat() {
        viewModelScope.launch {
            chatUseCases.deleteAll()
            liteRtLmManager.resetConversation()
        }
    }

    fun initializeModel(modelPath: String, onResult: (Boolean) -> Unit) {
        Log.d("ChatViewModel", "Starting model initialization")
        viewModelScope.launch(Dispatchers.IO) {
            val persona = SystemPrompt.byId(userProfileUseCases.getProfile()?.selectedPersonaId)
            lastKnownPersonaId = persona.id
            _uiState.value = _uiState.value.copy(persona = persona)
            val isLowRam = context.isLowRamDevice()
            val success = if (isLowRam) {
                _uiState.value = _uiState.value.copy(showLowRamError = true)
                liteRtLmManager.reinitializeWithCpu(modelPath, persona.systemPrompt)
            } else {
                liteRtLmManager.initialize(modelPath, persona.systemPrompt)
            }
            _uiState.value = _uiState.value.copy(
                isModelReady = success,
                showLowRamError = if (!success) true else false
            )
            onResult(success)
        }
    }
}
