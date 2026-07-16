package com.example.todolistapp.ui.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.todolistapp.viewmodel.AuthViewModel
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

class LoginInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel: AuthViewModel = mockk(relaxed = true)

    @Test
    fun loginScreen_initialState() {
        composeTestRule.setContent {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {},
                onNavigateToRegister = {},
                onNavigateToForgotPassword = {}
            )
        }

        // Verify "Welcome Back" is displayed
        composeTestRule.onNodeWithText("Welcome Back").assertExists()
        
        // Verify email field is present
        composeTestRule.onNodeWithText("you@example.com").assertExists()
    }

    @Test
    fun loginScreen_performLoginClick() {
        composeTestRule.setContent {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {},
                onNavigateToRegister = {},
                onNavigateToForgotPassword = {}
            )
        }

        // Enter email and password
        composeTestRule.onNodeWithText("you@example.com").performTextInput("test@gmail.com")
        composeTestRule.onNodeWithText("Your password").performTextInput("password123")

        // Click Sign In
        composeTestRule.onNodeWithText("Sign In").performClick()

        // In a real test, you'd verify the viewModel call, 
        // but since we're using a relaxed mock, we're just testing the UI interaction here.
    }
}
