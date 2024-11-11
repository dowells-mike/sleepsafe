package com.example.sleepsafe.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SleepInfoCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Hours Slept: ",
                fontSize = 20.sp,
                color = Color.Black
            )
            Text(
                text = "Sleep Quality Score: ",
                fontSize = 20.sp,
                color = Color.Black,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
