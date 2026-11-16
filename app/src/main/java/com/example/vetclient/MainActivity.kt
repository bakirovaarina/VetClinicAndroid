package com.example.vetclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vetclient.ui.ForgotPasswordScreen
import com.example.vetclient.ui.LoginScreen
import com.example.vetclient.ui.MainScreen
import com.example.vetclient.ui.RegisterScreen
import com.example.vetclient.ui.WelcomeScreen
import com.example.vetclient.ui.theme.VetClientTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VetClientTheme {
                MaterialTheme {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "welcome") {

                        composable("welcome") {
                            WelcomeScreen(
                                onNavigateToRegister = { navController.navigate("register") },
                                onNavigateToLogin = { navController.navigate("login") }
                            )
                        }

                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = { name, id ->
                                    navController.navigate("main/$name/$id") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = { navController.navigate("register") },
                                onNavigateToForgot = { navController.navigate("forgot_password") }
                            )
                        }

                        composable("forgot_password") {
                            ForgotPasswordScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("register") {
                            RegisterScreen(
                                onNavigateToLogin = { navController.navigate("login") }
                            )
                        }

                        composable("main/{userName}/{userId}") { backStackEntry ->
                            val userName = backStackEntry.arguments?.getString("userName") ?: "Гость"
                            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 1
                            MainScreen(
                                userName = userName,
                                userId = userId,
                                onLogout = {
                                    navController.navigate("welcome") {
                                        popUpTo("main/{userName}/{userId}") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}