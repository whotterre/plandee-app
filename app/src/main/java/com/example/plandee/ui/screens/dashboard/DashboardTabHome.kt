package com.example.plandee.ui.screens.dashboard

import android.widget.Toast
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Whatshot
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
import com.example.plandee.data.repository.DailyConsumptionBar
import com.example.plandee.data.repository.MonthlyTimelineBar
import com.example.plandee.data.repository.TrafficSummary
import com.example.plandee.data.security.SessionManager
import com.example.plandee.ui.components.RetroTactileCard
import com.example.plandee.ui.theme.*

val MobileEmeraldGreen = Color(0xFF10B981) // Emerald Green matching user image

@Composable
fun DashboardTabHome(
    summary: TrafficSummary?,
    dailyBars: List<DailyConsumptionBar> = emptyList(),
    timelineBars: List<MonthlyTimelineBar> = emptyList(),
    selectedDayIndex: Int = 29,
    leaderboardItems: List<AppLeaderboardItem> = emptyList(),
    onDaySelected: (Int) -> Unit = {},
    onNavigateToAuditor: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager.getInstance(context) }

    val totalGbText = remember(summary) { summary?.totalGb?.toString() ?: "0.0" }
    val wifiPercent = remember(summary) { summary?.wifiPercent ?: 50 }
    val mobilePercent = remember(summary) { summary?.mobilePercent ?: 50 }
    val wifiGbText = remember(summary) { summary?.wifiGb?.toString() ?: "0.0" }
    val mobileGbText = remember(summary) { summary?.mobileGb?.toString() ?: "0.0" }
    val dailyBurnText = remember(summary) { summary?.avgDailyBurnGb?.toString() ?: "0.0" }
    val peakWindowText = remember(summary) { summary?.peakWindow ?: "Night Owl (11PM-6AM)" }

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
        Spacer(modifier = Modifier.height(12.dp))

        // RETRO TACTILE DONUT CHART CARD - NETWORK TELEMETRY
        RetroTactileCard(
            modifier = Modifier.fillMaxWidth(),
            accentGlow = NeonEmeraldGlow
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(NeonEmeraldGlow)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TODAY'S NETWORK TELEMETRY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonEmeraldGlow,
                            letterSpacing = 1.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier.size(170.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val strokeWidth = 24.dp
                    val wifiSweep = (wifiPercent.toFloat() / 100f) * 360f
                    val mobileSweep = (mobilePercent.toFloat() / 100f) * 360f

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = SkyBlueWifi,
                            startAngle = -90f,
                            sweepAngle = wifiSweep,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth.toPx())
                        )
                        drawArc(
                            color = MobileEmeraldGreen,
                            startAngle = -90f + wifiSweep,
                            sweepAngle = mobileSweep,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth.toPx())
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = totalGbText,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 32.sp
                            )
                        )
                        Text(
                            text = "GB TRANSFERRED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LegendItem(color = SkyBlueWifi, label = "Wi-Fi ($wifiPercent%)", value = "$wifiGbText GB")
                    LegendItem(color = MobileEmeraldGreen, label = "Mobile Data ($mobilePercent%)", value = "$mobileGbText GB")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 7-DAY DAILY CONSUMPTION STACKED BAR CHART
        RetroTactileCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "7-Day Daily Consumption",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SkyBlueWifi))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Wi-Fi", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MobileEmeraldGreen))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Mobile Data", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val maxBarGb = remember(dailyBars) {
                val maxVal = dailyBars.maxOfOrNull { it.wifiGb + it.mobileGb } ?: 1.0f
                if (maxVal <= 0.1f) 1.0f else maxVal
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                dailyBars.forEach { bar ->
                    val wifiHeightPct = (bar.wifiGb / maxBarGb).coerceIn(0f, 1f)
                    val mobileHeightPct = (bar.mobileGb / maxBarGb).coerceIn(0f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .width(18.dp)
                                .fillMaxHeight(0.85f),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            if (bar.wifiGb > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(wifiHeightPct.coerceAtLeast(0.05f))
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(SkyBlueWifi)
                                )
                            }
                            if (bar.mobileGb > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(mobileHeightPct.coerceAtLeast(0.05f))
                                        .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                                        .background(MobileEmeraldGreen)
                                )
                            }
                            if (bar.wifiGb <= 0 && bar.mobileGb <= 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(RetroBorderMetallic)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = bar.day,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }

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
                        modifier = Modifier.size(20.dp)
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

            Spacer(modifier = Modifier.height(12.dp))

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

        Spacer(modifier = Modifier.height(20.dp))

        // 30-DAY MONTHLY TIMELINE SCROLLER
        Text(
            text = "30-Day Monthly Timeline",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            state = timelineState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(timelineBars) { idx, bar ->
                val isSelected = idx == selectedDayIndex
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) NeonEmeraldGlow.copy(alpha = 0.15f) else RetroCardSurface)
                        .border(
                            1.5.dp,
                            if (isSelected) NeonEmeraldGlow else RetroBorderMetallic,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onDaySelected(idx) }
                        .padding(vertical = 10.dp),
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

        Spacer(modifier = Modifier.height(20.dp))

        // METRICS ROW: DAILY BURN & PEAK USAGE WINDOW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Whatshot,
                iconColor = NeonRoseAccent,
                label = "30-Day Daily Burn",
                value = "$dailyBurnText GB/day"
            )

            MetricTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Nightlight,
                iconColor = RetroAmberGold,
                label = "Peak Usage Window",
                value = peakWindowText
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun LegendItem(color: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
private fun MetricTile(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    value: String
) {
    RetroTactileCard(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp
            )
        )
    }
}
