package com.example.plandee.ui.screens.dashboard

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plandee.R
import com.example.plandee.data.monetization.RewardedAdManager
import com.example.plandee.ui.components.RetroTactileCard
import com.example.plandee.ui.theme.*
import java.text.DecimalFormat

data class RecommendedBundle(
    val title: String,
    val priceText: String,
    val priceAmount: Int,
    val durationText: String,
    val ussdCode: String,
    val bonusText: String,
    val estimatedSavingsText: String
)

@Composable
fun DashboardTabAuditor(
    isPro: Boolean = false,
    tokens: Int = 2,
    onTokenConsumed: () -> Unit = {},
    onNavigateToPro: () -> Unit = {}
) {
    val context = LocalContext.current
    val df = remember { DecimalFormat("#,###") }

    val carriers = listOf("MTN", "Airtel", "Glo", "9mobile")
    val durations = listOf("Monthly", "Weekly", "Daily", "Night")

    var selectedCarrier by remember { mutableStateOf("MTN") }
    var selectedDuration by remember { mutableStateOf("Monthly") }
    var targetBudget by remember { mutableFloatStateOf(5000f) }
    var showZeroTokenDialog by remember { mutableStateOf(false) }

    val optimalBundle: RecommendedBundle = remember(selectedCarrier, targetBudget, selectedDuration) {
        val budgetInt = targetBudget.toInt()
        when (selectedCarrier) {
            "MTN" -> when {
                selectedDuration == "Daily" || budgetInt <= 1000 -> RecommendedBundle(
                    title = "MTN 1.5GB Daily Flexi",
                    priceText = "₦500",
                    priceAmount = 500,
                    durationText = " / 1 Day",
                    ussdCode = "*312*1#",
                    bonusText = "Includes 500MB Night Stream",
                    estimatedSavingsText = "Saves ₦1,200/mo over pay-as-you-go"
                )
                selectedDuration == "Weekly" || budgetInt <= 3000 -> RecommendedBundle(
                    title = "MTN 7GB Weekly Special",
                    priceText = "₦2,000",
                    priceAmount = 2000,
                    durationText = " / 7 Days",
                    ussdCode = "*312*2#",
                    bonusText = "Anytime Data + 1GB YouTube Bonus",
                    estimatedSavingsText = "Saves ₦2,500/mo over standard plans"
                )
                selectedDuration == "Night" -> RecommendedBundle(
                    title = "MTN 10GB Night Owl Express",
                    priceText = "₦1,500",
                    priceAmount = 1500,
                    durationText = " / 30 Days (11PM - 6AM)",
                    ussdCode = "*312*8#",
                    bonusText = "Unthrottled High Speed Night Data",
                    estimatedSavingsText = "Saves ₦4,500/mo for heavy night users"
                )
                else -> RecommendedBundle(
                    title = "MTN 20GB Monthly Plan",
                    priceText = "₦7,500",
                    priceAmount = 7500,
                    durationText = " / 30 Days",
                    ussdCode = "*312#",
                    bonusText = "Anytime + 5GB Night Bonus",
                    estimatedSavingsText = "Saves ₦5,000/mo compared to daily renewals"
                )
            }
            "Airtel" -> when {
                budgetInt <= 1500 -> RecommendedBundle(
                    title = "Airtel 2GB Binge Data",
                    priceText = "₦500",
                    priceAmount = 500,
                    durationText = " / 1 Day",
                    ussdCode = "*141*500#",
                    bonusText = "Fast 4G Streaming Data",
                    estimatedSavingsText = "Saves ₦900/week"
                )
                selectedDuration == "Weekly" || budgetInt <= 4000 -> RecommendedBundle(
                    title = "Airtel 10GB Super Weekly",
                    priceText = "₦3,000",
                    priceAmount = 3000,
                    durationText = " / 7 Days",
                    ussdCode = "*141*3000#",
                    bonusText = "Includes Free WhatsApp & Socials",
                    estimatedSavingsText = "Saves ₦3,000/mo for social apps"
                )
                else -> RecommendedBundle(
                    title = "Airtel 15GB Monthly Bundle",
                    priceText = "₦6,000",
                    priceAmount = 6000,
                    durationText = " / 30 Days",
                    ussdCode = "*141#",
                    bonusText = "Anytime 4G LTE Data",
                    estimatedSavingsText = "Saves ₦4,200/mo on monthly renewal"
                )
            }
            "Glo" -> when {
                budgetInt <= 1500 -> RecommendedBundle(
                    title = "Glo 2.5GB Special",
                    priceText = "₦500",
                    priceAmount = 500,
                    durationText = " / 2 Days",
                    ussdCode = "*777#",
                    bonusText = "Glo Grandmasters of Data",
                    estimatedSavingsText = "Saves ₦1,500/mo"
                )
                else -> RecommendedBundle(
                    title = "Glo 18GB Mega Data",
                    priceText = "₦5,000",
                    priceAmount = 5000,
                    durationText = " / 30 Days",
                    ussdCode = "*777#",
                    bonusText = "Includes 4GB Night Bonus",
                    estimatedSavingsText = "Saves ₦6,000/mo"
                )
            }
            else -> RecommendedBundle(
                title = "9mobile 11.5GB Smart Data",
                priceText = "₦4,000",
                priceAmount = 4000,
                durationText = " / 30 Days",
                ussdCode = "*200#",
                bonusText = "Includes Free Night Streaming",
                estimatedSavingsText = "Saves ₦3,500/mo"
            )
        }
    }

    fun handleRunRecommendation() {
        if (isPro) return
        if (tokens > 0) {
            onTokenConsumed()
        } else {
            showZeroTokenDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Recommended Plans",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isPro) "⚡ Pro Member: Unlimited Access" else "Tokens Available: $tokens",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isPro) NeonEmeraldGlow else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(RetroCardSurfaceElevated)
                    .border(1.dp, RetroBorderMetallic, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = NeonEmeraldGlow,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // FORM SECTION: CARRIER SELECTOR TABS
        Text(
            text = "Select Network Carrier",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            carriers.forEach { carrier ->
                val isSelected = selectedCarrier == carrier
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) NeonEmeraldGlow else RetroCardSurface)
                        .border(
                            1.5.dp,
                            if (isSelected) NeonEmeraldGlow else RetroBorderMetallic,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            selectedCarrier = carrier
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = carrier,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // DURATION SELECTOR SEGMENT
        Text(
            text = "Plan Duration Preference",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            durations.forEach { dur ->
                val isSelected = selectedDuration == dur
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            selectedDuration = dur
                        },
                    color = if (isSelected) RetroCardSurfaceElevated else RetroCardSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) NeonEmeraldGlow else RetroBorderMetallic
                    )
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dur,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NeonEmeraldGlow else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // TARGET BUDGET SLIDER CARD
        RetroTactileCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Target Budget Input",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "₦${df.format(targetBudget.toInt())}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NeonEmeraldGlow
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = targetBudget,
                onValueChange = {
                    targetBudget = it
                },
                valueRange = 500f..20000f,
                steps = 39,
                colors = SliderDefaults.colors(
                    thumbColor = NeonEmeraldGlow,
                    activeTrackColor = NeonEmeraldGlow,
                    inactiveTrackColor = RetroTactileBg
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "₦500", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                Text(text = "₦20,000", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // FORM SUBMIT ACTION BUTTON: RECOMMEND
        Button(
            onClick = { handleRunRecommendation() },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonEmeraldGlow)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Recommend",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "✨ Recommend Data Plan (-1 Token)",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SAVINGS INSIGHT BANNER
        RetroTactileCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Mascot Logo",
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = "${optimalBundle.estimatedSavingsText} on $selectedCarrier!",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // DYNAMIC OPTIMAL MATCH CARD
        RetroTactileCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = NeonEmeraldGlow,
            accentGlow = NeonEmeraldGlow
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = NeonEmeraldGlow.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(NeonEmeraldGlow)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "OPTIMAL MATCH",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonEmeraldGlow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = optimalBundle.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = optimalBundle.priceText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NeonEmeraldGlow
                    )
                )
                Text(
                    text = optimalBundle.durationText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.OfflineBolt,
                    contentDescription = "Bonus",
                    tint = RetroAmberGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = optimalBundle.bonusText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { handleRunRecommendation() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonEmeraldGlow)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Dial",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dial ${optimalBundle.ussdCode}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }

    // ZERO TOKEN INTERCEPT DIALOG
    if (showZeroTokenDialog) {
        AlertDialog(
            onDismissRequest = { showZeroTokenDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Zero Tokens",
                        tint = RetroAmberGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Out of Recommendation Tokens")
                }
            },
            text = {
                Text("You have 0 tokens remaining. Watch a short rewarded ad to gain +1 token or upgrade to Pro for unlimited recommendations!")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showZeroTokenDialog = false
                        (context as? Activity)?.let { activity ->
                            RewardedAdManager.getInstance(context).showAd(activity) { _ ->
                                onTokenConsumed()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmeraldGlow)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.OndemandVideo,
                            contentDescription = "Watch Ad",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Watch Ad (+1 Token)")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showZeroTokenDialog = false
                        onNavigateToPro()
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Go Pro",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Go Pro (Unlimited)")
                    }
                }
            },
            containerColor = RetroCardSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
