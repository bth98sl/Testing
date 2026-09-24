package com.example.blink.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.example.blink.FilterType
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

enum class EditorTab { FILTERS, ADJUST, CROP, EFFECTS, AI_TOOLS }
enum class AdjustOption { EXPOSURE, CONTRAST, BRIGHTNESS, HIGHLIGHTS, SHADOWS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    bitmap: Bitmap?,
    activeFilter: FilterType,
    isAutoEnhanced: Boolean,
    isBgRemoved: Boolean,
    onToggleAutoEnhance: () -> Unit,
    onToggleRemoveBg: () -> Unit,
    onSelectFilter: (FilterType) -> Unit,
    onApplyAdjustments: (exposure: Float, contrast: Float, brightness: Float) -> Unit,
    onProceed: () -> Unit,
    onBackClick: () -> Unit,
    onBitmapUpdated: ((Bitmap) -> Unit)? = null
) {
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(EditorTab.ADJUST) }
    var selectedAdjustOption by remember { mutableStateOf(AdjustOption.BRIGHTNESS) }

    var exposureValue by remember { mutableFloatStateOf(0f) }
    var contrastValue by remember { mutableFloatStateOf(1f) }
    var brightnessValue by remember { mutableFloatStateOf(0f) }
    var highlightsValue by remember { mutableFloatStateOf(0f) }
    var shadowsValue by remember { mutableFloatStateOf(0f) }

    var isProcessingAI by remember { mutableStateOf(false) }
    var processedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // 🟢 Khởi tạo Launcher gọi màn hình Crop chuẩn của CanHub (hỗ trợ kéo 4 góc mượt mà, không crash/ANR)
    val cropImageLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                val inputStream = context.contentResolver.openInputStream(uri)
                val croppedBitmap = BitmapFactory.decodeStream(inputStream)
                if (croppedBitmap != null) {
                    processedBitmap = croppedBitmap
                    onBitmapUpdated?.invoke(croppedBitmap)
                    Toast.makeText(context, "Đã cắt ảnh thành công!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        selectedTab = EditorTab.ADJUST
    }

    // Hàm thực hiện chuẩn bị Uri và mở màn hình Crop
    fun launchCropTool() {
        val currentBmp = processedBitmap ?: return
        try {
            val cacheFile = File(context.cacheDir, "crop_temp_${System.currentTimeMillis()}.jpg")
            cacheFile.outputStream().use { out ->
                currentBmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            val cropOptions = CropImageOptions().apply {
                guidelines = CropImageView.Guidelines.ON
                activityBackgroundColor = android.graphics.Color.parseColor("#07080A")
                toolbarColor = android.graphics.Color.parseColor("#121318")
                toolbarTintColor = android.graphics.Color.WHITE
                toolbarBackButtonColor = android.graphics.Color.WHITE
                toolbarTitleColor = android.graphics.Color.WHITE
                cropMenuCropButtonTitle = "Lưu"
                borderCornerColor = android.graphics.Color.parseColor("#FE0377") // Màu hồng chủ đạo Blink
                borderLineColor = android.graphics.Color.WHITE
                fixAspectRatio = false // Cho phép người dùng kéo tự do các cạnh và góc
            }

            cropImageLauncher.launch(CropImageContractOptions(uri, cropOptions))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Lỗi mở công cụ cắt ảnh: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(bitmap) {
        if (bitmap != null) {
            withContext(Dispatchers.IO) {
                processedBitmap = safeResizeBitmap(bitmap, 1920)
            }
        } else {
            processedBitmap = null
        }
    }

    fun processRemoveBackground() {
        if (processedBitmap == null) return
        isProcessingAI = true

        val inputImage = InputImage.fromBitmap(processedBitmap!!, 0)
        val options = SubjectSegmenterOptions.Builder()
            .enableForegroundBitmap()
            .build()

        val segmenter = SubjectSegmentation.getClient(options)

        segmenter.process(inputImage)
            .addOnSuccessListener { result ->
                isProcessingAI = false
                val foregroundBitmap = result.foregroundBitmap
                if (foregroundBitmap != null) {
                    processedBitmap = foregroundBitmap
                    onBitmapUpdated?.invoke(foregroundBitmap)
                    onToggleRemoveBg()
                    Toast.makeText(context, "Background Removed!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "No subject detected!", Toast.LENGTH_SHORT).show()
                }
                segmenter.close()
            }
            .addOnFailureListener { e ->
                isProcessingAI = false
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                segmenter.close()
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF07080A))
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // --- 1. TOP BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onBackClick() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1F25))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { /* Undo */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    Text(
                        text = when (selectedTab) {
                            EditorTab.ADJUST -> selectedAdjustOption.name.lowercase().capitalizeWords()
                            EditorTab.AI_TOOLS -> "AI Tools"
                            EditorTab.FILTERS -> "Filters"
                            EditorTab.EFFECTS -> "Effects"
                            else -> "Editor"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    IconButton(onClick = { /* Redo */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                TextButton(onClick = { onProceed() }) {
                    Text(
                        text = "Save",
                        color = Color(0xFFFE0377),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            // --- 2. CANVAS HIỂN THỊ ẢNH CHÍNH ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (processedBitmap != null) {
                    Image(
                        bitmap = processedBitmap!!.asImageBitmap(),
                        contentDescription = "Editing Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    CircularProgressIndicator(color = Color(0xFFFE0377))
                }

                if (isProcessingAI) {
                    CircularProgressIndicator(color = Color(0xFFFE0377))
                }
            }

            // --- 3. THANH CÔNG CỤ DƯỚI ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF121318))
                    .padding(top = 12.dp, bottom = 8.dp)
            ) {
                when (selectedTab) {
                    EditorTab.ADJUST -> {
                        val (currentVal, range) = when (selectedAdjustOption) {
                            AdjustOption.EXPOSURE -> exposureValue to (-100f..100f)
                            AdjustOption.CONTRAST -> contrastValue to (0.5f..1.5f)
                            AdjustOption.BRIGHTNESS -> brightnessValue to (-100f..100f)
                            AdjustOption.HIGHLIGHTS -> highlightsValue to (-50f..50f)
                            AdjustOption.SHADOWS -> shadowsValue to (-50f..50f)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(selectedAdjustOption.name.lowercase().capitalizeWords(), color = Color.Gray, fontSize = 14.sp)
                            Text(
                                text = String.format(Locale.US, "%.0f", currentVal),
                                color = Color(0xFFFE0377),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Slider(
                            value = currentVal,
                            onValueChange = { newVal ->
                                when (selectedAdjustOption) {
                                    AdjustOption.EXPOSURE -> exposureValue = newVal
                                    AdjustOption.CONTRAST -> contrastValue = newVal
                                    AdjustOption.BRIGHTNESS -> brightnessValue = newVal
                                    AdjustOption.HIGHLIGHTS -> highlightsValue = newVal
                                    AdjustOption.SHADOWS -> shadowsValue = newVal
                                }
                                onApplyAdjustments(exposureValue, contrastValue, brightnessValue)
                            },
                            valueRange = range,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFE0377),
                                activeTrackColor = Color(0xFFFE0377),
                                inactiveTrackColor = Color(0xFF2C2D35)
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            items(AdjustOption.entries) { option ->
                                Text(
                                    text = option.name.lowercase().capitalizeWords(),
                                    color = if (selectedAdjustOption == option) Color.White else Color.Gray,
                                    fontWeight = if (selectedAdjustOption == option) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    modifier = Modifier
                                        .clickable { selectedAdjustOption = option }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    EditorTab.AI_TOOLS -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = {
                                    onToggleAutoEnhance()
                                    onApplyAdjustments(15f, 1.15f, 10f)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAutoEnhanced) Color(0xFFFE0377) else Color(0xFF22232A)
                                )
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                                Spacer(Modifier.width(6.dp))
                                Text("Auto Enhance")
                            }

                            Button(
                                onClick = { processRemoveBackground() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isBgRemoved) Color(0xFFFE0377) else Color(0xFF22232A)
                                )
                            ) {
                                Icon(Icons.Default.Face, contentDescription = null, tint = Color.White)
                                Spacer(Modifier.width(6.dp))
                                Text("Remove BG")
                            }
                        }
                    }

                    EditorTab.FILTERS -> {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(FilterType.entries) { filter ->
                                FilterChipItem(
                                    title = filter.name,
                                    isSelected = activeFilter == filter,
                                    onClick = { onSelectFilter(filter) }
                                )
                            }
                        }
                    }

                    else -> {}
                }

                HorizontalDivider(color = Color(0xFF22232A), thickness = 1.dp)

                // --- 4. NAVIGATION TABS ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EditorTabItem(
                        icon = Icons.Default.Filter,
                        label = "Filters",
                        isSelected = selectedTab == EditorTab.FILTERS,
                        onClick = { selectedTab = EditorTab.FILTERS }
                    )
                    EditorTabItem(
                        icon = Icons.Default.Tune,
                        label = "Adjust",
                        isSelected = selectedTab == EditorTab.ADJUST,
                        onClick = { selectedTab = EditorTab.ADJUST }
                    )
                    EditorTabItem(
                        icon = Icons.Default.Crop,
                        label = "Crop",
                        isSelected = selectedTab == EditorTab.CROP,
                        onClick = {
                            selectedTab = EditorTab.CROP
                            if (processedBitmap != null) {
                                launchCropTool() // 🟢 Mở màn hình cắt ảnh chuyên nghiệp có kéo 4 góc/cạnh
                            }
                        }
                    )
                    EditorTabItem(
                        icon = Icons.Default.AutoAwesome,
                        label = "AI Tools",
                        isSelected = selectedTab == EditorTab.AI_TOOLS,
                        onClick = { selectedTab = EditorTab.AI_TOOLS }
                    )
                }
            }
        }
    }
}

private fun safeResizeBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    if (width <= maxDimension && height <= maxDimension) return bitmap

    val ratio = width.toFloat() / height.toFloat()
    val newWidth: Int
    val newHeight: Int
    if (width > height) {
        newWidth = maxDimension
        newHeight = (maxDimension / ratio).toInt()
    } else {
        newHeight = maxDimension
        newWidth = (maxDimension * ratio).toInt()
    }
    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
}

@Composable
private fun EditorTabItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color(0xFFFE0377) else Color.Gray,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isSelected) Color.White else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun FilterChipItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) Color(0xFFFE0377) else Color(0xFF22232A)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title.take(3),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, color = Color.White, fontSize = 11.sp)
    }
}

private fun String.capitalizeWords(): String =
    this.replace("_", " ")
        .split(" ")
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        }