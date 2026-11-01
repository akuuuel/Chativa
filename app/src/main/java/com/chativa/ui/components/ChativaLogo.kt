package com.chativa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chativa.ui.theme.ChativaBlueSecondary
import com.chativa.ui.theme.ChativaPurplePrimary

@Composable
fun ChativaLogo(
    size: Dp = 80.dp,
    showTitle: Boolean = true,
    titleSize: Int = 28
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(ChativaPurplePrimary, ChativaBlueSecondary)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QuestionAnswer,
                contentDescription = "Logo CHATIVA",
                tint = Color.White,
                modifier = Modifier.size(size * 0.55f)
            )
        }

        if (showTitle) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "CHATIVA",
                fontSize = titleSize.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
            Text(
                text = "Pesan & Panggilan Instan",
                fontSize = (titleSize * 0.45).sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
