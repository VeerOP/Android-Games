package com.example.games.ui.games.snake

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnakeScreen(onBack: () -> Unit, viewModel: SnakeViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Snake Game") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Score: ${state.score}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val (x, y) = dragAmount
                            if (abs(x) > abs(y)) {
                                if (x > 0) viewModel.onDirectionChange(Direction.RIGHT)
                                else viewModel.onDirectionChange(Direction.LEFT)
                            } else {
                                if (y > 0) viewModel.onDirectionChange(Direction.DOWN)
                                else viewModel.onDirectionChange(Direction.UP)
                            }
                        }
                    }
            ) {
                SnakeGameBoard(state = state)

                if (state.isGameOver) {
                    GameOverOverlay(
                        score = state.score,
                        onRestart = { viewModel.restartGame() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SnakeControls(
                onDirectionChange = { viewModel.onDirectionChange(it) }
            )
        }
    }
}

@Composable
fun SnakeGameBoard(state: SnakeState) {
    val snakeColor = MaterialTheme.colorScheme.primary
    val foodColor = MaterialTheme.colorScheme.error
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cellSize = size.width / state.boardSize

        // Draw grid lines
        for (i in 0..state.boardSize) {
            drawLine(
                color = gridColor,
                start = Offset(i * cellSize, 0f),
                end = Offset(i * cellSize, size.height),
                strokeWidth = 1f
            )
            drawLine(
                color = gridColor,
                start = Offset(0f, i * cellSize),
                end = Offset(size.width, i * cellSize),
                strokeWidth = 1f
            )
        }

        // Draw food
        drawRect(
            color = foodColor,
            topLeft = Offset(state.food.first * cellSize + 2.dp.toPx(), state.food.second * cellSize + 2.dp.toPx()),
            size = Size(cellSize - 4.dp.toPx(), cellSize - 4.dp.toPx())
        )

        // Draw snake
        state.snake.forEachIndexed { index, part ->
            val isHead = index == 0
            drawRoundRect(
                color = if (isHead) snakeColor else snakeColor.copy(alpha = 0.7f),
                topLeft = Offset(part.first * cellSize + 1.dp.toPx(), part.second * cellSize + 1.dp.toPx()),
                size = Size(cellSize - 2.dp.toPx(), cellSize - 2.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                    if (isHead) 8.dp.toPx() else 4.dp.toPx()
                )
            )
        }
    }
}

@Composable
fun SnakeControls(onDirectionChange: (Direction) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ControlButton(Icons.Default.KeyboardArrowUp, "Up") { onDirectionChange(Direction.UP) }
        Row {
            ControlButton(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Left") { onDirectionChange(Direction.LEFT) }
            Spacer(modifier = Modifier.width(48.dp))
            ControlButton(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Right") { onDirectionChange(Direction.RIGHT) }
        }
        ControlButton(Icons.Default.KeyboardArrowDown, "Down") { onDirectionChange(Direction.DOWN) }
    }
}

@Composable
fun ControlButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(56.dp),
        shape = CircleShape
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription, modifier = Modifier.size(32.dp))
    }
}

@Composable
fun GameOverOverlay(score: Int, onRestart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .padding(32.dp)
        ) {
            Text(
                text = "Game Over",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Final Score: $score",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRestart,
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Restart Game")
            }
        }
    }
}
