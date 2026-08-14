package com.example.plandee.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.plandee.ui.theme.*

@Composable
fun RetroTactileCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderColor: Color = RetroBorderMetallic,
    accentGlow: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val cardBrush = Brush.verticalGradient(
        colors = listOf(
            RetroCardSurfaceElevated,
            RetroCardSurface
        )
    )

    Surface(
        modifier = modifier
            .clip(shape)
            .border(
                BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            RetroBevelHighlight,
                            borderColor
                        )
                    )
                ),
                shape = shape
            ),
        shape = shape,
        color = Color.Transparent,
        shadowElevation = 6.dp,
        tonalElevation = 6.dp,
        onClick = onClick ?: {}
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBrush)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                if (accentGlow != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(accentGlow)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                content()
            }
        }
    }
}
