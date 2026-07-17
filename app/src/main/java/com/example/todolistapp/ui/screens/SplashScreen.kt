package com.example.todolistapp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolistapp.ui.theme.*
import kotlinx.coroutines.delay

/**
 * SplashScreen provides the entry animation for the application.
 * It displays the app logo and tagline before navigating to the main content.
 */
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.6f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "alpha"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(2500) // Increased delay for better visibility
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BgDark, Color(0xFF120B2E), BgDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(
                        Brush.linearGradient(listOf(Purple, PurpleLight)),
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("✦", fontSize = 42.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "TaskTide",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                modifier = Modifier.alpha(alpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Organize. Focus. Achieve.",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha)
            )
        }
    }
}
