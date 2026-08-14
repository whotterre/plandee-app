package com.example.plandee.ui.screens.dashboard

import android.app.Activity
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plandee.data.monetization.ProRepository
import com.example.plandee.ui.components.RetroTactileCard
import com.example.plandee.ui.theme.*

enum class ProPackageType(val title: String, val priceText: String, val badgeText: String?) {
    MONTHLY("Monthly", "₦1,500 / mo", null),
    YEARLY("Yearly", "₦12,000 / yr", "SAVE 33% • BEST VALUE"),
    LIFETIME("Lifetime", "₦30,000 One-time", "UNLIMITED FOREVER")
}

@Composable
fun DashboardTabPro() {
    val context = LocalContext.current
    val proRepository = remember { ProRepository.getInstance(context) }
    val isPro by proRepository.isProState.collectAsState()
    val offerings by proRepository.offeringsState.collectAsState()

    var selectedPackageType by remember { mutableStateOf(ProPackageType.YEARLY) }

    val proFeatures = listOf(
        "Unlimited AI Telecom Bundle Matcher",
        "Automated Off-peak & Night Data Saver",
        "Carrier Tariff Price Drop Alerts",
        "Multi-SIM & Family Line Analytics",
        "Export CSV Telecom Expense Reports"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(NeonEmeraldGlow.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Pro",
                tint = NeonEmeraldGlow,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isPro) "PlanDee Pro Active" else "PlanDee Pro",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isPro) "You have unlocked unlimited AI recommendations and telecom intelligence." else "Unlock automated telecom intelligence & maximum data savings.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        RetroTactileCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            proFeatures.forEach { feat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Check",
                        tint = NeonEmeraldGlow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = feat,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!isPro) {
            Text(
                text = "Choose Subscription Plan",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // REVENUECAT PACKAGE SELECTION CARDS (Monthly, Yearly, Lifetime)
            ProPackageType.entries.forEach { pkgType ->
                val isSelected = selectedPackageType == pkgType
                val rcPkg = when (pkgType) {
                    ProPackageType.MONTHLY -> offerings?.current?.monthly
                    ProPackageType.YEARLY -> offerings?.current?.annual
                    ProPackageType.LIFETIME -> offerings?.current?.lifetime
                }

                val displayPrice = rcPkg?.product?.price?.formatted ?: pkgType.priceText

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) NeonEmeraldGlow.copy(alpha = 0.12f) else RetroCardSurface)
                        .border(
                            1.5.dp,
                            if (isSelected) NeonEmeraldGlow else RetroBorderMetallic,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedPackageType = pkgType }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = pkgType.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                pkgType.badgeText?.let { badge ->
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = NeonEmeraldGlow
                                    ) {
                                        Text(
                                            text = badge,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = displayPrice,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NeonEmeraldGlow
                                )
                            )
                        }

                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedPackageType = pkgType },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonEmeraldGlow)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    (context as? Activity)?.let { activity ->
                        val selectedRcPkg = when (selectedPackageType) {
                            ProPackageType.MONTHLY -> offerings?.current?.monthly
                            ProPackageType.YEARLY -> offerings?.current?.annual
                            ProPackageType.LIFETIME -> offerings?.current?.lifetime
                        }
                        if (selectedRcPkg != null) {
                            proRepository.purchasePackage(activity, selectedRcPkg)
                        } else {
                            proRepository.purchaseProDefault(activity)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonEmeraldGlow)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Upgrade",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Subscribe to PlanDee Pro",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // REVENUECAT CUSTOMER CENTER / RESTORE PURCHASES
            TextButton(
                onClick = {
                    proRepository.restorePurchases()
                }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restore",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Restore Purchases (Customer Center)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = NeonEmeraldGlow.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonEmeraldGlow)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PlanDee Pro Entitlement Active",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonEmeraldGlow
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}
