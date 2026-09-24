package com.example.blink.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

data class UserProfile(
  val name: String = "Android Photographer",
  val bio: String = "Mobile visual creator & photography enthusiast",
  val avatarUrl: String? = null,
  val joinedDate: String = "Joined Jan 2024",
  val photosEditedCount: Int = 128,
  val aiEnhanceCount: Int = 42,
  val bgRemovedCount: Int = 19
)

data class SampleImage(
  val id: String,
  val title: String,
  val thumbnail: String
)

val SAMPLE_IMAGES = listOf(
  SampleImage(
    id = "sample_1",
    title = "Neon Portrait",
    thumbnail = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80"
  ),
  SampleImage(
    id = "sample_2",
    title = "Urban Vibe",
    thumbnail = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=400&q=80"
  ),
  SampleImage(
    id = "sample_3",
    title = "Golden Hour",
    thumbnail = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=400&q=80"
  )
)

@Composable
fun ProfileScreen(
  profile: UserProfile,
  onUpdateProfile: (name: String?, bio: String?, avatarUrl: String?) -> Unit,
  onNavigateToSettings: () -> Unit,
  onSelectRecentImage: (SampleImage) -> Unit,
  modifier: Modifier = Modifier
) {
  var isEditingName by remember { mutableStateOf(false) }
  var tempName by remember(profile.name) { mutableStateOf(profile.name) }
  var tempBio by remember(profile.bio) { mutableStateOf(profile.bio) }

  // Avatar Picker Launcher
  val avatarLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    uri?.let {
      onUpdateProfile(null, null, it.toString())
    }
  }

  fun handleSaveInfo() {
    onUpdateProfile(
      tempName.ifBlank { "Android Photographer" },
      tempBio.ifBlank { "Mobile visual creator & photography enthusiast" },
      null
    )
    isEditingName = false
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF121316))
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 16.dp, vertical = 20.dp)
  ) {
    // ==========================================
    // 1. HEADER PROFILE CARD
    // ==========================================
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(28.dp))
        .background(Color(0xFF1E1F24))
        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(28.dp))
        .padding(20.dp)
    ) {
      // Background Accent Glow
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .offset(x = 30.dp, y = (-30).dp)
          .size(140.dp)
          .blur(40.dp)
          .background(Color(0xFFD0BCFF).copy(alpha = 0.10f), CircleShape)
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
      ) {
        // Avatar with Camera Overlay
        Box(
          modifier = Modifier.size(88.dp)
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .clip(CircleShape)
              .background(Color(0xFF2B2C30))
              .border(3.dp, Color(0xFFD0BCFF).copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            if (!profile.avatarUrl.isNullOrEmpty()) {
              AsyncImage(
                model = profile.avatarUrl,
                contentDescription = profile.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )
            } else {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color(0xFFD0BCFF),
                modifier = Modifier.size(40.dp)
              )
            }
          }

          // Change Avatar Button
          Box(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .size(32.dp)
              .clip(CircleShape)
              .background(Color(0xFFD0BCFF))
              .clickable { avatarLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.PhotoCamera,
              contentDescription = "Change avatar",
              tint = Color(0xFF381E72),
              modifier = Modifier.size(16.dp)
            )
          }
        }

        // User Info & Bio
        Column(
          modifier = Modifier.weight(1f)
        ) {
          if (isEditingName) {
            Column(
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedTextField(
                value = tempName,
                onValueChange = { tempName = it },
                placeholder = { Text("Enter your name", fontSize = 12.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                  focusedContainerColor = Color(0xFF2B2C30),
                  unfocusedContainerColor = Color(0xFF2B2C30),
                  focusedBorderColor = Color(0xFFD0BCFF),
                  unfocusedBorderColor = Color(0xFF49454F),
                  focusedTextColor = Color(0xFFE3E2E6),
                  unfocusedTextColor = Color(0xFFE3E2E6)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
              )

              OutlinedTextField(
                value = tempBio,
                onValueChange = { tempBio = it },
                placeholder = { Text("Short bio", fontSize = 12.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                  focusedContainerColor = Color(0xFF2B2C30),
                  unfocusedContainerColor = Color(0xFF2B2C30),
                  focusedBorderColor = Color(0xFF49454F),
                  unfocusedBorderColor = Color(0xFF49454F),
                  focusedTextColor = Color(0xFFC4C7C5),
                  unfocusedTextColor = Color(0xFFC4C7C5)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
              )

              Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Button(
                  onClick = { handleSaveInfo() },
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                  shape = CircleShape
                ) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF381E72),
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Save", color = Color(0xFF381E72), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                  onClick = {
                    tempName = profile.name
                    tempBio = profile.bio
                    isEditingName = false
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B2C30)),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                  shape = CircleShape
                ) {
                  Text("Cancel", color = Color(0xFFC4C7C5), fontSize = 12.sp)
                }
              }
            }
          } else {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text(
                text = profile.name,
                color = Color(0xFFE3E2E6),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
              )
              IconButton(
                onClick = { isEditingName = true },
                modifier = Modifier.size(24.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Edit,
                  contentDescription = "Edit profile",
                  tint = Color(0xFF938F99),
                  modifier = Modifier.size(14.dp)
                )
              }
            }

            Text(
              text = profile.bio,
              color = Color(0xFF938F99),
              fontSize = 12.sp,
              lineHeight = 16.sp,
              modifier = Modifier.padding(top = 2.dp)
            )

            // Badges
            Row(
              modifier = Modifier.padding(top = 10.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = CircleShape,
                color = Color(0xFF381E72).copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.3f))
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFD0BCFF),
                    modifier = Modifier.size(12.dp)
                  )
                  Text(
                    text = "Creator Pro",
                    color = Color(0xFFD0BCFF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                  )
                }
              }

              Surface(
                shape = CircleShape,
                color = Color(0xFF2B2C30)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = Color(0xFF938F99),
                    modifier = Modifier.size(12.dp)
                  )
                  Text(
                    text = profile.joinedDate,
                    color = Color(0xFFC4C7C5),
                    fontSize = 11.sp
                  )
                }
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // ==========================================
    // 2. STATS CARDS SECTION
    // ==========================================
    Text(
      text = "ACTIVITY OVERVIEW",
      color = Color(0xFF938F99),
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      letterSpacing = 0.8.sp,
      modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      StatCard(
        icon = Icons.Default.Layers,
        value = profile.photosEditedCount.toString(),
        label = "Edited",
        iconBg = Color(0xFF4F378B).copy(alpha = 0.3f),
        iconTint = Color(0xFFD0BCFF),
        modifier = Modifier.weight(1f)
      )
      StatCard(
        icon = Icons.Default.AutoAwesome,
        value = profile.aiEnhanceCount.toString(),
        label = "AI Enhanced",
        iconBg = Color(0xFF381E72).copy(alpha = 0.4f),
        iconTint = Color(0xFFD0BCFF),
        modifier = Modifier.weight(1f)
      )
      StatCard(
        icon = Icons.Default.ContentCut,
        value = profile.bgRemovedCount.toString(),
        label = "Cutouts",
        iconBg = Color(0xFF7D5260).copy(alpha = 0.3f),
        iconTint = Color(0xFFFFD8E4),
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    // ==========================================
    // 3. QUICK PHOTO PROJECTS GALLERY
    // ==========================================
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "QUICK PHOTO PROJECTS",
        color = Color(0xFF938F99),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp
      )
      Text(
        text = "Tap to edit",
        color = Color(0xFFD0BCFF),
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      SAMPLE_IMAGES.forEach { sample ->
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF1E1F24))
            .border(1.dp, Color(0xFF33353A), RoundedCornerShape(18.dp))
            .clickable { onSelectRecentImage(sample) }
            .padding(8.dp)
        ) {
          Column {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2B2C30))
            ) {
              AsyncImage(
                model = sample.thumbnail,
                contentDescription = sample.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )
            }

            Text(
              text = sample.title,
              color = Color(0xFFE3E2E6),
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.padding(top = 6.dp)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // ==========================================
    // 4. ON-DEVICE STORAGE STATUS
    // ==========================================
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = Color(0xFF1E1F24),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF33353A)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(Color(0xFF2B2C30)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Storage,
              contentDescription = null,
              tint = Color(0xFFD0BCFF),
              modifier = Modifier.size(20.dp)
            )
          }

          Column {
            Text(
              text = "On-Device Photo Storage",
              color = Color(0xFFE3E2E6),
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Path: Pictures/PhotoEditor (Local)",
              color = Color(0xFF938F99),
              fontSize = 11.sp
            )
          }
        }

        Surface(
          shape = CircleShape,
          color = Color(0xFF2B2C30)
        ) {
          Text(
            text = "Active",
            color = Color(0xFFD0BCFF),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // ==========================================
    // 5. SETTINGS SHORTCUT BUTTON
    // ==========================================
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = Color(0xFF1E1F24),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF33353A)),
      modifier = Modifier
        .fillMaxWidth()
        .clickable { onNavigateToSettings() }
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            tint = Color(0xFFD0BCFF),
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = "Open App Settings & Export Options",
            color = Color(0xFFE3E2E6),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }

        Icon(
          imageVector = Icons.Default.ChevronRight,
          contentDescription = null,
          tint = Color(0xFF938F99),
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}

@Composable
private fun StatCard(
  icon: ImageVector,
  value: String,
  label: String,
  iconBg: Color,
  iconTint: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(20.dp),
    color = Color(0xFF1E1F24),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF33353A)),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(iconBg),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = iconTint,
          modifier = Modifier.size(16.dp)
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = value,
        color = Color(0xFFE3E2E6),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
      Text(
        text = label,
        color = Color(0xFF938F99),
        fontSize = 10.sp
      )
    }
  }
}