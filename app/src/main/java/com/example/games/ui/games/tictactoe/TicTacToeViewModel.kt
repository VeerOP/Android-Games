package com.example.games.ui.games.tictactoe

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

enum class Player {
    X, O
}

sealed class GameResult {
    object Draw : GameResult()
    data class Winner(val player: Player, val line: List<Int>) : GameResult()
}

data class TicTacToeState(
    val board: List<Player?> = List(9) { null },
    val currentPlayer: Player = Player.X,
    val gameResult: GameResult? = null,
    val scoreX: Int = 0,
    val scoreO: Int = 0,
)

class TicTacToeViewModel : ViewModel() {
    private val _state = mutableStateOf(TicTacToeState())
    val state: State<TicTacToeState> = _state

    fun onCellClick(index: Int) {
        val currentState = _state.value
        if ((currentState.board[index] != null) || (currentState.gameResult != null)) return

        val newBoard = currentState.board.toMutableList()
        newBoard[index] = currentState.currentPlayer

        val result = checkWinner(newBoard)
        val nextPlayer = if (currentState.currentPlayer == Player.X) Player.O else Player.X

        if (result != null) {
            val newScoreX = if (result is GameResult.Winner && result.player == Player.X) currentState.scoreX + 1 else currentState.scoreX
            val newScoreO = if (result is GameResult.Winner && result.player == Player.O) currentState.scoreO + 1 else currentState.scoreO
            
            _state.value = currentState.copy(
                board = newBoard,
                gameResult = result,
                scoreX = newScoreX,
                scoreO = newScoreO
            )
        } else {
            _state.value = currentState.copy(
                board = newBoard,
                currentPlayer = nextPlayer
            )
        }
    }

    fun resetGame() {
        _state.value = _state.value.copy(
            board = List(9) { null },
            currentPlayer = Player.X,
            gameResult = null
        )
    }

    fun resetAll() {
        _state.value = TicTacToeState()
    }

    private fun checkWinner(board: List<Player?>): GameResult? {
        val winningPositions = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Columns
            listOf(0, 4, 8), listOf(2, 4, 6)                 // Diagonals
        )

        for (pos in winningPositions) {
            if (board[pos[0]] != null && board[pos[0]] == board[pos[1]] && board[pos[0]] == board[pos[2]]) {
                return GameResult.Winner(board[pos[0]]!!, pos)
            }
        }

        if (board.all { it != null }) {
            return GameResult.Draw
        }

        return null
    }
}
