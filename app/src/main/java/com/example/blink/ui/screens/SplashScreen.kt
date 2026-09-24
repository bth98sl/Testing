package com.example.blink.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.blink.R


@Composable
fun SplashScreen(
  onFinish: () -> Unit,
  durationMs: Long = 2400L
) {
  var isExiting by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    delay(durationMs)
    isExiting = true
    delay(400)
    onFinish()
  }

  // Hiệu ứng mờ dần và thu nhỏ khi kết thúc Splash
  val alphaAnim by animateFloatAsState(
    targetValue = if (isExiting) 0f else 1f,
    animationSpec = tween(durationMillis = 500),
    label = "alpha"
  )

  val scaleAnim by animateFloatAsState(
    targetValue = if (isExiting) 0.95f else 1f,
    animationSpec = tween(durationMillis = 500),
    label = "scale"
  )

  // Hiệu ứng nháy đốm Loading
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseAlpha"
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .alpha(alphaAnim)
      .scale(scaleAnim)
      .background(
        brush = Brush.linearGradient(
          colors = listOf(
            Color(0xFFFE0377),
            Color(0xFFF9A8D4),
            Color(0xFFF9A8D4),
            Color(0xFFFDF2F8)
          ),
          start = Offset(0f, 0f),
          end = Offset(1000f, 2000f)
        )
      )
      .clickable {
        if (!isExiting) {
          isExiting = true
          onFinish()
        }
      }
  ) {
    // Lớp ánh sáng phát ra ở tâm
    Box(
      modifier = Modifier
        .align(Alignment.Center)
        .size(300.dp)
        .background(
          brush = Brush.radialGradient(
            colors = listOf(
              Color.White.copy(alpha = 0.3f),
              Color(0xFFD7AFFC).copy(alpha = 0.15f),
              Color.Transparent
            )
          ),
          shape = CircleShape
        )
    )

    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp, vertical = 24.dp)
    )
//    {
//
//      // 1. Header (Status Bar)
//      Row(
//        modifier = Modifier
//          .fillMaxWidth()
//          .align(Alignment.TopCenter)
//          .padding(top = 16.dp),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically
//      ) {
//        Row(
//          verticalAlignment = Alignment.CenterVertically,
//          horizontalArrangement = Arrangement.spacedBy(6.dp)
//        ) {
//          Icon(
//            imageVector = Icons.Default.Star,
//            contentDescription = null,
//            tint = Color(0xFFFE0377),
//            modifier = Modifier.size(14.dp)
//          )
//          Text(
//            text = "AI Photo Editor",
//            color = Color.Black.copy(alpha = 0.8f),
//            fontSize = 11.sp,
//            fontWeight = FontWeight.Medium,
//            letterSpacing = 0.5.sp
//          )
//        }

//        Text(
//          text = "PRO",
//          color = Color.Black.copy(alpha = 0.9f),
//          fontSize = 11.sp,
//          fontFamily = FontFamily.Monospace,
//          fontWeight = FontWeight.ExtraBold,
//          letterSpacing = 0.5.sp
//        )
//      }

      // 2. Nội dung chính: Logo AI Brand + Subtitle + Loading
      Column(
        modifier = Modifier
          .align(Alignment.Center)
          .offset(y = (-20).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {

        // Logo AI Brand mới
        BlinkBrandLogo()

        Spacer(modifier = Modifier.height(8.dp))

        // Khung Loading AI filters
        Row(
          modifier = Modifier
            .background(
              color = Color.White.copy(alpha = 0.12f),
              shape = RoundedCornerShape(50)
            )
            .border(
              width = 1.5.dp,
              color = Color.White.copy(alpha = 0.25f),
              shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 14.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .alpha(pulseAlpha)
              .background(Color(0xFFFE0377), CircleShape)
          )
          Text(
            text = stringResource(id = R.string.loading_ai_filters),
            color = Color.Black.copy(alpha = 0.9f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }
  }
//}

@Composable
fun BlinkBrandLogo(
  modifier: Modifier = Modifier
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = modifier
  ) {
    // Load trực tiếp file ảnh logo gốc giúp chuẩn 100% thiết kế
    Image(
      painter = painterResource(id = R.drawable.icon_logo_blink),
      contentDescription = "Blink Logo",
      modifier = Modifier
        .width(280.dp) // Cân chỉnh kích thước logo theo ý muốn
        .wrapContentHeight()
    )
  }
}