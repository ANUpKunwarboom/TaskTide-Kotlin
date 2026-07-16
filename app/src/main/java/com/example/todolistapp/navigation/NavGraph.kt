package com.example.todolistapp.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.todolistapp.ui.screens.*
import com.example.todolistapp.viewmodel.AuthViewModel
import com.example.todolistapp.viewmodel.NoteViewModel
import com.example.todolistapp.viewmodel.TaskViewModel
import com.google.firebase.auth.FirebaseAuth

object Routes {
    const val SPLASH          = "splash"
    const val LOGIN           = "login"
    const val REGISTER        = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val DASHBOARD       = "dashboard"
    const val PROFILE         = "profile"
    const val NOTES           = "notes"
}

@Composable
fun NavGraph() {
    val navController  = rememberNavController()
    val authViewModel  : AuthViewModel = viewModel()
    val taskViewModel  : TaskViewModel = viewModel()
    val noteViewModel  : NoteViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen(
                onTimeout = {
                    val dest = if (FirebaseAuth.getInstance().currentUser != null)
                        Routes.DASHBOARD else Routes.LOGIN
                    navController.navigate(dest) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel             = authViewModel,
                onLoginSuccess        = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister      = { navController.navigate(Routes.REGISTER) },
                onNavigateToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                viewModel         = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onBack    = { navController.popBackStack() }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                authViewModel = authViewModel,
                taskViewModel = taskViewModel,
                onLogout      = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                },
                onOpenProfile = { navController.navigate(Routes.PROFILE) },
                onOpenNotes   = { navController.navigate(Routes.NOTES) }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                authViewModel = authViewModel,
                onBack        = { navController.popBackStack() },
                onLogout      = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.PROFILE) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.NOTES) {
            NotesScreen(
                viewModel = noteViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}