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
import com.example.vetclient.ui.theme.VetClientTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VetClientTheme {
                MaterialTheme {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "login") {

                        // Экран Входа
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = { name ->
                                    navController.navigate("main/$name") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = { navController.navigate("register") },
                                onNavigateToForgot = { navController.navigate("forgot_password") }
                            )
                        }

                        // Экран Восстановления пароля
                        composable("forgot_password") {
                            ForgotPasswordScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // Экран Регистрации
                        composable("register") {
                            RegisterScreen(
                                onNavigateToLogin = { navController.navigate("login") }
                            )
                        }

                        // Главный экран
                        composable("main/{userName}") { backStackEntry ->
                            val userName = backStackEntry.arguments?.getString("userName") ?: "Гость"
                            MainScreen(userName = userName)
                        }
                    }
                }
            }
        }
    }
}

