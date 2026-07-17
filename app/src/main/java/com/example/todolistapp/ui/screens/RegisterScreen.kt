package com.example.todolistapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolistapp.ui.theme.*
import com.example.todolistapp.viewmodel.AuthState
import com.example.todolistapp.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
/**
 * RegisterScreen allows new users to create an account.
 * It collects user details like name, email, and password.
 */
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()
    val isLoading = authState is AuthState.Loading

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onRegisterSuccess()
            viewModel.resetState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDark, Color(0xFF13102B), BgDark)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Brush.linearGradient(listOf(Purple, PurpleLight)), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("✦", fontSize = 28.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Create Account", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
            Text(
                "Join TaskTide and boost your productivity",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            if (authState is AuthState.Error) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Red.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text("⚠ ${(authState as AuthState.Error).message}", color = Red, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Name
            Text("FULL NAME", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Your full name", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email
            Text("EMAIL ADDRESS", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("you@example.com", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextMuted) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password
            Text("PASSWORD", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("At least 6 characters", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = TextMuted
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = { viewModel.register(name.trim(), email.trim(), password) },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("Already have an account? ", color = TextSecondary, fontSize = 14.sp)
                Text(
                    "Sign In",
                    color = PurpleLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
