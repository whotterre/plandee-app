package com.example.plandee.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.plandee.data.monetization.ProRepository
import com.example.plandee.data.repository.AppLeaderboardItem
import com.example.plandee.data.repository.DailyConsumptionBar
import com.example.plandee.data.repository.TrafficRepository
import com.example.plandee.data.repository.TrafficSummary
import com.example.plandee.data.security.SessionManager
import com.example.plandee.data.telemetry.NetworkEvent
import com.example.plandee.data.telemetry.TrafficMonitor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class DashboardUiState(
    val summary: TrafficSummary? = null,
    val dailyConsumption: List<DailyConsumptionBar> = emptyList(),
    val leaderboardItems: List<AppLeaderboardItem> = emptyList(),
    val allAppsItems: List<AppLeaderboardItem> = emptyList(),
    val isPro: Boolean = false,
    val tokens: Int = 2,
    val isLoading: Boolean = false
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TrafficRepository(application)
    private val sessionManager = SessionManager.getInstance(application)
    private val proRepository = ProRepository.getInstance(application)

    private val _uiState = MutableStateFlow(DashboardUiState(tokens = sessionManager.getTokens()))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    val networkEventFlow: SharedFlow<NetworkEvent> = TrafficMonitor.instance?.networkEventFlow
        ?: MutableSharedFlow()

    init {
        refreshData()

        TrafficMonitor.instance?.onDataUpdatedListener = {
            refreshData()
        }

        viewModelScope.launch {
            proRepository.isProState.collect { isPro ->
                _uiState.value = _uiState.value.copy(isPro = isPro)
            }
        }

        // Real-Time 2-second Telemetry Ticker Loop
        viewModelScope.launch {
            while (isActive) {
                delay(2000)
                refreshData()
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            val summary = repository.getTrafficSummary()
            val daily = repository.getDailyConsumption()
            val leaderboard = repository.getAppLeaderboard()
            val allApps = repository.getAllAppsLeaderboard()
            val tokens = sessionManager.getTokens()

            _uiState.value = _uiState.value.copy(
                summary = summary,
                dailyConsumption = daily,
                leaderboardItems = leaderboard,
                allAppsItems = allApps,
                tokens = tokens
            )
        }
    }

    fun consumeToken() {
        if (sessionManager.consumeToken()) {
            _uiState.value = _uiState.value.copy(tokens = sessionManager.getTokens())
        }
    }

    fun forceSyncData(networkType: String? = null) {
        viewModelScope.launch {
            TrafficMonitor.instance?.forceSampling(networkType)
            refreshData()
        }
    }
}
