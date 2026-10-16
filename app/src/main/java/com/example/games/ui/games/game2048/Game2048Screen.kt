package com.example.games.ui.games.game2048

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.abs

@Composable
fun Game2048Screen(
    onBack: () -> Unit,
    viewModel: Game2048ViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var dragAmountAccumulated by remember { mutableStateOf(Offset.Zero) }
    var moveTriggered by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(text = "2048", fontSize = 40.sp, fontWeight = FontWeight.Bold)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                ScoreBox(label = "SCORE", score = uiState.score)
                Spacer(modifier = Modifier.height(4.dp))
                ScoreBox(label = "BEST", score = uiState.highScore)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { viewModel.undo() },
                enabled = uiState.canUndo,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Undo")
            }
            Button(onClick = { viewModel.resetGame() }) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Grid
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .background(Color(0xFFBBADA0), RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { 
                            dragAmountAccumulated = Offset.Zero
                            moveTriggered = false
                        },
                        onDrag = { change, dragAmount ->
                            if (!moveTriggered) {
                                dragAmountAccumulated += dragAmount
                                val threshold = 50f
                                val x = dragAmountAccumulated.x
                                val y = dragAmountAccumulated.y
                                if (abs(x) > threshold || abs(y) > threshold) {
                                    if (abs(x) > abs(y)) {
                                        if (x > 0) viewModel.move(Direction.RIGHT)
                                        else viewModel.move(Direction.LEFT)
                                    } else {
                                        if (y > 0) viewModel.move(Direction.DOWN)
                                        else viewModel.move(Direction.UP)
                                    }
                                    moveTriggered = true 
                                }
                            }
                        }
                    )
                }
                .padding(8.dp)
        ) {
            GridContent(grid = uiState.grid)

            if (uiState.isGameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x88FFFFFF), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Game Over!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF776E65))
                        Button(onClick = { viewModel.resetGame() }) {
                            Text("Try Again")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreBox(label: String, score: Int) {
    Surface(
        color = Color(0xFFBBADA0),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, color = Color(0xFFEEE4DA), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = score.toString(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun GridContent(grid: List<List<Int>>) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        grid.forEach { row ->
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { value ->
                    Box(modifier = Modifier.weight(1f)) {
                        Tile(value = value)
                    }
                }
            }
        }
    }
}

@Composable
fun Tile(value: Int) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = getTileColor(value),
        shape = RoundedCornerShape(4.dp)
    ) {
        if (value != 0) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = value.toString(),
                    fontSize = if (value < 100) 32.sp else if (value < 1000) 24.sp else 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (value <= 4) Color(0xFF776E65) else Color.White
                )
            }
        }
    }
}

fun getTileColor(value: Int): Color {
    return when (value) {
        0 -> Color(0xFFCDC1B4)
        2 -> Color(0xFFEEE4DA)
        4 -> Color(0xFFEDE0C8)
        8 -> Color(0xFFF2B179)
        16 -> Color(0xFFF59563)
        32 -> Color(0xFFF67C5F)
        64 -> Color(0xFFF65E3B)
        128 -> Color(0xFFEDCF72)
        256 -> Color(0xFFEDCC61)
        512 -> Color(0xFFEDC850)
        1024 -> Color(0xFFEDC53F)
        2048 -> Color(0xFFEDC22E)
        else -> Color(0xFF3C3A32)
    }
}

@Preview(showBackground = true)
@Composable
fun Game2048Preview() {
    MaterialTheme {
        Game2048Screen(onBack = {})
    }
}
