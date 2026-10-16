package com.example.games.ui.games.tictactoe

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class Player {
    X, O
}

enum class GameMode {
    AI, TWO_PLAYER
}

sealed class GameResult {
    object Draw : GameResult()
    data class Winner(val player: Player, val line: List<Int>) : GameResult()
}

data class TicTacToeState(
    val board: List<Player?> = List(9) { null },
    val currentPlayer: Player = Player.X,
    val gameResult: GameResult? = null,
    val mode: GameMode = GameMode.AI,
    val scoreX: Int = 0,
    val scoreO: Int = 0,
    val scoreDraws: Int = 0,
    val isAiThinking: Boolean = false
)

class TicTacToeViewModel : ViewModel() {
    private val _state = mutableStateOf(TicTacToeState())
    val state: State<TicTacToeState> = _state

    fun setMode(newMode: GameMode) {
        if (_state.value.mode != newMode) {
            _state.value = _state.value.copy(
                mode = newMode,
                board = List(9) { null },
                currentPlayer = Player.X,
                gameResult = null
            )
        }
    }

    fun onCellClick(index: Int) {
        val currentState = _state.value
        if (currentState.board[index] != null ||
            currentState.gameResult != null ||
            currentState.isAiThinking
        ) return

        makeMove(index, currentState.currentPlayer)

        // If Single Player vs AI and game is still ongoing, trigger AI move
        val stateAfterPlayerMove = _state.value
        if (stateAfterPlayerMove.mode == GameMode.AI &&
            stateAfterPlayerMove.gameResult == null &&
            stateAfterPlayerMove.currentPlayer == Player.O
        ) {
            triggerAiMove()
        }
    }

    private fun makeMove(index: Int, player: Player) {
        val currentState = _state.value
        val newBoard = currentState.board.toMutableList()
        newBoard[index] = player

        val result = checkWinner(newBoard)
        val nextPlayer = if (player == Player.X) Player.O else Player.X

        if (result != null) {
            var newScoreX = currentState.scoreX
            var newScoreO = currentState.scoreO
            var newScoreDraws = currentState.scoreDraws

            when (result) {
                is GameResult.Winner -> {
                    if (result.player == Player.X) newScoreX++ else newScoreO++
                }
                is GameResult.Draw -> newScoreDraws++
            }

            _state.value = currentState.copy(
                board = newBoard,
                gameResult = result,
                scoreX = newScoreX,
                scoreO = newScoreO,
                scoreDraws = newScoreDraws,
                isAiThinking = false
            )
        } else {
            _state.value = currentState.copy(
                board = newBoard,
                currentPlayer = nextPlayer,
                isAiThinking = false
            )
        }
    }

    private fun triggerAiMove() {
        _state.value = _state.value.copy(isAiThinking = true)
        viewModelScope.launch {
            delay(350) // Natural thinking delay
            val board = _state.value.board
            val bestMove = findBestMove(board)
            if (bestMove != -1 && _state.value.gameResult == null) {
                makeMove(bestMove, Player.O)
            }
        }
    }

    // Minimax / Smart Heuristic AI
    private fun findBestMove(board: List<Player?>): Int {
        val available = board.indices.filter { board[it] == null }
        if (available.isEmpty()) return -1

        // 1. Check if AI can win in 1 move
        for (i in available) {
            val copy = board.toMutableList()
            copy[i] = Player.O
            if (checkWinner(copy) is GameResult.Winner) return i
        }

        // 2. Block player if player can win in 1 move
        for (i in available) {
            val copy = board.toMutableList()
            copy[i] = Player.X
            if (checkWinner(copy) is GameResult.Winner) return i
        }

        // 3. Take center if available
        if (board[4] == null) return 4

        // 4. Take corners if available
        val corners = listOf(0, 2, 6, 8).filter { board[it] == null }
        if (corners.isNotEmpty()) return corners[Random.nextInt(corners.size)]

        // 5. Pick random available
        return available[Random.nextInt(available.size)]
    }

    fun resetGame() {
        _state.value = _state.value.copy(
            board = List(9) { null },
            currentPlayer = Player.X,
            gameResult = null,
            isAiThinking = false
        )
    }

    fun resetScores() {
        _state.value = _state.value.copy(
            board = List(9) { null },
            currentPlayer = Player.X,
            gameResult = null,
            scoreX = 0,
            scoreO = 0,
            scoreDraws = 0,
            isAiThinking = false
        )
    }

    private fun checkWinner(board: List<Player?>): GameResult? {
        val winningPositions = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Columns
            listOf(0, 4, 8), listOf(2, 4, 6)                   // Diagonals
        )

        for (pos in winningPositions) {
            if (board[pos[0]] != null &&
                board[pos[0]] == board[pos[1]] &&
                board[pos[0]] == board[pos[2]]
            ) {
                return GameResult.Winner(board[pos[0]]!!, pos)
            }
        }

        if (board.all { it != null }) {
            return GameResult.Draw
        }

        return null
    }
}
