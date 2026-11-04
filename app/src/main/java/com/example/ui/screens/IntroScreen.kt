package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CricketLogoBadge
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun IntroScreen(
    onDismiss: () -> Unit,
    onPlaySound: () -> Unit,
    modifier: Modifier = Modifier
) {
    var stage by remember { mutableStateOf(0) } // 0: loading, 1: Customize AI, 2: Founders, 3: ready to transition
    var audioTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!audioTriggered) {
            audioTriggered = true
            onPlaySound()
        }
        delay(300)
        stage = 1 // Stage 1: CUSTOMIZE AI
        delay(1400)
        stage = 2 // Stage 2: Ishant Mali & Harshal Mali
        delay(1800)
        stage = 3
        delay(800)
        onDismiss()
    }

    // Grid animation background
    val infiniteTransition = rememberInfiniteTransition(label = "grid")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offsetY"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgMidnight),
        contentAlignment = Alignment.Center
    ) {
        // Perspective Grid Background
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.18f)) {
            val width = size.width
            val height = size.height
            val lineSpacing = 40.dp.toPx()

            // Vertical lines converging
            for (x in 0..width.toInt() step lineSpacing.toInt()) {
                drawLine(
                    color = ElectricBlue,
                    start = Offset(x.toFloat(), 0f),
                    end = Offset(x.toFloat(), height),
                    strokeWidth = 1f
                )
            }
            // Horizontal lines scrolling
            val startY = offsetY % lineSpacing
            for (y in startY.toInt()..height.toInt() step lineSpacing.toInt()) {
                drawLine(
                    color = ElectricBlue,
                    start = Offset(0f, y.toFloat()),
                    end = Offset(width, y.toFloat()),
                    strokeWidth = 1f
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Center Branding & Cinematic Text Sequence
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Glowing Game Logo
                CricketLogoBadge(size = 96.dp)

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "CRICKET RANKING PRO",
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = TextPrimary,
                    letterSpacing = 2.5.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "ELITE CRICKET STATISTICS PLATFORM",
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    color = ElectricBlue,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Cinematic Text Drop Stages
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = stage == 1,
                        enter = fadeIn(tween(400)) + slideInVertically(tween(600)) { -it },
                        exit = fadeOut(tween(300)) + slideOutVertically(tween(300)) { it }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "CUSTOMIZE AI",
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Black,
                                fontSize = 28.sp,
                                color = TrophyGold,
                                letterSpacing = 3.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "PRESENTED BY STUDIO",
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = ElectricBlue,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = stage >= 2,
                        enter = fadeIn(tween(500)) + slideInVertically(tween(700)) { -it },
                        exit = fadeOut(tween(300))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "ISHANT MALI",
                                    fontFamily = OrbitronFontFamily,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp,
                                    color = Color.White,
                                    letterSpacing = 2.sp
                                )
                                Text(
                                    text = "CO-FOUNDER & CREATOR",
                                    fontFamily = OrbitronFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = TrophyGold,
                                    letterSpacing = 1.5.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "HARSHAL MALI",
                                    fontFamily = OrbitronFontFamily,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = Color.White,
                                    letterSpacing = 2.sp
                                )
                                Text(
                                    text = "CREATIVE PRODUCTION LEAD",
                                    fontFamily = OrbitronFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = ElectricBlue,
                                    letterSpacing = 1.5.sp
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Button(
                    onClick = {
                        onPlaySound()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(52.dp)
                        .testTag("enter_league_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = TrophyGold),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "ENTER LEAGUE",
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = Color(0xFF080D18),
                        letterSpacing = 2.sp
                    )
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("skip_intro_btn")
                ) {
                    Text(
                        text = "SKIP INTRO",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 11.sp,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
