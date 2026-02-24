package com.example.footballtracker.ui.matchsetup

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.footballtracker.ui.PlayerScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchSetupScreen(
    viewModel: MatchSetupViewModel
) {
    val teamA by viewModel.teamA.collectAsState()
    val teamB by viewModel.teamB.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Football Manager") })
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            item {

                PlayerScreen(
                    teamA = teamA,
                    teamB = teamB,
                    onAddPlayerToTeam = { name, team ->
                        viewModel.addPlayerToTeam(name, team)
                    },
                    onAddPlayerToDb = { name ->
                        viewModel.addPlayerToDb(name)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.saveMatchAndSendToWatch() }
                ) {
                    Text("Save Match & Send to Watch")
                }
            }
        }
    }
}
