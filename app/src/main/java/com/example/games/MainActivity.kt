package com.example.games

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.games.ui.games.game2048.Game2048Screen
import com.example.games.ui.games.flappybird.FlappyBirdScreen
import com.example.games.ui.games.memorymatch.MemoryMatchScreen
import com.example.games.ui.games.snake.SnakeScreen
import com.example.games.ui.games.tictactoe.TicTacToeScreen
import com.example.games.ui.home.HomeScreen
import com.example.games.ui.theme.GamesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GamesTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = Screen.Home.route) {
                    composable(Screen.Home.route) {
                        HomeScreen { screen ->
                            navController.navigate(screen.route)
                        }
                    }
                    composable(Screen.TicTacToe.route) { 
                        TicTacToeScreen(onBack = { navController.popBackStack() }) 
                    }
                    composable(Screen.FlappyBird.route) { 
                        FlappyBirdScreen(onBack = { navController.popBackStack() }) 
                    }
                    composable(Screen.Snake.route) { 
                        SnakeScreen(onBack = { navController.popBackStack() }) 
                    }
                    composable(Screen.MemoryMatch.route) {
                        MemoryMatchScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Screen.Puzzle2048.route) { 
                        Game2048Screen(onBack = { navController.popBackStack() }) 
                    }
                }
            }
        }
    }
}
