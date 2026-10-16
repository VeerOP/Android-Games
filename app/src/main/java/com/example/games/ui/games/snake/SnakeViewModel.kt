package com.example.games.ui.games.snake

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.games.data.PreferencesManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class SnakeGameStatus {
    READY, PLAYING, PAUSED, GAME_OVER
}

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

data class SnakeState(
    val snake: List<Pair<Int, Int>> = listOf(Pair(6, 10), Pair(5, 10), Pair(4, 10)),
    val food: Pair<Int, Int> = Pair(14, 10),
    val currentDirection: Direction = Direction.RIGHT,
    val score: Int = 0,
    val highScore: Int = 0,
    val status: SnakeGameStatus = SnakeGameStatus.READY,
    val boardSize: Int = 20,
    val moveDelay: Long = 180L
)

class SnakeViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PreferencesManager.getInstance(application)

    private val _state = MutableStateFlow(
        SnakeState(highScore = prefs.getSnakeHighScore())
    )
    val state: StateFlow<SnakeState> = _state.asStateFlow()

    private var gameJob: Job? = null
    // Input queue: buffers requested direction to prevent rapid 180° turn bug
    private var nextDirection: Direction = Direction.RIGHT

    fun startGame() {
        if (_state.value.status == SnakeGameStatus.PLAYING) return

        if (_state.value.status == SnakeGameStatus.GAME_OVER) {
            resetState()
        }

        _state.update { it.copy(status = SnakeGameStatus.PLAYING) }
        nextDirection = _state.value.currentDirection

        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            while (isActive && _state.value.status == SnakeGameStatus.PLAYING) {
                delay(_state.value.moveDelay)
                moveSnake()
            }
        }
    }

    fun togglePause() {
        if (_state.value.status == SnakeGameStatus.PLAYING) {
            gameJob?.cancel()
            _state.update { it.copy(status = SnakeGameStatus.PAUSED) }
        } else if (_state.value.status == SnakeGameStatus.PAUSED) {
            startGame()
        }
    }

    fun onDirectionChange(requestedDirection: Direction) {
        val activeDirection = _state.value.currentDirection

        // If player taps a direction while in READY or PAUSED state, start playing!
        if (_state.value.status == SnakeGameStatus.READY || _state.value.status == SnakeGameStatus.PAUSED) {
            if (!isOpposite(requestedDirection, activeDirection)) {
                nextDirection = requestedDirection
                _state.update { it.copy(currentDirection = requestedDirection) }
            }
            startGame()
            return
        }

        // Prevent 180-degree immediate reversal into snake's own body
        if (!isOpposite(requestedDirection, activeDirection)) {
            nextDirection = requestedDirection
        }
    }

    private fun isOpposite(dir1: Direction, dir2: Direction): Boolean {
        return (dir1 == Direction.UP && dir2 == Direction.DOWN) ||
               (dir1 == Direction.DOWN && dir2 == Direction.UP) ||
               (dir1 == Direction.LEFT && dir2 == Direction.RIGHT) ||
               (dir1 == Direction.RIGHT && dir2 == Direction.LEFT)
    }

    private fun moveSnake() {
        val currentState = _state.value
        if (currentState.status != SnakeGameStatus.PLAYING) return

        val directionToApply = nextDirection
        val head = currentState.snake.first()
        val newHead = when (directionToApply) {
            Direction.UP -> Pair(head.first, head.second - 1)
            Direction.DOWN -> Pair(head.first, head.second + 1)
            Direction.LEFT -> Pair(head.first - 1, head.second)
            Direction.RIGHT -> Pair(head.first + 1, head.second)
        }

        // Check wall collision
        if (newHead.first < 0 || newHead.first >= currentState.boardSize ||
            newHead.second < 0 || newHead.second >= currentState.boardSize
        ) {
            handleGameOver(currentState)
            return
        }

        // Check self collision (ignoring tail tip if not eating)
        val bodyToCheck = if (newHead == currentState.food) currentState.snake else currentState.snake.dropLast(1)
        if (bodyToCheck.contains(newHead)) {
            handleGameOver(currentState)
            return
        }

        val newSnake = mutableListOf(newHead).apply {
            addAll(currentState.snake)
        }

        if (newHead == currentState.food) {
            val newScore = currentState.score + 10
            val newHighScore = maxOf(newScore, currentState.highScore)
            prefs.saveSnakeHighScore(newHighScore)
            // Progressively speed up, minimum 70ms
            val newDelay = maxOf(70L, currentState.moveDelay - 3)

            _state.update {
                it.copy(
                    snake = newSnake,
                    food = generateFood(newSnake, currentState.boardSize),
                    currentDirection = directionToApply,
                    score = newScore,
                    highScore = newHighScore,
                    moveDelay = newDelay
                )
            }
        } else {
            newSnake.removeAt(newSnake.size - 1)
            _state.update {
                it.copy(
                    snake = newSnake,
                    currentDirection = directionToApply
                )
            }
        }
    }

    private fun handleGameOver(currentState: SnakeState) {
        gameJob?.cancel()
        val finalHighScore = maxOf(currentState.score, currentState.highScore)
        prefs.saveSnakeHighScore(finalHighScore)
        _state.update {
            it.copy(
                status = SnakeGameStatus.GAME_OVER,
                highScore = finalHighScore
            )
        }
    }

    private fun generateFood(snake: List<Pair<Int, Int>>, boardSize: Int): Pair<Int, Int> {
        val totalCells = boardSize * boardSize
        if (snake.size >= totalCells) return Pair(-1, -1)

        val snakeSet = snake.toSet()
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (x in 0 until boardSize) {
            for (y in 0 until boardSize) {
                val point = Pair(x, y)
                if (!snakeSet.contains(point)) {
                    emptyCells.add(point)
                }
            }
        }
        return if (emptyCells.isNotEmpty()) {
            emptyCells[Random.nextInt(emptyCells.size)]
        } else {
            Pair(0, 0)
        }
    }

    fun restartGame() {
        gameJob?.cancel()
        resetState()
        startGame()
    }

    private fun resetState() {
        nextDirection = Direction.RIGHT
        _state.update {
            SnakeState(
                highScore = prefs.getSnakeHighScore(),
                status = SnakeGameStatus.READY
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameJob?.cancel()
    }
}
