package com.example.vetclient.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val VetGreen = Color(0xFF00897B)
val VetLightBackground = Color(0xFFF5F9F9)

@Composable
fun HomeScreen(userName: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VetLightBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "Здравствуйте,",
            fontSize = 16.sp,
            color = Color.Gray
        )
        Text(
            text = userName,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = VetGreen
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VetGreen)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Ближайшая запись", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                Text("Приём у врача", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Завтра в 14:30 (Иванов И.И.)", color = Color.White, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Помогите бездомным животным", fontWeight = FontWeight.Bold)
                    Text("Сбор на корм для приюта", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Новости и акции", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Скидка 15% на груминг", fontWeight = FontWeight.Bold)
                Text("Только до конца месяца! Запишите своего питомца на стрижку.", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}