package com.example.plandee.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.plandee.data.telemetry.NetworkEvent
import com.example.plandee.ui.screens.dashboard.DashboardTabAlerts
import com.example.plandee.ui.screens.dashboard.DashboardTabAuditor
import com.example.plandee.ui.screens.dashboard.DashboardTabHome
import com.example.plandee.ui.screens.dashboard.DashboardTabLeaderboard
import com.example.plandee.ui.screens.dashboard.DashboardTabPro
import com.example.plandee.ui.theme.*
import com.example.plandee.viewmodels.DashboardViewModel
import kotlinx.coroutines.delay
import java.text.DecimalFormat

enum class DashboardTab(val title: String, val icon: ImageVector) {
    AUDITOR("Auditor", Icons.Default.Shield),
    USAGE("Usage", Icons.Default.BarChart),
    ALERTS("Alerts", Icons.Default.NotificationsActive),
    LEADERBOARD("Leaderboard", Icons.Default.Leaderboard),
    PRO("Pro", Icons.Default.Star)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAuth: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel()
) {
    var activeTab by remember { mutableStateOf(DashboardTab.USAGE) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    var activeBannerMessage by remember { mutableStateOf<String?>(null) }
    var isBannerConnected by remember { mutableStateOf(true) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val df = remember { DecimalFormat("#.#") }

    LaunchedEffect(Unit) {
        viewModel.networkEventFlow.collect { event ->
            when (event) {
                is NetworkEvent.Connected -> {
                    isBannerConnected = true
                    activeBannerMessage = "Data Turned ON via ${event.source}"
                }
                is NetworkEvent.Disconnected -> {
                    isBannerConnected = false
                    val mbText = df.format(event.sessionMb)
                    activeBannerMessage = "Data Turned OFF (${event.source}): $mbText MB used"
                }
            }
            delay(4000)
            activeBannerMessage = null
        }
    }

    LaunchedEffect(activeTab) {
        viewModel.refreshData()
        if (activeTab == DashboardTab.PRO) {
            viewModel.fetchProStatus()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Scaffold(
            topBar = {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Plan Dee",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeonEmeraldGlow
                            )
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (uiState.isPro) NeonEmeraldGlow.copy(alpha = 0.15f) else RetroAmberGold.copy(alpha = 0.15f),
                                border = BorderStroke(
                                    1.5.dp,
                                    if (uiState.isPro) NeonEmeraldGlow else RetroAmberGold
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(if (uiState.isPro) NeonEmeraldGlow else RetroAmberGold)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (uiState.isPro) "DEE PRO" else "${uiState.tokens} TOKENS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (uiState.isPro) NeonEmeraldGlow else RetroAmberGold,
                                            fontSize = 11.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CrimsonAlertBg,
                                border = BorderStroke(1.dp, NeonRoseAccent.copy(alpha = 0.5f)),
                                modifier = Modifier.clickable { showLogoutDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = "Log Out",
                                        tint = NeonRoseAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Logout",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NeonRoseAccent,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = activeBannerMessage != null,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isBannerConnected) NeonEmeraldGlow else NeonRoseAccent,
                            shadowElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isBannerConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                                    contentDescription = "Network Status",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = activeBannerMessage ?: "",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = RetroCardSurface,
                    tonalElevation = 10.dp,
                    shadowElevation = 10.dp,
                    border = BorderStroke(1.dp, RetroBevelHighlight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DashboardTab.entries.forEach { tab ->
                            val isSelected = activeTab == tab
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { activeTab = tab }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) NeonEmeraldGlow else Color.Transparent)
                                        .border(
                                            1.dp,
                                            if (isSelected) NeonEmeraldGlow else Color.Transparent,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title,
                                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) NeonEmeraldGlow else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.forceSyncData() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (activeTab) {
                    DashboardTab.AUDITOR -> DashboardTabAuditor(
                        isPro = uiState.isPro,
                        tokens = uiState.tokens,
                        mlRecommendation = uiState.mlRecommendation,
                        onRunMLMatch = { carrier, budget, duration -> viewModel.runMLRecommendationMatch(carrier, budget, duration) },
                        onTokenConsumed = { viewModel.consumeToken() },
                        onRewardAdWatched = { viewModel.rewardAdToken() },
                        onNavigateToPro = { activeTab = DashboardTab.PRO }
                    )
                    DashboardTab.USAGE -> DashboardTabHome(
                        summary = uiState.summary,
                        dailyBars = uiState.dailyConsumption,
                        timelineBars = uiState.monthlyTimeline,
                        selectedDayIndex = uiState.selectedDayIndex,
                        onDaySelected = { viewModel.selectTimelineDay(it) },
                        onNavigateToAuditor = { activeTab = DashboardTab.AUDITOR }
                    )
                    DashboardTab.ALERTS -> DashboardTabAlerts()
                    DashboardTab.LEADERBOARD -> DashboardTabLeaderboard(
                        leaderboardItems = uiState.leaderboardItems,
                        allAppsItems = uiState.allAppsItems
                    )
                    DashboardTab.PRO -> DashboardTabPro(
                        onSubscribePro = { rcId -> viewModel.upgradeToPro(rcId) }
                    )
                }
            }
        }
    }

    // LOGOUT CONFIRMATION DIALOG
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = NeonRoseAccent)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Logout of PlanDee")
                }
            },
            text = {
                Text("Are you sure you want to log out of your PlanDee account?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout {
                            onNavigateToAuth()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRoseAccent)
                ) {
                    Text("Logout", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = RetroCardSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
