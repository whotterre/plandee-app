package com.example.plandee.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plandee.data.repository.AppLeaderboardItem
import com.example.plandee.data.telemetry.UsagePermissionBridge
import com.example.plandee.ui.components.RetroTactileCard
import com.example.plandee.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTabLeaderboard(
    leaderboardItems: List<AppLeaderboardItem> = emptyList(),
    allAppsItems: List<AppLeaderboardItem> = emptyList()
) {
    val context = LocalContext.current
    var showAllAppsSheet by remember { mutableStateOf(false) }
    var isUsageGranted by remember { mutableStateOf(UsagePermissionBridge.isUsageAccessGranted(context)) }

    val topItems = leaderboardItems
    val fullAppList = if (allAppsItems.isNotEmpty()) allAppsItems else topItems

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "App Data Ranking",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Real-time app usage on your device",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // PERMISSION TROUBLESHOOTING PROMPT CARD
        if (!isUsageGranted) {
            RetroTactileCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = Color(0xFFF59E0B)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Permission Required",
                        tint = RetroAmberGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "App Usage Access Required",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = RetroAmberGold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Android requires 'Usage Access' permission to track per-app Wi-Fi and Mobile data consumption on your device.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        UsagePermissionBridge.openUsageAccessSettings(context)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                ) {
                    Text(
                        text = "Grant Usage Access in Settings ⚙️",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        if (topItems.isEmpty()) {
            RetroTactileCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = "No Traffic",
                        tint = NeonEmeraldGlow,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (!isUsageGranted) "Usage Permission Needed" else "Scanning Device Traffic...",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (!isUsageGranted) "Tap the button above to grant Usage Access in Android Settings." else "Connect to Wi-Fi or Mobile Data and launch any app to track live consumption.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                topItems.forEach { app ->
                    key(app.name) {
                        AppRankingCard(app = app)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (fullAppList.isNotEmpty()) {
            OutlinedButton(
                onClick = { showAllAppsSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = RetroCardSurface,
                    contentColor = NeonEmeraldGlow
                ),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonEmeraldGlow)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatListBulleted,
                        contentDescription = "View All",
                        tint = NeonEmeraldGlow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "View All Apps (${fullAppList.size})",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonEmeraldGlow,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }

    if (showAllAppsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAllAppsSheet = false },
            containerColor = RetroCardSurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "All Installed Apps Data Usage",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${fullAppList.size} apps actively logged on device",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    IconButton(onClick = { showAllAppsSheet = false }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(color = RetroBorderMetallic)

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(fullAppList, key = { it.name }) { app ->
                        AppRankingCard(app = app)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppRankingCard(app: AppLeaderboardItem) {
    val iconAndColor = when (app.name.lowercase()) {
        "youtube" -> Pair(Icons.Default.Subscriptions, NeonRoseAccent)
        "instagram" -> Pair(Icons.Default.CameraAlt, Color(0xFFE1306C))
        else -> Pair(Icons.Default.Android, NeonEmeraldGlow)
    }

    RetroTactileCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconAndColor.second),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconAndColor.first,
                        contentDescription = app.name,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = app.usageGb,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Text(
                text = app.rank,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (app.rank == "#1") NeonEmeraldGlow else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        LinearProgressIndicator(
            progress = { app.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (app.rank == "#1") NeonEmeraldGlow else NeonCyanGlow,
            trackColor = RetroTactileBg
        )
    }
}
