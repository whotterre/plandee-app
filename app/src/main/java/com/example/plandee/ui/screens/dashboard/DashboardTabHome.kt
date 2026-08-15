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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plandee.data.repository.AppLeaderboardItem
import com.example.plandee.data.repository.MonthlyTimelineBar
import com.example.plandee.data.repository.TrafficSummary
import com.example.plandee.ui.components.RetroTactileCard
import com.example.plandee.ui.theme.*

@Composable
fun DashboardTabHome(
    summary: TrafficSummary?,
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
    val peakWindowText = remember(summary) { summary?.peakWindow ?: "Night Owl" }

    val timelineState = rememberLazyListState()

    // REQUIREMENT 2: AUTO-SCROLL TIMELINE TO CURRENT DAY (TODAY) ON LOAD
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

        // RETRO TACTILE DONUT CHART CARD - ELECTRIC INDIGO & CYBER TURQUOISE
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
                            color = DeeSkyBlue, // Sky Blue #38BDF8 / #06B6D4
                            startAngle = -90f,
                            sweepAngle = wifiSweep,
                            useCenter = false,
                            style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = NeonRoseAccent, // Mobile Pink #EC4899
                            startAngle = -90f + wifiSweep + 4f,
                            sweepAngle = (360f - wifiSweep - 8f).coerceAtLeast(0f),
                            useCenter = false,
                            style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = totalGbText,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 38.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "GB TODAY",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
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
                        color = NeonRoseAccent,
                        label = "Mobile Data ($mobilePercent%)",
                        value = "$mobileGbText GB"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // REQUIREMENT 2: 30-DAY HORIZONTALLY SCROLLABLE MONTHLY TIMELINE GRAPH WITH STACKED BARS
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

            // HORIZONTALLY SCROLLABLE TIMELINE GRAPH WITH LAZYROW & STACKED BARS
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
                        // STACKED BAR: Wi-Fi Data (Sky Blue #38BDF8) + Mobile Data (Pink #EC4899)
                        Column(
                            modifier = Modifier
                                .height(100.dp * totalHeightRatio)
                                .width(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                        ) {
                            // WI-FI DATA (Sky Blue #38BDF8 / DeeSkyBlue)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(bar.wifiGb.coerceAtLeast(0.1f))
                                    .background(DeeSkyBlue)
                            )
                            // MOBILE DATA (Pink #EC4899 / NeonRoseAccent)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(bar.mobileGb.coerceAtLeast(0.1f))
                                    .background(NeonRoseAccent)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = bar.dateLabel.take(2), // Day number e.g. "14", "15"
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

        // REQUIREMENT 2: DAY-TAP LEADERBOARD LIST DISPLAY FOR SELECTED DAY
        RetroTactileCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedBar != null && selectedBar.isToday) "Today's App Rankings" else "Rankings for ${selectedBar?.dateLabel ?: "Selected Day"}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Text(
                    text = "${leaderboardItems.size} Apps",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NeonEmeraldGlow,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (leaderboardItems.isEmpty()) {
                Text(
                    text = "No app traffic recorded for this specific day.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    leaderboardItems.take(5).forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.rank,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.rank == "#1") NeonEmeraldGlow else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = item.sharePercentText,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NeonCyanGlow,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }

                            Text(
                                text = item.usageGb,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        LinearProgressIndicator(
                            progress = { item.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = if (item.rank == "#1") NeonEmeraldGlow else DeeSkyBlue,
                            trackColor = RetroTactileBg
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // RETRO AI MATCHER CARD
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = NeonEmeraldGlow,
            shadowElevation = 8.dp,
            onClick = onNavigateToAuditor
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Matcher",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "AI Telecom Matcher",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Find optimal data plans tailored to your burn rate",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Open",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
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
                label = "Daily Burn Rate",
                value = "$dailyBurnText GB/day"
            )

            MetricTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Nightlight,
                iconColor = RetroAmberGold,
                label = "Peak Window",
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
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
