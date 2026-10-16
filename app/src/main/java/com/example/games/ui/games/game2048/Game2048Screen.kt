package com.example.games.ui.games.game2048

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.games.ui.common.GameOverCard
import com.example.games.ui.common.GameTopBar
import com.example.games.ui.theme.Color2048
import kotlin.math.abs

@Composable
fun Game2048Screen(
    onBack: () -> Unit,
    viewModel: Game2048ViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            GameTopBar(
                title = "2048",
                onBack = onBack,
                accentColor = Color2048,
                highScore = uiState.highScore,
                onReset = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.resetGame()
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Score Board & Action Row
            ScoreAndActionsRow(
                score = uiState.score,
                highScore = uiState.highScore,
                canUndo = uiState.canUndo,
                onUndo = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.undo()
                },
                onReset = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.resetGame()
                }
            )

            // 4x4 Grid Board with Zero-Recomposition High-Performance Swipe Detection
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFBBADA0))
                    .pointerInput(Unit) {
                        var totalDragX = 0f
                        var totalDragY = 0f
                        var isGestureTriggered = false

                        detectDragGestures(
                            onDragStart = {
                                totalDragX = 0f
                                totalDragY = 0f
                                isGestureTriggered = false
                            },
                            onDragEnd = {
                                totalDragX = 0f
                                totalDragY = 0f
                                isGestureTriggered = false
                            },
                            onDragCancel = {
                                totalDragX = 0f
                                totalDragY = 0f
                                isGestureTriggered = false
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                if (!isGestureTriggered) {
                                    totalDragX += dragAmount.x
                                    totalDragY += dragAmount.y
                                    val threshold = 25f // Responsive quick flick threshold
                                    if (abs(totalDragX) > threshold || abs(totalDragY) > threshold) {
                                        isGestureTriggered = true
                                        val moved = if (abs(totalDragX) > abs(totalDragY)) {
                                            if (totalDragX > 0) viewModel.move(Direction.RIGHT)
                                            else viewModel.move(Direction.LEFT)
                                        } else {
                                            if (totalDragY > 0) viewModel.move(Direction.DOWN)
                                            else viewModel.move(Direction.UP)
                                        }
                                        if (moved) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                    }
                                }
                            }
                        )
                    }
                    .padding(10.dp)
            ) {
                GridBoardContent(grid = uiState.grid)
            }

            // Quick instruction footer
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = "Swipe in any direction to merge matching tiles",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Win Celebration Dialog
        if (uiState.isWin) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissWinBanner() },
                title = {
                    Text(
                        "🎉 You Reached 2048!",
                        fontWeight = FontWeight.Bold,
                        color = Color2048
                    )
                },
                text = {
                    Text("Incredible job! Would you like to keep playing for an even higher score or start a fresh board?")
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.dismissWinBanner() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color2048)
                    ) {
                        Text("Keep Playing")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.resetGame() }) {
                        Text("New Game")
                    }
                }
            )
        }

        // Game Over Overlay
        AnimatedVisibility(
            visible = uiState.isGameOver,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            GameOverCard(
                title = "No More Moves!",
                score = uiState.score,
                highScore = uiState.highScore,
                accentColor = Color2048,
                onRestart = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.resetGame()
                },
                onBack = onBack
            )
        }
    }
}

@Composable
fun ScoreAndActionsRow(
    score: Int,
    highScore: Int,
    canUndo: Boolean,
    onUndo: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ScoreBadgeItem(label = "SCORE", value = score, bgColor = Color(0xFFBBADA0))
            ScoreBadgeItem(label = "BEST", value = highScore, bgColor = Color(0xFF8F7A66))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(
                onClick = onUndo,
                enabled = canUndo,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Undo", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onReset,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color2048),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "New Game", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ScoreBadgeItem(label: String, value: Int, bgColor: Color) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEEE4DA)
            )
            Text(
                text = "$value",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

@Composable
fun GridBoardContent(grid: List<List<Int>>) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        grid.forEach { row ->
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { value ->
                    Box(modifier = Modifier.weight(1f)) {
                        TileView(value = value)
                    }
                }
            }
        }
    }
}

@Composable
fun TileView(value: Int) {
    val (bgColor, textColor) = getTileColors(value)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        if (value != 0) {
            Text(
                text = value.toString(),
                fontSize = when {
                    value < 100 -> 30.sp
                    value < 1000 -> 24.sp
                    else -> 18.sp
                },
                fontWeight = FontWeight.Black,
                color = textColor
            )
        }
    }
}

fun getTileColors(value: Int): Pair<Color, Color> {
    return when (value) {
        0 -> Pair(Color(0xFFCDC1B4), Color.Transparent)
        2 -> Pair(Color(0xFFEEE4DA), Color(0xFF776E65))
        4 -> Pair(Color(0xFFEDE0C8), Color(0xFF776E65))
        8 -> Pair(Color(0xFFF2B179), Color.White)
        16 -> Pair(Color(0xFFF59563), Color.White)
        32 -> Pair(Color(0xFFF67C5F), Color.White)
        64 -> Pair(Color(0xFFEA580C), Color.White)
        128 -> Pair(Color(0xFFEDCF72), Color.White)
        256 -> Pair(Color(0xFFEDCC61), Color.White)
        512 -> Pair(Color(0xFFEAB308), Color.White)
        1024 -> Pair(Color(0xFFCA8A04), Color.White)
        2048 -> Pair(Color(0xFF8B5CF6), Color.White)
        else -> Pair(Color(0xFF4C1D95), Color.White)
    }
}
