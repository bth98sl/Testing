package com.example.blink

import androidx.compose.ui.graphics.vector.ImageVector

// ==========================================
// ENUM TYPES
// ==========================================

enum class FilterPreset {
    NONE,
    VINTAGE,
    CINEMATIC,
    BW,
    BRIGHT
}

enum class UnsplashCategory {
    ALL,
    PORTRAIT,
    NATURE,
    URBAN,
    MINIMAL,
    ARCHITECTURE
}

enum class SampleCategory {
    PORTRAIT,
    NATURE,
    URBAN
}

enum class ExportFormat {
    AUTO,
    PNG,
    JPEG
}

enum class ThemeMode {
    DARK_AMOLED,
    DARK_MATERIAL
}

// ==========================================
// DATA CLASSES
// ==========================================

data class UnsplashPhoto(
    val id: String,
    val title: String,
    val author: String,
    val authorUsername: String,
    val category: UnsplashCategory,
    val url: String,
    val thumbUrl: String,
    val likes: Int,
    val dimensions: String,
    val colorTone: String
)

data class EnhanceParams(
    val brightness: Float = 0f,   // -0.3 to 0.3
    val contrast: Float = 0f,     // -0.3 to 0.4
    val saturation: Float = 0f,   // -0.3 to 0.5
    val warmth: Float = 0f,       // -0.3 to 0.3
    val highlights: Float = 0f,   // -0.4 to 0.3
    val shadows: Float = 0f,      // -0.3 to 0.4
    val sharpness: Float = 0f,    // 0 to 0.4
    val explanation: String = ""
)

data class SampleImage(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: SampleCategory,
    val url: String,
    val thumbnail: String
)

data class FilterInfo(
    val id: FilterPreset,
    val name: String,
    val description: String,
    val iconName: String
)

data class UserProfile(
    val name: String,
    val email: String,
    val bio: String,
    val avatarUrl: String,
    val joinedDate: String,
    val photosEditedCount: Int,
    val aiEnhanceCount: Int,
    val bgRemovedCount: Int
)

data class AppSettings(
    val exportFormat: ExportFormat = ExportFormat.AUTO,
    val exportQuality: Float = 0.94f, // 0.85 to 1.0
    val maxResolution: Int = 1920,    // 1080, 1920, 2560
    val bgTolerance: Int = 38,        // 25 to 55
    val hapticFeedback: Boolean = true,
    val autoSaveToDevice: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.DARK_AMOLED,
    val savePath: String = "Pictures/PhotoEditor"
)
