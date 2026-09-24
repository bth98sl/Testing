package com.example.blink.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.blink.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ArtworkSlide(
  val id: String,
  val imageRes: Int,
  val titleRes: Int,
  val subtitleRes: Int,
  val accentColors: List<Color>
)

val ARTWORK_SLIDES = listOf(
  ArtworkSlide(
    id = "floral",
    imageRes = R.drawable.onboarding_neon_card_1788940279037,
    titleRes = R.string.paywall_slide1_title,
    subtitleRes = R.string.paywall_slide1_subtitle,
    accentColors = listOf(Color(0xFFFE0377).copy(alpha = 0.3f), Color(0xFF8C52FF).copy(alpha = 0.2f), Color.Transparent)
  ),
  ArtworkSlide(
    id = "portrait",
    imageRes = R.drawable.onboarding_portrait_showcase_1788939364227,
    titleRes = R.string.paywall_slide2_title,
    subtitleRes = R.string.paywall_slide2_subtitle,
    accentColors = listOf(Color(0xFFFE0377).copy(alpha = 0.35f), Color(0xFFFF6069).copy(alpha = 0.2f), Color.Transparent)
  ),
  ArtworkSlide(
    id = "cinematic",
    imageRes = R.drawable.onboarding_cinematic_card_1788940261363,
    titleRes = R.string.paywall_slide3_title,
    subtitleRes = R.string.paywall_slide3_subtitle,
    accentColors = listOf(Color(0xFF8C52FF).copy(alpha = 0.3f), Color(0xFFFE0377).copy(alpha = 0.2f), Color.Transparent)
  )
)

@Composable
fun PaywallScreen(
  onDismiss: () -> Unit,
  onSubscribe: (planId: String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var activeSlide by remember { mutableStateOf(0) }
  var enableFreeTrial by remember { mutableStateOf(true) }
  var selectedPlan by remember { mutableStateOf("yearly") }
  var isProcessing by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    while (true) {
      delay(4200)
      activeSlide = (activeSlide + 1) % ARTWORK_SLIDES.size
    }
  }

  val currentArtwork = ARTWORK_SLIDES[activeSlide]

  fun handleContinue() {
    isProcessing = true
    scope.launch {
      delay(600)
      isProcessing = false
      onSubscribe(selectedPlan)
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF07080A))
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(bottom = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // ==========================================
      // 1. TOP ANIMATED ARTWORK HERO
      // ==========================================
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(440.dp)
      ) {
        AnimatedContent(
          targetState = currentArtwork,
          transitionSpec = {
            fadeIn(animationSpec = androidx.compose.animation.core.tween(900)) togetherWith
                    fadeOut(animationSpec = androidx.compose.animation.core.tween(800))
          },
          label = "ArtworkSlideAnimation"
        ) { slide ->
          Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
              model = ImageRequest.Builder(context)
                .data(slide.imageRes)
                .crossfade(true)
                .build(),
              contentDescription = "Artwork Showcase",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop,
              alignment = if (slide.id == "portrait") Alignment.TopCenter else Alignment.Center
            )
          }
        }

        // Ambient Glow Gradient
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(currentArtwork.accentColors))
        )

        // Dark Fade to AMOLED Background at bottom
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .align(Alignment.BottomCenter)
            .background(
              Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color(0xFF07080A))
              )
            )
        )

        // Top Controls (Close button)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.Start,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(Color.Black.copy(alpha = 0.4f))
              .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
              .clickable { onDismiss() },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = Color.White.copy(alpha = 0.8f),
              modifier = Modifier.size(16.dp)
            )
          }
        }

        // Hero Title & Subtitle + Dots Indicator
        Column(
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 8.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.Black.copy(alpha = 0.35f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            modifier = Modifier.padding(horizontal = 24.dp)
          ) {
            Column(
              modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = stringResource(currentArtwork.titleRes),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
              )
              Text(
                text = stringResource(currentArtwork.subtitleRes),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE3E2E6).copy(alpha = 0.9f),
                modifier = Modifier.padding(top = 2.dp)
              )
            }
          }

          // Pagination Dots
          Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            ARTWORK_SLIDES.forEachIndexed { idx, _ ->
              val isSelected = activeSlide == idx
              Box(
                modifier = Modifier
                  .height(6.dp)
                  .width(if (isSelected) 20.dp else 6.dp)
                  .clip(CircleShape)
                  .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.3f))
                  .clickable { activeSlide = idx }
              )
            }
          }
        }
      }

      // ==========================================
      // 2. BOTTOM SECTION: PRICING & CTA
      // ==========================================
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp)
          .padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Free Trial Toggle Row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = stringResource(R.string.enable_free_trial),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
          )

          Switch(
            checked = enableFreeTrial,
            onCheckedChange = { enableFreeTrial = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.Black,
              checkedTrackColor = Color.White,
              uncheckedThumbColor = Color.White,
              uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
            )
          )
        }

        // PLAN 1: Yearly Access
        val isYearlySelected = selectedPlan == "yearly"
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { selectedPlan = "yearly" }
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 8.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(if (isYearlySelected) Color(0xFF14151B) else Color(0xFF111216))
              .border(
                border = if (isYearlySelected) {
                  BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(
                      colors = listOf(
                        Color(0xFFFE0377),
                        Color(0xFF8C52FF)
                      )
                    )
                  )
                } else {
                  BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                },
                shape = RoundedCornerShape(16.dp)
              )
              .padding(16.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = stringResource(R.string.yearly_access),
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Text(
                  text = if (enableFreeTrial) {
                    stringResource(R.string.yearly_trial_desc)
                  } else {
                    stringResource(R.string.yearly_no_trial_desc)
                  },
                  fontSize = 11.sp,
                  color = Color(0xFF8C8C94),
                  fontStyle = FontStyle.Italic,
                  modifier = Modifier.padding(top = 2.dp)
                )
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = stringResource(R.string.yearly_price_per_week),
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Text(
                  text = stringResource(R.string.per_week),
                  fontSize = 10.sp,
                  color = Color(0xFF8C8C94)
                )
              }
            }
          }

          // Badge "Best Offer"
          Box(
            modifier = Modifier
              .padding(start = 16.dp)
              .align(Alignment.TopStart)
              .background(
                Brush.horizontalGradient(listOf(Color(0xFFFE0377), Color(0xFF8C52FF))),
                CircleShape
              )
              .padding(horizontal = 10.dp, vertical = 3.dp)
          ) {
            Text(
              text = stringResource(R.string.best_offer),
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        // PLAN 2: Weekly Access
        val isWeeklySelected = selectedPlan == "weekly"
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isWeeklySelected) Color(0xFF14151B) else Color(0xFF111216))
            .border(
              border = if (isWeeklySelected) {
                BorderStroke(
                  width = 1.5.dp,
                  brush = Brush.horizontalGradient(
                    colors = listOf(
                      Color(0xFFFE0377),
                      Color(0xFF8C52FF)
                    )
                  )
                )
              } else {
                BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
              },
              shape = RoundedCornerShape(16.dp)
            )
            .clickable { selectedPlan = "weekly" }
            .padding(16.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = stringResource(R.string.weekly_access),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = stringResource(R.string.weekly_desc),
                fontSize = 11.sp,
                color = Color(0xFF8C8C94),
                modifier = Modifier.padding(top = 2.dp)
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = stringResource(R.string.weekly_price_per_week),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = stringResource(R.string.per_week),
                fontSize = 10.sp,
                color = Color(0xFF8C8C94)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Primary CTA Button
        Button(
          onClick = { handleContinue() },
          enabled = !isProcessing,
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
          shape = CircleShape,
          colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
          if (isProcessing) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              color = Color.Black,
              strokeWidth = 2.dp
            )
          } else {
            Text(
              text = stringResource(R.string.btn_continue),
              color = Color.Black,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        // Footer Links
        val termsToastMsg = stringResource(R.string.terms_toast)
        val privacyToastMsg = stringResource(R.string.privacy_toast)

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = stringResource(R.string.terms_of_use),
            color = Color(0xFF71717A),
            fontSize = 11.sp,
            modifier = Modifier.clickable {
              Toast.makeText(context, termsToastMsg, Toast.LENGTH_SHORT).show()
            }
          )
          Text(text = " • ", color = Color(0xFF71717A), fontSize = 11.sp)
          Text(
            text = stringResource(R.string.privacy_policy),
            color = Color(0xFF71717A),
            fontSize = 11.sp,
            modifier = Modifier.clickable {
              Toast.makeText(context, privacyToastMsg, Toast.LENGTH_SHORT).show()
            }
          )
        }
      }
    }
  }
}