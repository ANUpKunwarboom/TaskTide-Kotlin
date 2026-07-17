package com.example.todolistapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.todolistapp.navigation.NavGraph
import com.example.todolistapp.ui.theme.TodoListAppTheme

/**
 * SplashActivity serves as the entry point of the TaskTide application.
 * It initializes the theme and sets up the navigation graph.
 */
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodoListAppTheme {
                NavGraph()
            }
        }
    }
}
