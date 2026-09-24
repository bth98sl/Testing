package com.example.blink.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ResultScreen(
  resultImage: Any?, // Bitmap, Uri hoặc String URL
  isPng: Boolean = false,
  fileSizeBytes: Long = 1024 * 512, // Default ~512KB
  onEditAgain: () -> Unit,
  onPickNewImage: () -> Unit,
  onSaveToGallery: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var hasSaved by remember { mutableStateOf(false) }
  var shareFeedback by remember { mutableStateOf<String?>(null) }

  // Tính toán thông tin File
  val timestamp = remember {
    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
  }
  val ext = if (isPng) "png" else "jpg"
  val downloadFileName = "IMG_EDIT_$timestamp.$ext"

  val sizeKb = fileSizeBytes / 1024
  val sizeDisplay = if (sizeKb > 1024) {
    String.format(Locale.US, "%.1f MB", sizeKb / 1024f)
  } else {
    "$sizeKb KB"
  }

  fun handleSaveToDevice() {
    onSaveToGallery()
    hasSaved = true
    scope.launch {
      delay(4000)
      hasSaved = false
    }
  }

  fun handleShare() {
    try {
      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = if (isPng) "image/png" else "image/jpeg"
        // Trong thực tế, bạn có thể truyền FileProvider Uri của ảnh tại đây
        putExtra(Intent.EXTRA_TITLE, "Photo edited with Blink AI Photo Editor")
      }
      context.startActivity(Intent.createChooser(shareIntent, "Share Photo"))
      shareFeedback = "Opened Share Sheet!"
      scope.launch {
        delay(3000)
        shareFeedback = null
      }
    } catch (e: Exception) {
      handleSaveToDevice()
      shareFeedback = "Photo saved directly to device."
      scope.launch {
        delay(3500)
        shareFeedback = null
      }
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF121316))
      .padding(16.dp),
    verticalArrangement = Arrangement.SpaceBetween,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // ==========================================
      // 1. TOP HEADER INFO
      // ==========================================
      Spacer(modifier = Modifier.height(8.dp))
      Surface(
        shape = CircleShape,
        color = Color(0xFF381E72).copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          Color(0xFFD0BCFF).copy(alpha = 0.3f)
        )
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = Color(0xFFD0BCFF),
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = "Editing Complete",
            color = Color(0xFFD0BCFF),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Your Photo is Ready",
        color = Color(0xFFE3E2E6),
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
      )

      Spacer(modifier = Modifier.height(6.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = Color(0xFF1E1F24),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF33353A))
        ) {
          Text(
            text = if (isPng) "PNG (Transparent)" else "JPG High Quality",
            color = Color(0xFF938F99),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
          )
        }
        Text(text = "•", color = Color(0xFF938F99), fontSize = 12.sp)
        Text(text = sizeDisplay, color = Color(0xFF938F99), fontSize = 12.sp)
      }
    }

    // ==========================================
    // 2. MAIN IMAGE DISPLAY
    // ==========================================
    Box(
      modifier = Modifier
        .padding(vertical = 16.dp)
        .fillMaxWidth()
        .weight(1f, fill = false),
      contentAlignment = Alignment.Center
    ) {
      Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF0A0A0C),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          Color.White.copy(alpha = 0.08f)
        ),
        shadowElevation = 16.dp,
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight()
      ) {
        Box(
          modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(22.dp)),
          contentAlignment = Alignment.Center
        ) {
          // Transparent Checkerboard Canvas behind PNG
          if (isPng) {
            Canvas(modifier = Modifier.matchParentSize()) {
              val squareSize = 16.dp.toPx()
              val cols = (size.width / squareSize).toInt() + 1
              val rows = (size.height / squareSize).toInt() + 1
              for (i in 0 until cols) {
                for (j in 0 until rows) {
                  if ((i + j) % 2 == 0) {
                    drawRect(
                      color = Color(0xFF1F2024),
                      topLeft = Offset(i * squareSize, j * squareSize),
                      size = Size(squareSize, squareSize)
                    )
                  }
                }
              }
            }
          }

          // Processed Image
          AsyncImage(
            model = resultImage,
            contentDescription = "Processed result",
            contentScale = ContentScale.Fit,
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 420.dp)
              .clip(RoundedCornerShape(22.dp))
          )
        }
      }
    }

    // ==========================================
    // 3. BOTTOM ACTIONS & TOASTS
    // ==========================================
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Save confirmation toast
      AnimatedVisibility(
        visible = hasSaved,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Color(0xFF1E1F24),
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFFA8C7FA).copy(alpha = 0.4f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.CheckCircle,
              contentDescription = null,
              tint = Color(0xFFA8C7FA),
              modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Saved to your device!",
                color = Color(0xFFE3E2E6),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "File saved as: $downloadFileName",
                color = Color(0xFF938F99),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }
      }

      // Share Feedback Banner
      AnimatedVisibility(
        visible = shareFeedback != null,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        shareFeedback?.let { msg ->
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E1F24),
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              Color(0xFFD0BCFF).copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFFD0BCFF),
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = msg,
                color = Color(0xFFD0BCFF),
                fontSize = 12.sp
              )
            }
          }
        }
      }

      // Large Action Button: Save to Device
      Button(
        onClick = { handleSaveToDevice() },
        modifier = Modifier
          .fillMaxWidth()
          .height(58.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF))
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Download,
            contentDescription = null,
            tint = Color(0xFF381E72),
            modifier = Modifier.size(22.dp)
          )
          Text(
            text = "Save to Device",
            color = Color(0xFF381E72),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      // Secondary Action Row: Share + Edit Again + New Photo
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Share
        SecondaryActionButton(
          icon = Icons.Default.Share,
          label = "Share",
          iconTint = Color(0xFFD0BCFF),
          onClick = { handleShare() },
          modifier = Modifier.weight(1f)
        )

        // Edit Again
        SecondaryActionButton(
          icon = Icons.AutoMirrored.Filled.ArrowBack,
          label = "Edit Again",
          iconTint = Color(0xFFC4C7C5),
          onClick = onEditAgain,
          modifier = Modifier.weight(1f)
        )

        // New Photo
        SecondaryActionButton(
          icon = Icons.Default.Image,
          label = "New Photo",
          iconTint = Color(0xFFC4C7C5),
          onClick = onPickNewImage,
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
private fun SecondaryActionButton(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  iconTint: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = CircleShape,
    color = Color(0xFF1E1F24),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF33353A)),
    modifier = modifier
      .height(48.dp)
      .clickable { onClick() }
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconTint,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = label,
        color = Color(0xFFE3E2E6),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1
      )
    }
  }
}