package com.example.games.ui.home

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.games.Screen
import com.example.games.data.PreferencesManager
import com.example.games.ui.theme.*

data class GameCardItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val screen: Screen,
    val getHighScoreText: (PreferencesManager) -> String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onGameSelected: (Screen) -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val prefs = remember { PreferencesManager.getInstance(context) }

    val games = remember {
        listOf(
            GameCardItem(
                title = "Tic-Tac-Toe",
                subtitle = "Single Player vs AI & 2-Player Pass-and-Play",
                icon = Icons.Default.GridView,
                accentColor = ColorTicTacToe,
                screen = Screen.TicTacToe,
                getHighScoreText = { null }
            ),
            GameCardItem(
                title = "Flappy Bird",
                subtitle = "Smooth 60 FPS physics arcade flight",
                icon = Icons.Default.AirplanemodeActive,
                accentColor = ColorFlappy,
                screen = Screen.FlappyBird,
                getHighScoreText = { p ->
                    val s = p.getFlappyHighScore()
                    if (s > 0) "Best: $s" else null
                }
            ),
            GameCardItem(
                title = "Snake",
                subtitle = "Retro arcade crawler with swipe & D-Pad controls",
                icon = Icons.Default.BugReport,
                accentColor = ColorSnake,
                screen = Screen.Snake,
                getHighScoreText = { p ->
                    val s = p.getSnakeHighScore()
                    if (s > 0) "Best: $s" else null
                }
            ),
            GameCardItem(
                title = "Memory Match",
                subtitle = "3D card flip puzzle with star ratings",
                icon = Icons.Default.Psychology,
                accentColor = ColorMemory,
                screen = Screen.MemoryMatch,
                getHighScoreText = { p ->
                    val s = p.getMemoryBestMoves("EASY")
                    if (s != Int.MAX_VALUE) "Best: $s moves" else null
                }
            ),
            GameCardItem(
                title = "2048",
                subtitle = "Swipe & merge sliding number puzzle with undo",
                icon = Icons.Default.Filter9Plus,
                accentColor = Color2048,
                screen = Screen.Puzzle2048,
                getHighScoreText = { p ->
                    val s = p.get2048HighScore()
                    if (s > 0) "Best: $s" else null
                }
            )
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "Arcade Hub",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {
            // Hero Welcome Banner
            item {
                HomeHeroBanner()
            }

            // Games List Cards
            items(games) { game ->
                ModernGameCard(
                    game = game,
                    highScoreText = game.getHighScoreText(prefs),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onGameSelected(game.screen)
                    }
                )
            }
        }
    }
}

@Composable
fun HomeHeroBanner() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pick a Game",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Classic arcade challenges with smooth touch controls & persistent high scores.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                )
            }
        }
    }
}

@Composable
fun ModernGameCard(
    game: GameCardItem,
    highScoreText: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vibrant Icon Box
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(game.accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = game.icon,
                    contentDescription = null,
                    tint = game.accentColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Game Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = game.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (highScoreText != null) {
                        Surface(
                            color = GoldTrophy.copy(alpha = 0.15f),
                            shape = CircleShape
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = GoldTrophy,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = highScoreText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = GoldTrophy
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = game.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Play Arrow Button
            Surface(
                shape = CircleShape,
                color = game.accentColor,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
