package com.example.footballtracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.footballtracker.connectiq.ConnectIQManager
import com.example.footballtracker.ui.matchsetup.MatchSetupScreen
import com.example.footballtracker.ui.matchsetup.MatchSetupViewModel

@Composable
fun NavGraph(
    connectIQManager: ConnectIQManager
) {
    val navController = rememberNavController()
    val viewModel = remember {
        MatchSetupViewModel(connectIQManager)
    }

    NavHost(navController, startDestination = "setup") {

        composable("setup") {
            MatchSetupScreen(viewModel)
        }

        composable("history") {
            MatchListScreen()
        }
    }
}

@Composable
fun MatchListScreen() {
    TODO("Not yet implemented")
}