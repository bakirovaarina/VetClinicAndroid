package com.example.vetclient.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vetclient.network.*
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    var profile by mutableStateOf<UserProfile?>(null)
    var isLoading by mutableStateOf(false)

    fun loadProfile(userId: Int) {
        isLoading = true
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getProfile(userId)
                if (response.isSuccessful) {
                    profile = response.body()
                }
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }
}

@Composable
fun ProfileScreen(userId: Int, viewModel: ProfileViewModel = viewModel()) {
    val profileNavController = rememberNavController()

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
    }

    NavHost(navController = profileNavController, startDestination = "profile_main") {
        composable("profile_main") {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                Text("Профиль", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(24.dp))

                if (viewModel.isLoading) {
                    CircularProgressIndicator(color = VetGreen)
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = VetGreen, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(viewModel.profile?.full_name ?: "Загрузка...", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Text(viewModel.profile?.email ?: "", fontSize = 14.sp, color = Color.Gray)
                                    Text(viewModel.profile?.phone ?: "", fontSize = 14.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { profileNavController.navigate("help") },
                        colors = CardDefaults.cardColors(containerColor = VetGreen)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Помощь бездомным животным", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        composable("help") {
            HelpScreen(
                userId = userId,
                onNavigateBack = { profileNavController.popBackStack() }
            )
        }
    }
}