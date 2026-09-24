package com.example.blink.ui.components

import com.example.blink.data.ScreenType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


private data class NavItemData(
  val id: ScreenType,
  val label: String,
  val icon: ImageVector,
  val badge: String? = null
)

@Composable
fun AndroidNavigationBarComponent(
  currentScreen: ScreenType,
  onNavigate: (ScreenType) -> Unit,
  hasActiveEdit: Boolean,
  modifier: Modifier = Modifier
) {
  // Ẩn thanh điều hướng trong màn hình splash, onboarding và paywall
  if (currentScreen == ScreenType.SPLASH ||
    currentScreen == ScreenType.ONBOARDING ||
    currentScreen == ScreenType.PAYWALL
  ) {
    return
  }

  val homeLabel = if (hasActiveEdit && (currentScreen == ScreenType.EDITOR || currentScreen == ScreenType.RESULT)) {
    "Editing"
  } else {
    "Photos"
  }

  val homeBadge = if (hasActiveEdit && currentScreen != ScreenType.EDITOR && currentScreen != ScreenType.RESULT) {
    "1"
  } else {
    null
  }

  val items = listOf(
    NavItemData(
      id = ScreenType.HOME,
      label = homeLabel,
      icon = Icons.Default.Image,
      badge = homeBadge
    ),
    NavItemData(
      id = ScreenType.UNSPLASH,
      label = "Unsplash",
      icon = Icons.Default.Explore
    ),
    NavItemData(
      id = ScreenType.PROFILE,
      label = "Profile",
      icon = Icons.Default.Person
    ),
    NavItemData(
      id = ScreenType.SETTINGS,
      label = "Settings",
      icon = Icons.Default.Settings
    )
  )

  fun isSelected(id: ScreenType): Boolean {
    return if (id == ScreenType.HOME) {
      currentScreen == ScreenType.HOME || currentScreen == ScreenType.EDITOR || currentScreen == ScreenType.RESULT
    } else {
      currentScreen == id
    }
  }

  NavigationBar(
    modifier = modifier,
    containerColor = Color(0xFF1A1B1F),
    contentColor = Color(0xFFE8DEF8)
  ) {
    items.forEach { item ->
      val selected = isSelected(item.id)

      NavigationBarItem(
        selected = selected,
        onClick = {
          if (item.id == ScreenType.HOME) {
            val destination = if (hasActiveEdit && (currentScreen == ScreenType.EDITOR || currentScreen == ScreenType.RESULT)) {
              currentScreen
            } else {
              ScreenType.HOME
            }
            onNavigate(destination)
          } else {
            onNavigate(item.id)
          }
        },
        icon = {
          if (item.badge != null) {
            BadgedBox(
              badge = {
                Badge(
                  containerColor = Color(0xFFD0BCFF),
                  contentColor = Color(0xFF381E72)
                ) {
                  Text(text = item.badge)
                }
              }
            ) {
              Icon(
                imageVector = item.icon,
                contentDescription = item.label
              )
            }
          } else {
            Icon(
              imageVector = item.icon,
              contentDescription = item.label
            )
          }
        },
        label = {
          Text(
            text = item.label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
          )
        },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = Color(0xFFE8DEF8),
          selectedTextColor = Color(0xFFE8DEF8),
          indicatorColor = Color(0xFF4A4458),
          unselectedIconColor = Color(0xFF938F99),
          unselectedTextColor = Color(0xFF938F99)
        )
      )
    }
  }
}