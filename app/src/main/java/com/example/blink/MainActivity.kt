package com.example.blink

import android.content.ContentValues
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream

// Import các màn hình từ package ui.screens
import com.example.blink.ui.screens.EditorScreen
import com.example.blink.ui.screens.GalleryScreen
import com.example.blink.ui.screens.HomeScreen
import com.example.blink.ui.screens.OnboardingScreen
import com.example.blink.ui.screens.PaywallScreen
import com.example.blink.ui.screens.SplashScreen

enum class Screen { SPLASH, ONBOARDING, PAYWALL, HOME, GALLERY, UNSPLASH, EDITOR, RESULT, SETTINGS }
enum class FilterType { NONE, VINTAGE, CINEMATIC, BW, BRIGHT }

data class RecentItem(
    val id: String,
    val title: String,
    val imageUrl: String,
    val timestamp: Long = System.currentTimeMillis()
)

fun formatTimeAgo(timestamp: Long): String {
    val diffSec = (System.currentTimeMillis() - timestamp) / 1000
    return when {
        diffSec < 60 -> "Just now"
        diffSec < 3600 -> "${diffSec / 60} mins ago"
        diffSec < 86400 -> "${diffSec / 3600} hours ago"
        diffSec < 172800 -> "Yesterday"
        else -> "${diffSec / 86400} days ago"
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    surface = Color(0xFF07080A),
                    primary = Color(0xFFFE0377),
                    onPrimary = Color.White,
                    surfaceVariant = Color(0xFF121318)
                )
            ) {
                PhotoEditorApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditorApp() {
    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }
    var previousScreen by remember { mutableStateOf(Screen.HOME) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var processedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var activeFilter by remember { mutableStateOf(FilterType.NONE) }
    var isAutoEnhanced by remember { mutableStateOf(false) }
    var isBgRemoved by remember { mutableStateOf(false) }
    var isProUser by remember { mutableStateOf(false) }

    var userName by remember { mutableStateOf("User Name") }
    var userAvatarUrl by remember { mutableStateOf<String?>(null) }

    var recentItems by remember {
        mutableStateOf(
            listOf(
                RecentItem("1", "Urban Nightscape", "https://images.unsplash.com/photo-1514565131-fce0801e5785?w=500", System.currentTimeMillis() - 600000),
                RecentItem("2", "Moody Portrait", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500", System.currentTimeMillis() - 7200000),
                RecentItem("3", "Morning Vintage", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500", System.currentTimeMillis() - 86400000)
            )
        )
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    fun addRecentImage(title: String, urlOrUri: String) {
        val newItem = RecentItem(
            id = System.currentTimeMillis().toString(),
            title = title,
            imageUrl = urlOrUri,
            timestamp = System.currentTimeMillis()
        )
        recentItems = listOf(newItem) + recentItems.filterNot { it.imageUrl == urlOrUri }
    }

    fun loadBitmapFromUrlAndNavigate(imageUrl: String, source: Screen = Screen.HOME) {
        coroutineScope.launch {
            val bitmap = loadBitmapFromUrl(context, imageUrl)
            if (bitmap != null) {
                selectedBitmap = bitmap
                processedBitmap = bitmap
                previousScreen = source
                currentScreen = Screen.EDITOR
            }
        }
    }

    Scaffold(
        topBar = {
            if (currentScreen != Screen.ONBOARDING &&
                currentScreen != Screen.SPLASH &&
                currentScreen != Screen.PAYWALL &&
                currentScreen != Screen.HOME &&
                currentScreen != Screen.GALLERY &&
                currentScreen != Screen.EDITOR) {

                TopAppBar(
                    title = {
                        Text(
                            when (currentScreen) {
                                Screen.UNSPLASH -> "Unsplash Gallery"
                                Screen.SETTINGS -> "Settings"
                                else -> ""
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            currentScreen = when (currentScreen) {
                                Screen.RESULT -> Screen.EDITOR
                                Screen.SETTINGS, Screen.UNSPLASH -> if (selectedBitmap != null) Screen.EDITOR else Screen.HOME
                                else -> Screen.HOME
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF07080A),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        },
        bottomBar = {
            if (currentScreen != Screen.ONBOARDING &&
                currentScreen != Screen.SPLASH &&
                currentScreen != Screen.PAYWALL &&
                currentScreen != Screen.EDITOR &&
                currentScreen != Screen.GALLERY) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xB307080A),
                                    Color(0xFF07080A)
                                )
                            )
                        )
                        .navigationBarsPadding()
                        .height(84.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isHomeSelected = currentScreen in listOf(Screen.HOME, Screen.EDITOR, Screen.RESULT)
                        GlassNavItem(
                            isSelected = isHomeSelected,
                            icon = Icons.Default.Home,
                            onClick = { currentScreen = if (selectedBitmap != null) Screen.EDITOR else Screen.HOME }
                        )

                        val isUnsplashSelected = currentScreen == Screen.UNSPLASH
                        GlassNavItem(
                            isSelected = isUnsplashSelected,
                            icon = Icons.Outlined.Explore,
                            onClick = { currentScreen = Screen.UNSPLASH }
                        )

                        val isSettingsSelected = currentScreen == Screen.SETTINGS
                        GlassNavItem(
                            isSelected = isSettingsSelected,
                            icon = Icons.Default.Settings,
                            onClick = { currentScreen = Screen.SETTINGS }
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFF07080A)
    ) { paddingValues ->
        // 🟢 CẬP NHẬT: Cho phép HOME, UNSPLASH, SETTINGS tràn xuống dưới Bottom Bar
        val isFullscreenScreen = currentScreen in listOf(
            Screen.HOME, Screen.UNSPLASH, Screen.SETTINGS,
            Screen.GALLERY, Screen.EDITOR, Screen.SPLASH, Screen.ONBOARDING, Screen.PAYWALL
        )

        Box(
            modifier = Modifier
                .padding(
                    top = if (isFullscreenScreen) 0.dp else paddingValues.calculateTopPadding(),
                    bottom = if (isFullscreenScreen) 0.dp else paddingValues.calculateBottomPadding()
                )
                .fillMaxSize()
        ) {
            when (currentScreen) {
                Screen.SPLASH -> SplashScreen(
                    onFinish = { currentScreen = Screen.ONBOARDING }
                )

                Screen.ONBOARDING -> OnboardingScreen(
                    onComplete = { currentScreen = Screen.PAYWALL }
                )

                Screen.PAYWALL -> PaywallScreen(
                    onDismiss = { currentScreen = Screen.HOME },
                    onSubscribe = { _ ->
                        isProUser = true
                        currentScreen = Screen.HOME
                    }
                )

                Screen.HOME -> HomeScreen(
                    isProUser = isProUser,
                    userName = userName,
                    userAvatarUrl = userAvatarUrl,
                    recentList = recentItems,
                    onImageSelected = { uriOrUrl, title ->
                        addRecentImage(title, uriOrUrl)
                        previousScreen = Screen.HOME
                        if (uriOrUrl.startsWith("http")) {
                            loadBitmapFromUrlAndNavigate(uriOrUrl, Screen.HOME)
                        } else {
                            val bitmap = loadBitmapFromUri(context, Uri.parse(uriOrUrl))
                            selectedBitmap = bitmap
                            processedBitmap = bitmap
                            currentScreen = Screen.EDITOR
                        }
                    },
                    onNavigateToGallery = { currentScreen = Screen.GALLERY },
                    onNavigateToUnsplash = { currentScreen = Screen.UNSPLASH },
                    onNavigateToSettings = { currentScreen = Screen.SETTINGS },
                    onOpenPaywall = { currentScreen = Screen.PAYWALL },
                    onOpenCodeModal = { }
                )

                Screen.GALLERY -> GalleryScreen(
                    onImageSelected = { any ->
                        val uri = any as Uri
                        val uriString = uri.toString()
                        addRecentImage("Gallery Photo", uriString)
                        val bitmap = loadBitmapFromUri(context, uri)
                        selectedBitmap = bitmap
                        processedBitmap = bitmap
                        previousScreen = Screen.GALLERY
                        currentScreen = Screen.EDITOR
                    },
                    onBackClick = { currentScreen = Screen.HOME }
                )

                Screen.EDITOR -> EditorScreen(
                    bitmap = processedBitmap ?: selectedBitmap,
                    activeFilter = activeFilter,
                    isAutoEnhanced = isAutoEnhanced,
                    isBgRemoved = isBgRemoved,
                    onToggleAutoEnhance = {
                        isAutoEnhanced = !isAutoEnhanced
                        coroutineScope.launch {
                            processedBitmap = applyClientProcessing(
                                source = selectedBitmap,
                                activeFilter = activeFilter
                            )
                        }
                    },
                    onToggleRemoveBg = {
                        isBgRemoved = !isBgRemoved
                        coroutineScope.launch {
                            processedBitmap = applyClientProcessing(
                                source = selectedBitmap,
                                activeFilter = activeFilter
                            )
                        }
                    },
                    onSelectFilter = { filter ->
                        activeFilter = filter
                        coroutineScope.launch {
                            processedBitmap = applyClientProcessing(
                                source = selectedBitmap,
                                activeFilter = filter
                            )
                        }
                    },
                    onApplyAdjustments = { exposure, contrast, brightness ->
                        coroutineScope.launch {
                            processedBitmap = applyClientProcessing(
                                source = selectedBitmap,
                                exposure = exposure,
                                contrast = contrast,
                                brightness = brightness,
                                activeFilter = activeFilter
                            )
                        }
                    },
                    onBitmapUpdated = { newBitmap ->
                        selectedBitmap = newBitmap
                        processedBitmap = newBitmap
                    },
                    onProceed = { currentScreen = Screen.RESULT },
                    onBackClick = { currentScreen = previousScreen }
                )

                Screen.RESULT -> ResultScreen(
                    bitmap = processedBitmap ?: selectedBitmap,
                    onSaveToDevice = {
                        (processedBitmap ?: selectedBitmap)?.let { bmp ->
                            saveBitmapToGallery(context, bmp)
                        }
                    }
                )

                Screen.SETTINGS -> SettingsScreen()

                Screen.UNSPLASH -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Unsplash Explorer Screen",
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResultScreen(bitmap: Bitmap?, onSaveToDevice: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit
                )
            }
        }

        Button(
            onClick = onSaveToDevice,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp)
                .height(52.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE0377))
        ) {
            Icon(Icons.Default.Download, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Save to Device", color = Color.White, fontSize = 16.sp)
        }
    }
}

suspend fun applyClientProcessing(
    source: Bitmap?,
    exposure: Float = 0f,
    contrast: Float = 1f,
    brightness: Float = 0f,
    highlights: Float = 0f,
    shadows: Float = 0f,
    activeFilter: FilterType = FilterType.NONE
): Bitmap? = withContext(Dispatchers.Default) {
    if (source == null) return@withContext null

    val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(result)
    val paint = Paint()

    val cm = ColorMatrix()

    val c = contrast
    val b = brightness + (exposure * 1.2f)
    val contrastMatrix = floatArrayOf(
        c, 0f, 0f, 0f, b,
        0f, c, 0f, 0f, b,
        0f, 0f, c, 0f, b,
        0f, 0f, 0f, 1f, 0f
    )
    cm.postConcat(ColorMatrix(contrastMatrix))

    when (activeFilter) {
        FilterType.BW -> cm.postConcat(ColorMatrix().apply { setSaturation(0f) })
        FilterType.VINTAGE -> cm.postConcat(ColorMatrix(floatArrayOf(
            1.1f, 0f, 0f, 0f, 10f,
            0f, 0.95f, 0f, 0f, 0f,
            0f, 0f, 0.8f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )))
        FilterType.BRIGHT -> cm.postConcat(ColorMatrix(floatArrayOf(
            1.15f, 0f, 0f, 0f, 10f,
            0f, 1.15f, 0f, 0f, 10f,
            0f, 0f, 1.15f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )))
        FilterType.CINEMATIC -> cm.postConcat(ColorMatrix(floatArrayOf(
            1.2f, 0f, 0f, 0f, 10f,
            0f, 1.05f, 0f, 0f, 5f,
            0f, 0f, 0.85f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )))
        FilterType.NONE -> {}
    }

    paint.colorFilter = ColorMatrixColorFilter(cm)
    canvas.drawBitmap(source, 0f, 0f, paint)
    result
}

fun saveBitmapToGallery(context: Context, bitmap: Bitmap) {
    val filename = "IMG_EDIT_${System.currentTimeMillis()}.jpg"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/PhotoEditor")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
    }

    val resolver = context.contentResolver
    val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    imageUri?.let { uri ->
        resolver.openOutputStream(uri)?.use { stream: OutputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }
    }
}

fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return runCatching {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    }.getOrNull()
}

suspend fun loadBitmapFromUrl(context: Context, urlString: String): Bitmap? = withContext(Dispatchers.IO) {
    runCatching {
        val url = java.net.URL(urlString)
        val connection = url.openConnection()
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        connection.getInputStream().use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    }.getOrNull()
}

@Composable
fun SettingsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Settings Screen", color = Color.White, fontSize = 20.sp)
    }
}

@Composable
private fun GlassNavItem(
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val activeColor = Color.White
    val inactiveColor = Color.White.copy(alpha = 0.5f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            surface = Color(0xFF07080A),
            primary = Color(0xFFFE0377),
            onPrimary = Color.White,
            surfaceVariant = Color(0xFF121318)
        )
    ) {
        HomeScreen(
            isProUser = false,
            userName = "User Name",
            userAvatarUrl = null,
            recentList = listOf(
                RecentItem("1", "Urban Nightscape", "https://images.unsplash.com/photo-1514565131-fce0801e5785?w=500", System.currentTimeMillis() - 600000),
                RecentItem("2", "Moody Portrait", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500", System.currentTimeMillis() - 7200000)
            ),
            onImageSelected = { _, _ -> },
            onNavigateToGallery = {},
            onNavigateToUnsplash = {},
            onNavigateToSettings = {},
            onOpenPaywall = {},
            onOpenCodeModal = {}
        )
    }
}