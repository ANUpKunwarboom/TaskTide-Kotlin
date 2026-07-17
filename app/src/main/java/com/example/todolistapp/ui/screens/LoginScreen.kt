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
 * LoginScreen provides the user interface for existing users to sign in.
 * It handles email/password input and navigates to registration or password reset.
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()
    val isLoading = authState is AuthState.Loading

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(BgDark, Color(0xFF13102B), BgDark))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        Brush.linearGradient(listOf(Purple, PurpleLight)),
                        RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("✦", fontSize = 28.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Text(
                text = "Sign in to continue to TaskTide",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Error message
            if (authState is AuthState.Error) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Red.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "⚠ ${(authState as AuthState.Error).message}",
                        color = Red,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Email field
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

            // Password field
            Text("PASSWORD", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Your password", color = TextMuted) },
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

            // Forgot password
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = onNavigateToForgotPassword) {
                    Text("Forgot Password?", color = PurpleLight, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Login button
            Button(
                onClick = { viewModel.login(email.trim(), password) },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign up link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Don't have an account? ", color = TextSecondary, fontSize = 14.sp)
                Text(
                    "Sign Up",
                    color = PurpleLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Purple,
    unfocusedBorderColor = BorderColor,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = Purple,
    focusedContainerColor = SurfaceDark,
    unfocusedContainerColor = SurfaceDark
)

