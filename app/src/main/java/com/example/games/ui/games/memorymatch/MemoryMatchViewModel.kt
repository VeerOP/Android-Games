package com.example.games.ui.games.memorymatch

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.games.data.PreferencesManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class Difficulty(val label: String, val rows: Int, val cols: Int) {
    EASY("Easy", 3, 4),      // 12 cards = 6 pairs (fits phone beautifully)
    MEDIUM("Medium", 4, 4),  // 16 cards = 8 pairs
    HARD("Hard", 5, 4)       // 20 cards = 10 pairs
}

data class CardThemeItem(
    val icon: ImageVector,
    val color: Color
)

data class MemoryCard(
    val id: Int,
    val themeItem: CardThemeItem,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)

class MemoryMatchViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PreferencesManager.getInstance(application)

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

    var bestMoves by mutableStateOf(Int.MAX_VALUE)
        private set

    private var firstFlippedCardIndex: Int? = null
    private var isProcessing = false
    private var timerJob: Job? = null

    // Fun, vibrant icon pool with distinct colors
    private val iconPool = listOf(
        CardThemeItem(Icons.Default.RocketLaunch, Color(0xFFEF4444)),
        CardThemeItem(Icons.Default.SportsEsports, Color(0xFF8B5CF6)),
        CardThemeItem(Icons.Default.Pets, Color(0xFFF97316)),
        CardThemeItem(Icons.Default.Diamond, Color(0xFF06B6D4)),
        CardThemeItem(Icons.Default.Fastfood, Color(0xFFF59E0B)),
        CardThemeItem(Icons.Default.MusicNote, Color(0xFFEC4899)),
        CardThemeItem(Icons.Default.Star, Color(0xFFEAB308)),
        CardThemeItem(Icons.Default.Favorite, Color(0xFFE11D48)),
        CardThemeItem(Icons.Default.Lightbulb, Color(0xFF10B981)),
        CardThemeItem(Icons.Default.SportsBasketball, Color(0xFFF97316)),
        CardThemeItem(Icons.Default.Sailing, Color(0xFF3B82F6)),
        CardThemeItem(Icons.Default.ElectricBolt, Color(0xFFFACC15))
    )

    init {
        startNewGame()
    }

    fun setGameDifficulty(newDifficulty: Difficulty) {
        difficulty = newDifficulty
        startNewGame()
    }

    fun startNewGame() {
        timerJob?.cancel()
        val totalCards = difficulty.rows * difficulty.cols
        val numPairs = totalCards / 2
        val chosenItems = iconPool.shuffled().take(numPairs)
        val deck = (chosenItems + chosenItems).shuffled()

        cards = deck.mapIndexed { index, item ->
            MemoryCard(id = index, themeItem = item)
        }

        moves = 0
        timeSeconds = 0L
        isWon = false
        firstFlippedCardIndex = null
        isProcessing = false
        bestMoves = prefs.getMemoryBestMoves(difficulty.name)

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

    fun onCardClicked(index: Int): Boolean {
        if (isProcessing ||
            cards.getOrNull(index)?.isFlipped == true ||
            cards.getOrNull(index)?.isMatched == true ||
            isWon
        ) return false

        val currentList = cards.toMutableList()
        currentList[index] = currentList[index].copy(isFlipped = true)
        cards = currentList

        if (firstFlippedCardIndex == null) {
            firstFlippedCardIndex = index
            return true
        } else {
            val firstIndex = firstFlippedCardIndex!!
            moves++

            if (cards[firstIndex].themeItem.icon == cards[index].themeItem.icon) {
                // Match found!
                val matchedList = cards.toMutableList()
                matchedList[firstIndex] = matchedList[firstIndex].copy(isMatched = true)
                matchedList[index] = matchedList[index].copy(isMatched = true)
                cards = matchedList
                firstFlippedCardIndex = null
                checkWinCondition()
                return true
            } else {
                // No match: lock and flip back after 750ms
                isProcessing = true
                viewModelScope.launch {
                    delay(750)
                    val resetList = cards.toMutableList()
                    resetList[firstIndex] = resetList[firstIndex].copy(isFlipped = false)
                    resetList[index] = resetList[index].copy(isFlipped = false)
                    cards = resetList
                    firstFlippedCardIndex = null
                    isProcessing = false
                }
                return false
            }
        }
    }

    private fun checkWinCondition() {
        if (cards.isNotEmpty() && cards.all { it.isMatched }) {
            isWon = true
            timerJob?.cancel()
            prefs.saveMemoryBestScore(difficulty.name, moves)
            bestMoves = prefs.getMemoryBestMoves(difficulty.name)
        }
    }

    fun getStarRating(): Int {
        val minPossibleMoves = (difficulty.rows * difficulty.cols) / 2
        return when {
            moves <= minPossibleMoves * 1.4 -> 3
            moves <= minPossibleMoves * 2.0 -> 2
            else -> 1
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
