package com.example.plandee.ui.screens.dashboard

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
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
    var filterUserAppsOnly by remember { mutableStateOf(true) }

    val rawTopItems = leaderboardItems
    val rawFullList = if (allAppsItems.isNotEmpty()) allAppsItems else rawTopItems

    val topItems = remember(rawTopItems, filterUserAppsOnly) {
        if (filterUserAppsOnly) rawTopItems.filter { !it.isSystemApp } else rawTopItems
    }

    val fullAppList = remember(rawFullList, filterUserAppsOnly) {
        if (filterUserAppsOnly) rawFullList.filter { !it.isSystemApp } else rawFullList
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "App Data Ranking",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Real-time per-app data usage on device",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // USER APPS VS ALL SYSTEM FILTER TOGGLE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterTabButton(
                label = "📱 User Apps Only",
                isSelected = filterUserAppsOnly,
                onClick = { filterUserAppsOnly = true },
                modifier = Modifier.weight(1f)
            )

            FilterTabButton(
                label = "⚙️ All Apps & System",
                isSelected = !filterUserAppsOnly,
                onClick = { filterUserAppsOnly = false },
                modifier = Modifier.weight(1f)
            )
        }

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
                        text = if (!isUsageGranted) "Tap the button above to grant Usage Access in Android Settings." else "Launch YouTube or any active app to display live per-app consumption.",
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
                    key(app.packageName) {
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
                            text = "Installed Apps Data Usage",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${fullAppList.size} apps logged on device today",
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
                    items(fullAppList, key = { it.packageName }) { app ->
                        AppRankingCard(app = app)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterTabButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) NeonEmeraldGlow else RetroCardSurface)
            .border(
                1.5.dp,
                if (isSelected) NeonEmeraldGlow else RetroBorderMetallic,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
private fun RealAppIconImage(packageName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val iconBitmap = remember(packageName) {
        try {
            val drawable = context.packageManager.getApplicationIcon(packageName)
            drawable.toBitmap(width = 96, height = 96).asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    if (iconBitmap != null) {
        Image(
            bitmap = iconBitmap,
            contentDescription = packageName,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(NeonEmeraldGlow.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Android,
                contentDescription = packageName,
                tint = NeonEmeraldGlow,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun AppRankingCard(app: AppLeaderboardItem) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }

    RetroTactileCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // REAL INSTALLED APP ICON
                RealAppIconImage(
                    packageName = app.packageName,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = app.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (app.isSystemApp) RetroBorderMetallic else NeonEmeraldGlow.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = app.categoryText,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    color = if (app.isSystemApp) MaterialTheme.colorScheme.onSurfaceVariant else NeonEmeraldGlow,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
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

        Spacer(modifier = Modifier.height(12.dp))

        // PROPORTIONAL DATA SHARE PERCENTAGE LABEL & BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Data Contribution",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = app.sharePercentText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (app.rank == "#1") NeonEmeraldGlow else NeonCyanGlow
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { app.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (app.rank == "#1") NeonEmeraldGlow else NeonCyanGlow,
            trackColor = RetroTactileBg
        )

        // EXPANDABLE PLAIN-ENGLISH INFO & SAFE ACTION CONTROL
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
            ) {
                HorizontalDivider(color = RetroBorderMetallic)

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = NeonCyanGlow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = app.explanationText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // SAFE ACTION BUTTON: RESTRICT BACKGROUND DATA
                Button(
                    onClick = {
                        try {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", app.packageName, null)
                            )
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RetroCardSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RetroBorderMetallic)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = NeonEmeraldGlow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⚙️ Restrict Background Data",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }
    }
}
