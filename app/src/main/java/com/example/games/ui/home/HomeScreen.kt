package com.example.games.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.games.Screen

data class GameInfo(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val screen: Screen
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onGameSelected: (Screen) -> Unit) {
    val games = listOf(
        GameInfo("Tic-Tac-Toe", Icons.Default.GridView, Color(0xFF4CAF50), Screen.TicTacToe),
        GameInfo("Flappy Bird", Icons.Default.AirplanemodeActive, Color(0xFF2196F3), Screen.FlappyBird),
        GameInfo("Snake", Icons.Default.BugReport, Color(0xFFFF9800), Screen.Snake),
        GameInfo("Memory Match", Icons.Default.Apps, Color(0xFFE91E63), Screen.MemoryMatch),
        GameInfo("2048", Icons.Default.Extension, Color(0xFF9C27B0), Screen.Puzzle2048)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Game Hub",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            items(games) { game ->
                GameCard(game = game, onClick = { onGameSelected(game.screen) })
            }
        }
    }
}

@Composable
fun GameCard(game: GameInfo, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = game.color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = game.icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = game.color
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = game.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = game.color),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Play", fontSize = 12.sp)
            }
        }
    }
}
