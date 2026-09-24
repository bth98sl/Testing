package com.example.blink.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

enum class CategoryType(val id: String, val label: String) {
  ALL("all", "All"),
  PORTRAIT("portrait", "Portraits"),
  NATURE("nature", "Nature"),
  URBAN("urban", "Urban & Night"),
  MINIMAL("minimal", "Minimalist"),
  ARCHITECTURE("architecture", "Architecture")
}

data class UnsplashPhoto(
  val id: String,
  val title: String,
  val author: String,
  val category: String,
  val colorTone: String,
  val likes: Int,
  val dimensions: String,
  val thumbUrl: String,
  val fullUrl: String = thumbUrl
)

// Sample Mock Data
val SAMPLE_UNSPLASH_PHOTOS = listOf(
  UnsplashPhoto("1", "Neon Cyberpunk Girl", "Aleksei", "portrait", "Neon Blue", 1420, "1080x1350", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=600&q=80"),
  UnsplashPhoto("2", "Misty Alpine Forest", "Elena", "nature", "Emerald Green", 980, "1920x1080", "https://images.unsplash.com/photo-1448375240586-882707db888b?auto=format&fit=crop&w=600&q=80"),
  UnsplashPhoto("3", "Tokyo Alley at Night", "Kenji", "urban", "Warm Orange", 2100, "1080x1350", "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?auto=format&fit=crop&w=600&q=80"),
  UnsplashPhoto("4", "Minimal Concrete Curve", "Sarah", "minimal", "Monochrome", 650, "1080x1080", "https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=600&q=80"),
  UnsplashPhoto("5", "Futuristic Glass Tower", "David", "architecture", "Steel Gray", 820, "1080x1350", "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&w=600&q=80")
)

@Composable
fun UnsplashScreen(
  onSelectPhotoToEdit: (UnsplashPhoto) -> Unit,
  modifier: Modifier = Modifier,
  photos: List<UnsplashPhoto> = SAMPLE_UNSPLASH_PHOTOS
) {
  var searchQuery by remember { mutableStateOf("") }
  var activeCategory by remember { mutableStateOf(CategoryType.ALL) }

  // Filter logic
  val filteredPhotos = remember(activeCategory, searchQuery, photos) {
    photos.filter { photo ->
      val matchCategory = activeCategory == CategoryType.ALL || photo.category.equals(activeCategory.id, ignoreCase = true)
      val matchSearch = searchQuery.isBlank() ||
              photo.title.contains(searchQuery, ignoreCase = true) ||
              photo.author.contains(searchQuery, ignoreCase = true) ||
              photo.colorTone.contains(searchQuery, ignoreCase = true)
      matchCategory && matchSearch
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF121316))
  ) {
    // ==========================================
    // TOP SEARCH & FILTER BAR
    // ==========================================
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color(0xFF121316).copy(alpha = 0.95f))
        .padding(horizontal = 16.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Search Input
      Surface(
        shape = CircleShape,
        color = Color(0xFF1E1F24),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF33353A)),
        modifier = Modifier
          .fillMaxWidth()
          .height(44.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = Color(0xFF938F99),
            modifier = Modifier.size(18.dp)
          )

          Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
          ) {
            if (searchQuery.isEmpty()) {
              Text(
                text = "Search Unsplash (portrait, mountains, tokyo night...)",
                color = Color(0xFF938F99),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }

            BasicTextField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              textStyle = TextStyle(
                color = Color(0xFFE3E2E6),
                fontSize = 12.sp
              ),
              singleLine = true,
              cursorBrush = SolidColor(Color(0xFFD0BCFF)),
              modifier = Modifier.fillMaxWidth()
            )
          }

          if (searchQuery.isNotEmpty()) {
            IconButton(
              onClick = { searchQuery = "" },
              modifier = Modifier.size(24.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Clear",
                tint = Color(0xFF938F99),
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }

      // Category Horizontal Scroll
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 2.dp)
      ) {
        items(CategoryType.values()) { category ->
          val isSelected = activeCategory == category
          Surface(
            shape = CircleShape,
            color = if (isSelected) Color(0xFFD0BCFF) else Color(0xFF1E1F24),
            border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)) else null,
            modifier = Modifier
              .height(32.dp)
              .clickable { activeCategory = category }
          ) {
            Box(
              modifier = Modifier.padding(horizontal = 14.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = category.label,
                color = if (isSelected) Color(0xFF381E72) else Color(0xFFC4C7C5),
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
              )
            }
          }
        }
      }
    }

    HorizontalDivider(color = Color.White.copy(alpha = 0.06f))

    // ==========================================
    // MAIN GALLERY GRID / EMPTY STATE
    // ==========================================
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
    ) {
      // Header stats
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
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
            text = "Curated Unsplash Gallery (${filteredPhotos.size})",
            color = Color(0xFFE3E2E6),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
        Text(
          text = "Creative Free License",
          color = Color(0xFF938F99),
          fontSize = 11.sp
        )
      }

      if (filteredPhotos.isEmpty()) {
        // Empty State
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(Color(0xFF1E1F24))
              .border(1.dp, Color(0xFF33353A), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Outlined.Search,
              contentDescription = null,
              tint = Color(0xFF938F99),
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "No matching photos found",
            color = Color(0xFFE3E2E6),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
          )

          Text(
            text = "Try other keywords like \"mountain\", \"night\", \"portrait\", or \"minimal\"",
            color = Color(0xFF938F99),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
          )

          Button(
            onClick = {
              searchQuery = ""
              activeCategory = CategoryType.ALL
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF2B2C30),
              contentColor = Color(0xFFD0BCFF)
            ),
            shape = CircleShape
          ) {
            Text("View all photos", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          }
        }
      } else {
        // 2-Column Photo Grid
        LazyVerticalGrid(
          columns = GridCells.Fixed(2),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          contentPadding = PaddingValues(bottom = 24.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          items(filteredPhotos, key = { it.id }) { photo ->
            UnsplashPhotoCard(
              photo = photo,
              onSelect = { onSelectPhotoToEdit(photo) }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun UnsplashPhotoCard(
  photo: UnsplashPhoto,
  onSelect: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(20.dp),
    color = Color(0xFF1E1F24),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
    shadowElevation = 4.dp,
    modifier = Modifier.fillMaxWidth()
  ) {
    Column {
      // Image Container with Aspect Ratio (4/5)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(0.8f)
          .background(Color(0xFF2B2C30))
          .clickable { onSelect() }
      ) {
        AsyncImage(
          model = photo.thumbUrl,
          contentDescription = photo.title,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        // Dark Gradient Overlay for readability
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color.Black.copy(alpha = 0.2f),
                  Color.Transparent,
                  Color.Black.copy(alpha = 0.8f)
                )
              )
            )
        )

        // Top Left Badge: Color Tone
        Surface(
          shape = CircleShape,
          color = Color.Black.copy(alpha = 0.6f),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
          modifier = Modifier
            .padding(10.dp)
            .align(Alignment.TopStart)
        ) {
          Text(
            text = photo.colorTone,
            color = Color(0xFFE3E2E6),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
          )
        }

        // Top Right Badge: Likes
        Surface(
          shape = CircleShape,
          color = Color.Black.copy(alpha = 0.6f),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
          modifier = Modifier
            .padding(10.dp)
            .align(Alignment.TopEnd)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Favorite,
              contentDescription = null,
              tint = Color(0xFFFFD8E4),
              modifier = Modifier.size(12.dp)
            )
            Text(
              text = "${photo.likes}",
              color = Color(0xFFFFD8E4),
              fontSize = 10.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }

        // Bottom Overlay Details
        Column(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(10.dp)
        ) {
          Text(
            text = photo.title,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = "Photo by ${photo.author}",
            color = Color(0xFFC4C7C5),
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      // Card Action Footer
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFF1E1F24))
          .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = photo.dimensions,
          color = Color(0xFF938F99),
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace
        )

        Surface(
          shape = CircleShape,
          color = Color(0xFFD0BCFF),
          modifier = Modifier.clickable { onSelect() }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.AutoFixHigh,
              contentDescription = null,
              tint = Color(0xFF381E72),
              modifier = Modifier.size(12.dp)
            )
            Text(
              text = "Edit Photo",
              color = Color(0xFF381E72),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}