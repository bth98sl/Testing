package com.example.blink.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun AndroidFrame(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  // Tự động cập nhật thời gian mỗi 30 giây
  var currentTime by remember { mutableStateOf("12:00") }

  LaunchedEffect(Unit) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    while (true) {
      currentTime = LocalTime.now().format(formatter)
      delay(30000L)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF111111))
  ) {
    // Status Bar mô phỏng Android
    StatusBarSection(currentTime = currentTime)

    // Vùng chứa nội dung giao diện chính
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .background(Color(0xFF111111))
    ) {
      content()
    }

    // Thanh vuốt Android Gesture Navigation Pill
    GesturePillBar()
  }
}

@Composable
private fun StatusBarSection(currentTime: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(40.dp)
      .padding(horizontal = 24.dp)
      .background(Color(0xFF111111)),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Đồng hồ
    Text(
      text = currentTime,
      color = Color.White,
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium,
      fontFamily = FontFamily.Monospace
    )

    // Các biểu tượng hệ thống (5G, Wifi, Pin)
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Thẻ 5G
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(4.dp))
          .background(Color(0xFF1C1C1E))
          .padding(horizontal = 4.dp, vertical = 2.dp)
      ) {
        Text(
          text = "5G",
          color = Color(0xFFD7AFFC),
          fontSize = 10.sp,
          fontWeight = FontWeight.SemiBold
        )
      }

      // Wifi
      Icon(
        imageVector = Icons.Default.Wifi,
        contentDescription = "Wifi",
        tint = Color.White,
        modifier = Modifier.size(14.dp)
      )

      // Pin
      Icon(
        imageVector = Icons.Default.BatteryFull,
        contentDescription = "Battery",
        tint = Color.White,
        modifier = Modifier.size(16.dp)
      )
    }
  }
}

@Composable
private fun GesturePillBar() {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .width(128.dp)
        .height(4.dp)
        .clip(CircleShape)
        .background(Color(0xFF3A3A3C))
    )
  }
}