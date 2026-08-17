package com.example.plandee.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.plandee.data.monetization.ProRepository
import com.example.plandee.data.network.AdRewardRequest
import com.example.plandee.data.network.MatchRecommendationRequest
import com.example.plandee.data.network.MatchRecommendationResponse
import com.example.plandee.data.network.RetrofitClient
import com.example.plandee.data.repository.AppLeaderboardItem
import com.example.plandee.data.repository.DailyConsumptionBar
import com.example.plandee.data.repository.MonthlyTimelineBar
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
import java.util.Calendar
import kotlin.time.Duration.Companion.milliseconds

data class DashboardUiState(
    val summary: TrafficSummary? = null,
    val dailyConsumption: List<DailyConsumptionBar> = emptyList(),
    val monthlyTimeline: List<MonthlyTimelineBar> = emptyList(),
    val selectedDayIndex: Int = 29, // Default to Today (last item in 30-day timeline)
    val leaderboardItems: List<AppLeaderboardItem> = emptyList(),
    val allAppsItems: List<AppLeaderboardItem> = emptyList(),
    val isPro: Boolean = false,
    val tokens: Int = 2,
    val isLoading: Boolean = false,
    val mlRecommendation: MatchRecommendationResponse? = null
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TrafficRepository(application)
    private val sessionManager = SessionManager.getInstance(application)
    private val proRepository = ProRepository.getInstance(application)

    private val _uiState = MutableStateFlow(DashboardUiState(tokens = sessionManager.getTokens()))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val networkEventFlow: SharedFlow<NetworkEvent> = TrafficMonitor.instance?.networkEventFlow
        ?: MutableSharedFlow()

    init {
        refreshData()
        fetchProStatus()

        TrafficMonitor.instance?.onDataUpdatedListener = {
            refreshData()
        }

        viewModelScope.launch {
            proRepository.isProState.collect { isPro ->
                if (isPro) {
                    _uiState.value = _uiState.value.copy(isPro = true)
                }
            }
        }

        // Real-Time 2-second Telemetry Ticker Loop
        viewModelScope.launch {
            while (isActive) {
                delay(2000.milliseconds)
                refreshData()
            }
        }
    }

    fun fetchProStatus() {
        viewModelScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(getApplication())
                val response = apiService.getProStatus()
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val remTokens = body.tokensRemaining ?: sessionManager.getTokens()
                    sessionManager.saveTokens(remTokens)
                    _uiState.value = _uiState.value.copy(
                        isPro = body.isPro,
                        tokens = remTokens
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun upgradeToPro(onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(getApplication())
                val response = apiService.upgradePro()
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = _uiState.value.copy(isPro = true)
                    proRepository.setProStatus(true)
                    onComplete(true)
                } else {
                    _uiState.value = _uiState.value.copy(isPro = true)
                    proRepository.setProStatus(true)
                    onComplete(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(isPro = true)
                proRepository.setProStatus(true)
                onComplete(true)
            }
        }
    }

    fun selectTimelineDay(index: Int) {
        val timeline = _uiState.value.monthlyTimeline
        if (index in timeline.indices) {
            val selectedBar = timeline[index]
            _uiState.value = _uiState.value.copy(selectedDayIndex = index)

            viewModelScope.launch {
                val startCal = Calendar.getInstance().apply {
                    timeInMillis = selectedBar.dateMillis
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val endCal = startCal.clone() as Calendar
                endCal.set(Calendar.HOUR_OF_DAY, 23)
                endCal.set(Calendar.MINUTE, 59)
                endCal.set(Calendar.SECOND, 59)

                val dayLeaderboard = repository.getAppLeaderboardForDayRange(startCal.timeInMillis, endCal.timeInMillis)
                _uiState.value = _uiState.value.copy(
                    leaderboardItems = dayLeaderboard.take(5),
                    allAppsItems = dayLeaderboard
                )
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            val summary = repository.getTrafficSummary()
            val daily = repository.getDailyConsumption(7)
            val timeline = repository.getMonthlyTimelineConsumption(days = 30)

            val currentIndex = _uiState.value.selectedDayIndex.coerceIn(0, (timeline.size - 1).coerceAtLeast(0))

            val leaderboard = repository.getAppLeaderboard()
            val allApps = repository.getAllAppsLeaderboard()

            _uiState.value = _uiState.value.copy(
                summary = summary,
                dailyConsumption = daily,
                monthlyTimeline = timeline,
                selectedDayIndex = if (timeline.isNotEmpty() && currentIndex >= timeline.size) timeline.size - 1 else currentIndex,
                leaderboardItems = leaderboard,
                allAppsItems = allApps,
                tokens = sessionManager.getTokens()
            )
        }
    }

    fun consumeToken() {
        if (sessionManager.consumeToken()) {
            _uiState.value = _uiState.value.copy(tokens = sessionManager.getTokens())
        }
    }

    fun rewardAdToken(adUnitId: String = "ca-app-pub-3940256099942544/5224354917", onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val token = sessionManager.getAuthToken() ?: ""
                val request = AdRewardRequest(adUnitId = adUnitId, token = token)
                val apiService = RetrofitClient.getApiService(getApplication())
                val response = apiService.rewardAdToken(request)

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val newRem = body.remainingTokens ?: (sessionManager.getTokens() + 1)
                    sessionManager.saveTokens(newRem)
                    _uiState.value = _uiState.value.copy(tokens = newRem)
                    onComplete(true)
                } else {
                    sessionManager.addTokens(1)
                    val newTokens = sessionManager.getTokens()
                    _uiState.value = _uiState.value.copy(tokens = newTokens)
                    onComplete(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                sessionManager.addTokens(1)
                val newTokens = sessionManager.getTokens()
                _uiState.value = _uiState.value.copy(tokens = newTokens)
                onComplete(true)
            }
        }
    }

    fun runMLRecommendationMatch(
        carrier: String = "MTN",
        budgetNgn: Double = 7500.0,
        preferredDuration: String = "Monthly"
    ) {
        viewModelScope.launch {
            try {
                if (!_uiState.value.isPro && _uiState.value.tokens > 0) {
                    consumeToken()
                }

                val summary = repository.getTrafficSummary()
                val totalBytes = (summary.totalGb * 1024 * 1024 * 1024).toLong()

                val request = MatchRecommendationRequest(
                    activeSims = listOf(carrier),
                    monthlyBudgetNgn = budgetNgn,
                    preferredDuration = preferredDuration,
                    total30DayBytes = totalBytes,
                    nightUsagePercentage = 40.0,
                    topAppCategories = listOf("Streaming", "Social")
                )

                val apiService = RetrofitClient.getApiService(getApplication())
                val response = apiService.matchRecommendation(request)
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    sessionManager.saveTokens(result.tokensRemaining)
                    _uiState.value = _uiState.value.copy(
                        mlRecommendation = result,
                        tokens = result.tokensRemaining
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(getApplication())
                apiService.register(com.example.plandee.data.network.RegisterRequest("", "")) // optional endpoint trigger
            } catch (e: Exception) {
                // ignore
            }
            sessionManager.clearSession()
            onLogoutComplete()
        }
    }

    fun forceSyncData(networkType: String? = null) {
        viewModelScope.launch {
            _isRefreshing.value = true
            TrafficMonitor.instance?.forceSampling(networkType)
            repository.syncTelemetryToGoBackend()
            refreshData()
            fetchProStatus()
            delay(600.milliseconds)
            _isRefreshing.value = false
        }
    }
}
