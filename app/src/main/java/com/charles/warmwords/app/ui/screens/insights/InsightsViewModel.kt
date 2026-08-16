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
import java.util.Locale
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
    val allSessions: List<ChatSessionSummary> = emptyList(),
    val pagedSessions: List<ChatSessionSummary> = emptyList(),
    val searchQuery: String = "",
    val currentPage: Int = 1,
    val totalPages: Int = 1,
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
                recomputePage()
            }
        }
        // Sessions whose AI note failed (or hadn't been generated yet because the model wasn't
        // loaded) get another chance the moment the on-device engine becomes usable. Notes that
        // were already saved are skipped by generateMissingNotes, so nothing regenerates.
        viewModelScope.launch {
            liteRtLmManager.isReady.collect { ready ->
                if (ready) generateMissingNotes(_uiState.value.allSessions)
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
                allSessions = sessions,
                currentPage = 1,
                isLoading = false
            )
            recomputePage()

            generateMissingNotes(sessions)
        }
    }

    /**
     * Re-runs the search filter and slices the current page out of [InsightsUiState.allSessions],
     * keeping the shared session list coherent with whichever state last changed (sessions, notes,
     * query or page).
     */
    private fun recomputePage() {
        val state = _uiState.value
        val query = state.searchQuery.trim().lowercase(Locale.getDefault())

        val filtered = if (query.isEmpty()) {
            state.allSessions
        } else {
            state.allSessions.filter { session ->
                val note = state.sessionNotes[session.startTimestamp].orEmpty()
                session.preview.lowercase(Locale.getDefault()).contains(query) ||
                    note.lowercase(Locale.getDefault()).contains(query) ||
                    session.transcript.lowercase(Locale.getDefault()).contains(query)
            }
        }

        val totalPages = maxOf(1, (filtered.size + PAGE_SIZE - 1) / PAGE_SIZE)
        val safePage = state.currentPage.coerceIn(1, totalPages)
        val fromIndex = (safePage - 1) * PAGE_SIZE
        val paged = if (filtered.isEmpty()) {
            emptyList()
        } else {
            filtered.subList(fromIndex, minOf(fromIndex + PAGE_SIZE, filtered.size))
        }

        _uiState.value = state.copy(
            pagedSessions = paged,
            currentPage = safePage,
            totalPages = totalPages
        )
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query, currentPage = 1)
        recomputePage()
    }

    fun nextPage() {
        val state = _uiState.value
        if (state.currentPage < state.totalPages) {
            _uiState.value = state.copy(currentPage = state.currentPage + 1)
            recomputePage()
        }
    }

    fun previousPage() {
        val state = _uiState.value
        if (state.currentPage > 1) {
            _uiState.value = state.copy(currentPage = state.currentPage - 1)
            recomputePage()
        }
    }

    fun goToPage(page: Int) {
        _uiState.value = _uiState.value.copy(currentPage = page)
        recomputePage()
    }

    /** Persists a manually written (full) insight for a session, replacing any AI note. */
    fun saveInsight(sessionStartTimestamp: Long, note: String) {
        val trimmed = note.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            sessionNoteUseCases.saveNote(
                SessionNoteModel(sessionStartTimestamp = sessionStartTimestamp, note = trimmed)
            )
        }
    }

    /** Asks the on-device model for a short note on any finished session that doesn't have one yet. */
    private fun generateMissingNotes(sessions: List<ChatSessionSummary>) {
        if (!liteRtLmManager.isInitialized()) return

        val toGenerate = sessions.filter {
            !it.isLikelyOngoing && !_uiState.value.sessionNotes.containsKey(it.startTimestamp)
        }
        if (toGenerate.isEmpty()) return

        viewModelScope.launch {
            // Run sequentially: each call spins up a throwaway LiteRT-LM conversation, so doing
            // them one at a time keeps the on-device engine from choking on parallel inference.
            toGenerate.forEach { session ->
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

    companion object {
        private const val PAGE_SIZE = 5
    }
}
