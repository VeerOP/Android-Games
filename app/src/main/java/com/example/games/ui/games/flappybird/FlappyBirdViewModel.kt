package com.example.games.ui.games.flappybird

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.games.data.PreferencesManager
import kotlin.random.Random

enum class FlappyStatus {
    READY, PLAYING, PAUSED, GAME_OVER
}

data class Pipe(
    val id: Long,
    val x: Float,
    val gapY: Float,
    val gapHeight: Float,
    val width: Float,
    var hasScored: Boolean = false
)

data class Cloud(
    val x: Float,
    val y: Float,
    val speed: Float,
    val size: Float
)

class FlappyBirdViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PreferencesManager.getInstance(application)

    // Virtual physics space constants
    val virtualWidth = 1000f
    val virtualHeight = 1600f
    val birdX = 250f
    val birdRadius = 38f
    val pipeWidth = 140f
    val pipeGap = 420f

    private val gravity = 1.05f
    private val jumpImpulse = -19.5f
    private val pipeSpeed = 6.2f
    private val pipeSpawnDistance = 580f

    // UI state (recomposes UI ONLY on actual changes)
    var status by mutableStateOf(FlappyStatus.READY)
        private set
    var score by mutableIntStateOf(0)
        private set
    var highScore by mutableIntStateOf(prefs.getFlappyHighScore())
        private set

    // Animation / Render state (read inside Canvas DrawScope for 120 FPS hardware drawing)
    var birdY by mutableFloatStateOf(800f)
        private set
    var birdRotation by mutableFloatStateOf(0f)
        private set
    var pipes by mutableStateOf<List<Pipe>>(emptyList())
        private set
    var clouds by mutableStateOf<List<Cloud>>(generateInitialClouds())
        private set

    private var birdVelocity = 0f
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
        if (status == FlappyStatus.PLAYING) return

        birdY = virtualHeight * 0.45f
        birdVelocity = jumpImpulse * 0.7f
        birdRotation = -20f
        pipes = emptyList()
        score = 0
        status = FlappyStatus.PLAYING
    }

    fun jump() {
        when (status) {
            FlappyStatus.READY -> startGame()
            FlappyStatus.PLAYING -> {
                birdVelocity = jumpImpulse
                birdRotation = -25f
            }
            FlappyStatus.PAUSED -> {
                status = FlappyStatus.PLAYING
            }
            FlappyStatus.GAME_OVER -> {
                startGame()
            }
        }
    }

    fun togglePause() {
        if (status == FlappyStatus.PLAYING) {
            status = FlappyStatus.PAUSED
        } else if (status == FlappyStatus.PAUSED) {
            status = FlappyStatus.PLAYING
        }
    }

    // Called on hardware vsync frame pulse
    fun updateFrame() {
        if (status != FlappyStatus.PLAYING) return

        // 1. Update Bird physics
        birdVelocity += gravity
        val newY = birdY + birdVelocity
        val newRotation = if (birdVelocity < 0) {
            -25f
        } else {
            (-25f + (birdVelocity * 4.5f)).coerceAtMost(70f)
        }

        // 2. Move Pipes
        val movedPipes = pipes
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
        var currentScore = score
        pipesWithNew.forEach { pipe ->
            if (!pipe.hasScored && birdX > pipe.x + pipe.width) {
                pipe.hasScored = true
                currentScore++
            }
        }

        // 5. Parallax Clouds
        val newClouds = clouds.map { cloud ->
            var nx = cloud.x - cloud.speed
            if (nx + cloud.size < 0) nx = virtualWidth + 50f
            cloud.copy(x = nx)
        }

        // 6. Collision Check
        var collision = false
        val groundY = virtualHeight - 100f

        if (newY - birdRadius < 0f || newY + birdRadius >= groundY) {
            collision = true
        }

        if (!collision) {
            for (pipe in pipesWithNew) {
                if (birdX + birdRadius > pipe.x && birdX - birdRadius < pipe.x + pipe.width) {
                    if (newY - birdRadius < pipe.gapY || newY + birdRadius > pipe.gapY + pipe.gapHeight) {
                        collision = true
                        break
                    }
                }
            }
        }

        if (collision) {
            val finalHighScore = maxOf(currentScore, highScore)
            prefs.saveFlappyHighScore(finalHighScore)
            score = currentScore
            highScore = finalHighScore
            status = FlappyStatus.GAME_OVER
        } else {
            birdY = newY
            birdRotation = newRotation
            pipes = pipesWithNew
            clouds = newClouds
            if (score != currentScore) {
                score = currentScore
            }
        }
    }

    fun restartGame() {
        status = FlappyStatus.READY
        pipes = emptyList()
        clouds = generateInitialClouds()
        highScore = prefs.getFlappyHighScore()
    }
}
