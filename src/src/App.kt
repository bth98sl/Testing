package com.example.blink

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

// ==========================================
// 1. DATA MODELS & ENUMS
// ==========================================

enum class ScreenType {
    SPLASH, ONBOARDING, PAYWALL, HOME, UNSPLASH, EDITOR, RESULT, PROFILE, SETTINGS
}

data class SampleImage(val id: String, val url: String)
data class UnsplashPhoto(val id: String, val url: String)

data class UserProfile(
    val name: String = "Alex Morgan",
    val email: String = "alex.creator@android.dev",
    val bio: String = "Mobile Photographer & Android Visual Creator",
    val avatarUrl: String = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80",
    val joinedDate: String = "September 2026",
    val photosEditedCount: Int = 8,
    val aiEnhanceCount: Int = 14,
    val bgRemovedCount: Int = 6
)

data class AppSettings(
    val exportFormat: String = "auto",
    val exportQuality: Float = 0.94f,
    val maxResolution: Int = 1920,
    val bgTolerance: Int = 38,
    val hapticFeedback: Boolean = true,
    val autoSaveToDevice: Boolean = true,
    val themeMode: String = "dark_amoled",
    val savePath: String = "Pictures/PhotoEditor"
)

data class ResultImage(
    val dataUrl: String,
    val byteArray: ByteArray? = null
)

// ==========================================
// 2. MAIN COMPOSABLE APP
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlinkApp() {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("android_photo_editor_prefs", Context.MODE_PRIVATE)
    }

    // States (Tương đương useState)
    var currentScreen by rememberSaveable { mutableStateOf(ScreenType.ONBOARDING) }
    var previousScreen by rememberSaveable { mutableStateOf(ScreenType.HOME) }
    var selectedImage by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedImageName by rememberSaveable { mutableStateOf("image.jpg") }
    var resultImage by remember { mutableStateOf<ResultImage?>(null) }
    var isCodeModalOpen by rememberSaveable { mutableStateOf(false) }
    
    var isProUser by rememberSaveable {
        mutableStateOf(sharedPreferences.getBoolean("is_pro", false))
    }

    var userProfile by remember { mutableStateOf(UserProfile()) }
    var appSettings by remember { mutableStateOf(AppSettings()) }

    // Helper Điều hướng
    fun navigateTo(target: ScreenType) {
        if (target != currentScreen) {
            previousScreen = currentScreen
            currentScreen = target
        }
    }

    // Điều hướng nút Back cứng/vuốt Android (BackHandler)
    fun handleBack() {
        when (currentScreen) {
            ScreenType.SETTINGS, ScreenType.PROFILE, ScreenType.UNSPLASH,
            ScreenType.ONBOARDING, ScreenType.PAYWALL -> {
                if (selectedImage != null && (previousScreen == ScreenType.EDITOR || previousScreen == ScreenType.RESULT)) {
                    currentScreen = previousScreen
                } else {
                    currentScreen = ScreenType.HOME
                }
            }
            ScreenType.RESULT -> currentScreen = ScreenType.EDITOR
            ScreenType.EDITOR -> currentScreen = ScreenType.HOME
            else -> {}
        }
    }

    BackHandler(enabled = currentScreen != ScreenType.HOME) {
        handleBack()
    }

    // Tiêu đề & phụ đề TopAppBar
    val (title, subtitle) = when (currentScreen) {
        ScreenType.PAYWALL -> "Blink PRO" to "Unlock Creative Powers"
        ScreenType.PROFILE -> "User Profile" to userProfile.name
        ScreenType.SETTINGS -> "System Settings" to "Material 3 • Export Preferences"
        ScreenType.UNSPLASH -> "Unsplash Gallery" to "Explore & Edit Free Photos"
        ScreenType.SPLASH -> "App Launch" to "Material 3 • Jetpack Compose"
        ScreenType.ONBOARDING -> "Welcome Tour" to "Blink Photo Editor • Material 3"
        ScreenType.EDITOR -> "Photo Studio" to selectedImageName
        ScreenType.RESULT -> "Export & Save" to "Ready to share and download"
        ScreenType.HOME -> "Blink Photo Editor" to "Material 3 • Jetpack Compose"
    }

    val showTopBar = currentScreen !in listOf(ScreenType.ONBOARDING, ScreenType.SPLASH, ScreenType.PAYWALL)

    // Khung giao diện chính (Scaffold)
    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBarComponent(
                    title = title,
                    subtitle = subtitle,
                    showBack = currentScreen != ScreenType.HOME,
                    onBack = { handleBack() },
                    showReset = currentScreen == ScreenType.EDITOR,
                    onReset = {
                        val temp = selectedImage
                        selectedImage = null
                        selectedImage = temp
                    },
                    onOpenCodeModal = { isCodeModalOpen = true },
                    onOpenPaywall = { navigateTo(ScreenType.PAYWALL) },
                    isPro = isProUser
                )
            }
        },
        bottomBar = {
            AndroidNavigationBarComponent(
                currentScreen = currentScreen,
                onNavigate = { navigateTo(it) },
                hasActiveEdit = selectedImage != null
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Chuyển cảnh hiệu ứng mượt giữa các màn hình (Crossfade)
            Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                when (screen) {
                    ScreenType.SPLASH -> SplashScreen(onFinish = { navigateTo(ScreenType.ONBOARDING) })
                    ScreenType.ONBOARDING -> OnboardingScreen(onComplete = {
                        sharedPreferences.edit().putBoolean("has_seen_onboarding", true).apply()
                        navigateTo(ScreenType.PAYWALL)
                    })
                    ScreenType.PAYWALL -> PaywallScreen(
                        onDismiss = { navigateTo(ScreenType.HOME) },
                        onSubscribe = { _ ->
                            isProUser = true
                            sharedPreferences.edit().putBoolean("is_pro", true).apply()
                            navigateTo(ScreenType.HOME)
                        }
                    )
                    ScreenType.HOME -> HomeScreen(
                        onImageSelected = { url, name ->
                            selectedImage = url
                            selectedImageName = name
                            navigateTo(ScreenType.EDITOR)
                        },
                        onSelectSample = { sample ->
                            selectedImage = sample.url
                            selectedImageName = "${sample.id}.jpg"
                            navigateTo(ScreenType.EDITOR)
                        },
                        onNavigateToUnsplash = { navigateTo(ScreenType.UNSPLASH) }
                    )
                    ScreenType.UNSPLASH -> UnsplashScreen(
                        onSelectPhotoToEdit = { photo ->
                            selectedImage = photo.url
                            selectedImageName = "${photo.id}.jpg"
                            navigateTo(ScreenType.EDITOR)
                        }
                    )
                    ScreenType.EDITOR -> {
                        selectedImage?.let { imgSource ->
                            EditorScreen(
                                imageSource = imgSource,
                                imageName = selectedImageName,
                                settings = appSettings,
                                onProceedToResult = { dataUrl ->
                                    resultImage = ResultImage(dataUrl)
                                    userProfile = userProfile.copy(photosEditedCount = userProfile.photosEditedCount + 1)
                                    navigateTo(ScreenType.RESULT)
                                },
                                onUsedEnhance = {
                                    userProfile = userProfile.copy(aiEnhanceCount = userProfile.aiEnhanceCount + 1)
                                },
                                onUsedRemoveBg = {
                                    userProfile = userProfile.copy(bgRemovedCount = userProfile.bgRemovedCount + 1)
                                }
                            )
                        }
                    }
                    ScreenType.RESULT -> {
                        resultImage?.let { res ->
                            ResultScreen(
                                resultDataUrl = res.dataUrl,
                                originalFileName = selectedImageName,
                                onEditAgain = { navigateTo(ScreenType.EDITOR) },
                                onPickNewImage = {
                                    selectedImage = null
                                    resultImage = null
                                    navigateTo(ScreenType.HOME)
                                }
                            )
                        }
                    }
                    ScreenType.PROFILE -> ProfileScreen(
                        profile = userProfile,
                        onUpdateProfile = { updated -> userProfile = updated },
                        onNavigateToSettings = { navigateTo(ScreenType.SETTINGS) }
                    )
                    ScreenType.SETTINGS -> SettingsScreen(
                        settings = appSettings,
                        onUpdateSettings = { updated -> appSettings = updated },
                        onResetSettings = { appSettings = AppSettings() },
                        onOpenCodeModal = { isCodeModalOpen = true },
                        onReopenOnboarding = { navigateTo(ScreenType.SPLASH) }
                    )
                }
            }

            if (isCodeModalOpen) {
                KotlinCodeModalDialog(onDismiss = { isCodeModalOpen = false })
            }
        }
    }
}