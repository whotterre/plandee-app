package com.example.plandee.ui.screens.dashboard

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.plandee.data.security.SessionManager
import com.example.plandee.ui.components.RetroTactileCard
import com.example.plandee.ui.theme.*

@Composable
fun DashboardTabAlerts() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager.getInstance(context) }
    var customAlertInput by remember { mutableStateOf(sessionManager.getCustomDataAlertMb().toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Data Spend Alerts",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Set custom thresholds to receive high-priority push notifications when data is used.",
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // CUSTOM DATA SPEND ALERT NOTIFICATION SETTINGS CARD (NO ACTIVE BADGE)
        RetroTactileCard(
            modifier = Modifier.fillMaxWidth(),
            accentGlow = RetroAmberGold
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Alerts",
                    tint = RetroAmberGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Custom Data Limit Alert",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Receive high-priority system push notifications whenever your mobile session data reaches this custom limit:",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )

            Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(28.dp))
    }
}
