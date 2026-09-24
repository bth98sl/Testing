package com.example.blink.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TopAppBar(
  title: String,
  subtitle: String? = null,
  onBack: (() -> Unit)? = null,
  showBack: Boolean = false,
  onReset: (() -> Unit)? = null,
  showReset: Boolean = false,
  onOpenCodeModal: (() -> Unit)? = null,
  onOpenPaywall: (() -> Unit)? = null,
  isPro: Boolean = false,
  modifier: Modifier = Modifier
) {
  Surface(
    color = Color(0xFF121316),
    modifier = modifier
      .fillMaxWidth()
      .height(64.dp)
      .border(
        width = 1.dp,
        color = Color.White.copy(alpha = 0.06f)
      )
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Left Section: Back / Logo + Title
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.weight(1f, fill = false)
      ) {
        if (showBack && onBack != null) {
          IconButton(
            onClick = onBack,
            modifier = Modifier.size(40.dp)
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color(0xFFE3E2E6),
              modifier = Modifier.size(20.dp)
            )
          }
        } else {
          AppLogoSmall()
        }

        Column(
          verticalArrangement = Arrangement.Center,
          modifier = Modifier.weight(1f, fill = false)
        ) {
          Text(
            text = title,
            color = Color(0xFFE3E2E6),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          subtitle?.let {
            Text(
              text = it,
              color = Color(0xFF938F99),
              fontSize = 12.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }

      // Right Section: PRO + Reset + Jetpack Compose Code Button
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        // PRO Button
        if (onOpenPaywall != null) {
          val proModifier = if (isPro) {
            Modifier
              .background(
                color = Color(0xFFFE0377).copy(alpha = 0.2f),
                shape = CircleShape
              )
              .border(
                width = 1.dp,
                color = Color(0xFFFE0377).copy(alpha = 0.4f),
                shape = CircleShape
              )
          } else {
            Modifier.background(
              brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFFFE0377), Color(0xFFD7AFFC))
              ),
              shape = CircleShape
            )
          }

          Surface(
            shape = CircleShape,
            color = Color.Transparent,
            modifier = Modifier
              .height(32.dp)
              .then(proModifier)
              .clickable { onOpenPaywall() }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = null,
                tint = if (isPro) Color(0xFFFE0377) else Color.White,
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = "PRO",
                color = if (isPro) Color(0xFFFE0377) else Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        // Reset Button
        if (showReset && onReset != null) {
          Surface(
            shape = CircleShape,
            color = Color.Transparent,
            modifier = Modifier
              .height(36.dp)
              .clickable { onReset() }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset",
                tint = Color(0xFFC4C7C5),
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = "Reset",
                color = Color(0xFFC4C7C5),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }

        // Code Modal Button
        if (onOpenCodeModal != null) {
          Surface(
            shape = CircleShape,
            color = Color(0xFF1E1F24),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF33353A)),
            modifier = Modifier
              .height(36.dp)
              .clickable { onOpenCodeModal() }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Code,
                contentDescription = null,
                tint = Color(0xFFD0BCFF),
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "Jetpack Compose",
                color = Color(0xFFD0BCFF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun AppLogoSmall() {
  Box(
    modifier = Modifier
      .size(28.dp)
      .clip(CircleShape)
      .background(Color(0xFFFE0377)),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = Icons.Default.AutoAwesome,
      contentDescription = null,
      tint = Color.White,
      modifier = Modifier.size(16.dp)
    )
  }
}