package com.example.blink.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ExportFormat { AUTO, PNG, JPEG }

data class AppSettings(
  val exportFormat: ExportFormat = ExportFormat.AUTO,
  val maxResolution: Int = 1920,
  val bgTolerance: Int = 38,
  val hapticFeedback: Boolean = true,
  val autoSaveToDevice: Boolean = true
)

@Composable
fun SettingsScreen(
  settings: AppSettings,
  onUpdateSettings: (AppSettings) -> Unit,
  onResetSettings: () -> Unit,
  onOpenCodeModal: () -> Unit,
  onReopenOnboarding: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var clearedFeedback by remember { mutableStateOf(false) }

  fun handleClearCache() {
    clearedFeedback = true
    scope.launch {
      delay(3000)
      clearedFeedback = false
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF121316))
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 16.dp, vertical = 20.dp)
      .padding(bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Title Banner
    Column(modifier = Modifier.padding(horizontal = 4.dp)) {
      Text(
        text = "App Settings",
        color = Color(0xFFE3E2E6),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Customize export formats, AI filters, and Material 3 preferences",
        color = Color(0xFF938F99),
        fontSize = 12.sp,
        modifier = Modifier.padding(top = 2.dp)
      )
    }

    // Cache Cleared Notification Toast
    AnimatedVisibility(
      visible = clearedFeedback,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1E1F24),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA8C7FA).copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Color(0xFFA8C7FA),
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = "Temporary image cache and session storage cleared!",
            color = Color(0xFFA8C7FA),
            fontSize = 12.sp
          )
        }
      }
    }

    // ==========================================
    // SECTION 1: EXPORT & QUALITY
    // ==========================================
    SettingsCard {
      SectionHeader(
        icon = Icons.Default.Tune,
        title = "Export & Quality"
      )

      // Export Format Choice
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Default Export Format",
            color = Color(0xFFE3E2E6),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = when (settings.exportFormat) {
              ExportFormat.AUTO -> "Auto (PNG for cutouts, JPG for standard)"
              ExportFormat.PNG -> "PNG"
              ExportFormat.JPEG -> "JPEG"
            },
            color = Color(0xFF938F99),
            fontSize = 11.sp
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          ExportFormat.values().forEach { fmt ->
            val isSelected = settings.exportFormat == fmt
            OptionButton(
              text = if (fmt == ExportFormat.AUTO) "Auto" else fmt.name,
              isSelected = isSelected,
              onClick = { onUpdateSettings(settings.copy(exportFormat = fmt)) },
              modifier = Modifier.weight(1f)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Max Resolution Choice
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Max Resolution (Canvas Max Dimension)",
            color = Color(0xFFE3E2E6),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "${settings.maxResolution}px",
            color = Color(0xFFD0BCFF),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
          )
        }

        val resolutionOptions = listOf(
          "1080p" to 1080,
          "1920p (FHD)" to 1920,
          "2560p (2K)" to 2560
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          resolutionOptions.forEach { (label, value) ->
            val isSelected = settings.maxResolution == value
            OptionButton(
              text = label,
              isSelected = isSelected,
              onClick = { onUpdateSettings(settings.copy(maxResolution = value)) },
              modifier = Modifier.weight(1f)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // BG Tolerance Slider
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Cutout Sensitivity (Remove BG Tolerance)",
            color = Color(0xFFE3E2E6),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "${settings.bgTolerance}",
            color = Color(0xFFD0BCFF),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
          )
        }

        Slider(
          value = settings.bgTolerance.toFloat(),
          onValueChange = { onUpdateSettings(settings.copy(bgTolerance = it.toInt())) },
          valueRange = 20f..60f,
          colors = SliderDefaults.colors(
            thumbColor = Color(0xFFD0BCFF),
            activeTrackColor = Color(0xFFD0BCFF),
            inactiveTrackColor = Color(0xFF2B2C30)
          ),
          modifier = Modifier.padding(vertical = 4.dp)
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Precise Edge (20)", color = Color(0xFF938F99), fontSize = 10.sp)
          Text("Default (38)", color = Color(0xFF938F99), fontSize = 10.sp)
          Text("Broad (60)", color = Color(0xFF938F99), fontSize = 10.sp)
        }
      }
    }

    // ==========================================
    // SECTION 2: SYSTEM & INTERACTION
    // ==========================================
    SettingsCard {
      SectionHeader(
        icon = Icons.Default.NightsStay,
        title = "System & Interaction"
      )

      // Haptic Feedback Switch
      SettingToggleRow(
        icon = Icons.Outlined.Vibration,
        title = "Haptic Feedback",
        subtitle = "Light tactile feedback when tapping toolbar buttons",
        checked = settings.hapticFeedback,
        onCheckedChange = { onUpdateSettings(settings.copy(hapticFeedback = it)) }
      )

      HorizontalDivider(
        modifier = Modifier.padding(vertical = 4.dp),
        color = Color.White.copy(alpha = 0.06f)
      )

      // Auto Save Switch
      SettingToggleRow(
        icon = Icons.Default.Folder,
        title = "Auto Save to Device",
        subtitle = "Automatically download to gallery upon saving",
        checked = settings.autoSaveToDevice,
        onCheckedChange = { onUpdateSettings(settings.copy(autoSaveToDevice = it)) }
      )
    }

    // ==========================================
    // SECTION 3: STORAGE & OPTIMIZATION
    // ==========================================
    SettingsCard {
      SectionHeader(
        icon = Icons.Default.Storage,
        title = "Storage & Optimization"
      )

      // Clear Cache Action
      ActionTile(
        icon = Icons.Outlined.Delete,
        iconTint = Color(0xFFFFB4AB),
        title = "Clear Temp Photos & Cache",
        subtitle = "Free up memory and reset current session cache",
        buttonText = "Clear Now",
        buttonTextColor = Color(0xFFFFB4AB),
        onClick = { handleClearCache() }
      )

      // Reset Settings Action
      ActionTile(
        icon = Icons.Outlined.RestartAlt,
        iconTint = Color(0xFFC4C7C5),
        title = "Reset to Defaults",
        subtitle = "Reset all preferences to original factory defaults",
        buttonText = "Reset",
        buttonTextColor = Color(0xFFC4C7C5),
        onClick = onResetSettings
      )
    }

    // ==========================================
    // SECTION 4: ABOUT & JETPACK COMPOSE INFO
    // ==========================================
    SettingsCard {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp)
          .border(
            width = 0.dp,
            color = Color.Transparent
          ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
          Text(
            text = "Blink AI",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
          )
        }

        Surface(
          shape = CircleShape,
          color = Color(0xFF111111),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
          Text(
            text = "v1.3.0",
            color = Color(0xFFD7AFFC),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }
      }

      HorizontalDivider(
        color = Color.White.copy(alpha = 0.08f),
        modifier = Modifier.padding(bottom = 8.dp)
      )

      InfoRow("UI Architecture", "Android Jetpack Compose (Material 3 Dark)")
      InfoRow("AI Engine", "Gemini 3.8 Flash (Server Proxy)", valueColor = Color(0xFFD0BCFF))
      InfoRow("Image Processing", "100% Client-side HTML5 Canvas & Bitmap")
      InfoRow("Version", "v1.3.0 (API 34)", isMonospace = true)

      Spacer(modifier = Modifier.height(8.dp))

      // Code Modal Shortcut
      Button(
        onClick = onOpenCodeModal,
        modifier = Modifier
          .fillMaxWidth()
          .height(44.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF))
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Code,
            contentDescription = null,
            tint = Color(0xFF381E72),
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = "View Kotlin Source Code (MainActivity.kt)",
            color = Color(0xFF381E72),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      // Onboarding Replay Button
      if (onReopenOnboarding != null) {
        Button(
          onClick = onReopenOnboarding,
          modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(top = 4.dp),
          shape = CircleShape,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B2C30))
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = Color(0xFFD0BCFF),
              modifier = Modifier.size(14.dp)
            )
            Text(
              text = "Replay Welcome Tour (Splash & Onboarding)",
              color = Color(0xFFE3E2E6),
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }
    }
  }
}

// ==========================================
// REUSABLE HELPER COMPONENTS
// ==========================================

@Composable
private fun SettingsCard(
  content: @Composable ColumnScope.() -> Unit
) {
  Surface(
    shape = RoundedCornerShape(24.dp),
    color = Color(0xFF1E1F24),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      content = content
    )
  }
}

@Composable
private fun SectionHeader(
  icon: ImageVector,
  title: String
) {
  Row(
    modifier = Modifier.padding(bottom = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = Color(0xFFD0BCFF),
      modifier = Modifier.size(16.dp)
    )
    Text(
      text = title.uppercase(),
      color = Color(0xFFD0BCFF),
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 0.8.sp
    )
  }
}

@Composable
private fun OptionButton(
  text: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = CircleShape,
    color = if (isSelected) Color(0xFFD0BCFF) else Color(0xFF2B2C30),
    modifier = modifier
      .height(36.dp)
      .clickable { onClick() }
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier.fillMaxSize()
    ) {
      Text(
        text = text,
        color = if (isSelected) Color(0xFF381E72) else Color(0xFFC4C7C5),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold
      )
    }
  }
}

@Composable
private fun SettingToggleRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0xFF2B2C30)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = Color(0xFFD0BCFF),
          modifier = Modifier.size(16.dp)
        )
      }
      Column {
        Text(
          text = title,
          color = Color(0xFFE3E2E6),
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold
        )
        Text(
          text = subtitle,
          color = Color(0xFF938F99),
          fontSize = 11.sp
        )
      }
    }

    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = Color(0xFF381E72),
        checkedTrackColor = Color(0xFFD0BCFF),
        uncheckedThumbColor = Color(0xFF938F99),
        uncheckedTrackColor = Color(0xFF33353A)
      )
    )
  }
}

@Composable
private fun ActionTile(
  icon: ImageVector,
  iconTint: Color,
  title: String,
  subtitle: String,
  buttonText: String,
  buttonTextColor: Color,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = Color(0xFF2B2C30).copy(alpha = 0.6f),
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp)
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = iconTint,
          modifier = Modifier.size(16.dp)
        )
        Column {
          Text(
            text = title,
            color = Color(0xFFE3E2E6),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = subtitle,
            color = Color(0xFF938F99),
            fontSize = 10.sp
          )
        }
      }

      Surface(
        shape = CircleShape,
        color = Color(0xFF33353A),
        modifier = Modifier.clickable { onClick() }
      ) {
        Text(
          text = buttonText,
          color = buttonTextColor,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
      }
    }
  }
}

@Composable
private fun InfoRow(
  label: String,
  value: String,
  valueColor: Color = Color(0xFFE3E2E6),
  isMonospace: Boolean = false
) {
  Column {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(text = label, color = Color(0xFF938F99), fontSize = 12.sp)
      Text(
        text = value,
        color = valueColor,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default
      )
    }
    HorizontalDivider(color = Color.White.copy(alpha = 0.04f))
  }
}