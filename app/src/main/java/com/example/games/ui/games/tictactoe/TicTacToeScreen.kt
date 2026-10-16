package com.example.games.ui.games.tictactoe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.games.ui.common.GameTopBar
import com.example.games.ui.theme.ColorTicTacToe

@Composable
fun TicTacToeScreen(
    onBack: () -> Unit,
    viewModel: TicTacToeViewModel = viewModel()
) {
    val state by viewModel.state
    val haptic = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            GameTopBar(
                title = "Tic-Tac-Toe",
                onBack = onBack,
                accentColor = ColorTicTacToe,
                onReset = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.resetScores()
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Mode Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                FilterChip(
                    selected = state.mode == GameMode.AI,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.setMode(GameMode.AI)
                    },
                    label = { Text("vs Computer") },
                    leadingIcon = {
                        Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ColorTicTacToe.copy(alpha = 0.2f),
                        selectedLabelColor = ColorTicTacToe
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                )

                FilterChip(
                    selected = state.mode == GameMode.TWO_PLAYER,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.setMode(GameMode.TWO_PLAYER)
                    },
                    label = { Text("Pass & Play") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ColorTicTacToe.copy(alpha = 0.2f),
                        selectedLabelColor = ColorTicTacToe
                    )
                )
            }

            // Scoreboard
            TicTacToeScoreBoard(
                mode = state.mode,
                scoreX = state.scoreX,
                scoreO = state.scoreO,
                draws = state.scoreDraws
            )

            // Turn & Status Banner
            TurnStatusBadge(state = state)

            // 3x3 Board
            TicTacToeBoard(
                board = state.board,
                gameResult = state.gameResult,
                onCellClick = { index ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onCellClick(index)
                }
            )

            // Bottom Action
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state.gameResult != null) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.resetGame()
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorTicTacToe)
                    ) {
                        Text(
                            text = "Next Round",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TicTacToeScoreBoard(mode: GameMode, scoreX: Int, scoreO: Int, draws: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScorePlayerItem(
                label = if (mode == GameMode.AI) "You (X)" else "Player X",
                score = scoreX,
                color = Color(0xFF3B82F6)
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            ScorePlayerItem(
                label = "Ties",
                score = draws,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            ScorePlayerItem(
                label = if (mode == GameMode.AI) "AI (O)" else "Player O",
                score = scoreO,
                color = Color(0xFFEF4444)
            )
        }
    }
}

@Composable
fun ScorePlayerItem(label: String, score: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$score",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                color = color
            )
        )
    }
}

@Composable
fun TurnStatusBadge(state: TicTacToeState) {
    val text = when (val result = state.gameResult) {
        is GameResult.Draw -> "Game Drawn!"
        is GameResult.Winner -> {
            if (state.mode == GameMode.AI) {
                if (result.player == Player.X) "🎉 You Won!" else "🤖 AI Won!"
            } else {
                "Player ${result.player} Wins!"
            }
        }
        null -> {
            if (state.isAiThinking) "AI is thinking..."
            else if (state.mode == GameMode.AI) {
                if (state.currentPlayer == Player.X) "Your Turn (X)" else "AI Turn (O)"
            } else {
                "Player ${state.currentPlayer}'s Turn"
            }
        }
    }

    val tintColor = when (val result = state.gameResult) {
        is GameResult.Winner -> if (result.player == Player.X) Color(0xFF3B82F6) else Color(0xFFEF4444)
        is GameResult.Draw -> MaterialTheme.colorScheme.onSurfaceVariant
        null -> if (state.currentPlayer == Player.X) Color(0xFF3B82F6) else Color(0xFFEF4444)
    }

    Surface(
        color = tintColor.copy(alpha = 0.12f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = tintColor
        )
    }
}

@Composable
fun TicTacToeBoard(
    board: List<Player?>,
    gameResult: GameResult?,
    onCellClick: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(14.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(9) { index ->
                val winningLine = (gameResult as? GameResult.Winner)?.line ?: emptyList()
                val isWinningCell = index in winningLine

                CellItem(
                    player = board[index],
                    isWinningCell = isWinningCell,
                    onClick = { onCellClick(index) }
                )
            }
        }
    }
}

@Composable
fun CellItem(
    player: Player?,
    isWinningCell: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isWinningCell) 1.06f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cellScale"
    )

    val bgColor by animateColorAsState(
        targetValue = when {
            isWinningCell -> ColorTicTacToe.copy(alpha = 0.28f)
            else -> MaterialTheme.colorScheme.surface
        },
        label = "cellBg"
    )

    val contentColor = when (player) {
        Player.X -> Color(0xFF3B82F6)
        Player.O -> Color(0xFFEF4444)
        null -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = player == null, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isWinningCell) 6.dp else 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (player != null) {
                Icon(
                    imageVector = if (player == Player.X) Icons.Default.Close else Icons.Default.RadioButtonUnchecked,
                    contentDescription = player.name,
                    modifier = Modifier.fillMaxSize(0.65f),
                    tint = contentColor
                )
            }
        }
    }
}
