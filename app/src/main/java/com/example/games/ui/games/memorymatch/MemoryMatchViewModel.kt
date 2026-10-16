package com.example.games.ui.games.memorymatch

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

enum class Difficulty(val rows: Int, val cols: Int) {
    EASY(4, 4),
    HARD(6, 6)
}

data class MemoryCard(
    val id: Int,
    val content: ImageVector,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)

class MemoryMatchViewModel : ViewModel() {
    var cards by mutableStateOf<List<MemoryCard>>(emptyList())
        private set

    var moves by mutableStateOf(0)
        private set

    var timeSeconds by mutableStateOf(0L)
        private set

    var difficulty by mutableStateOf(Difficulty.EASY)
        private set

    var isWon by mutableStateOf(false)
        private set

    private var firstFlippedCardIndex: Int? = null
    private var isProcessing = false
    private var timerJob: Job? = null

    private val availableIcons = listOf(
        Icons.Default.Favorite, Icons.Default.Star, Icons.Default.Home, Icons.Default.Settings,
        Icons.Default.Call, Icons.Default.Email, Icons.Default.Face, Icons.Default.Build,
        Icons.Default.Check, Icons.Default.Close, Icons.Default.Add, Icons.Default.ThumbUp,
        Icons.Default.Warning, Icons.Default.Info, Icons.Default.Lock, Icons.Default.Person,
        Icons.Default.ShoppingCart, Icons.Default.Search
    )

    init {
        startNewGame()
    }

    fun setGameDifficulty(newDifficulty: Difficulty) {
        difficulty = newDifficulty
        startNewGame()
    }

    fun startNewGame() {
        val totalCards = difficulty.rows * difficulty.cols
        val numPairs = totalCards / 2
        val icons = availableIcons.shuffled().take(numPairs)
        val cardContents = (icons + icons).shuffled()

        cards = cardContents.mapIndexed { index, icon ->
            MemoryCard(id = index, content = icon)
        }

        moves = 0
        timeSeconds = 0L
        isWon = false
        firstFlippedCardIndex = null
        isProcessing = false
        
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && !isWon) {
                delay(1000L)
                timeSeconds++
            }
        }
    }

    fun onCardClicked(index: Int) {
        if (isProcessing || cards[index].isFlipped || cards[index].isMatched || isWon) return

        val newCards = cards.toMutableList()
        newCards[index] = newCards[index].copy(isFlipped = true)
        cards = newCards

        if (firstFlippedCardIndex == null) {
            firstFlippedCardIndex = index
        } else {
            val firstIndex = firstFlippedCardIndex!!
            moves++
            
            if (cards[firstIndex].content == cards[index].content) {
                // Match found
                val updatedCards = cards.toMutableList()
                updatedCards[firstIndex] = updatedCards[firstIndex].copy(isMatched = true)
                updatedCards[index] = updatedCards[index].copy(isMatched = true)
                cards = updatedCards
                
                firstFlippedCardIndex = null
                checkWinCondition()
            } else {
                // No match
                isProcessing = true
                viewModelScope.launch {
                    delay(1000)
                    val updatedCards = cards.toMutableList()
                    updatedCards[firstIndex] = updatedCards[firstIndex].copy(isFlipped = false)
                    updatedCards[index] = updatedCards[index].copy(isFlipped = false)
                    cards = updatedCards
                    
                    firstFlippedCardIndex = null
                    isProcessing = false
                }
            }
        }
    }

    private fun checkWinCondition() {
        if (cards.all { it.isMatched }) {
            isWon = true
            timerJob?.cancel()
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
    }
}
