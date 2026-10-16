package com.example.games.ui.games.memorymatch

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryMatchScreen(
    viewModel: MemoryMatchViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Memory Match") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.startNewGame() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Game")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Moves: ${viewModel.moves}", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Time: ${formatTime(viewModel.timeSeconds)}",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            DifficultySelector(
                currentDifficulty = viewModel.difficulty,
                onDifficultySelected = { viewModel.setGameDifficulty(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(viewModel.difficulty.cols),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(viewModel.cards) { index, card ->
                    MemoryCardItem(
                        card = card,
                        onClick = { viewModel.onCardClicked(index) }
                    )
                }
            }
        }
    }

    if (viewModel.isWon) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Congratulations!") },
            text = {
                Text(
                    "You won in ${viewModel.moves} moves and ${formatTime(viewModel.timeSeconds)}!"
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.startNewGame() }) {
                    Text("Play Again")
                }
            },
            dismissButton = {
                TextButton(onClick = onBack) {
                    Text("Exit")
                }
            }
        )
    }
}

@Composable
fun DifficultySelector(
    currentDifficulty: Difficulty,
    onDifficultySelected: (Difficulty) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Difficulty.entries.forEach { difficulty ->
            FilterChip(
                selected = currentDifficulty == difficulty,
                onClick = { onDifficultySelected(difficulty) },
                label = { Text(difficulty.name.lowercase().capitalize()) },
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun MemoryCardItem(
    card: MemoryCard,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (card.isFlipped || card.isMatched) 180f else 0f,
        animationSpec = tween(durationMillis = 400)
    )

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 8 * density
            }
            .clickable(enabled = !card.isFlipped && !card.isMatched) { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (card.isMatched) MaterialTheme.colorScheme.tertiaryContainer 
                            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (rotation > 90f) {
                // Back side of the card (the one with the content)
                Icon(
                    imageVector = card.content,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize(0.6f)
                        .graphicsLayer { rotationY = 180f },
                    tint = if (card.isMatched) MaterialTheme.colorScheme.onTertiaryContainer 
                           else MaterialTheme.colorScheme.primary
                )
            } else {
                // Front side of the card
                Text(
                    text = "?",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
