package com.example.plandee.ui.screens.dashboard

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plandee.data.repository.AppLeaderboardItem
import com.example.plandee.data.repository.MonthlyTimelineBar
import com.example.plandee.data.security.SessionManager
import com.example.plandee.ui.components.RetroTactileCard
import com.example.plandee.ui.theme.*

@Composable
fun DashboardTabAlerts(
    timelineBars: List<MonthlyTimelineBar> = emptyList(),
    selectedDayIndex: Int = 29,
    leaderboardItems: List<AppLeaderboardItem> = emptyList(),
    onDaySelected: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager.getInstance(context) }
    var customAlertInput by remember { mutableStateOf(sessionManager.getCustomDataAlertMb().toString()) }

    val timelineState = rememberLazyListState()

    LaunchedEffect(timelineBars.size) {
        if (timelineBars.isNotEmpty()) {
            val targetIndex = (timelineBars.size - 1).coerceAtLeast(0)
            timelineState.scrollToItem(targetIndex)
        }
    }

    val selectedBar = remember(timelineBars, selectedDayIndex) {
        if (timelineBars.isNotEmpty() && selectedDayIndex in timelineBars.indices) {
            timelineBars[selectedDayIndex]
        } else null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Alerts & Data Timeline",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Manage custom data spend push alerts & inspect 30-day usage history.",
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // CUSTOM DATA SPEND ALERT NOTIFICATION SETTINGS CARD
        RetroTactileCard(
            modifier = Modifier.fillMaxWidth(),
            accentGlow = RetroAmberGold
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Alerts",
                        tint = RetroAmberGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Custom Data Spend Alert Limit",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = RetroAmberGold.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "ACTIVE",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = RetroAmberGold,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Receive high-priority system push notifications whenever your mobile session data reaches this custom limit:",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = customAlertInput,
                    onValueChange = { newValue -> customAlertInput = newValue.filter { it.isDigit() } },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("e.g. 500") },
                    suffix = { Text("MB", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RetroAmberGold)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RetroAmberGold,
                        unfocusedBorderColor = RetroBorderMetallic
                    )
                )

                Button(
                    onClick = {
                        val mb = customAlertInput.toIntOrNull() ?: 500
                        sessionManager.saveCustomDataAlertMb(mb)
                        Toast.makeText(context, "Data spend alert threshold set to $mb MB!", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RetroAmberGold),
                    modifier = Modifier.height(52.dp)
                ) {
                    Text("Save Alert", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 30-DAY MONTHLY TIMELINE SCROLLER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "30-Day Usage Scroller",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Calendar", tint = NeonEmeraldGlow, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            state = timelineState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(timelineBars) { idx, bar ->
                val isSelected = idx == selectedDayIndex
                Box(
                    modifier = Modifier
                        .width(76.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) NeonEmeraldGlow.copy(alpha = 0.15f) else RetroCardSurface)
                        .border(
                            1.5.dp,
                            if (isSelected) NeonEmeraldGlow else RetroBorderMetallic,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onDaySelected(idx) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = bar.dayLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) NeonEmeraldGlow else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = bar.dateLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${bar.totalGb} GB",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) NeonEmeraldGlow else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // DAY BREAKDOWN USAGE DETAILS
        if (selectedBar != null) {
            RetroTactileCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Usage Breakdown for ${selectedBar.dayLabel}, ${selectedBar.dateLabel}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Wi-Fi Transferred:", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Text("${selectedBar.wifiGb} GB", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = SkyBlueWifi))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mobile Data Transferred:", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Text("${selectedBar.mobileGb} GB", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF10B981)))
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}
