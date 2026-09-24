package com.example.blink.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.blink.R
import com.example.blink.RecentItem
import com.example.blink.formatTimeAgo
import com.example.blink.ui.components.PhotoPermissionRationaleDialog

private val LocalClarenceTwoFont = FontFamily(
  Font(R.font.clarence_two, FontWeight.Normal)
)

data class PresetStyleItem(
  val id: String,
  val name: String,
  val subtitle: String,
  val imageUrl: String
)

data class UnsplashCuratedItem(
  val id: String,
  val title: String,
  val imageUrl: String
)

private fun openAppSettings(context: Context) {
  val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
    data = Uri.fromParts("package", context.packageName, null)
  }
  context.startActivity(intent)
}

// Hàm kiểm tra xem ứng dụng đã được cấp quyền đọc ảnh hay chưa
private fun checkPhotoPermission(context: Context): Boolean {
  val permissionToCheck = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    Manifest.permission.READ_MEDIA_IMAGES
  } else {
    Manifest.permission.READ_EXTERNAL_STORAGE
  }
  return ContextCompat.checkSelfPermission(
    context,
    permissionToCheck
  ) == PackageManager.PERMISSION_GRANTED
}

@Composable
fun HomeScreen(
  isProUser: Boolean = false,
  userAvatarUrl: String? = null,
  userName: String? = stringResource(R.string.default_user_name),
  recentList: List<RecentItem> = emptyList(),
  onImageSelected: (String, String) -> Unit = { _, _ -> },
  onNavigateToGallery: () -> Unit = {},
  onNavigateToUnsplash: () -> Unit = {},
  onNavigateToSettings: () -> Unit = {},
  onOpenPaywall: () -> Unit = {},
  onOpenCodeModal: () -> Unit = {}
) {
  val context = LocalContext.current
  val activity = context as? Activity

  var showRationaleDialog by remember { mutableStateOf(false) }
  var showGoToSettingsDialog by remember { mutableStateOf(false) }

  val requestPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val isGranted = when {
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
        permissions[Manifest.permission.READ_MEDIA_IMAGES] == true ||
                permissions[Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED] == true
      }
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
        permissions[Manifest.permission.READ_MEDIA_IMAGES] == true
      }
      else -> {
        permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
      }
    }

    if (isGranted) {
      onNavigateToGallery()
    } else {
      val permissionToCheck = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
      } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
      }

      val showRationale = activity?.let {
        ActivityCompat.shouldShowRequestPermissionRationale(it, permissionToCheck)
      } ?: false

      if (!showRationale) {
        showGoToSettingsDialog = true
      } else {
        Toast.makeText(context, context.getString(R.string.need_permission), Toast.LENGTH_SHORT).show()
      }
    }
  }

  fun handleChoosePhotoClick() {
    if (checkPhotoPermission(context)) {
      onNavigateToGallery()
    } else {
      showRationaleDialog = true
    }
  }

  if (showRationaleDialog) {
    PhotoPermissionRationaleDialog(
      onConfirm = {
        showRationaleDialog = false

        val permissionsToRequest = when {
          Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
            arrayOf(
              Manifest.permission.READ_MEDIA_IMAGES,
              Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            )
          }
          Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
          }
          else -> {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
          }
        }

        requestPermissionLauncher.launch(permissionsToRequest)
      },
      onDismiss = {
        showRationaleDialog = false
      }
    )
  }

  if (showGoToSettingsDialog) {
    AlertDialog(
      onDismissRequest = { showGoToSettingsDialog = false },
      title = {
        Text(
          text = stringResource(R.string.permission_dialog_title),
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      },
      text = {
        Text(
          text = stringResource(R.string.need_permission),
          color = Color.LightGray
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            showGoToSettingsDialog = false
            openAppSettings(context)
          }
        ) {
          Text(stringResource(R.string.settings), color = Color(0xFFFF007A), fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showGoToSettingsDialog = false }) {
          Text(stringResource(R.string.skip), color = Color.Gray)
        }
      },
      containerColor = Color(0xFF1E1F23)
    )
  }

  val presetStyles = listOf(
    PresetStyleItem("1", "Cinematic", "Warm & Muted Tones", "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=500"),
    PresetStyleItem("2", "Neon Film", "Vibrant Cyberpunk", "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=500"),
    PresetStyleItem("3", "Soft Film", "Dreamy Aesthetic", "https://images.unsplash.com/photo-1518895949257-7621c3c786d7?w=500")
  )

  val unsplashItems = listOf(
    UnsplashCuratedItem("1", "Cinematic", "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=500"),
    UnsplashCuratedItem("2", "Moody", "https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=500"),
    UnsplashCuratedItem("3", "Dark Tone", "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=500")
  )

  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp
  val isTablet = screenWidth > 600.dp
  val cardWidth = if (isTablet) 220.dp else (screenWidth * 0.42f).coerceIn(140.dp, 180.dp)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF07080A))
      .verticalScroll(rememberScrollState())
      // 🟢 ĐÃ CẬP NHẬT: Thêm padding bottom 110.dp để nội dung cuộn chui qua thanh BottomBar mờ
      .padding(bottom = 110.dp)
  ) {
    // HEADER
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Blink",
        color = Color.White,
        fontSize = 30.sp,
        fontFamily = LocalClarenceTwoFont,
        letterSpacing = (-0.5).sp
      )

      Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
          onClick = onOpenPaywall,
          shape = RoundedCornerShape(20.dp),
          color = Color(0xFFFFCFE5),
          modifier = Modifier.padding(end = 12.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (isProUser) stringResource(R.string.pro_active) else stringResource(R.string.get_pro),
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF2D2232)
            )
          }
        }

        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(0xFF2A2B30))
            .clickable { onNavigateToSettings() },
          contentAlignment = Alignment.Center
        ) {
          if (!userAvatarUrl.isNullOrEmpty()) {
            AsyncImage(
              model = userAvatarUrl,
              contentDescription = "User Avatar",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )
          } else {
            val firstLetter = userName?.trim()?.takeIf { it.isNotEmpty() }?.first()?.uppercase() ?: "U"
            Text(
              text = firstLetter,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // MAIN BUTTON "CHOOSE YOUR PHOTO"
    Surface(
      onClick = { handleChoosePhotoClick() },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      shape = RoundedCornerShape(28.dp),
      color = Color.Transparent
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            brush = Brush.horizontalGradient(
              colors = listOf(
                Color(0xFFF9A8D4),
                Color(0xFFFBCFE8),
                Color(0xFFE5E7EB)
              )
            )
          )
          .border(
            width = 1.dp,
            brush = Brush.verticalGradient(
              colors = listOf(
                Color.White,
                Color.White.copy(alpha = 0.4f)
              )
            ),
            shape = RoundedCornerShape(28.dp)
          )
          .padding(horizontal = 20.dp, vertical = 28.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            SparklePhotoIcon(
              modifier = Modifier.size(32.dp),
              color = Color(0xFFFF007A)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
              text = stringResource(R.string.choose_photo),
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1F101A),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Color(0xFFFF007A),
            modifier = Modifier.size(24.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(28.dp))

    // SECTION 1: RECENT
    if (recentList.isNotEmpty()) {
      SectionHeader(
        title = stringResource(R.string.section_recent),
        actionText = stringResource(R.string.see_more),
        onActionClick = {}
      )

      Spacer(modifier = Modifier.height(12.dp))

      LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        items(recentList) { item ->
          Card(
            modifier = Modifier
              .width(cardWidth * 0.9f)
              .height(cardWidth * 1.15f)
              .clickable { onImageSelected(item.imageUrl, item.title) },
            shape = RoundedCornerShape(16.dp)
          ) {
            Box(modifier = Modifier.fillMaxSize()) {
              AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
              )
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(
                    Brush.verticalGradient(
                      colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                      startY = 100f
                    )
                  )
              )
              Column(
                modifier = Modifier
                  .align(Alignment.BottomStart)
                  .padding(12.dp)
              ) {
                Text(
                  text = item.title,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = formatTimeAgo(item.timestamp),
                  fontSize = 11.sp,
                  color = Color.LightGray
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))
    }

    // SECTION 2: PRESET STYLES
    SectionHeader(
      title = stringResource(R.string.section_preset_styles),
      badgeText = stringResource(R.string.badge_new),
      actionText = stringResource(R.string.see_more),
      onActionClick = {}
    )

    Spacer(modifier = Modifier.height(12.dp))

    LazyRow(
      contentPadding = PaddingValues(horizontal = 20.dp),
      horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      items(presetStyles) { item ->
        Card(
          modifier = Modifier
            .width(cardWidth)
            .height(cardWidth * 0.75f)
            .clickable { onImageSelected(item.imageUrl, item.name) },
          shape = RoundedCornerShape(16.dp)
        ) {
          Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
              model = item.imageUrl,
              contentDescription = item.name,
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(
                  Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                  )
                )
            )
            Column(
              modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
            ) {
              Text(
                text = item.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = item.subtitle,
                fontSize = 11.sp,
                color = Color.LightGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(28.dp))

    // SECTION 3: UNSPLASH
    SectionHeader(
      title = stringResource(R.string.section_unsplash),
      actionText = stringResource(R.string.see_more),
      onActionClick = onNavigateToUnsplash
    )

    Spacer(modifier = Modifier.height(12.dp))

    LazyRow(
      contentPadding = PaddingValues(horizontal = 20.dp),
      horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      items(unsplashItems) { item ->
        Card(
          modifier = Modifier
            .width(cardWidth)
            .height(cardWidth * 0.75f)
            .clickable { onImageSelected(item.imageUrl, item.title) },
          shape = RoundedCornerShape(16.dp)
        ) {
          Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
              model = item.imageUrl,
              contentDescription = item.title,
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(
                  Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                  )
                )
            )
            Text(
              text = item.title,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun SectionHeader(
  title: String,
  subtitle: String? = null,
  badgeText: String? = null,
  actionText: String = "",
  onActionClick: () -> Unit = {}
) {
  val infiniteTransition = rememberInfiniteTransition(label = "teeter")
  val rotationAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = -12f,
    animationSpec = infiniteRepeatable(
      animation = keyframes {
        durationMillis = 2500
        0.0f at 0 with FastOutSlowInEasing
        -12.0f at 200 with FastOutSlowInEasing
        0.0f at 400 with FastOutSlowInEasing
        -12.0f at 600 with FastOutSlowInEasing
        0.0f at 800 with FastOutSlowInEasing
        0.0f at 2500
      },
      repeatMode = RepeatMode.Restart
    ),
    label = "rotation"
  )

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      modifier = Modifier.weight(1f, fill = false),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      badgeText?.let {
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = it,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFFFF007A),
          letterSpacing = 0.5.sp,
          modifier = Modifier.graphicsLayer {
            transformOrigin = TransformOrigin(pivotFractionX = 0f, pivotFractionY = 1f)
            rotationZ = rotationAngle
          }
        )
      }

      subtitle?.let {
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = it,
          fontSize = 12.sp,
          color = Color(0xFF8A8B8F),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }

    if (actionText.isNotEmpty()) {
      Spacer(modifier = Modifier.width(12.dp))
      Text(
        text = actionText,
        fontSize = 13.sp,
        color = Color(0xFF8A8B8F),
        modifier = Modifier.clickable { onActionClick() }
      )
    }
  }
}

@Composable
fun SparklePhotoIcon(
  modifier: Modifier = Modifier.size(44.dp),
  color: Color = Color(0xFFFF007A)
) {
  val infiniteTransition = rememberInfiniteTransition(label = "SparkleAnimation")

  val star1Scale by infiniteTransition.animateFloat(
    initialValue = 0.6f,
    targetValue = 1.1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "Star1Scale"
  )

  val star2Scale by infiniteTransition.animateFloat(
    initialValue = 0.8f,
    targetValue = 0.4f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "Star2Scale"
  )

  val star3Scale by infiniteTransition.animateFloat(
    initialValue = 0.5f,
    targetValue = 1.2f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "Star3Scale"
  )

  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    val strokeWidth = w * 0.065f
    val strokeStyle = Stroke(
      width = strokeWidth,
      cap = StrokeCap.Round,
      join = StrokeJoin.Round
    )

    val outerR = w * 0.22f
    val outerLeft = strokeWidth / 2
    val outerTop = h * 0.16f
    val outerRight = w * 0.83f
    val outerBottom = h - strokeWidth / 2

    val outerPath1 = Path().apply {
      moveTo(w * 0.50f, outerTop)
      lineTo(outerLeft + outerR, outerTop)
      arcTo(
        rect = Rect(outerLeft, outerTop, outerLeft + outerR * 2, outerTop + outerR * 2),
        startAngleDegrees = -90f,
        sweepAngleDegrees = -90f,
        forceMoveTo = false
      )
      lineTo(outerLeft, outerBottom - outerR)
      arcTo(
        rect = Rect(outerLeft, outerBottom - outerR * 2, outerLeft + outerR * 2, outerBottom),
        startAngleDegrees = 180f,
        sweepAngleDegrees = -90f,
        forceMoveTo = false
      )
      lineTo(outerRight - outerR, outerBottom)
      arcTo(
        rect = Rect(outerRight - outerR * 2, outerBottom - outerR * 2, outerRight, outerBottom),
        startAngleDegrees = 90f,
        sweepAngleDegrees = -90f,
        forceMoveTo = false
      )
      lineTo(outerRight, h * 0.52f)
    }
    drawPath(path = outerPath1, color = color, style = strokeStyle)

    val innerLeft = w * 0.28f
    val innerTop = h * 0.42f
    val innerRight = w * 0.65f
    val innerBottom = h * 0.77f
    val innerR = w * 0.10f

    val innerRect = Rect(innerLeft, innerTop, innerRight, innerBottom)
    drawRoundRect(
      color = color,
      topLeft = Offset(innerRect.left, innerRect.top),
      size = Size(innerRect.width, innerRect.height),
      cornerRadius = CornerRadius(innerR),
      style = strokeStyle
    )

    drawCircle(
      color = color,
      radius = w * 0.035f,
      center = Offset(w * 0.40f, h * 0.52f)
    )

    val mountainPath = Path().apply {
      moveTo(innerLeft + strokeWidth * 0.8f, h * 0.68f)
      lineTo(w * 0.41f, h * 0.60f)
      lineTo(w * 0.48f, h * 0.65f)
      lineTo(w * 0.56f, h * 0.58f)
      lineTo(innerRight - strokeWidth * 0.8f, h * 0.68f)
    }
    drawPath(path = mountainPath, color = color, style = strokeStyle)

    fun drawStar(center: Offset, baseSize: Float, scale: Float) {
      val animatedSize = baseSize * scale
      if (animatedSize <= 0f) return

      val starPath = Path().apply {
        moveTo(center.x, center.y - animatedSize)
        quadraticTo(center.x, center.y, center.x + animatedSize, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + animatedSize)
        quadraticTo(center.x, center.y, center.x - animatedSize, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - animatedSize)
        close()
      }
      drawPath(
        path = starPath,
        color = color.copy(alpha = scale.coerceIn(0.2f, 1.0f))
      )
    }

    drawStar(center = Offset(w * 0.73f, h * 0.14f), baseSize = w * 0.13f, scale = star1Scale)
    drawStar(center = Offset(w * 0.60f, h * 0.32f), baseSize = w * 0.08f, scale = star2Scale)
    drawStar(center = Offset(w * 0.84f, h * 0.37f), baseSize = w * 0.095f, scale = star3Scale)
  }
}