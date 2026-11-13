package com.example.vetclient.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

class ShopViewModel : ViewModel() {
    var products by mutableStateOf<List<Product>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    var cart by mutableStateOf<Map<Product, Int>>(emptyMap())

    var orderMessage by mutableStateOf("")
    var isOrderSuccess by mutableStateOf(false)
    var lastOrderId by mutableStateOf<Int?>(null)

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

    fun removeFromCart(product: Product) {
        val currentQty = cart[product] ?: 0
        if (currentQty > 1) {
            cart = cart + (product to currentQty - 1)
        } else {
            cart = cart - product
        }
    }

    fun getCartTotal(): Double {
        return cart.entries.sumOf { it.key.price.toDouble() * it.value }
    }

    fun getCartItemCount(): Int {
        return cart.values.sum()
    }

    fun checkout(userId: Int) {
        if (cart.isEmpty()) return

        isLoading = true
        viewModelScope.launch {
            try {
                val items = cart.map { OrderItem(it.key.id, it.value) }
                val response = ApiClient.apiService.createOrder(CreateOrderRequest(userId, items))

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == "success") {
                        orderMessage = body.message
                        lastOrderId = body.order_id
                        isOrderSuccess = true
                        cart = emptyMap()
                        loadProducts()
                    } else {
                        orderMessage = body?.message ?: "Ошибка"
                    }
                } else {
                    orderMessage = "Ошибка сервера"
                }
            } catch (e: Exception) {
                orderMessage = "Нет связи с сервером"
            } finally {
                isLoading = false
            }
        }
    }
}

@Composable
fun ShopScreen(userId: Int, viewModel: ShopViewModel = viewModel()) {
    val shopNavController = rememberNavController()

    LaunchedEffect(Unit) {
        viewModel.loadProducts()
    }

    NavHost(navController = shopNavController, startDestination = "catalog") {
        composable("catalog") {
            Scaffold(
                floatingActionButton = {
                    if (viewModel.getCartItemCount() > 0) {
                        ExtendedFloatingActionButton(
                            onClick = { shopNavController.navigate("cart") },
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
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)
                ) {
                    Text("Магазин", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (viewModel.isLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = VetGreen) }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(viewModel.products) { product ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(product.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Text("${product.category} • Остаток: ${product.stock_qty} шт.", fontSize = 12.sp, color = Color.Gray)
                                            Text("${product.price} руб.", color = VetGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        }
                                        IconButton(
                                            onClick = { viewModel.addToCart(product) },
                                            colors = IconButtonDefaults.iconButtonColors(containerColor = VetGreen.copy(alpha = 0.1f))
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Добавить", tint = VetGreen)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        composable("cart") {
            CartScreen(
                viewModel = viewModel,
                userId = userId,
                onNavigateBack = { shopNavController.popBackStack() },
                onOrderSuccess = {
                    shopNavController.navigate("success") {
                        popUpTo("cart") { inclusive = true }
                    }
                }
            )
        }

        composable("success") {
            OrderSuccessScreen(
                orderId = viewModel.lastOrderId,
                total = viewModel.getCartTotal(),
                onNavigateToCatalog = {
                    shopNavController.navigate("catalog") {
                        popUpTo("catalog") { inclusive = true }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: ShopViewModel,
    userId: Int,
    onNavigateBack: () -> Unit,
    onOrderSuccess: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Корзина") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VetGreen, titleContentColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F9F9)
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)
        ) {
            if (viewModel.cart.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Корзина пуста", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(viewModel.cart.entries.toList()) { entry ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(entry.key.name, fontWeight = FontWeight.Bold)
                                    Text("${entry.key.price} руб. x ${entry.value} шт.", color = Color.Gray, fontSize = 14.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { viewModel.removeFromCart(entry.key) }) {
                                        Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text("${entry.value}", fontSize = 16.sp)
                                    IconButton(onClick = { viewModel.addToCart(entry.key) }) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Итого: ${viewModel.getCartTotal()} руб.", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.checkout(userId)
                        onOrderSuccess()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VetGreen)
                ) {
                    Text("Оформить заказ (самовывоз)")
                }

                if (viewModel.orderMessage.isNotEmpty() && !viewModel.isOrderSuccess) {
                    Text(viewModel.orderMessage, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}

@Composable
fun OrderSuccessScreen(
    orderId: Int?,
    total: Double,
    onNavigateToCatalog: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = VetGreen,
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text("Заказ успешно оформлен!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = VetGreen)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Номер заказа: #${orderId ?: "---"}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Сумма: ${total} руб.", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Способ получения: Самовывоз", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Статус: Новый", fontSize = 16.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNavigateToCatalog,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VetGreen)
        ) {
            Text("Вернуться в магазин")
        }
    }
}