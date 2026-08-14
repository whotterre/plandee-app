package com.example.plandee.ui.screens.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plandee.R
import com.example.plandee.data.repository.DailyConsumptionBar
import com.example.plandee.data.repository.TrafficSummary
import com.example.plandee.ui.components.RetroTactileCard
import com.example.plandee.ui.theme.*

@Composable
fun DashboardTabHome(
    summary: TrafficSummary?,
    dailyBars: List<DailyConsumptionBar> = emptyList(),
    onNavigateToAuditor: () -> Unit
) {
    val totalGbText = remember(summary) { summary?.totalGb?.toString() ?: "0.0" }
    val wifiPercent = remember(summary) { summary?.wifiPercent ?: 50 }
    val mobilePercent = remember(summary) { summary?.mobilePercent ?: 50 }
    val wifiGbText = remember(summary) { summary?.wifiGb?.toString() ?: "0.0" }
    val mobileGbText = remember(summary) { summary?.mobileGb?.toString() ?: "0.0" }
    val dailyBurnText = remember(summary) { summary?.avgDailyBurnGb?.toString() ?: "0.0" }
    val peakWindowText = remember(summary) { summary?.peakWindow ?: "Night Owl" }

    val bars = if (dailyBars.isNotEmpty()) dailyBars else remember {
        listOf(
            DailyConsumptionBar("Mon", 1.2f, 0.4f),
            DailyConsumptionBar("Tue", 1.8f, 0.6f),
            DailyConsumptionBar("Wed", 1.5f, 0.5f),
            DailyConsumptionBar("Thu", 2.1f, 0.8f),
            DailyConsumptionBar("Fri", 2.4f, 0.9f),
            DailyConsumptionBar("Sat", 1.4f, 0.6f),
            DailyConsumptionBar("Sun", 0.8f, 0.4f)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // RETRO TACTILE DONUT CHART CARD - TODAY'S USAGE
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
                            color = NeonCyanGlow,
                            startAngle = -90f,
                            sweepAngle = wifiSweep,
                            useCenter = false,
                            style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = NeonRoseAccent,
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
                        color = NeonCyanGlow,
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

        // RETRO TACTILE DAILY STACKED BAR CHART
        RetroTactileCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Traffic Breakdown",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "7 DAYS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NeonEmeraldGlow,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val maxGb = remember(bars) { bars.maxOfOrNull { it.wifiGb + it.mobileGb }?.coerceAtLeast(2f) ?: 3f }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                bars.forEach { bar ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        val totalBarGb = bar.wifiGb + bar.mobileGb
                        val totalHeightRatio = (totalBarGb / maxGb).coerceIn(0.1f, 1f)

                        Column(
                            modifier = Modifier
                                .fillMaxHeight(totalHeightRatio)
                                .width(14.dp)
                                .clip(RoundedCornerShape(6.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(bar.wifiGb.coerceAtLeast(0.1f))
                                    .background(NeonCyanGlow)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(bar.mobileGb.coerceAtLeast(0.1f))
                                    .background(NeonRoseAccent)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = bar.day,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
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
                iconColor = Color(0xFF8B5CF6),
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
