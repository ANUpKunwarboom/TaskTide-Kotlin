package com.example.todolistapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolistapp.ui.theme.*
import com.example.todolistapp.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
/**
 * ForgotPasswordScreen allows users to request a password reset email.
 * It provides a simple interface to enter an email address.
 */
@Composable
fun ForgotPasswordScreen(viewModel: AuthViewModel, onBack: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var loading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDark, Color(0xFF13102B), BgDark)))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp)) {
            Spacer(modifier = Modifier.height(40.dp))
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Reset Password", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
            Text(
                "Enter your email and we'll send you a reset link",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            message?.let { (success, msg) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            (if (success) Green else Red).copy(alpha = 0.1f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(msg, color = if (success) Green else Red, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("you@example.com", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextMuted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    loading = true
                    viewModel.sendPasswordReset(email.trim()) { success, msg ->
                        loading = false
                        message = success to msg
                    }
                },
                enabled = !loading && email.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) {
                if (loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Send Reset Link", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
