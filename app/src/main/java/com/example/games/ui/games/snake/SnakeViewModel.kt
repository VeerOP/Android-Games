package com.example.games.ui.games.snake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class SnakeState(
    val snake: List<Pair<Int, Int>> = listOf(Pair(5, 5)),
    val food: Pair<Int, Int> = Pair(10, 10),
    val direction: Direction = Direction.RIGHT,
    val score: Int = 0,
    val isGameOver: Boolean = false,
    val boardSize: Int = 20,
    val moveDelay: Long = 200L
)

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

class SnakeViewModel : ViewModel() {
    private val _state = MutableStateFlow(SnakeState())
    val state: StateFlow<SnakeState> = _state.asStateFlow()

    init {
        startGame()
    }

    private fun startGame() {
        viewModelScope.launch {
            while (true) {
                if (!_state.value.isGameOver) {
                    moveSnake()
                }
                delay(_state.value.moveDelay)
            }
        }
    }

    fun onDirectionChange(newDirection: Direction) {
        val currentDirection = _state.value.direction
        // Prevent 180 degree turns
        if (newDirection == Direction.UP && currentDirection != Direction.DOWN ||
            newDirection == Direction.DOWN && currentDirection != Direction.UP ||
            newDirection == Direction.LEFT && currentDirection != Direction.RIGHT ||
            newDirection == Direction.RIGHT && currentDirection != Direction.LEFT
        ) {
            _state.update { it.copy(direction = newDirection) }
        }
    }

    private fun moveSnake() {
        _state.update { currentState ->
            val head = currentState.snake.first()
            val newHead = when (currentState.direction) {
                Direction.UP -> Pair(head.first, head.second - 1)
                Direction.DOWN -> Pair(head.first, head.second + 1)
                Direction.LEFT -> Pair(head.first - 1, head.second)
                Direction.RIGHT -> Pair(head.first + 1, head.second)
            }

            // Check collision with walls
            if (newHead.first < 0 || newHead.first >= currentState.boardSize ||
                newHead.second < 0 || newHead.second >= currentState.boardSize
            ) {
                return@update currentState.copy(isGameOver = true)
            }

            // Check collision with self
            if (currentState.snake.contains(newHead)) {
                return@update currentState.copy(isGameOver = true)
            }

            val newSnake = mutableListOf(newHead).apply {
                addAll(currentState.snake)
            }

            // Check if food is eaten
            if (newHead == currentState.food) {
                val newScore = currentState.score + 10
                val newMoveDelay = maxOf(50L, currentState.moveDelay - 2) // Increase speed
                currentState.copy(
                    snake = newSnake,
                    food = generateFood(newSnake, currentState.boardSize),
                    score = newScore,
                    moveDelay = newMoveDelay
                )
            } else {
                newSnake.removeAt(newSnake.size - 1)
                currentState.copy(snake = newSnake)
            }
        }
    }

    private fun generateFood(snake: List<Pair<Int, Int>>, boardSize: Int): Pair<Int, Int> {
        if (snake.size >= boardSize * boardSize) return Pair(-1, -1) // Game Won or no space
        var newFood: Pair<Int, Int>
        do {
            newFood = Pair(Random.nextInt(boardSize), Random.nextInt(boardSize))
        } while (snake.contains(newFood))
        return newFood
    }

    fun restartGame() {
        _state.update { SnakeState() }
    }
}
