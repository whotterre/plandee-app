package com.example.plandee.ui.screens.dashboard

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plandee.data.repository.AppLeaderboardItem
import com.example.plandee.data.repository.DailyConsumptionBar
import com.example.plandee.data.repository.MonthlyTimelineBar
import com.example.plandee.data.repository.TrafficSummary
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
    val totalGbText = remember(summary) { summary?.totalGb?.toString() ?: "0.0" }
    val wifiPercent = remember(summary) { summary?.wifiPercent ?: 50 }
    val mobilePercent = remember(summary) { summary?.mobilePercent ?: 50 }
    val wifiGbText = remember(summary) { summary?.wifiGb?.toString() ?: "0.0" }
    val mobileGbText = remember(summary) { summary?.mobileGb?.toString() ?: "0.0" }
    val dailyBurnText = remember(summary) { summary?.avgDailyBurnGb?.toString() ?: "0.0" }
    val peakWindowText = remember(summary) { summary?.peakWindow ?: "Night Owl (11PM-6AM)" }

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
                    modifier = Modifier.size(190.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val wifiSweep = remember(wifiPercent) { (wifiPercent.toFloat() / 100f) * 360f }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = DeeSkyBlue,
                            startAngle = -90f,
                            sweepAngle = wifiSweep,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 24.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                        drawArc(
                            color = MobileEmeraldGreen,
                            startAngle = -90f + wifiSweep,
                            sweepAngle = 360f - wifiSweep,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 24.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = totalGbText,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 34.sp
                            )
                        )
                        Text(
                            text = "GB TOTAL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Chart Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    LegendItem(
                        color = DeeSkyBlue,
                        label = "Wi-Fi ($wifiPercent%)",
                        value = "$wifiGbText GB"
                    )

                    LegendItem(
                        color = MobileEmeraldGreen,
                        label = "Mobile Data ($mobilePercent%)",
                        value = "$mobileGbText GB"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 7-DAY DAILY CONSUMPTION STACKED HISTOGRAM CHART (MATCHING USER SCREENSHOT SPEC)
        RetroTactileCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Daily Consumption",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Combined network traffic over the last 7 days",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(DeeSkyBlue))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Wi-Fi", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp))
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MobileEmeraldGreen))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mobile Data", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val maxDailyGb = remember(dailyBars) {
                dailyBars.maxOfOrNull { it.wifiGb + it.mobileGb }?.coerceAtLeast(0.5f) ?: 1.0f
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                dailyBars.forEach { bar ->
                    val totalGb = bar.wifiGb + bar.mobileGb
                    val heightRatio = (totalGb / maxDailyGb).coerceIn(0.15f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(36.dp)
                    ) {
                        // Stacked Bar (20% narrower width = 18.dp)
                        Column(
                            modifier = Modifier
                                .height(100.dp * heightRatio)
                                .width(18.dp)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        ) {
                            // TOP PORTION: Mobile Data (Emerald Green #10B981)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(bar.mobileGb.coerceAtLeast(0.01f))
                                    .background(MobileEmeraldGreen)
                            )
                            // BOTTOM PORTION: Wi-Fi Data (Sky Blue #38BDF8)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(bar.wifiGb.coerceAtLeast(0.01f))
                                    .background(DeeSkyBlue)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = bar.day,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = DeeSkyBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 30-DAY HORIZONTALLY SCROLLABLE MONTHLY TIMELINE GRAPH
        RetroTactileCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Timeline (30 Days)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (selectedBar != null) "Selected: ${selectedBar.dateLabel} (${selectedBar.dayLabel})" else "Tap any day to view rankings",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NeonEmeraldGlow,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NeonEmeraldGlow.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "30 DAYS",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonEmeraldGlow,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val maxGb = remember(timelineBars) {
                timelineBars.maxOfOrNull { it.wifiGb + it.mobileGb }?.coerceAtLeast(1.5f) ?: 2f
            }

            LazyRow(
                state = timelineState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                itemsIndexed(timelineBars) { index, bar ->
                    val isSelected = index == selectedDayIndex
                    val totalBarGb = bar.wifiGb + bar.mobileGb
                    val totalHeightRatio = (totalBarGb / maxGb).coerceIn(0.12f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) NeonEmeraldGlow.copy(alpha = 0.2f) else Color.Transparent)
                            .border(
                                1.5.dp,
                                if (isSelected) NeonEmeraldGlow else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onDaySelected(index) }
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .height(100.dp * totalHeightRatio)
                                .width(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(bar.mobileGb.coerceAtLeast(0.1f))
                                    .background(MobileEmeraldGreen)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(bar.wifiGb.coerceAtLeast(0.1f))
                                    .background(DeeSkyBlue)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = bar.dateLabel.take(2),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) NeonEmeraldGlow else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // METRICS TILES
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
