package com.example.blink.ui.screens

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.blink.R
import kotlinx.coroutines.delay
import kotlin.math.abs

val ClarenceTwoFont = FontFamily(
  Font(R.font.clarence_two, FontWeight.Normal)
)

data class OnboardingSlide(
  val id: String,
  @StringRes val cardTitleRes: Int,
  @StringRes val cardBadgeRes: Int,
  @StringRes val headlineRes: Int,
  @StringRes val descriptionRes: Int,
  val imageUrl: String,
  val accentGradient: List<Color>,
  val bgGradient: List<Color>,
  val glowColor: Color
)

val ONBOARDING_SLIDES = listOf(
  OnboardingSlide(
    id = "cinematic_film",
    cardTitleRes = R.string.slide1_title,
    cardBadgeRes = R.string.slide1_badge,
    headlineRes = R.string.slide1_headline,
    descriptionRes = R.string.slide1_desc,
    imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80",
    accentGradient = listOf(Color(0xFFFE0377), Color(0xFFFF6069), Color(0xFFFE0377)),
    bgGradient = listOf(Color(0xFFFF3385), Color(0xFFF472B6), Color(0xFFFBCFE8)),
    glowColor = Color(0x6BFE0377)
  ),
  OnboardingSlide(
    id = "magic_retouch",
    cardTitleRes = R.string.slide2_title,
    cardBadgeRes = R.string.slide2_badge,
    headlineRes = R.string.slide2_headline,
    descriptionRes = R.string.slide2_desc,
    imageUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=800&q=80",
    accentGradient = listOf(Color(0xFFFE0377), Color(0xFFFF6069), Color(0xFFFE0377)),
    bgGradient = listOf(Color(0xFFF472B6), Color(0xFFF9A8D4), Color(0xFFFDF2F8)),
    glowColor = Color(0x6BD7AFFC)
  ),
  OnboardingSlide(
    id = "artistic_studio",
    cardTitleRes = R.string.slide3_title,
    cardBadgeRes = R.string.slide3_badge,
    headlineRes = R.string.slide3_headline,
    descriptionRes = R.string.slide3_desc,
    imageUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=800&q=80",
    accentGradient = listOf(Color(0xFFFE0377), Color(0xFFFF6069), Color(0xFFFE0377)),
    bgGradient = listOf(Color(0xFFF472B6), Color(0xFFF472B6), Color(0xFFFBCFE8)),
    glowColor = Color(0x73D7AFFC)
  )
)

@Composable
fun OnboardingScreen(
  onComplete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val initialPage = 1000 * ONBOARDING_SLIDES.size
  var currentIndex by remember { mutableIntStateOf(initialPage) }
  var isContinued by remember { mutableStateOf(false) }

  val haptic = LocalHapticFeedback.current
  val realIndex = currentIndex % ONBOARDING_SLIDES.size
  val currentSlide = ONBOARDING_SLIDES[realIndex]

  val configuration = LocalConfiguration.current
  val screenWidthDp = configuration.screenWidthDp.dp
  val screenHeightDp = configuration.screenHeightDp.dp

  val cardHeight = (screenHeightDp * 0.44f).coerceIn(280.dp, 400.dp)
  val cardWidth = (screenWidthDp * 0.68f).coerceIn(220.dp, 290.dp)

  LaunchedEffect(Unit) {
    while (true) {
      delay(3500L)
      currentIndex++
    }
  }

  val animatedBgGradient by animateColorAsState(
    targetValue = currentSlide.bgGradient.first(),
    animationSpec = tween(800, easing = FastOutSlowInEasing),
    label = "bgGradient"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            animatedBgGradient,
            currentSlide.bgGradient[1],
            currentSlide.bgGradient[2]
          )
        )
      )
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .systemBarsPadding()
        .padding(vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "Blink",
            color = Color.White,
            fontSize = 28.sp,
            fontFamily = ClarenceTwoFont,
            letterSpacing = (-0.5).sp
          )
          Surface(
            shape = CircleShape,
            color = Color.Transparent,
            modifier = Modifier.background(
              Brush.horizontalGradient(
                listOf(Color(0xFFFE0377), Color(0xFFFBCFE8))
              ),
              CircleShape
            )
          ) {
            Text(
              text = "PRO",
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
          }
        }

        TextButton(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onComplete()
          }
        ) {
          Text(
            text = stringResource(R.string.skip),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      // Middle Content
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .pointerInput(Unit) {
              awaitPointerEventScope {
                while (true) {
                  awaitPointerEvent()
                }
              }
            },
          contentAlignment = Alignment.Center
        ) {
          val visibleRange = (currentIndex - 2)..(currentIndex + 2)

          for (i in visibleRange) {
            val slideIdx = ((i % ONBOARDING_SLIDES.size) + ONBOARDING_SLIDES.size) % ONBOARDING_SLIDES.size
            val slide = ONBOARDING_SLIDES[slideIdx]

            val effectiveOffset = (i - currentIndex).toFloat()
            val isCenter = i == currentIndex

            val targetTranslateX = effectiveOffset * (screenWidthDp.value * 0.44f)
            val targetScale = (1f - (abs(effectiveOffset) * 0.12f)).coerceIn(0.85f, 1f)
            val targetRotationZ = effectiveOffset * 6f
            val targetAlpha = (1f - (abs(effectiveOffset) * 0.15f)).coerceIn(0f, 1f)

            val animatedTranslateX by animateFloatAsState(
              targetValue = targetTranslateX,
              animationSpec = tween(700, easing = FastOutSlowInEasing),
              label = "translateX"
            )
            val animatedScale by animateFloatAsState(
              targetValue = targetScale,
              animationSpec = tween(700, easing = FastOutSlowInEasing),
              label = "scale"
            )
            val animatedRotationZ by animateFloatAsState(
              targetValue = targetRotationZ,
              animationSpec = tween(700, easing = FastOutSlowInEasing),
              label = "rotationZ"
            )
            val animatedAlpha by animateFloatAsState(
              targetValue = targetAlpha,
              animationSpec = tween(700, easing = FastOutSlowInEasing),
              label = "alpha"
            )

            key(i) {
              Card(
                modifier = Modifier
                  .width(cardWidth)
                  .fillMaxHeight()
                  .zIndex(10f - abs(effectiveOffset))
                  .graphicsLayer {
                    translationX = animatedTranslateX * density
                    scaleX = animatedScale
                    scaleY = animatedScale
                    rotationZ = animatedRotationZ
                    alpha = animatedAlpha
                  },
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1F24)),
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (isCenter) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.14f)
                )
              ) {
                Box(modifier = Modifier.fillMaxSize()) {
                  AsyncImage(
                    model = slide.imageUrl,
                    contentDescription = stringResource(slide.headlineRes),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                  )

                  Box(
                    modifier = Modifier
                      .fillMaxSize()
                      .background(
                        Brush.verticalGradient(
                          colors = listOf(
                            Color.Black.copy(alpha = 0.75f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                          )
                        )
                      )
                  )

                  Text(
                    text = stringResource(slide.cardTitleRes),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 24.sp,
                    modifier = Modifier
                      .align(Alignment.TopStart)
                      .padding(16.dp)
                  )

                  Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.55f),
                    border = androidx.compose.foundation.BorderStroke(
                      1.dp,
                      Color.White.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier
                      .align(Alignment.BottomStart)
                      .padding(14.dp)
                  ) {
                    Row(
                      modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                      ),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFD7AFFC),
                        modifier = Modifier.size(11.dp)
                      )
                      Text(
                        text = stringResource(slide.cardBadgeRes),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                      )
                    }
                  }
                }
              }
            }
          }
        }

        // Dots & Info Text
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(horizontal = 24.dp)
        ) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
          ) {
            ONBOARDING_SLIDES.forEachIndexed { idx, _ ->
              val isSelected = realIndex == idx
              val dotWidth by animateDpAsState(
                targetValue = if (isSelected) 28.dp else 8.dp,
                animationSpec = tween(500, easing = FastOutSlowInEasing),
                label = "dotWidth"
              )

              Box(
                modifier = Modifier
                  .height(5.dp)
                  .width(dotWidth)
                  .clip(CircleShape)
                  .background(
                    if (isSelected) {
                      Brush.horizontalGradient(currentSlide.accentGradient)
                    } else {
                      Brush.linearGradient(
                        listOf(
                          Color.White.copy(alpha = 0.2f),
                          Color.White.copy(alpha = 0.2f)
                        )
                      )
                    }
                  )
              )
            }
          }

          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 4.dp)
          ) {
            Text(
              text = stringResource(currentSlide.headlineRes),
              color = Color.Black,
              fontSize = 20.sp,
              fontWeight = FontWeight.ExtraBold,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = stringResource(currentSlide.descriptionRes),
              color = Color.Black.copy(alpha = 0.7f),
              fontSize = 12.sp,
              lineHeight = 16.sp,
              textAlign = TextAlign.Center,
              maxLines = 2
            )
          }
        }
      }

      // Bottom Button
      Button(
        onClick = {
          if (!isContinued) {
            isContinued = true
            currentIndex++
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onComplete()
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp)
          .navigationBarsPadding()
          .padding(bottom = 8.dp)
          .height(52.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF242428)),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          Color.White.copy(alpha = 0.2f)
        )
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = if (isContinued) {
              stringResource(R.string.get_started)
            } else {
              stringResource(R.string.continue_button)
            },
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
          )
          Icon(
            imageVector = if (isContinued) Icons.Default.AutoAwesome else Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFFD7AFFC),
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}