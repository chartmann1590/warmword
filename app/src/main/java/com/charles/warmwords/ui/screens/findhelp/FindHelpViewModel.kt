package com.charles.warmwords.ui.screens.findhelp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charles.warmwords.analytics.AnalyticsManager
import com.charles.warmwords.domain.usecase.CrisisUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FindHelpUiState(
    val resources: List<com.charles.warmwords.data.local.entity.CrisisResource> = emptyList(),
    val searchResults: List<ProviderSearchResult> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FindHelpViewModel @Inject constructor(
    private val crisisUseCases: CrisisUseCases,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FindHelpUiState())
    val uiState: StateFlow<FindHelpUiState> = _uiState

    init {
        loadCrisisResources()
    }

    private fun loadCrisisResources() {
        viewModelScope.launch {
            crisisUseCases.getLocalResources().collect { resources ->
                _uiState.value = _uiState.value.copy(
                    resources = resources,
                    searchResults = ProviderDatabase.mentalHealthResources
                )
            }
        }
    }

    fun trackResourceOpened() {
        analyticsManager.logEvent(AnalyticsManager.EVENT_CRISIS_RESOURCE_OPENED)
    }

    fun refresh() {
        loadCrisisResources()
    }
}
