package com.example.games.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("arcade_games_prefs", Context.MODE_PRIVATE)

    fun getSnakeHighScore(): Int = prefs.getInt(KEY_SNAKE_HIGH_SCORE, 0)
    fun saveSnakeHighScore(score: Int) {
        if (score > getSnakeHighScore()) {
            prefs.edit().putInt(KEY_SNAKE_HIGH_SCORE, score).apply()
        }
    }

    fun getFlappyHighScore(): Int = prefs.getInt(KEY_FLAPPY_HIGH_SCORE, 0)
    fun saveFlappyHighScore(score: Int) {
        if (score > getFlappyHighScore()) {
            prefs.edit().putInt(KEY_FLAPPY_HIGH_SCORE, score).apply()
        }
    }

    fun get2048HighScore(): Int = prefs.getInt(KEY_2048_HIGH_SCORE, 0)
    fun save2048HighScore(score: Int) {
        if (score > get2048HighScore()) {
            prefs.edit().putInt(KEY_2048_HIGH_SCORE, score).apply()
        }
    }

    fun getMemoryBestMoves(difficulty: String): Int =
        prefs.getInt("${KEY_MEMORY_BEST_MOVES}_$difficulty", Int.MAX_VALUE)

    fun saveMemoryBestScore(difficulty: String, moves: Int) {
        val currentBest = getMemoryBestMoves(difficulty)
        if (moves < currentBest) {
            prefs.edit().putInt("${KEY_MEMORY_BEST_MOVES}_$difficulty", moves).apply()
        }
    }

    companion object {
        private const val KEY_SNAKE_HIGH_SCORE = "snake_high_score"
        private const val KEY_FLAPPY_HIGH_SCORE = "flappy_high_score"
        private const val KEY_2048_HIGH_SCORE = "2048_high_score"
        private const val KEY_MEMORY_BEST_MOVES = "memory_best_moves"

        @Volatile
        private var INSTANCE: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
