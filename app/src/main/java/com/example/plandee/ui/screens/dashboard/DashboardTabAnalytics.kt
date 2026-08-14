package com.example.plandee.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plandee.data.repository.DailyConsumptionBar
import com.example.plandee.data.repository.TrafficSummary
import com.example.plandee.ui.theme.DeeEmerald
import com.example.plandee.ui.theme.DeeSkyBlue
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardTabAnalytics(
    summary: TrafficSummary? = null,
    dailyConsumption: List<DailyConsumptionBar> = emptyList()
) {
    var selectedTimePeriod by remember { mutableStateOf("7 Days") }
    val timePeriods = listOf("7 Days", "14 Days", "30 Days")

    val weekData = if (dailyConsumption.isNotEmpty()) dailyConsumption else remember {
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

    val maxDailyTotal = remember(weekData) {
        weekData.maxOf { it.wifiGb + it.mobileGb }
    }

    val mobileGbStr = summary?.mobileGb?.toString() ?: "4.2"
    val wifiGbStr = summary?.wifiGb?.toString() ?: "11.2"
    val estCostStr = remember(summary) {
        val cost = summary?.mobileCostEstNaira ?: 3100
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        "₦${formatter.format(cost)}"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title Header
        Text(
            text = "Usage Analytics",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Time Filter Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            timePeriods.forEach { period ->
                val isSelected = selectedTimePeriod == period
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) DeeEmerald else MaterialTheme.colorScheme.surfaceContainer)
                        .border(1.dp, if (isSelected) DeeEmerald else MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .clickable { selectedTimePeriod = period }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = period,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // App Filter Dropdown
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All Apps",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Dropdown",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Daily Consumption Stacked Bar Chart Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
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
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Combined network traffic over the last 7 days",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(DeeSkyBlue))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Wi-Fi", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(DeeEmerald))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mobile Data", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Stacked Bars Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weekData.forEach { data ->
                        val total = data.wifiGb + data.mobileGb
                        val barHeightFraction = total / maxDailyTotal

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            // Stacked Bar Container
                            Column(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height((130 * barHeightFraction).dp)
                                    .clip(RoundedCornerShape(6.dp))
                            ) {
                                // Mobile Data segment (top - emerald)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(data.mobileGb)
                                        .background(DeeEmerald)
                                )
                                // Wi-Fi segment (bottom - sky blue)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(data.wifiGb)
                                        .background(DeeSkyBlue)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = data.day,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // MOBILE DATA BURNED CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(DeeEmerald.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiTethering,
                            contentDescription = "Mobile Data",
                            tint = DeeEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "MOBILE DATA BURNED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = mobileGbStr,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GB",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "📉 Estimated Value: $estCostStr",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DeeEmerald,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // WI-FI OFFLOADED CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(DeeSkyBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = "Wi-Fi",
                            tint = DeeSkyBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "WI-FI OFFLOADED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = wifiGbStr,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GB",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}
