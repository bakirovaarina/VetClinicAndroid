package com.example.vetclient.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    fundTitle: String,
    onPaymentSuccess: (Double) -> Unit,
    onNavigateBack: () -> Unit
) {
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var selectedAmount by remember { mutableStateOf(500.0) }
    var customAmount by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    var cardError by remember { mutableStateOf<String?>(null) }
    var expiryError by remember { mutableStateOf<String?>(null) }
    var cvvError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }

    val amounts = listOf(500.0, 1000.0, 2000.0, 5000.0)

    LaunchedEffect(isProcessing) {
        if (isProcessing) {
            delay(2000)
            isProcessing = false
            isSuccess = true
        }
    }

    fun formatCardNumber(input: String): String {
        val digits = input.filter { it.isDigit() }
        val limited = if (digits.length > 16) digits.substring(0, 16) else digits
        val sb = StringBuilder()
        for (i in limited.indices) {
            if (i > 0 && i % 4 == 0) sb.append(" ")
            sb.append(limited[i])
        }
        return sb.toString()
    }

    fun formatExpiryDate(input: String): String {
        val digits = input.filter { it.isDigit() }
        val limited = if (digits.length > 4) digits.substring(0, 4) else digits
        return if (limited.length >= 3) {
            "${limited.substring(0, 2)}/${limited.substring(2)}"
        } else {
            limited
        }
    }

    fun validate(): Boolean {
        var isValid = true

        if (selectedAmount <= 0) {
            amountError = "Введите сумму больше 0"
            isValid = false
        } else {
            amountError = null
        }

        val cardDigits = cardNumber.filter { it.isDigit() }
        if (cardDigits.length != 16) {
            cardError = "Номер карты должен содержать 16 цифр"
            isValid = false
        } else {
            cardError = null
        }

        if (expiryDate.length != 5 || expiryDate[2] != '/') {
            expiryError = "Введите срок в формате ММ/ГГ"
            isValid = false
        } else {
            val month = expiryDate.substring(0, 2).toIntOrNull()
            if (month == null || month < 1 || month > 12) {
                expiryError = "Месяц должен быть от 01 до 12"
                isValid = false
            } else {
                expiryError = null
            }
        }

        if (cvv.length != 3) {
            cvvError = "CVV должен содержать 3 цифры"
            isValid = false
        } else {
            cvvError = null
        }

        return isValid
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F9F9))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = VetGreen)
            }
            Text("Оплата", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isSuccess) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VetGreen, modifier = Modifier.size(100.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Оплата прошла успешно!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = VetGreen)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Спасибо за вашу помощь!", fontSize = 16.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { onPaymentSuccess(selectedAmount) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VetGreen)
                ) {
                    Text("Вернуться")
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Пожертвование на", fontSize = 14.sp, color = Color.Gray)
                    Text(fundTitle, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Выберите сумму", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        amounts.forEach { amount ->
                            Button(
                                onClick = { selectedAmount = amount; customAmount = ""; amountError = null },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedAmount == amount && customAmount.isEmpty()) VetGreen else Color.LightGray
                                )
                            ) {
                                Text("${amount.toInt()} ₽", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customAmount,
                        onValueChange = {
                            customAmount = it.filter { char -> char.isDigit() }
                            selectedAmount = customAmount.toDoubleOrNull() ?: 0.0
                            amountError = null
                        },
                        label = { Text("Своя сумма (руб.)") },
                        isError = amountError != null,
                        supportingText = { amountError?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Данные карты", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = {
                            cardNumber = formatCardNumber(it)
                            cardError = null
                        },
                        label = { Text("Номер карты") },
                        leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = cardError != null,
                        supportingText = { cardError?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = {
                                expiryDate = formatExpiryDate(it)
                                expiryError = null
                            },
                            label = { Text("ММ/ГГ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = expiryError != null,
                            supportingText = { expiryError?.let { Text(it) } },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = cvv,
                            onValueChange = {
                                if (it.length <= 3) cvv = it.filter { char -> char.isDigit() }
                                cvvError = null
                            },
                            label = { Text("CVV") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = cvvError != null,
                            supportingText = { cvvError?.let { Text(it) } },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isProcessing) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = VetGreen)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (validate()) {
                                    isProcessing = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VetGreen)
                        ) {
                            Text("Оплатить ${selectedAmount.toInt()} ₽", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}