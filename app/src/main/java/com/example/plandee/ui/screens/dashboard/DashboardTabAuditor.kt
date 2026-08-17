package com.example.plandee.ui.screens.dashboard

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Psychology
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
import com.example.plandee.data.monetization.RewardedAdManager
import com.example.plandee.data.network.MatchedPlanResultDto
import com.example.plandee.data.network.MatchRecommendationResponse
import com.example.plandee.ui.components.RetroTactileCard
import com.example.plandee.ui.components.VtuPurchaseDialog
import com.example.plandee.ui.theme.*
import java.text.DecimalFormat

@Composable
fun DashboardTabAuditor(
    isPro: Boolean = false,
    tokens: Int = 2,
    mlRecommendation: MatchRecommendationResponse? = null,
    onRunMLMatch: (String, Double, String) -> Unit = { _, _, _ -> },
    onTokenConsumed: () -> Unit = {},
    onRewardAdWatched: () -> Unit = {},
    onNavigateToPro: () -> Unit = {}
) {
    val context = LocalContext.current
    val df = remember { DecimalFormat("#,###") }

    val carriers = listOf("MTN", "Airtel", "Glo", "9mobile")
    val durations = listOf("Weekly", "Monthly", "Daily", "Night")

    var selectedCarrier by remember { mutableStateOf("MTN") }
    var selectedDuration by remember { mutableStateOf("Weekly") }

    var budgetInputText by remember { mutableStateOf("1000") }
    var budgetValidationError by remember { mutableStateOf<String?>(null) }
    var showZeroTokenDialog by remember { mutableStateOf(false) }

    // VTU RECHARGE MODAL SELECTION STATE
    var selectedVtuPlan by remember { mutableStateOf<MatchedPlanResultDto?>(null) }

    fun validateAndGetBudget(): Double? {
        val cleanText = budgetInputText.trim()
        val parsed = cleanText.toDoubleOrNull()
        return when {
            cleanText.isEmpty() -> {
                budgetValidationError = "Budget cannot be empty"
                null
            }
            parsed == null -> {
                budgetValidationError = "Please enter a valid number"
                null
            }
            parsed < 100.0 -> {
                budgetValidationError = "Minimum budget is ₦100"
                null
            }
            parsed > 500000.0 -> {
                budgetValidationError = "Maximum budget is ₦500,000"
                null
            }
            else -> {
                budgetValidationError = null
                parsed
            }
        }
    }

    LaunchedEffect(Unit) {
        val validB = validateAndGetBudget() ?: 1000.0
        onRunMLMatch(selectedCarrier, validB, selectedDuration)
    }

    fun handleRunRecommendation() {
        val validBudget = validateAndGetBudget() ?: return
        if (!isPro && tokens <= 0) {
            showZeroTokenDialog = true
            return
        }
        onRunMLMatch(selectedCarrier, validBudget, selectedDuration)
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
                    text = if (isPro) "Pro Member: Unlimited Access" else "Tokens Available: $tokens",
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
                            validateAndGetBudget()?.let { b ->
                                onRunMLMatch(carrier, b, selectedDuration)
                            }
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            durations.forEach { dur ->
                val isSelected = selectedDuration == dur
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) RetroCardSurfaceElevated else RetroCardSurface)
                        .border(
                            1.5.dp,
                            if (isSelected) NeonEmeraldGlow else RetroBorderMetallic,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            selectedDuration = dur
                            validateAndGetBudget()?.let { b ->
                                onRunMLMatch(selectedCarrier, b, dur)
                            }
                        },
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

        Spacer(modifier = Modifier.height(20.dp))

        // TARGET BUDGET VALIDATED NUMERIC TEXT INPUT BOX
        RetroTactileCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Target Budget (NGN ₦)",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = budgetInputText,
                onValueChange = { newValue ->
                    budgetInputText = newValue.filter { it.isDigit() }
                    validateAndGetBudget()?.let { b ->
                        onRunMLMatch(selectedCarrier, b, selectedDuration)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter target budget e.g. 1000") },
                prefix = {
                    Text(
                        text = "₦ ",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = NeonEmeraldGlow,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = budgetValidationError != null,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonEmeraldGlow,
                    unfocusedBorderColor = RetroBorderMetallic,
                    errorBorderColor = NeonRoseAccent
                )
            )

            if (budgetValidationError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = budgetValidationError!!,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NeonRoseAccent,
                        fontWeight = FontWeight.Bold
                    )
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rule: Enter budget between ₦100 and ₦500,000",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // FORM SUBMIT ACTION BUTTON: RECOMMEND ML MATCH
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
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "AI Recommend",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Find Best Tariff Plan (DP Optimizer)",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // LIVE FEATURED MATCH RESULT CARD WITH VTU & USSD ACTIONS
        if (mlRecommendation?.featuredPlan != null) {
            val plan = mlRecommendation.featuredPlan
            RetroTactileCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonEmeraldGlow,
                accentGlow = NeonEmeraldGlow
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NeonEmeraldGlow.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${plan.matchScorePercentage}% MATCH • ${plan.carrier.uppercase()} ${selectedDuration.uppercase()}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonEmeraldGlow,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Text(
                        text = "₦${df.format(plan.price.toInt())}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonEmeraldGlow
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = plan.planName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Data Allowance: ${plan.dataAllowanceGb} GB • Validity: ${plan.validityDays} Days",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // EXPLANATION SUMMARY
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = RetroCardSurfaceElevated
                ) {
                    Text(
                        text = mlRecommendation.analysisSummary,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SlateTextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // DUAL ACTION BUTTONS: VTU RECHARGE & USSD DIAL
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { selectedVtuPlan = plan },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmeraldGlow)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = "VTU", tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "⚡ Buy (VTU)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        }
                    }

                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, NeonEmeraldGlow)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = "Dial", tint = NeonEmeraldGlow, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = plan.ussdCode, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = NeonEmeraldGlow))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ALTERNATIVE MATCHED PLANS
            if (!mlRecommendation.alternativePlans.isNullOrEmpty()) {
                Text(
                    text = "Alternative Matches for ${selectedCarrier}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                mlRecommendation.alternativePlans.forEach { altPlan ->
                    RetroTactileCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = altPlan.planName,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "${altPlan.dataAllowanceGb} GB • ${altPlan.validityDays} Days • ${altPlan.matchScorePercentage}% Match",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "₦${df.format(altPlan.price.toInt())}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NeonEmeraldGlow
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { selectedVtuPlan = altPlan },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmeraldGlow),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("Buy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }

    // VTU PURCHASE MODAL DIALOG
    selectedVtuPlan?.let { vtuPlan ->
        VtuPurchaseDialog(
            plan = vtuPlan,
            onDismiss = { selectedVtuPlan = null }
        )
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
                                onRewardAdWatched()
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
                            imageVector = Icons.Default.Notifications,
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
