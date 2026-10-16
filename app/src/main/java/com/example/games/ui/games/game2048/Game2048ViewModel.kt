package com.example.games.ui.games.game2048

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.games.data.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

data class Game2048State(
    val grid: List<List<Int>> = List(4) { List(4) { 0 } },
    val score: Int = 0,
    val highScore: Int = 0,
    val isGameOver: Boolean = false,
    val isWin: Boolean = false,
    val hasCelebratedWin: Boolean = false,
    val canUndo: Boolean = false
)

enum class Direction { UP, DOWN, LEFT, RIGHT }

class Game2048ViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PreferencesManager.getInstance(application)

    private val _uiState = MutableStateFlow(
        Game2048State(highScore = prefs.get2048HighScore())
    )
    val uiState: StateFlow<Game2048State> = _uiState.asStateFlow()

    // Undo stack keeping up to 5 previous snapshots
    private val historyStack = ArrayDeque<Game2048State>()

    init {
        resetGame()
    }

    fun resetGame() {
        val initialGrid = MutableList(4) { MutableList(4) { 0 } }
        addRandomTile(initialGrid)
        addRandomTile(initialGrid)
        historyStack.clear()

        _uiState.value = Game2048State(
            grid = initialGrid.map { it.toList() },
            score = 0,
            highScore = prefs.get2048HighScore(),
            isGameOver = false,
            isWin = false,
            hasCelebratedWin = false,
            canUndo = false
        )
    }

    private fun addRandomTile(grid: MutableList<MutableList<Int>>) {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0..3) {
            for (c in 0..3) {
                if (grid[r][c] == 0) emptyCells.add(r to c)
            }
        }
        if (emptyCells.isNotEmpty()) {
            val (r, c) = emptyCells[Random.nextInt(emptyCells.size)]
            grid[r][c] = if (Random.nextFloat() < 0.9f) 2 else 4
        }
    }

    fun move(direction: Direction): Boolean {
        val currentState = _uiState.value
        if (currentState.isGameOver) return false

        val currentGrid = currentState.grid.map { it.toMutableList() }.toMutableList()
        val (newGrid, moveScore) = performMove(currentGrid, direction)

        if (newGrid != currentState.grid) {
            // Push to history
            if (historyStack.size >= 5) historyStack.removeFirst()
            historyStack.addLast(currentState)

            val gridWithNewTile = newGrid.map { it.toMutableList() }.toMutableList()
            addRandomTile(gridWithNewTile)

            val finalGrid = gridWithNewTile.map { it.toList() }
            val newScore = currentState.score + moveScore
            val newHighScore = maxOf(newScore, currentState.highScore)
            prefs.save2048HighScore(newHighScore)

            val reached2048 = finalGrid.any { row -> row.any { it == 2048 } }
            val shouldCelebrateWin = reached2048 && !currentState.hasCelebratedWin
            val gameOver = !canMove(finalGrid)

            _uiState.update {
                it.copy(
                    grid = finalGrid,
                    score = newScore,
                    highScore = newHighScore,
                    isWin = shouldCelebrateWin,
                    hasCelebratedWin = it.hasCelebratedWin || reached2048,
                    isGameOver = gameOver,
                    canUndo = historyStack.isNotEmpty()
                )
            }
            return true
        }
        return false
    }

    fun dismissWinBanner() {
        _uiState.update { it.copy(isWin = false) }
    }

    fun undo() {
        if (historyStack.isNotEmpty()) {
            val previous = historyStack.removeLast()
            _uiState.value = previous.copy(
                canUndo = historyStack.isNotEmpty(),
                isWin = false
            )
        }
    }

    private fun performMove(grid: List<List<Int>>, direction: Direction): Pair<List<List<Int>>, Int> {
        var scoreGain = 0
        val newGrid = MutableList(4) { MutableList(4) { 0 } }

        when (direction) {
            Direction.LEFT -> {
                for (i in 0..3) {
                    val (newRow, gain) = slideAndMerge(grid[i])
                    scoreGain += gain
                    for (j in 0..3) newGrid[i][j] = newRow[j]
                }
            }
            Direction.RIGHT -> {
                for (i in 0..3) {
                    val (newRow, gain) = slideAndMerge(grid[i].reversed())
                    scoreGain += gain
                    val finalRow = newRow.reversed()
                    for (j in 0..3) newGrid[i][j] = finalRow[j]
                }
            }
            Direction.UP -> {
                for (j in 0..3) {
                    val col = (0..3).map { grid[it][j] }
                    val (newCol, gain) = slideAndMerge(col)
                    scoreGain += gain
                    for (i in 0..3) newGrid[i][j] = newCol[i]
                }
            }
            Direction.DOWN -> {
                for (j in 0..3) {
                    val col = (0..3).map { grid[it][j] }.reversed()
                    val (newCol, gain) = slideAndMerge(col)
                    scoreGain += gain
                    val finalCol = newCol.reversed()
                    for (i in 0..3) newGrid[i][j] = finalCol[i]
                }
            }
        }

        return Pair(newGrid.map { it.toList() }, scoreGain)
    }

    private fun slideAndMerge(row: List<Int>): Pair<List<Int>, Int> {
        val nonZero = row.filter { it != 0 }.toMutableList()
        var score = 0
        val result = mutableListOf<Int>()
        var i = 0
        while (i < nonZero.size) {
            if (i + 1 < nonZero.size && nonZero[i] == nonZero[i + 1]) {
                val newValue = nonZero[i] * 2
                result.add(newValue)
                score += newValue
                i += 2
            } else {
                result.add(nonZero[i])
                i++
            }
        }
        while (result.size < 4) {
            result.add(0)
        }
        return Pair(result, score)
    }

    private fun canMove(grid: List<List<Int>>): Boolean {
        for (r in 0..3) {
            for (c in 0..3) {
                if (grid[r][c] == 0) return true
                if (c < 3 && grid[r][c] == grid[r][c + 1]) return true
                if (r < 3 && grid[r][c] == grid[r + 1][c]) return true
            }
        }
        return false
    }
}
