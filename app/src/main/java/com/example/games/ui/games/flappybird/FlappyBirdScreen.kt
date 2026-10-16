package com.example.games.ui.games.flappybird

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.games.ui.common.GameOverCard
import com.example.games.ui.theme.ColorFlappy
import kotlinx.coroutines.isActive

// Pre-defined static colors for zero allocation in DrawScope
private val ColorSkyTop = Color(0xFF38BDF8)
private val ColorSkyMid = Color(0xFFBAE6FD)
private val ColorSkyBottom = Color(0xFFE0F2FE)
private val ColorCloud = Color.White.copy(alpha = 0.85f)
private val ColorPipe = Color(0xFF22C55E)
private val ColorPipeRim = Color(0xFF16A34A)
private val ColorPipeHighlight = Color(0xFF86EFAC).copy(alpha = 0.45f)
private val ColorGroundDirt = Color(0xFFD97706)
private val ColorGroundGrass = Color(0xFF16A34A)
private val ColorBirdCenter = Color(0xFFFEF08A)
private val ColorBirdEdge = Color(0xFFEAB308)
private val ColorBirdWing = Color.White.copy(alpha = 0.9f)
private val ColorBirdBeak = Color(0xFFF97316)

@Composable
fun FlappyBirdScreen(
    onBack: () -> Unit,
    viewModel: FlappyBirdViewModel = viewModel()
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    // Synchronize game updates directly to the hardware display VSYNC
    LaunchedEffect(viewModel.status) {
        if (viewModel.status == FlappyStatus.PLAYING) {
            while (isActive && viewModel.status == FlappyStatus.PLAYING) {
                withFrameMillis {
                    viewModel.updateFrame()
                }
            }
        }
    }

    // Pre-allocated brush cached across frames
    val skyBrush = remember {
        Brush.verticalGradient(listOf(ColorSkyTop, ColorSkyMid, ColorSkyBottom))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.jump()
            }
    ) {
        // High-Performance Hardware Accelerated Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scaleX = size.width / viewModel.virtualWidth
            val scaleY = size.height / viewModel.virtualHeight

            // 1. Draw Sky Gradient
            drawRect(brush = skyBrush)

            // 2. Draw Clouds
            val clouds = viewModel.clouds
            for (cloud in clouds) {
                val cx = cloud.x * scaleX
                val cy = cloud.y * scaleY
                val cRadius = cloud.size * scaleX * 0.5f

                drawCircle(color = ColorCloud, radius = cRadius, center = Offset(cx, cy))
                drawCircle(color = ColorCloud, radius = cRadius * 0.7f, center = Offset(cx - cRadius * 0.6f, cy + cRadius * 0.15f))
                drawCircle(color = ColorCloud, radius = cRadius * 0.7f, center = Offset(cx + cRadius * 0.6f, cy + cRadius * 0.15f))
            }

            // 3. Draw Pipes
            val pipes = viewModel.pipes
            val rimHeight = 36f * scaleY
            val pipeHighlightWidth = 10f * scaleX
            for (pipe in pipes) {
                val px = pipe.x * scaleX
                val pw = pipe.width * scaleX
                val pGapY = pipe.gapY * scaleY
                val pGapHeight = pipe.gapHeight * scaleY

                // Top Pipe Body
                drawRoundRect(color = ColorPipe, topLeft = Offset(px, 0f), size = Size(pw, pGapY), cornerRadius = CornerRadius(6f, 6f))
                // Top Pipe Highlight
                drawRect(color = ColorPipeHighlight, topLeft = Offset(px + 8f * scaleX, 0f), size = Size(pipeHighlightWidth, pGapY))
                // Top Pipe Rim
                drawRoundRect(color = ColorPipeRim, topLeft = Offset(px - 6f * scaleX, pGapY - rimHeight), size = Size(pw + 12f * scaleX, rimHeight), cornerRadius = CornerRadius(8f, 8f))

                // Bottom Pipe Body
                val bottomPipeY = pGapY + pGapHeight
                val bottomPipeHeight = size.height - bottomPipeY
                drawRoundRect(color = ColorPipe, topLeft = Offset(px, bottomPipeY), size = Size(pw, bottomPipeHeight), cornerRadius = CornerRadius(6f, 6f))
                // Bottom Pipe Highlight
                drawRect(color = ColorPipeHighlight, topLeft = Offset(px + 8f * scaleX, bottomPipeY), size = Size(pipeHighlightWidth, bottomPipeHeight))
                // Bottom Pipe Rim
                drawRoundRect(color = ColorPipeRim, topLeft = Offset(px - 6f * scaleX, bottomPipeY), size = Size(pw + 12f * scaleX, rimHeight), cornerRadius = CornerRadius(8f, 8f))
            }

            // 4. Draw Ground Floor
            val groundHeight = 100f * scaleY
            val groundY = size.height - groundHeight
            drawRect(color = ColorGroundDirt, topLeft = Offset(0f, groundY), size = Size(size.width, groundHeight))
            drawRect(color = ColorGroundGrass, topLeft = Offset(0f, groundY), size = Size(size.width, 16f * scaleY))

            // 5. Draw Bird
            val bx = viewModel.birdX * scaleX
            val by = viewModel.birdY * scaleY
            val bRadius = viewModel.birdRadius * scaleX

            rotate(degrees = viewModel.birdRotation, pivot = Offset(bx, by)) {
                // Body
                drawCircle(
                    color = ColorBirdEdge,
                    radius = bRadius,
                    center = Offset(bx, by)
                )
                drawCircle(
                    color = ColorBirdCenter,
                    radius = bRadius * 0.88f,
                    center = Offset(bx - bRadius * 0.12f, by - bRadius * 0.12f)
                )

                // Wing
                drawRoundRect(
                    color = ColorBirdWing,
                    topLeft = Offset(bx - bRadius * 0.8f, by - bRadius * 0.35f),
                    size = Size(bRadius * 0.9f, bRadius * 0.65f),
                    cornerRadius = CornerRadius(bRadius * 0.3f, bRadius * 0.3f)
                )

                // Eye
                val eyeCenter = Offset(bx + bRadius * 0.45f, by - bRadius * 0.3f)
                drawCircle(color = Color.White, radius = bRadius * 0.34f, center = eyeCenter)
                drawCircle(color = Color.Black, radius = bRadius * 0.16f, center = Offset(eyeCenter.x + bRadius * 0.08f, eyeCenter.y))
                drawCircle(color = Color.White, radius = bRadius * 0.06f, center = Offset(eyeCenter.x + bRadius * 0.04f, eyeCenter.y - bRadius * 0.05f))

                // Beak
                drawRoundRect(
                    color = ColorBirdBeak,
                    topLeft = Offset(bx + bRadius * 0.7f, by - bRadius * 0.05f),
                    size = Size(bRadius * 0.55f, bRadius * 0.38f),
                    cornerRadius = CornerRadius(bRadius * 0.15f, bRadius * 0.15f)
                )
            }
        }

        // Top Navigation Bar & Score HUD (recomposes ONLY when score or status changes)
        FlappyTopBarHUD(
            score = viewModel.score,
            status = viewModel.status,
            onBack = onBack,
            onTogglePause = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.togglePause()
            }
        )

        // Ready Start Overlay
        if (viewModel.status == FlappyStatus.READY) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "FLAPPY BIRD",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = ColorFlappy
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap anywhere to flap & jump through pipes",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.startGame()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorFlappy),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("TAP TO FLY", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Paused Overlay
        if (viewModel.status == FlappyStatus.PAUSED) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PAUSED",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                )
            }
        }

        // Game Over Overlay
        AnimatedVisibility(
            visible = viewModel.status == FlappyStatus.GAME_OVER,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            GameOverCard(
                title = "Flappy Crashed!",
                score = viewModel.score,
                highScore = viewModel.highScore,
                accentColor = ColorFlappy,
                onRestart = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.restartGame()
                },
                onBack = onBack
            )
        }
    }
}

@Composable
fun FlappyTopBarHUD(
    score: Int,
    status: FlappyStatus,
    onBack: () -> Unit,
    onTogglePause: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Black.copy(alpha = 0.35f)
            ),
            modifier = Modifier.size(42.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Surface(
            color = Color.Black.copy(alpha = 0.35f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.displayMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )
        }

        IconButton(
            onClick = onTogglePause,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Black.copy(alpha = 0.35f)
            ),
            modifier = Modifier.size(42.dp)
        ) {
            Icon(
                imageVector = if (status == FlappyStatus.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = "Pause",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
