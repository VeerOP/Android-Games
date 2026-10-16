package com.example.games.ui.games.snake

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.games.ui.common.GameOverCard
import com.example.games.ui.common.GameTopBar
import com.example.games.ui.theme.ColorSnake
import com.example.games.ui.theme.GoldTrophy
import kotlin.math.abs

@Composable
fun SnakeScreen(
    onBack: () -> Unit,
    viewModel: SnakeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val haptic = LocalHapticFeedback.current

    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    Scaffold(
        topBar = {
            GameTopBar(
                title = "Snake",
                onBack = onBack,
                accentColor = ColorSnake,
                highScore = state.highScore,
                onReset = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.restartGame()
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // HUD Banner
            SnakeScoreHeader(
                score = state.score,
                highScore = state.highScore,
                status = state.status,
                onTogglePause = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.togglePause()
                }
            )

            // Game Board Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .pointerInput(state.status) {
                        detectDragGestures(
                            onDragStart = { dragOffset = Offset.Zero },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset += dragAmount
                                val minSwipeDist = 40f
                                if (abs(dragOffset.x) > minSwipeDist || abs(dragOffset.y) > minSwipeDist) {
                                    if (abs(dragOffset.x) > abs(dragOffset.y)) {
                                        if (dragOffset.x > 0) {
                                            viewModel.onDirectionChange(Direction.RIGHT)
                                        } else {
                                            viewModel.onDirectionChange(Direction.LEFT)
                                        }
                                    } else {
                                        if (dragOffset.y > 0) {
                                            viewModel.onDirectionChange(Direction.DOWN)
                                        } else {
                                            viewModel.onDirectionChange(Direction.UP)
                                        }
                                    }
                                    dragOffset = Offset.Zero
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                SnakeCanvasBoard(state = state)

                // Ready Overlay
                if (state.status == SnakeGameStatus.READY) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.startGame()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorSnake),
                            shape = RoundedCornerShape(20.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                            modifier = Modifier.height(50.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Tap to Start",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                // Paused Overlay
                if (state.status == SnakeGameStatus.PAUSED) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PAUSED",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 2.sp
                            )
                        )
                    }
                }
            }

            // Controls D-Pad
            SnakeDPadControls(
                onDirectionChange = { dir ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onDirectionChange(dir)
                }
            )
        }

        // Game Over Overlay
        AnimatedVisibility(
            visible = state.status == SnakeGameStatus.GAME_OVER,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            GameOverCard(
                title = "Snake Crashed!",
                score = state.score,
                highScore = state.highScore,
                accentColor = ColorSnake,
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
fun SnakeScoreHeader(
    score: Int,
    highScore: Int,
    status: SnakeGameStatus,
    onTogglePause: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Score: ",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Text(
                text = "$score",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = ColorSnake
                )
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Best: ",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Text(
                    text = "$highScore",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = GoldTrophy
                    )
                )
            }

            if (status == SnakeGameStatus.PLAYING || status == SnakeGameStatus.PAUSED) {
                IconButton(
                    onClick = onTogglePause,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (status == SnakeGameStatus.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = "Toggle Pause",
                        tint = ColorSnake
                    )
                }
            }
        }
    }
}

@Composable
fun SnakeCanvasBoard(state: SnakeState) {
    val snakeHeadColor = ColorSnake
    val snakeBodyColor = ColorSnake.copy(alpha = 0.85f)
    val foodColor = Color(0xFFEF4444)
    val dotGridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cellSize = size.width / state.boardSize

        // Draw subtle background dots at grid centers
        for (x in 0 until state.boardSize) {
            for (y in 0 until state.boardSize) {
                drawCircle(
                    color = dotGridColor,
                    radius = 1.2.dp.toPx(),
                    center = Offset((x + 0.5f) * cellSize, (y + 0.5f) * cellSize)
                )
            }
        }

        // Draw food (Juicy Apple style)
        val foodCenter = Offset(
            (state.food.first + 0.5f) * cellSize,
            (state.food.second + 0.5f) * cellSize
        )
        val foodRadius = cellSize * 0.42f
        drawCircle(
            color = foodColor,
            radius = foodRadius,
            center = foodCenter
        )
        // Food shine highlight
        drawCircle(
            color = Color.White.copy(alpha = 0.6f),
            radius = foodRadius * 0.3f,
            center = Offset(foodCenter.x - foodRadius * 0.35f, foodCenter.y - foodRadius * 0.35f)
        )
        // Little green stem / leaf
        drawCircle(
            color = Color(0xFF22C55E),
            radius = foodRadius * 0.25f,
            center = Offset(foodCenter.x + foodRadius * 0.3f, foodCenter.y - foodRadius * 0.7f)
        )

        // Draw Snake body segments
        state.snake.forEachIndexed { index, part ->
            val isHead = index == 0
            val segTopLeft = Offset(
                part.first * cellSize + 1.5.dp.toPx(),
                part.second * cellSize + 1.5.dp.toPx()
            )
            val segSize = Size(
                cellSize - 3.dp.toPx(),
                cellSize - 3.dp.toPx()
            )

            drawRoundRect(
                color = if (isHead) snakeHeadColor else snakeBodyColor,
                topLeft = segTopLeft,
                size = segSize,
                cornerRadius = CornerRadius(
                    if (isHead) cellSize * 0.4f else cellSize * 0.25f
                )
            )

            // Draw cute cartoon eyes on head
            if (isHead) {
                val headCenter = Offset(
                    (part.first + 0.5f) * cellSize,
                    (part.second + 0.5f) * cellSize
                )
                val eyeOffset = cellSize * 0.2f
                val eyeRadius = cellSize * 0.12f
                val pupilRadius = cellSize * 0.06f

                val (eye1Center, eye2Center) = when (state.currentDirection) {
                    Direction.UP -> Pair(
                        Offset(headCenter.x - eyeOffset, headCenter.y - eyeOffset * 0.6f),
                        Offset(headCenter.x + eyeOffset, headCenter.y - eyeOffset * 0.6f)
                    )
                    Direction.DOWN -> Pair(
                        Offset(headCenter.x - eyeOffset, headCenter.y + eyeOffset * 0.6f),
                        Offset(headCenter.x + eyeOffset, headCenter.y + eyeOffset * 0.6f)
                    )
                    Direction.LEFT -> Pair(
                        Offset(headCenter.x - eyeOffset * 0.6f, headCenter.y - eyeOffset),
                        Offset(headCenter.x - eyeOffset * 0.6f, headCenter.y + eyeOffset)
                    )
                    Direction.RIGHT -> Pair(
                        Offset(headCenter.x + eyeOffset * 0.6f, headCenter.y - eyeOffset),
                        Offset(headCenter.x + eyeOffset * 0.6f, headCenter.y + eyeOffset)
                    )
                }

                // White sclera
                drawCircle(color = Color.White, radius = eyeRadius, center = eye1Center)
                drawCircle(color = Color.White, radius = eyeRadius, center = eye2Center)
                // Black pupil
                drawCircle(color = Color.Black, radius = pupilRadius, center = eye1Center)
                drawCircle(color = Color.Black, radius = pupilRadius, center = eye2Center)
            }
        }
    }
}

@Composable
fun SnakeDPadControls(onDirectionChange: (Direction) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        DPadButton(Icons.Default.KeyboardArrowUp, "Up") { onDirectionChange(Direction.UP) }
        Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
            DPadButton(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Left") { onDirectionChange(Direction.LEFT) }
            DPadButton(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Right") { onDirectionChange(Direction.RIGHT) }
        }
        DPadButton(Icons.Default.KeyboardArrowDown, "Down") { onDirectionChange(Direction.DOWN) }
    }
}

@Composable
fun DPadButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier
            .size(52.dp)
            .shadow(4.dp, CircleShape),
        shape = CircleShape,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp)
        )
    }
}
