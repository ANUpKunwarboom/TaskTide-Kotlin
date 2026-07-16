package com.example.todolistapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolistapp.ui.theme.*
import kotlinx.coroutines.delay

enum class PomoMode(val label: String, val minutes: Int, val color: Color) {
    FOCUS("Focus", 25, Purple),
    SHORT_BREAK("Short Break", 5, Green),
    LONG_BREAK("Long Break", 15, Yellow)
}

@Composable
fun PomodoroCard(
    sessionsToday: Int,
    onSessionComplete: () -> Unit
) {
    var mode by remember { mutableStateOf(PomoMode.FOCUS) }
    var totalSeconds by remember { mutableStateOf(mode.minutes * 60) }
    var secondsLeft by remember { mutableStateOf(totalSeconds) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning, secondsLeft) {
        if (isRunning && secondsLeft > 0) {
            delay(1000)
            secondsLeft -= 1
            if (secondsLeft == 0) {
                isRunning = false
                if (mode == PomoMode.FOCUS) onSessionComplete()
            }
        }
    }

    fun switchMode(newMode: PomoMode) {
        mode = newMode
        totalSeconds = newMode.minutes * 60
        secondsLeft = totalSeconds
        isRunning = false
    }

    fun reset() {
        secondsLeft = totalSeconds
        isRunning = false
    }

    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    val progress = if (totalSeconds > 0) (totalSeconds - secondsLeft).toFloat() / totalSeconds else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "%02d:%02d".format(minutes, seconds),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isRunning) mode.color else TextPrimary,
                    letterSpacing = (-1).sp
                )
                Text(
                    mode.label.uppercase(),
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .width(110.dp)
                        .height(3.dp)
                        .background(BorderColor, RoundedCornerShape(2.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(
                                Brush.horizontalGradient(listOf(Purple, PurpleLight)),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { isRunning = !isRunning },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) Yellow.copy(alpha = 0.15f) else Green.copy(alpha = 0.15f),
                            contentColor = if (isRunning) Yellow else Green
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isRunning) "Pause" else "Start", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = { reset() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        border = BorderStroke(1.dp, BorderColor),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Sessions today: $sessionsToday", fontSize = 12.sp, color = TextMuted)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PomoMode.values().forEach { m ->
                PomoModeChip(
                    label = m.label,
                    selected = mode == m,
                    color = m.color,
                    onClick = { switchMode(m) }
                )
            }
        }
    }
}

@Composable
fun PomoModeChip(label: String, selected: Boolean, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (selected) color.copy(alpha = 0.18f) else BgDark,
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            fontSize = 11.sp,
            color = if (selected) color else TextMuted,
            fontWeight = FontWeight.Medium
        )
    }
}