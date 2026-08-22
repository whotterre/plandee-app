package com.example.plandee.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plandee.data.network.MatchedPlanResultDto
import com.example.plandee.data.network.RetrofitClient
import com.example.plandee.data.network.VtuPurchaseRequest
import com.example.plandee.ui.theme.*
import kotlinx.coroutines.launch
import java.text.DecimalFormat

@Composable
fun VtuPurchaseDialog(
    plan: MatchedPlanResultDto,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val df = remember { DecimalFormat("#,###") }

    var phoneNumberInput by remember { mutableStateOf("08031234567") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "VTU",
                    tint = NeonEmeraldGlow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Instant VTU Data Recharge")
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Directly purchase ${plan.planName} via VTU Virtual Top-Up Gateway:",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = RetroCardSurfaceElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Network:", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                            Text(plan.carrier.uppercase(), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = NeonEmeraldGlow))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Data Volume:", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                            Text("${plan.dataAllowanceGb} GB", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Price:", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                            Text("₦${df.format(plan.price.toInt())}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NeonEmeraldGlow))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = phoneNumberInput,
                    onValueChange = { newValue -> phoneNumberInput = newValue.filter { it.isDigit() } },
                    label = { Text("Target Mobile Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone", tint = NeonEmeraldGlow) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonEmeraldGlow,
                        unfocusedBorderColor = RetroBorderMetallic
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorMessage!!, style = MaterialTheme.typography.labelSmall.copy(color = NeonRoseAccent, fontWeight = FontWeight.Bold))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (phoneNumberInput.length < 10) {
                        errorMessage = "Enter a valid 11-digit Nigerian mobile number"
                    } else {
                        isLoading = true
                        errorMessage = null

                        scope.launch {
                            try {
                                val apiService = RetrofitClient.getApiService(context)
                                val request = VtuPurchaseRequest(
                                    phoneNumber = phoneNumberInput,
                                    carrier = plan.carrier,
                                    planId = plan.planId,
                                    planName = plan.planName,
                                    priceNgn = plan.price
                                )
                                val response = apiService.purchaseVtuData(request)
                                isLoading = false

                                if (response.isSuccessful && response.body() != null) {
                                    val body = response.body()!!
                                    Toast.makeText(context, body.message, Toast.LENGTH_LONG).show()
                                    onDismiss()
                                } else {
                                    errorMessage = "VTU recharge failed. Please try again."
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = "VTU Gateway Error: ${e.localizedMessage}"
                            }
                        }
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = NeonEmeraldGlow)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Confirm & Recharge Data", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { if (!isLoading) onDismiss() }) {
                Text("Cancel")
            }
        },
        containerColor = RetroCardSurface,
        shape = RoundedCornerShape(20.dp)
    )
}
