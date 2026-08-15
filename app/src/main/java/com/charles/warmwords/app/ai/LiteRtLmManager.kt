package com.charles.warmwords.app.ai

import android.util.Log
import androidx.annotation.WorkerThread
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.MessageCallback
import com.charles.warmwords.app.util.ModelConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LiteRtLmManager @Inject constructor() {

    private var engine: com.google.ai.edge.litertlm.Engine? = null
    private var conversation: com.google.ai.edge.litertlm.Conversation? = null

    private var modelPath: String? = null

    @Volatile
    var systemPrompt: String = SystemPrompt.DEFAULT_PERSONA.systemPrompt
        private set

    @WorkerThread
    @OptIn(com.google.ai.edge.litertlm.ExperimentalApi::class)
    suspend fun initialize(modelPath: String, systemPrompt: String = this.systemPrompt): Boolean {
        val file = File(modelPath)
        if (!file.exists()) {
            Log.e(TAG, "Model file not found: $modelPath")
            return false
        }

        this.modelPath = modelPath
        this.systemPrompt = systemPrompt

        return try {
            release()

            val engineConfig = com.google.ai.edge.litertlm.EngineConfig(
                modelPath = modelPath,
                backend = com.google.ai.edge.litertlm.Backend.GPU(),
                maxNumTokens = ModelConfig.MAX_TOKENS,
                cacheDir = file.parentFile?.absolutePath
            )

            engine = com.google.ai.edge.litertlm.Engine(engineConfig).also {
                it.initialize()
            }

            val conversationConfig = com.google.ai.edge.litertlm.ConversationConfig(
                systemInstruction = com.google.ai.edge.litertlm.Contents.of(systemPrompt),
                samplerConfig = com.google.ai.edge.litertlm.SamplerConfig(
                    topK = 40,
                    topP = 0.95,
                    temperature = 0.8
                )
            )

            conversation = engine?.createConversation(conversationConfig)

            try {
                com.google.ai.edge.litertlm.ExperimentalFlags.enableSpeculativeDecoding = true
            } catch (e: Exception) {
                Log.w(TAG, "Speculative decoding not available", e)
            }

            Log.d(TAG, "Engine initialized successfully with GPU backend")
            true
        } catch (e: Exception) {
            Log.w(TAG, "GPU backend failed, falling back to CPU", e)
            try {
                release()
                val cpuConfig = com.google.ai.edge.litertlm.EngineConfig(
                    modelPath = modelPath,
                    backend = com.google.ai.edge.litertlm.Backend.CPU(),
                    maxNumTokens = ModelConfig.MAX_TOKENS,
                    cacheDir = file.parentFile?.absolutePath
                )
                engine = com.google.ai.edge.litertlm.Engine(cpuConfig).also {
                    it.initialize()
                }
                conversation = engine?.createConversation(
                    com.google.ai.edge.litertlm.ConversationConfig(
                        systemInstruction = com.google.ai.edge.litertlm.Contents.of(systemPrompt),
                        samplerConfig = com.google.ai.edge.litertlm.SamplerConfig(
                            topK = 40,
                            topP = 0.95,
                            temperature = 0.8
                        )
                    )
                )
                Log.d(TAG, "Engine initialized with CPU fallback")
                true
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to initialize engine with any backend", e2)
                false
            }
        }
    }

    @WorkerThread
    suspend fun reinitializeWithCpu(modelPath: String? = null, systemPrompt: String = this.systemPrompt): Boolean {
        val path = modelPath ?: this.modelPath ?: run {
            Log.e(TAG, "Model path not set")
            return false
        }
        val file = File(path)
        if (!file.exists()) return false

        this.modelPath = path
        this.systemPrompt = systemPrompt

        return try {
            release()

            val engineConfig = com.google.ai.edge.litertlm.EngineConfig(
                modelPath = path,
                backend = com.google.ai.edge.litertlm.Backend.CPU(),
                maxNumTokens = ModelConfig.MAX_TOKENS,
                cacheDir = file.parentFile?.absolutePath
            )

            engine = com.google.ai.edge.litertlm.Engine(engineConfig).also {
                it.initialize()
            }

            val conversationConfig = com.google.ai.edge.litertlm.ConversationConfig(
                systemInstruction = com.google.ai.edge.litertlm.Contents.of(systemPrompt),
                samplerConfig = com.google.ai.edge.litertlm.SamplerConfig(
                    topK = 40,
                    topP = 0.95,
                    temperature = 0.8
                )
            )

            conversation = engine?.createConversation(conversationConfig)
            Log.d(TAG, "Engine reinitialized with CPU backend")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize with CPU backend", e)
            false
        }
    }

    fun streamMessage(text: String): Flow<String> {
        val conv = conversation ?: return flow {
            throw IllegalStateException("Conversation not initialized")
        }
        Log.d(TAG, "Starting streamMessage for text: ${text.take(50)}")

        return callbackFlow {
            val callback = object : MessageCallback {
                override fun onMessage(message: Message) {
                    trySend(extractText(message))
                }
                override fun onDone() {
                    close()
                }
                override fun onError(throwable: Throwable) {
                    Log.e(TAG, "MessageCallback error", throwable)
                    close(throwable)
                }
            }
            conv.sendMessageAsync(text, callback)
            awaitClose { }
        }
        .flowOn(Dispatchers.IO)
        .catch { e ->
            Log.e(TAG, "Error during streaming", e)
            throw e
        }
    }

    private fun extractText(message: Message): String {
        return try {
            message.contents.contents
                .filterIsInstance<Content.Text>()
                .firstOrNull()
                ?.text
                .orEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract text from message", e)
            message.toString()
        }
    }

    fun cancelResponse() {
        try {
            conversation?.cancelProcess()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to cancel process", e)
        }
    }

    fun isInitialized(): Boolean = engine != null && conversation != null

    fun resetConversation(newSystemPrompt: String? = null) {
        val engineRef = engine ?: return
        if (newSystemPrompt != null) {
            systemPrompt = newSystemPrompt
        }
        try {
            conversation?.close()
            conversation = engineRef.createConversation(
                com.google.ai.edge.litertlm.ConversationConfig(
                    systemInstruction = com.google.ai.edge.litertlm.Contents.of(systemPrompt),
                    samplerConfig = com.google.ai.edge.litertlm.SamplerConfig(
                        topK = 40,
                        topP = 0.95,
                        temperature = 0.8
                    )
                )
            )
            Log.d(TAG, "Conversation reset")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reset conversation", e)
        }
    }

    /** Switches persona by recreating the conversation with a new system prompt, clearing history. */
    fun switchPersona(newSystemPrompt: String) = resetConversation(newSystemPrompt)

    fun release() {
        try {
            conversation?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to close conversation", e)
        }
        try {
            engine?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to close engine", e)
        }
        conversation = null
        engine = null
    }

    companion object {
        private const val TAG = "LiteRtLmManager"
    }
}
