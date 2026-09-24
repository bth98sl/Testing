package com.example.blink.ui.screens

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class CropRatio(val label: String, val ratio: Float?) {
    FREE("Tự do", null),
    RATIO_1_1("1:1", 1f),
    RATIO_4_3("4:3", 4f / 3f),
    RATIO_16_9("16:9", 16f / 9f),
    RATIO_3_4("3:4", 3f / 4f)
}

@Composable
fun CustomCropScreen(
    bitmap: Bitmap,
    onDismiss: () -> Unit,
    onCropFinished: (Bitmap) -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var rotation by remember { mutableStateOf(0f) }
    var isFlipped by remember { mutableStateOf(false) }
    var selectedRatio by remember { mutableStateOf(CropRatio.FREE) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07080A))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // --- 1. KHUNG ẢNH & LƯỚI CROP THEO TỶ LỆ ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 70.dp, bottom = 150.dp, start = 16.dp, end = 16.dp)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, rotationChange ->
                        scale = (scale * zoom).coerceIn(0.5f, 5f)
                        offset += pan
                        rotation += rotationChange
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Crop Canvas",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = if (isFlipped) -scale else scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y,
                        rotationZ = rotation
                    ),
                contentScale = ContentScale.Fit
            )

            // Vẽ khung crop động theo tỷ lệ được chọn
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                val cropWidth: Float
                val cropHeight: Float

                when (selectedRatio.ratio) {
                    null -> {
                        cropWidth = canvasWidth * 0.85f
                        cropHeight = canvasHeight * 0.85f
                    }
                    else -> {
                        if (canvasWidth / canvasHeight > selectedRatio.ratio!!) {
                            cropHeight = canvasHeight * 0.8f
                            cropWidth = cropHeight * selectedRatio.ratio!!
                        } else {
                            cropWidth = canvasWidth * 0.85f
                            cropHeight = cropWidth / selectedRatio.ratio!!
                        }
                    }
                }

                val left = (canvasWidth - cropWidth) / 2f
                val top = (canvasHeight - cropHeight) / 2f
                val right = left + cropWidth
                val bottom = top + cropHeight

                // Vẽ lưới 3x3
                val thirdW = cropWidth / 3f
                val thirdH = cropHeight / 3f
                drawLine(Color.White.copy(alpha = 0.3f), Offset(left + thirdW, top), Offset(left + thirdW, bottom), strokeWidth = 1.dp.toPx())
                drawLine(Color.White.copy(alpha = 0.3f), Offset(left + 2 * thirdW, top), Offset(left + 2 * thirdW, bottom), strokeWidth = 1.dp.toPx())
                drawLine(Color.White.copy(alpha = 0.3f), Offset(left, top + thirdH), Offset(right, top + thirdH), strokeWidth = 1.dp.toPx())
                drawLine(Color.White.copy(alpha = 0.3f), Offset(left, top + 2 * thirdH), Offset(right, top + 2 * thirdH), strokeWidth = 1.dp.toPx())

                // Viền khung
                drawRect(Color.White, topLeft = Offset(left, top), size = Size(cropWidth, cropHeight), style = Stroke(width = 1.5.dp.toPx()))

                // 4 góc vuông màu hồng
                val cornerLength = 24.dp.toPx()
                val cornerStroke = 4.dp.toPx()
                val cornerColor = Color(0xFFFE0377)

                drawLine(cornerColor, Offset(left, top), Offset(left + cornerLength, top), strokeWidth = cornerStroke)
                drawLine(cornerColor, Offset(left, top), Offset(left, top + cornerLength), strokeWidth = cornerStroke)

                drawLine(cornerColor, Offset(right, top), Offset(right - cornerLength, top), strokeWidth = cornerStroke)
                drawLine(cornerColor, Offset(right, top), Offset(right, top + cornerLength), strokeWidth = cornerStroke)

                drawLine(cornerColor, Offset(left, bottom), Offset(left + cornerLength, bottom), strokeWidth = cornerStroke)
                drawLine(cornerColor, Offset(left, bottom), Offset(left, bottom - cornerLength), strokeWidth = cornerStroke)

                drawLine(cornerColor, Offset(right, bottom), Offset(right - cornerLength, bottom), strokeWidth = cornerStroke)
                drawLine(cornerColor, Offset(right, bottom), Offset(right, bottom - cornerLength), strokeWidth = cornerStroke)
            }
        }

        // --- 2. TOP BAR (Nút Đóng & Nút Tích Xác Nhận) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(Color(0xFF121318))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onDismiss() }) {
                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
            }

            Text("Chọn tỷ lệ cắt ảnh", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)

            IconButton(onClick = {
                try {
                    val canvasWidth = 1080f
                    val canvasHeight = 1920f

                    val matrix = Matrix().apply {
                        postRotate(rotation)
                        if (isFlipped) postScale(-1f, 1f)
                    }
                    val rotatedBitmap = Bitmap.createBitmap(
                        bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                    )
                    val finalCropped = Bitmap.createBitmap(
                        rotatedBitmap,
                        0,
                        0,
                        rotatedBitmap.width,
                        rotatedBitmap.height,
                        matrix,
                        true
                    )

                    onCropFinished(rotatedBitmap)
                } catch (e: Exception) {
                    onDismiss()
                }
            }) {
                Icon(Icons.Default.Check, contentDescription = "Done", tint = Color(0xFFFE0377))
            }
        }

        // --- 3. THANH CÔNG CỤ DƯỚI (Chọn tỷ lệ & Xoay/Lật) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color(0xFF121318))
                .padding(vertical = 8.dp)
        ) {
            // Danh sách chọn tỷ lệ phổ biến
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(CropRatio.entries) { ratioItem ->
                    val isSelected = selectedRatio == ratioItem
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color(0xFFFE0377) else Color(0xFF22232A))
                            .clickable { selectedRatio = ratioItem }
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ratioItem.label,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF22232A), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

            // Nút Xoay & Lật ảnh
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { rotation = (rotation + 90f) % 360f }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RotateRight, contentDescription = null, tint = Color(0xFFFE0377))
                        Spacer(Modifier.width(4.dp))
                        Text("Xoay 90°", color = Color.White)
                    }
                }

                TextButton(onClick = { isFlipped = !isFlipped }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Flip, contentDescription = null, tint = Color(0xFFFE0377))
                        Spacer(Modifier.width(4.dp))
                        Text("Lật ảnh", color = Color.White)
                    }
                }
            }
        }
    }
}