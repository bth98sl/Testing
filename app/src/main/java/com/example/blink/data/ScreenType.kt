package com.example.blink.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Định nghĩa danh sách các màn hình chính trong ứng dụng Blink
 */
enum class ScreenType(
    val title: String,
    val icon: ImageVector
) {
    SPLASH(
        title = "Splash",
        icon = Icons.Default.Image
    ),
    ONBOARDING(
        title = "Onboarding",
        icon = Icons.Default.Image
    ),
    PAYWALL(
        title = "Paywall",
        icon = Icons.Default.Image
    ),
    HOME(
        title = "Photos",
        icon = Icons.Default.Image
    ),
    UNSPLASH(
        title = "Unsplash",
        icon = Icons.Default.Explore
    ),
    EDITOR(
        title = "Editor",
        icon = Icons.Default.Image
    ),
    RESULT(
        title = "Result",
        icon = Icons.Default.Image
    ),
    PROFILE(
        title = "Profile",
        icon = Icons.Default.Person
    ),
    SETTINGS(
        title = "Settings",
        icon = Icons.Default.Settings
    )
}
