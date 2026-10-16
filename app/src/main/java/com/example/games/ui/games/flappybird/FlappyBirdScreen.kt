package com.example.games.ui.games.flappybird

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun FlappyBirdScreen(
    viewModel: FlappyBirdViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.state

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF70C5CE)) // Sky Blue
            .onGloballyPositioned {
                viewModel.onDimensionsChanged(it.size.width.toFloat(), it.size.height.toFloat())
            }
            .clickable(enabled = state.status == GameStatus.PLAYING) {
                viewModel.jump()
            }
    ) {
        // Clouds
        Canvas(modifier = Modifier.fillMaxSize()) {
            state.clouds.forEach { cloud ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.7f),
                    radius = cloud.size / 2,
                    center = Offset(cloud.x, cloud.y)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.7f),
                    radius = cloud.size / 3,
                    center = Offset(cloud.x - cloud.size / 3, cloud.y + cloud.size / 10)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.7f),
                    radius = cloud.size / 3,
                    center = Offset(cloud.x + cloud.size / 3, cloud.y + cloud.size / 10)
                )
            }
        }

        // Pipes
        Canvas(modifier = Modifier.fillMaxSize()) {
            state.pipes.forEach { pipe ->
                // Top Pipe
                drawRoundRect(
                    color = Color(0xFF73BF2E),
                    topLeft = Offset(pipe.x, 0f),
                    size = Size(pipe.width, pipe.gapY),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                // Rim for Top Pipe
                drawRoundRect(
                    color = Color(0xFF538021),
                    topLeft = Offset(pipe.x - 5f, pipe.gapY - 40f),
                    size = Size(pipe.width + 10f, 40f),
                    cornerRadius = CornerRadius(4f, 4f)
                )

                // Bottom Pipe
                drawRoundRect(
                    color = Color(0xFF73BF2E),
                    topLeft = Offset(pipe.x, pipe.gapY + pipe.gapHeight),
                    size = Size(pipe.width, size.height - (pipe.gapY + pipe.gapHeight)),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                // Rim for Bottom Pipe
                drawRoundRect(
                    color = Color(0xFF538021),
                    topLeft = Offset(pipe.x - 5f, pipe.gapY + pipe.gapHeight),
                    size = Size(pipe.width + 10f, 40f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }
        }

        // Bird
        Canvas(modifier = Modifier.fillMaxSize()) {
            val birdX = size.width / 4
            rotate(degrees = state.birdRotation, pivot = Offset(birdX, state.birdY)) {
                // Body
                drawCircle(
                    color = Color(0xFFFFEB3B),
                    radius = 30f,
                    center = Offset(birdX, state.birdY)
                )
                // Eye
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = Offset(birdX + 15f, state.birdY - 10f)
                )
                drawCircle(
                    color = Color.Black,
                    radius = 3f,
                    center = Offset(birdX + 18f, state.birdY - 10f)
                )
                // Beak
                drawRoundRect(
                    color = Color(0xFFFF5722),
                    topLeft = Offset(birdX + 25f, state.birdY - 2f),
                    size = Size(15f, 10f),
                    cornerRadius = CornerRadius(5f, 5f)
                )
                // Wing
                drawArc(
                    color = Color.White,
                    startAngle = 150f,
                    sweepAngle = 120f,
                    useCenter = true,
                    topLeft = Offset(birdX - 25f, state.birdY - 15f),
                    size = Size(30f, 30f)
                )
            }
        }

        // Score
        Text(
            text = state.score.toString(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp),
            style = MaterialTheme.typography.displayLarge.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black,
                    offset = Offset(4f, 4f),
                    blurRadius = 4f
                )
            )
        )

        // Overlay Screens
        when (state.status) {
            GameStatus.START -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(32.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "FLAPPY BIRD",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF538021)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Tap to jump and avoid pipes",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.startGame() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("START GAME")
                            }
                        }
                    }
                }
            }
            GameStatus.GAME_OVER -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(32.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "GAME OVER",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Red
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Score: ${state.score}",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                text = "Best: ${state.highScore}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.startGame() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("RESTART")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = onBack,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("BACK TO MENU")
                            }
                        }
                    }
                }
            }
            GameStatus.PLAYING -> {
                // No overlay during gameplay
            }
        }
    }
}
