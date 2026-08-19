package com.charles.warmwords.ui.screens.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charles.warmwords.analytics.AnalyticsManager
import com.charles.warmwords.data.model.JournalEntryModel
import com.charles.warmwords.domain.usecase.JournalUseCases
import com.charles.warmwords.domain.usecase.MoodUseCases
import com.charles.warmwords.data.model.MoodLogModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class JournalUiState(
    val entries: List<JournalEntryModel> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val journalUseCases: JournalUseCases,
    private val moodUseCases: MoodUseCases,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    val uiState: StateFlow<JournalUiState> = journalUseCases.allEntries
        .map { JournalUiState(entries = it) }
        .stateIn(viewModelScope, SharingStarted.Lazily, JournalUiState())

    fun deleteEntry(entry: JournalEntryModel) {
        viewModelScope.launch {
            journalUseCases.deleteEntry(entry)
        }
    }

fun addEntry(moodScore: Int, content: String, tags: List<String> = emptyList()) {
    viewModelScope.launch {
        journalUseCases.addEntry(
            JournalEntryModel(
                timestamp = System.currentTimeMillis(),
                moodScore = moodScore,
                content = content,
                tags = tags
            )
        )
            moodUseCases.addEntry(
                MoodLogModel(
                    timestamp = System.currentTimeMillis(),
                    score = moodScore
                )
            )
            analyticsManager.logEvent(AnalyticsManager.EVENT_JOURNAL_ENTRY_ADDED)
        }
    }

    suspend fun getWeeklyEntries(): List<JournalEntryModel> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        return journalUseCases.getEntriesSince(calendar.timeInMillis)
    }
}
