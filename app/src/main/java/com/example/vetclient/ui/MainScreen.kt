package com.example.vetclient.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Главная", Icons.Default.Home)
    object Pets : BottomNavItem("pets", "Питомцы", Icons.Default.Pets)
    object Appointments : BottomNavItem("appointments", "Запись", Icons.Default.DateRange)
    object Shop : BottomNavItem("shop", "Магазин", Icons.Default.ShoppingCart)
    object Profile : BottomNavItem("profile", "Профиль", Icons.Default.Person)
}

@Composable
fun MainScreen(userName: String, userId: Int, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Pets,
        BottomNavItem.Appointments,
        BottomNavItem.Shop,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            if (item.route == BottomNavItem.Home.route) {
                                navController.navigate(BottomNavItem.Home.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            } else {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VetGreen,
                            selectedTextColor = VetGreen,
                            indicatorColor = VetGreen.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    userName = userName,
                    userId = userId,
                    onNavigateToBooking = { category ->
                        navController.navigate("appointments_with_category/${category ?: ""}")
                    },
                    onNavigateToPets = {
                        navController.navigate(BottomNavItem.Pets.route)
                    },
                    onNavigateToShop = {
                        navController.navigate(BottomNavItem.Shop.route)
                    },
                    onNavigateToHelp = {
                        navController.navigate("help_home")
                    },
                    onNavigateToNotifications = {
                        navController.navigate("notifications")
                    }
                )
            }

            composable(BottomNavItem.Pets.route) {
                PetsScreen(
                    userId = userId,
                    onNavigateToAdd = { navController.navigate("pets_add") },
                    onNavigateToEdit = { pet ->
                        navController.navigate("pets_edit/${pet.id}")
                    },
                    onNavigateToHistory = { pet ->
                        navController.navigate("pets_history/${pet.id}")
                    }
                )
            }

            composable("pets_add") {
                AddPetScreen(
                    userId = userId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("pets_edit/{petId}") { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId")?.toIntOrNull() ?: 0
                val petsViewModel: PetsViewModel = viewModel()

                LaunchedEffect(userId) {
                    petsViewModel.loadPets(userId)
                }

                val petToEdit = petsViewModel.pets.find { it.id == petId }

                if (petsViewModel.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (petToEdit != null) {
                    AddPetScreen(
                        userId = userId,
                        petToEdit = petToEdit,
                        onNavigateBack = { navController.popBackStack() }
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Питомец не найден")
                    }
                }
            }

            composable("pets_history/{petId}") { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId")?.toIntOrNull() ?: 0
                HistoryScreen(
                    petId = petId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(BottomNavItem.Appointments.route) {
                val petsViewModel: PetsViewModel = viewModel()
                LaunchedEffect(userId) {
                    petsViewModel.loadPets(userId)
                }
                if (petsViewModel.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else {
                    BookingScreen(
                        userId = userId,
                        pets = petsViewModel.pets,
                        initialCategory = null,
                        onBookingSuccess = {
                            navController.navigate(BottomNavItem.Home.route) {
                                popUpTo(BottomNavItem.Home.route) { inclusive = true }
                            }
                        },
                        onNavigateBack = {
                            navController.navigate(BottomNavItem.Home.route) {
                                popUpTo(BottomNavItem.Home.route) { inclusive = true }
                            }
                        }
                    )
                }
            }

            composable("appointments_with_category/{category}") { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category")?.ifBlank { null }
                val petsViewModel: PetsViewModel = viewModel()
                LaunchedEffect(userId) {
                    petsViewModel.loadPets(userId)
                }
                if (petsViewModel.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else {
                    BookingScreen(
                        userId = userId,
                        pets = petsViewModel.pets,
                        initialCategory = category,
                        onBookingSuccess = {
                            navController.navigate(BottomNavItem.Home.route) {
                                popUpTo(BottomNavItem.Home.route) { inclusive = true }
                            }
                        },
                        onNavigateBack = {
                            navController.navigate(BottomNavItem.Home.route) {
                                popUpTo(BottomNavItem.Home.route) { inclusive = true }
                            }
                        }
                    )
                }
            }

            composable(BottomNavItem.Shop.route) {
                ShopScreen(userId = userId)
            }

            composable(BottomNavItem.Profile.route) {
                ProfileScreen(userId = userId, onLogout = onLogout)
            }

            composable("help_home") {
                HelpScreen(
                    userId = userId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("notifications") {
                NotificationsScreen(
                    userId = userId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}