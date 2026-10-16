package com.example.games.ui.games.flappybird

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.games.data.PreferencesManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class FlappyStatus {
    READY, PLAYING, PAUSED, GAME_OVER
}

data class Pipe(
    val id: Long,
    val x: Float,          // Virtual X coordinate
    val gapY: Float,       // Top of gap in virtual Y
    val gapHeight: Float,  // Height of gap
    val width: Float,      // Width of pipe
    var hasScored: Boolean = false
)

data class Cloud(
    val x: Float,
    val y: Float,
    val speed: Float,
    val size: Float
)

data class FlappyBirdState(
    // Virtual coordinates: 0..1000 width, 0..1600 height
    val birdY: Float = 800f,
    val birdVelocity: Float = 0f,
    val birdRotation: Float = 0f,
    val pipes: List<Pipe> = emptyList(),
    val score: Int = 0,
    val highScore: Int = 0,
    val status: FlappyStatus = FlappyStatus.READY,
    val clouds: List<Cloud> = emptyList()
)

class FlappyBirdViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PreferencesManager.getInstance(application)

    // Virtual physics space constants
    val virtualWidth = 1000f
    val virtualHeight = 1600f
    val birdX = 250f
    val birdRadius = 38f
    val pipeWidth = 140f
    val pipeGap = 420f       // Generous, fair gap for mobile touch screens

    private val gravity = 1.05f
    private val jumpImpulse = -19.5f
    private val pipeSpeed = 6.2f
    private val pipeSpawnDistance = 580f // Distance between consecutive pipes

    private val _state = mutableStateOf(
        FlappyBirdState(
            highScore = prefs.getFlappyHighScore(),
            clouds = generateInitialClouds()
        )
    )
    val state: State<FlappyBirdState> = _state

    private var gameJob: Job? = null
    private var nextPipeId = 0L

    private fun generateInitialClouds(): List<Cloud> {
        return List(6) {
            Cloud(
                x = Random.nextFloat() * virtualWidth,
                y = Random.nextFloat() * (virtualHeight * 0.45f),
                speed = 0.8f + Random.nextFloat() * 1.5f,
                size = 70f + Random.nextFloat() * 110f
            )
        }
    }

    fun startGame() {
        if (_state.value.status == FlappyStatus.PLAYING) return

        _state.value = _state.value.copy(
            birdY = virtualHeight * 0.45f,
            birdVelocity = jumpImpulse * 0.7f,
            birdRotation = -20f,
            pipes = emptyList(),
            score = 0,
            status = FlappyStatus.PLAYING
        )

        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            while (isActive && _state.value.status == FlappyStatus.PLAYING) {
                updateGameLoop()
                delay(16) // ~60 FPS
            }
        }
    }

    fun jump() {
        when (_state.value.status) {
            FlappyStatus.READY -> startGame()
            FlappyStatus.PLAYING -> {
                _state.value = _state.value.copy(
                    birdVelocity = jumpImpulse,
                    birdRotation = -25f
                )
            }
            FlappyStatus.PAUSED -> {
                _state.value = _state.value.copy(status = FlappyStatus.PLAYING)
                resumeLoop()
            }
            FlappyStatus.GAME_OVER -> {
                startGame()
            }
        }
    }

    fun togglePause() {
        if (_state.value.status == FlappyStatus.PLAYING) {
            gameJob?.cancel()
            _state.value = _state.value.copy(status = FlappyStatus.PAUSED)
        } else if (_state.value.status == FlappyStatus.PAUSED) {
            _state.value = _state.value.copy(status = FlappyStatus.PLAYING)
            resumeLoop()
        }
    }

    private fun resumeLoop() {
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            while (isActive && _state.value.status == FlappyStatus.PLAYING) {
                updateGameLoop()
                delay(16)
            }
        }
    }

    private fun updateGameLoop() {
        val current = _state.value
        if (current.status != FlappyStatus.PLAYING) return

        // 1. Update Bird physics
        val newVelocity = current.birdVelocity + gravity
        val newY = current.birdY + newVelocity
        // Rotate smoothly downwards as speed increases
        val newRotation = if (newVelocity < 0) {
            -25f
        } else {
            (-25f + (newVelocity * 4.5f)).coerceAtMost(70f)
        }

        // 2. Move Pipes
        val movedPipes = current.pipes
            .map { it.copy(x = it.x - pipeSpeed) }
            .filter { it.x + it.width > -50f }

        // 3. Spawn new pipe if needed
        val lastPipe = movedPipes.maxByOrNull { it.x }
        val pipesWithNew = if (lastPipe == null || lastPipe.x <= virtualWidth - pipeSpawnDistance) {
            val minGapY = 160f
            val maxGapY = virtualHeight - pipeGap - 240f
            val randomGapY = minGapY + Random.nextFloat() * (maxGapY - minGapY)
            movedPipes + Pipe(
                id = nextPipeId++,
                x = virtualWidth + 20f,
                gapY = randomGapY,
                gapHeight = pipeGap,
                width = pipeWidth
            )
        } else {
            movedPipes
        }

        // 4. Update Score accurately
        var newScore = current.score
        pipesWithNew.forEach { pipe ->
            if (!pipe.hasScored && birdX > pipe.x + pipe.width) {
                pipe.hasScored = true
                newScore++
            }
        }

        // 5. Parallax Clouds
        val newClouds = current.clouds.map { cloud ->
            var nx = cloud.x - cloud.speed
            if (nx + cloud.size < 0) nx = virtualWidth + 50f
            cloud.copy(x = nx)
        }

        // 6. Collision Check
        var collision = false
        val groundY = virtualHeight - 100f // Floor limit

        // Ground or ceiling
        if (newY - birdRadius < 0f || newY + birdRadius >= groundY) {
            collision = true
        }

        // Pipe collision
        if (!collision) {
            for (pipe in pipesWithNew) {
                // Horizontal overlap
                if (birdX + birdRadius > pipe.x && birdX - birdRadius < pipe.x + pipe.width) {
                    // Vertical hit (top pipe or bottom pipe)
                    if (newY - birdRadius < pipe.gapY || newY + birdRadius > pipe.gapY + pipe.gapHeight) {
                        collision = true
                        break
                    }
                }
            }
        }

        if (collision) {
            gameJob?.cancel()
            val finalHighScore = maxOf(newScore, current.highScore)
            prefs.saveFlappyHighScore(finalHighScore)
            _state.value = current.copy(
                status = FlappyStatus.GAME_OVER,
                score = newScore,
                highScore = finalHighScore
            )
        } else {
            _state.value = current.copy(
                birdY = newY,
                birdVelocity = newVelocity,
                birdRotation = newRotation,
                pipes = pipesWithNew,
                score = newScore,
                clouds = newClouds
            )
        }
    }

    fun restartGame() {
        gameJob?.cancel()
        _state.value = FlappyBirdState(
            highScore = prefs.getFlappyHighScore(),
            status = FlappyStatus.READY,
            clouds = generateInitialClouds()
        )
    }

    override fun onCleared() {
        super.onCleared()
        gameJob?.cancel()
    }
}
