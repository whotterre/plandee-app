package com.example.plandee.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plandee.ui.theme.DeeEmerald

data class NetworkOption(
    val id: String,
    val name: String,
    val brandColor: Color
)

@Composable
fun OnboardingStep1Screen(
    onContinue: (selectedNetworks: List<String>, country: String) -> Unit = { _, _ -> },
    onSkip: () -> Unit = {}
) {
    val networks = remember {
        listOf(
            NetworkOption("mtn", "MTN", Color(0xFFFFCC00)),
            NetworkOption("airtel", "Airtel", Color(0xFFE60000)),
            NetworkOption("glo", "Glo", Color(0xFF009900)),
            NetworkOption("9mobile", "9mobile", Color(0xFF005C2B))
        )
    }

    var selectedNetworkIds by remember { mutableStateOf(setOf("mtn", "airtel")) }
    var selectedCountry by remember { mutableStateOf("🇳🇬 Nigeria") }
    var countryDropdownExpanded by remember { mutableStateOf(false) }

    val countries = listOf("🇳🇬 Nigeria", "🇬🇭 Ghana", "🇰🇪 Kenya", "🇿🇦 South Africa", "🇬🇧 United Kingdom")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STEP 1 OF 2",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                TextButton(onClick = onSkip) {
                    Text(
                        text = "Skip",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "Select Your Primary\nNetworks",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle
            Text(
                text = "We use this to filter carrier-specific data bundles and optimize your telecom analytics.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 2x2 Network Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NetworkCard(
                        network = networks[0],
                        isSelected = selectedNetworkIds.contains(networks[0].id),
                        onClick = {
                            selectedNetworkIds = if (selectedNetworkIds.contains(networks[0].id)) {
                                selectedNetworkIds - networks[0].id
                            } else {
                                selectedNetworkIds + networks[0].id
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    NetworkCard(
                        network = networks[1],
                        isSelected = selectedNetworkIds.contains(networks[1].id),
                        onClick = {
                            selectedNetworkIds = if (selectedNetworkIds.contains(networks[1].id)) {
                                selectedNetworkIds - networks[1].id
                            } else {
                                selectedNetworkIds + networks[1].id
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NetworkCard(
                        network = networks[2],
                        isSelected = selectedNetworkIds.contains(networks[2].id),
                        onClick = {
                            selectedNetworkIds = if (selectedNetworkIds.contains(networks[2].id)) {
                                selectedNetworkIds - networks[2].id
                            } else {
                                selectedNetworkIds + networks[2].id
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    NetworkCard(
                        network = networks[3],
                        isSelected = selectedNetworkIds.contains(networks[3].id),
                        onClick = {
                            selectedNetworkIds = if (selectedNetworkIds.contains(networks[3].id)) {
                                selectedNetworkIds - networks[3].id
                            } else {
                                selectedNetworkIds + networks[3].id
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Region / Country Dropdown
            Text(
                text = "Region / Country",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                        .clickable { countryDropdownExpanded = true }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = selectedCountry,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown Chevron",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = countryDropdownExpanded,
                    onDismissRequest = { countryDropdownExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    countries.forEach { c ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = c,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                selectedCountry = c
                                countryDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Bottom CTA Button
            Button(
                onClick = { onContinue(selectedNetworkIds.toList(), selectedCountry) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(containerColor = DeeEmerald)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Continue Arrow",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun NetworkCard(
    network: NetworkOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(110.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) DeeEmerald else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        // Selection Checkmark Badge
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(DeeEmerald),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Center Content: Brand Logo Circle + Network Name
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(network.brandColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(network.brandColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = network.name.take(1),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = network.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}
