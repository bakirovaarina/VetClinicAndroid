package com.example.vetclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vetclient.ui.DashboardScreen
import com.example.vetclient.ui.ForgotPasswordScreen
import com.example.vetclient.ui.LoginScreen
import com.example.vetclient.ui.RegisterScreen
import com.example.vetclient.ui.theme.VetClientTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VetClientTheme {
                MaterialTheme {
                    // Настраиваем навигацию
                    val navController = rememberNavController()

                    // Начинаем с экрана входа (login)
                    NavHost(navController = navController, startDestination = "login") {

                        // Экран Входа
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = { name ->
                                    navController.navigate("dashboard/$name") {
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

                        // Главный экран (Dashboard)
                        composable("dashboard/{userName}") { backStackEntry ->
                            val userName = backStackEntry.arguments?.getString("userName") ?: "Гость"
                            DashboardScreen(
                                userName = userName,
                                onNavigateToPets = { },
                                onNavigateToAppointments = { },
                                onNavigateToShop = { },
                                onNavigateToServices = { },
                                onNavigateToHistory = { },
                                onNavigateToHelp = { }
                            )
                        }
                    }
                }
            }
        }
    }
}

