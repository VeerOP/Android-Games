package com.example.games.ui.games.flappybird

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.games.ui.common.GameOverCard
import com.example.games.ui.theme.ColorFlappy
import com.example.games.ui.theme.GoldTrophy

@Composable
fun FlappyBirdScreen(
    onBack: () -> Unit,
    viewModel: FlappyBirdViewModel = viewModel()
) {
    val state by viewModel.state
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

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
        // Render Full-Screen Canvas with Game Graphics
        FlappyCanvasRenderer(
            state = state,
            virtualWidth = viewModel.virtualWidth,
            virtualHeight = viewModel.virtualHeight,
            birdX = viewModel.birdX,
            birdRadius = viewModel.birdRadius
        )

        // Top Navigation Bar & Score HUD
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

            // Current Score Display
            Surface(
                color = Color.Black.copy(alpha = 0.35f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "${state.score}",
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )
            }

            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.togglePause()
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.35f)
                ),
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = if (state.status == FlappyStatus.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = "Pause",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Ready Start Overlay
        if (state.status == FlappyStatus.READY) {
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
        if (state.status == FlappyStatus.PAUSED) {
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
            visible = state.status == FlappyStatus.GAME_OVER,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            GameOverCard(
                title = "Flappy Crashed!",
                score = state.score,
                highScore = state.highScore,
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
fun FlappyCanvasRenderer(
    state: FlappyBirdState,
    virtualWidth: Float,
    virtualHeight: Float,
    birdX: Float,
    birdRadius: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val scaleX = size.width / virtualWidth
        val scaleY = size.height / virtualHeight

        // Draw Sky Background Gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF38BDF8), // Bright Sky Blue
                    Color(0xFFBAE6FD),
                    Color(0xFFE0F2FE)
                )
            )
        )

        // Draw Clouds
        state.clouds.forEach { cloud ->
            val cx = cloud.x * scaleX
            val cy = cloud.y * scaleY
            val cRadius = cloud.size * scaleX * 0.5f

            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = cRadius,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = cRadius * 0.7f,
                center = Offset(cx - cRadius * 0.6f, cy + cRadius * 0.15f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = cRadius * 0.7f,
                center = Offset(cx + cRadius * 0.6f, cy + cRadius * 0.15f)
            )
        }

        // Draw Pipes
        val pipeGreen = Color(0xFF22C55E)
        val pipeRimGreen = Color(0xFF16A34A)
        val pipeHighlight = Color(0xFF86EFAC)

        state.pipes.forEach { pipe ->
            val px = pipe.x * scaleX
            val pw = pipe.width * scaleX
            val pGapY = pipe.gapY * scaleY
            val pGapHeight = pipe.gapHeight * scaleY
            val rimHeight = 36f * scaleY

            // --- Top Pipe ---
            drawRoundRect(
                color = pipeGreen,
                topLeft = Offset(px, 0f),
                size = Size(pw, pGapY),
                cornerRadius = CornerRadius(6f, 6f)
            )
            // Top pipe glossy highlight strip
            drawRect(
                color = pipeHighlight.copy(alpha = 0.45f),
                topLeft = Offset(px + 8f * scaleX, 0f),
                size = Size(10f * scaleX, pGapY)
            )
            // Top pipe rim lip
            drawRoundRect(
                color = pipeRimGreen,
                topLeft = Offset(px - 6f * scaleX, pGapY - rimHeight),
                size = Size(pw + 12f * scaleX, rimHeight),
                cornerRadius = CornerRadius(8f, 8f)
            )

            // --- Bottom Pipe ---
            val bottomPipeY = pGapY + pGapHeight
            val bottomPipeHeight = size.height - bottomPipeY
            drawRoundRect(
                color = pipeGreen,
                topLeft = Offset(px, bottomPipeY),
                size = Size(pw, bottomPipeHeight),
                cornerRadius = CornerRadius(6f, 6f)
            )
            // Bottom pipe glossy highlight strip
            drawRect(
                color = pipeHighlight.copy(alpha = 0.45f),
                topLeft = Offset(px + 8f * scaleX, bottomPipeY),
                size = Size(10f * scaleX, bottomPipeHeight)
            )
            // Bottom pipe rim lip
            drawRoundRect(
                color = pipeRimGreen,
                topLeft = Offset(px - 6f * scaleX, bottomPipeY),
                size = Size(pw + 12f * scaleX, rimHeight),
                cornerRadius = CornerRadius(8f, 8f)
            )
        }

        // Draw Ground Floor with Grass
        val groundHeight = 100f * scaleY
        val groundY = size.height - groundHeight
        drawRect(
            color = Color(0xFFD97706), // Dirt Brown
            topLeft = Offset(0f, groundY),
            size = Size(size.width, groundHeight)
        )
        drawRect(
            color = Color(0xFF16A34A), // Grass Top Layer
            topLeft = Offset(0f, groundY),
            size = Size(size.width, 16f * scaleY)
        )

        // Draw Flappy Bird
        val bx = birdX * scaleX
        val by = state.birdY * scaleY
        val bRadius = birdRadius * scaleX

        rotate(degrees = state.birdRotation, pivot = Offset(bx, by)) {
            // Yellow Body
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFEF08A), Color(0xFFEAB308)),
                    center = Offset(bx - bRadius * 0.2f, by - bRadius * 0.2f),
                    radius = bRadius * 1.2f
                ),
                radius = bRadius,
                center = Offset(bx, by)
            )

            // White Wing
            drawRoundRect(
                color = Color.White.copy(alpha = 0.9f),
                topLeft = Offset(bx - bRadius * 0.8f, by - bRadius * 0.35f),
                size = Size(bRadius * 0.9f, bRadius * 0.65f),
                cornerRadius = CornerRadius(bRadius * 0.3f, bRadius * 0.3f)
            )

            // Big Eye
            val eyeCenter = Offset(bx + bRadius * 0.45f, by - bRadius * 0.3f)
            drawCircle(
                color = Color.White,
                radius = bRadius * 0.34f,
                center = eyeCenter
            )
            // Pupil
            drawCircle(
                color = Color.Black,
                radius = bRadius * 0.16f,
                center = Offset(eyeCenter.x + bRadius * 0.08f, eyeCenter.y)
            )
            // Eye shine
            drawCircle(
                color = Color.White,
                radius = bRadius * 0.06f,
                center = Offset(eyeCenter.x + bRadius * 0.04f, eyeCenter.y - bRadius * 0.05f)
            )

            // Beak
            drawRoundRect(
                color = Color(0xFFF97316),
                topLeft = Offset(bx + bRadius * 0.7f, by - bRadius * 0.05f),
                size = Size(bRadius * 0.55f, bRadius * 0.38f),
                cornerRadius = CornerRadius(bRadius * 0.15f, bRadius * 0.15f)
            )
        }
    }
}
