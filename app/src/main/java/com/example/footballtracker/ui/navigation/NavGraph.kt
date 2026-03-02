package com.example.footballtracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.footballtracker.connectiq.ConnectIQManager
import com.example.footballtracker.data.repository.MatchRepository
import com.example.footballtracker.ui.matchlist.MatchListScreen
import com.example.footballtracker.ui.matchlist.MatchListViewModel
import com.example.footballtracker.ui.matchsetup.MatchSetupScreen
import com.example.footballtracker.ui.matchsetup.MatchSetupViewModel
import com.example.footballtracker.ui.players.PlayersScreen
import com.example.footballtracker.ui.players.PlayersViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object MatchSetup : Screen("setup", "Match Setup", Icons.Default.Add)
    object MatchList : Screen("matchList", "Match List", Icons.AutoMirrored.Filled.List)
    object Players : Screen("players", "Players", Icons.Default.Person)
}

val items = listOf(
    Screen.MatchSetup,
    Screen.MatchList,
    Screen.Players
)

@Composable
fun NavGraph(
    connectIQManager: ConnectIQManager,
    repository: MatchRepository
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = "setup", Modifier.padding(innerPadding)) {

            composable("setup") {
                val viewModel = remember {
                    MatchSetupViewModel(connectIQManager, repository)
                }
                MatchSetupScreen(viewModel)
            }

            composable("matchList") {
                val viewModel = remember { MatchListViewModel(repository) }
                MatchListScreen(viewModel = viewModel)
            }

            composable("players") {
                val viewModel = remember { PlayersViewModel(repository) }
                PlayersScreen(viewModel = viewModel)
            }
        }
    }
}