package com.example.vetclient.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.example.vetclient.network.ApiClient
import com.example.vetclient.network.Product
import kotlinx.coroutines.launch

class ShopViewModel : ViewModel() {
    var products by mutableStateOf<List<Product>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    var cart by mutableStateOf<Map<Product, Int>>(emptyMap())

    fun loadProducts() {
        isLoading = true
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getProducts()
                if (response.isSuccessful) {
                    products = response.body() ?: emptyList()
                } else {
                    errorMessage = "Ошибка загрузки"
                }
            } catch (e: Exception) {
                errorMessage = "Нет связи с сервером"
            } finally {
                isLoading = false
            }
        }
    }

    fun addToCart(product: Product) {
        val currentQty = cart[product] ?: 0
        cart = cart + (product to currentQty + 1)
    }

    fun getCartTotal(): Double {
        return cart.entries.sumOf { it.key.price.toDouble() * it.value }
    }

    fun getCartItemCount(): Int {
        return cart.values.sum()
    }
}

@Composable
fun ShopScreen(viewModel: ShopViewModel = viewModel()) {
    LaunchedEffect(Unit) {
        viewModel.loadProducts()
    }

    Scaffold(
        floatingActionButton = {
            if (viewModel.getCartItemCount() > 0) {
                ExtendedFloatingActionButton(
                    onClick = { /* Переход в корзину */ },
                    containerColor = VetGreen,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Корзина (${viewModel.getCartItemCount()})")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Магазин", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VetGreen)
                }
            } else if (viewModel.errorMessage.isNotEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(viewModel.errorMessage, color = Color.Red)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(viewModel.products) { product ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(product.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("${product.category} • Остаток: ${product.stock_qty} шт.", fontSize = 12.sp, color = Color.Gray)
                                    Text("${product.price} руб.", color = VetGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                                IconButton(
                                    onClick = { viewModel.addToCart(product) },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = VetGreen.copy(alpha = 0.1f))
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Добавить в корзину", tint = VetGreen)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}