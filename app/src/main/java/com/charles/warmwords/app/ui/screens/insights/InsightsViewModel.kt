package com.charles.warmwords.app.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charles.warmwords.app.ai.LiteRtLmManager
import com.charles.warmwords.app.data.model.MoodLogModel
import com.charles.warmwords.app.data.model.SessionNoteModel
import com.charles.warmwords.app.data.model.SessionReminderModel
import com.charles.warmwords.app.domain.usecase.ChatUseCases
import com.charles.warmwords.app.domain.usecase.JournalUseCases
import com.charles.warmwords.app.domain.usecase.MoodUseCases
import com.charles.warmwords.app.domain.usecase.SessionNoteUseCases
import com.charles.warmwords.app.domain.usecase.SessionReminderUseCases
import com.charles.warmwords.app.reminders.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class MoodPoint(val day: String, val score: Float)

data class InsightsUiState(
    val weeklyMoodData: List<MoodPoint> = emptyList(),
    val totalEntries: Int = 0,
    val currentStreak: Int = 0,
    val averageMood: Float = 0f,
    val commonTags: List<Pair<String, Int>> = emptyList(),
    val totalChatSessions: Int = 0,
    val totalMessages: Int = 0,
    val recentSessions: List<ChatSessionSummary> = emptyList(),
    val sessionNotes: Map<Long, String> = emptyMap(),
    val notesBeingGenerated: Set<Long> = emptySet(),
    val upcomingReminders: List<SessionReminderModel> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val journalUseCases: JournalUseCases,
    private val moodUseCases: MoodUseCases,
    private val chatUseCases: ChatUseCases,
    private val sessionReminderUseCases: SessionReminderUseCases,
    private val sessionNoteUseCases: SessionNoteUseCases,
    private val liteRtLmManager: LiteRtLmManager,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow<InsightsUiState>(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState

    init {
        loadInsights()
        viewModelScope.launch {
            sessionReminderUseCases.allReminders.collect { reminders ->
                val upcoming = reminders.filter { it.timestamp > System.currentTimeMillis() }
                _uiState.value = _uiState.value.copy(upcomingReminders = upcoming)
            }
        }
        viewModelScope.launch {
            sessionNoteUseCases.allNotes.collect { notes ->
                _uiState.value = _uiState.value.copy(
                    sessionNotes = notes.associate { it.sessionStartTimestamp to it.note }
                )
            }
        }
    }

    private fun loadInsights() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            val weekStart = calendar.timeInMillis

            val moodEntries = moodUseCases.getEntriesSince(weekStart)
            val journalEntries = journalUseCases.getEntriesSince(weekStart)
            val totalEntries = journalUseCases.getCount()

            val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            val dailyMoods = Array(7) { mutableListOf<Float>() }

            moodEntries.forEach { entry ->
                val cal = Calendar.getInstance().apply { timeInMillis = entry.timestamp }
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                // Calendar.DAY_OF_WEEK is 1=Sunday..7=Saturday; remap to a Monday-first index (0..6).
                val mondayFirstIndex = (dayOfWeek + 5) % 7
                dailyMoods[mondayFirstIndex].add(entry.score.toFloat())
            }

            val weeklyMoodData = dayLabels.mapIndexed { index, label ->
                val scores = dailyMoods[index]
                val avg = if (scores.isNotEmpty()) scores.average().toFloat() else 0f
                MoodPoint(label, avg)
            }

            val averageMood = moodEntries.map { it.score.toFloat() }.average().takeIf { it.isNaN().not() } ?: 0f

            val tagFrequency = mutableMapOf<String, Int>()
            journalEntries.forEach { entry ->
                entry.tags.forEach { tag ->
                    tagFrequency[tag] = tagFrequency.getOrDefault(tag, 0) + 1
                }
            }
            val commonTags = tagFrequency.entries
                .sortedByDescending { it.value }
                .take(5)
                .map { it.key to it.value }

            val latestEntry = moodUseCases.getLatestEntry()
            val streak = calculateStreak(latestEntry, moodEntries)

            val allMessages = chatUseCases.allMessages.first()
            val sessions = buildChatSessions(allMessages)

            _uiState.value = _uiState.value.copy(
                weeklyMoodData = weeklyMoodData,
                totalEntries = totalEntries,
                currentStreak = streak,
                averageMood = averageMood.toFloat(),
                commonTags = commonTags,
                totalChatSessions = sessions.size,
                totalMessages = allMessages.size,
                recentSessions = sessions.take(10),
                isLoading = false
            )

            generateMissingNotes(sessions.take(10))
        }
    }

    /** Asks the on-device model for a short note on any finished session that doesn't have one yet. */
    private fun generateMissingNotes(sessions: List<ChatSessionSummary>) {
        if (!liteRtLmManager.isInitialized()) return
        val existingNotes = _uiState.value.sessionNotes
        val toGenerate = sessions.filter { !it.isLikelyOngoing && existingNotes[it.startTimestamp] == null }

        toGenerate.forEach { session ->
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(
                    notesBeingGenerated = _uiState.value.notesBeingGenerated + session.startTimestamp
                )
                val note = liteRtLmManager.summarizeConversation(session.transcript)
                if (!note.isNullOrBlank()) {
                    sessionNoteUseCases.saveNote(
                        SessionNoteModel(sessionStartTimestamp = session.startTimestamp, note = note)
                    )
                }
                _uiState.value = _uiState.value.copy(
                    notesBeingGenerated = _uiState.value.notesBeingGenerated - session.startTimestamp
                )
            }
        }
    }

    private fun calculateStreak(latestEntry: MoodLogModel?, allEntries: List<MoodLogModel>): Int {
        if (latestEntry == null) return 0
        val calendar = Calendar.getInstance().apply { timeInMillis = latestEntry.timestamp }
        val today = Calendar.getInstance()

        if (calendar.get(Calendar.YEAR) != today.get(Calendar.YEAR) ||
            calendar.get(Calendar.DAY_OF_YEAR) != today.get(Calendar.DAY_OF_YEAR)
        ) {
            return 0
        }

        var streak = 1
        val currentDay = today.clone() as Calendar

        for (i in 1..6) {
            (currentDay.add(Calendar.DAY_OF_YEAR, -1))
            val targetDay = currentDay.clone() as Calendar
            val hasEntry = allEntries.any { entry ->
                val entryCal = Calendar.getInstance().apply { timeInMillis = entry.timestamp }
                entryCal.get(Calendar.YEAR) == targetDay.get(Calendar.YEAR) &&
                    entryCal.get(Calendar.DAY_OF_YEAR) == targetDay.get(Calendar.DAY_OF_YEAR
                    )
            }
            if (hasEntry) streak++
            else break
        }
        return streak
    }

    fun scheduleReminder(timestamp: Long, label: String) {
        viewModelScope.launch {
            val id = sessionReminderUseCases.addReminder(
                SessionReminderModel(timestamp = timestamp, label = label)
            )
            reminderScheduler.schedule(id, timestamp, label)
        }
    }

    fun deleteReminder(reminder: SessionReminderModel) {
        viewModelScope.launch {
            reminderScheduler.cancel(reminder.id)
            sessionReminderUseCases.deleteReminder(reminder)
        }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadInsights()
    }
}
