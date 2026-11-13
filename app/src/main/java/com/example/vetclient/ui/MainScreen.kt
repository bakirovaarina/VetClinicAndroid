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
fun MainScreen(userName: String, userId: Int) {
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
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
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
                HomeScreen(userName = userName)
            }

            composable(BottomNavItem.Pets.route) {
                val petsViewModel: PetsViewModel = viewModel()
                val petsNavController = rememberNavController()
                NavHost(navController = petsNavController, startDestination = "pets_list") {
                    composable("pets_list") {
                        PetsScreen(
                            userId = userId,
                            onNavigateToAdd = { petsNavController.navigate("pets_add") },
                            onNavigateToEdit = { pet ->
                                petsViewModel.editingPet = pet
                                petsNavController.navigate("pets_edit")
                            },
                            onNavigateToHistory = { pet ->
                                petsNavController.navigate("pets_history/${pet.id}")
                            },
                            viewModel = petsViewModel
                        )
                    }
                    composable("pets_add") {
                        AddPetScreen(
                            userId = userId,
                            onNavigateBack = { petsNavController.popBackStack() }
                        )
                    }
                    composable("pets_edit") {
                        AddPetScreen(
                            userId = userId,
                            petToEdit = petsViewModel.editingPet,
                            onNavigateBack = { petsNavController.popBackStack() }
                        )
                    }
                    composable("pets_history/{petId}") { backStackEntry ->
                        val petId = backStackEntry.arguments?.getString("petId")?.toIntOrNull() ?: 0
                        HistoryScreen(
                            petId = petId,
                            onNavigateBack = { petsNavController.popBackStack() }
                        )
                    }
                }
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
                        onBookingSuccess = {
                            navController.navigate(BottomNavItem.Home.route)
                        }
                    )
                }
            }

            composable(BottomNavItem.Shop.route) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Экран 'Магазин' в разработке")
                }
            }

            composable(BottomNavItem.Profile.route) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Экран 'Профиль' в разработке")
                }
            }
        }
    }
}