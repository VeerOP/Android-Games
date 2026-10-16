package com.example.games.ui.games.tictactoe

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicTacToeScreen(
    onBack: () -> Unit,
    viewModel: TicTacToeViewModel = viewModel(),
) {
    val state by viewModel.state

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Tic-Tac-Toe", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.resetAll() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset All")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ScoreBoard(state.scoreX, state.scoreO)

            StatusIndicator(state)

            Board(
                board = state.board,
                gameResult = state.gameResult,
            ) { viewModel.onCellClick(index = it) }

            if (state.gameResult != null) {
                Button(
                    onClick = { viewModel.resetGame() },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text("Play Again", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun ScoreBoard(scoreX: Int, scoreO: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScoreCard("Player X", scoreX, Color(0xFF2196F3), Modifier.weight(1f))
        ScoreCard("Player O", scoreO, Color(0xFFF44336), Modifier.weight(1f))
    }
}

@Composable
fun ScoreCard(label: String, score: Int, color: Color, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = color.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = color)
            Text(
                score.toString(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun StatusIndicator(state: TicTacToeState) {
    val text = when (val result = state.gameResult) {
        is GameResult.Draw -> "It's a Draw!"
        is GameResult.Winner -> "Player ${result.player} Wins!"
        null -> "Player ${state.currentPlayer}'s Turn"
    }

    val color = when (val result = state.gameResult) {
        is GameResult.Winner -> if (result.player == Player.X) Color(0xFF2196F3) else Color(0xFFF44336)
        is GameResult.Draw -> MaterialTheme.colorScheme.onSurfaceVariant
        null -> if (state.currentPlayer == Player.X) Color(0xFF2196F3) else Color(0xFFF44336)
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
fun Board(
    board: List<Player?>,
    gameResult: GameResult?,
    onCellClick: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .padding(12.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(9) { index ->
                val winningLine = (gameResult as? GameResult.Winner)?.line ?: emptyList()
                val isWinningCell = index in winningLine

                Cell(
                    player = board[index],
                    isWinningCell = isWinningCell,
                    onClick = { onCellClick(index) }
                )
            }
        }
    }
}

@Composable
fun Cell(
    player: Player?,
    isWinningCell: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isWinningCell) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cellScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isWinningCell -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        label = "cellBackground"
    )

    val contentColor = when (player) {
        Player.X -> Color(0xFF2196F3)
        Player.O -> Color(0xFFF44336)
        null -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .aspectRatio(1f)
            .clickable(enabled = player == null, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isWinningCell) 8.dp else 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (player != null) {
                Icon(
                    imageVector = if (player == Player.X) Icons.Default.Close else Icons.Default.RadioButtonUnchecked,
                    contentDescription = player.name,
                    modifier = Modifier.fillMaxSize(0.6f),
                    tint = contentColor
                )
            }
        }
    }
}
