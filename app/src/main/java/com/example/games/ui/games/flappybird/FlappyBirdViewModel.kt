package com.example.games.ui.games.flappybird

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class GameStatus {
    START, PLAYING, GAME_OVER
}

data class Pipe(
    val x: Float,
    val gapY: Float, // Top of the gap
    val gapHeight: Float = 200f,
    val width: Float = 150f
)

data class FlappyBirdState(
    val birdY: Float = 0f,
    val birdVelocity: Float = 0f,
    val birdRotation: Float = 0f,
    val pipes: List<Pipe> = emptyList(),
    val score: Int = 0,
    val highScore: Int = 0,
    val status: GameStatus = GameStatus.START,
    val screenWidth: Float = 0f,
    val screenHeight: Float = 0f,
    val clouds: List<Cloud> = emptyList()
)

data class Cloud(
    val x: Float,
    val y: Float,
    val speed: Float,
    val size: Float
)

class FlappyBirdViewModel : ViewModel() {
    private val _state = mutableStateOf(FlappyBirdState())
    val state: State<FlappyBirdState> = _state

    private var gameJob: Job? = null
    
    private val gravity = 1.2f
    private val jumpStrength = -20f
    private val pipeSpeed = 8f
    private val pipeSpawnInterval = 1500L // ms
    private var lastPipeTime = 0L

    fun onDimensionsChanged(width: Float, height: Float) {
        if (_state.value.screenWidth == 0f) {
            _state.value = _state.value.copy(
                birdY = height / 2,
                screenWidth = width,
                screenHeight = height,
                clouds = generateInitialClouds(width, height)
            )
        }
    }

    private fun generateInitialClouds(width: Float, height: Float): List<Cloud> {
        return List(5) {
            Cloud(
                x = Random.nextFloat() * width,
                y = Random.nextFloat() * (height / 2),
                speed = 1f + Random.nextFloat() * 2f,
                size = 50f + Random.nextFloat() * 100f
            )
        }
    }

    fun startGame() {
        if (_state.value.status == GameStatus.PLAYING) return
        
        _state.value = _state.value.copy(
            birdY = _state.value.screenHeight / 2,
            birdVelocity = 0f,
            birdRotation = 0f,
            pipes = emptyList(),
            score = 0,
            status = GameStatus.PLAYING
        )
        
        lastPipeTime = System.currentTimeMillis()
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            while (isActive && _state.value.status == GameStatus.PLAYING) {
                updateGame()
                delay(16) // ~60 FPS
            }
        }
    }

    fun jump() {
        if (_state.value.status == GameStatus.PLAYING) {
            _state.value = _state.value.copy(birdVelocity = jumpStrength)
        } else if (_state.value.status != GameStatus.PLAYING) {
            startGame()
        }
    }

    private fun updateGame() {
        val currentState = _state.value
        if (currentState.status != GameStatus.PLAYING) return

        // Update Bird
        val newVelocity = currentState.birdVelocity + gravity
        val newY = currentState.birdY + newVelocity
        val newRotation = (newVelocity * 2f).coerceIn(-30f, 45f)

        // Update Pipes
        val newPipes = currentState.pipes.map { it.copy(x = it.x - pipeSpeed) }
            .filter { it.x + it.width > 0 }

        // Spawn new pipes
        val currentTime = System.currentTimeMillis()
        val pipesWithNew = if (currentTime - lastPipeTime > pipeSpawnInterval) {
            lastPipeTime = currentTime
            newPipes + Pipe(
                x = currentState.screenWidth,
                gapY = 100f + Random.nextFloat() * (currentState.screenHeight - 400f)
            )
        } else {
            newPipes
        }

        // Update Score
        var newScore = currentState.score
        pipesWithNew.forEach { pipe ->
            // If bird just passed the pipe
            if (currentState.screenWidth / 4 > pipe.x + pipe.width && currentState.screenWidth / 4 <= pipe.x + pipe.width + pipeSpeed) {
                newScore++
            }
        }

        // Update Clouds (Parallax)
        val newClouds = currentState.clouds.map { 
            var newX = it.x - it.speed
            if (newX + it.size < 0) newX = currentState.screenWidth
            it.copy(x = newX)
        }

        // Collision Detection
        val birdX = currentState.screenWidth / 4
        val birdRadius = 30f
        
        var collision = false
        // Ground or Ceiling
        if (newY - birdRadius < 0 || newY + birdRadius > currentState.screenHeight) {
            collision = true
        }

        // Pipes
        pipesWithNew.forEach { pipe ->
            if (birdX + birdRadius > pipe.x && birdX - birdRadius < pipe.x + pipe.width) {
                if (newY - birdRadius < pipe.gapY || newY + birdRadius > pipe.gapY + pipe.gapHeight) {
                    collision = true
                }
            }
        }

        if (collision) {
            _state.value = currentState.copy(
                status = GameStatus.GAME_OVER,
                highScore = maxOf(newScore, currentState.highScore)
            )
            gameJob?.cancel()
        } else {
            _state.value = currentState.copy(
                birdY = newY,
                birdVelocity = newVelocity,
                birdRotation = newRotation,
                pipes = pipesWithNew,
                score = newScore,
                clouds = newClouds
            )
        }
    }
}
