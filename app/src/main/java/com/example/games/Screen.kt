package com.example.games

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Game Hub")
    object TicTacToe : Screen("tic_tac_toe", "Tic-Tac-Toe")
    object FlappyBird : Screen("flappy_bird", "Flappy Bird")
    object Snake : Screen("snake", "Snake")
    object MemoryMatch : Screen("memory_match", "Memory Match")
    object Puzzle2048 : Screen("puzzle_2048", "2048")
}
