package com.example.blink.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blink.R

// ==========================================
// ENUMS & CONFIGURATIONS
// ==========================================

enum class LogoSize {
  XS, SM, MD, LG, XL
}

enum class LogoTextPosition {
  RIGHT, BOTTOM
}

private data class LogoSizeConfig(
  val boxSize: Dp,
  val cornerRadius: Dp,
  val textSize: TextUnit,
  val badgeTextSize: TextUnit,
  val badgePaddingHorizontal: Dp,
  val badgePaddingVertical: Dp
)

private fun getLogoSizeConfig(size: LogoSize): LogoSizeConfig {
  return when (size) {
    LogoSize.XS -> LogoSizeConfig(28.dp, 10.dp, 12.sp, 8.sp, 4.dp, 1.dp)
    LogoSize.SM -> LogoSizeConfig(36.dp, 12.dp, 14.sp, 9.sp, 6.dp, 2.dp)
    LogoSize.MD -> LogoSizeConfig(48.dp, 16.dp, 16.sp, 10.sp, 8.dp, 2.dp)
    LogoSize.LG -> LogoSizeConfig(64.dp, 22.dp, 20.sp, 12.sp, 8.dp, 2.dp)
    LogoSize.XL -> LogoSizeConfig(120.dp, 34.dp, 28.sp, 12.sp, 10.dp, 2.dp)
  }
}

// ==========================================
// MAIN COMPOSABLE LOGO
// ==========================================

@Composable
fun AppLogo(
  modifier: Modifier = Modifier,
  size: LogoSize = LogoSize.MD,
  showText: Boolean = false,
  textPosition: LogoTextPosition = LogoTextPosition.RIGHT,
  animate: Boolean = false,
  glow: Boolean = true,
  brandName: String = "Blink",
  subtitle: String = "Material 3 • Gemini AI"
) {
  val config = remember(size) { getLogoSizeConfig(size) }

  // Hiệu ứng Pulse nhấp nháy cho Glow
  val infiniteTransition = rememberInfiniteTransition(label = "GlowTransition")
  val glowAlpha by if (animate) {
    infiniteTransition.animateFloat(
      initialValue = 0.4f,
      targetValue = 0.75f,
      animationSpec = infiniteRepeatable(
        animation = tween(1200, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse
      ),
      label = "GlowAlpha"
    )
  } else {
    remember { mutableFloatStateOf(0.4f) }
  }

  val content: @Composable () -> Unit = {
    // Logo Icon Container
    Box(
      modifier = Modifier.size(config.boxSize),
      contentAlignment = Alignment.Center
    ) {
      if (glow) {
        // Background Ambient Glow Gradient
        Box(
          modifier = Modifier
            .fillMaxSize()
            .offset(y = 0.dp)
            .graphicsLayer {
              alpha = glowAlpha
            }
            .blur(16.dp)
            .clip(RoundedCornerShape(config.cornerRadius))
            .background(
              Brush.linearGradient(
                colors = listOf(
                  Color(0xFFFE0377),
                  Color(0xFFFF6069),
                  Color(0xFFD7AFFC)
                )
              )
            )
        )
      }

      // Primary App Icon Box
      Box(
        modifier = Modifier
          .fillMaxSize()
          .clip(RoundedCornerShape(config.cornerRadius))
          .background(Color(0xFF111111))
          .border(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.2f),
            shape = RoundedCornerShape(config.cornerRadius)
          ),
        contentAlignment = Alignment.Center
      ) {
        // Dùng Vector Icon mặc định thay cho resource bị thiếu
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = "Blink Logo Icon",
          tint = Color(0xFFD7AFFC),
          modifier = Modifier.size(config.boxSize * 0.5f)
        )

        // Overlays Highlight Glass
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color.White.copy(alpha = 0.2f),
                  Color.Transparent
                ),
                startY = 0f,
                endY = 100f
              )
            )
        )
      }
    }

    // Styled Brand Name & Badges
    if (showText) {
      Column(
        horizontalAlignment = if (textPosition == LogoTextPosition.BOTTOM) Alignment.CenterHorizontally else Alignment.Start
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = brandName,
            color = Color.White,
            fontSize = config.textSize,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.SansSerif
          )

          // PRO Badge
          Box(
            modifier = Modifier
              .clip(CircleShape)
              .background(
                Brush.horizontalGradient(
                  colors = listOf(
                    Color(0xFFFE0377),
                    Color(0xFFFF6069)
                  )
                )
              )
              .padding(
                horizontal = config.badgePaddingHorizontal,
                vertical = config.badgePaddingVertical
              )
          ) {
            Text(
              text = "PRO",
              color = Color.White,
              fontSize = config.badgeTextSize,
              fontWeight = FontWeight.Black
            )
          }
        }

        if (subtitle.isNotEmpty()) {
          Text(
            text = subtitle,
            color = Color(0xFFD7AFFC),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = if (textPosition == LogoTextPosition.BOTTOM) TextAlign.Center else TextAlign.Start
          )
        }
      }
    }
  }

  if (textPosition == LogoTextPosition.BOTTOM) {
    Column(
      modifier = modifier,
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      content()
    }
  } else {
    Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      content()
    }
  }
}