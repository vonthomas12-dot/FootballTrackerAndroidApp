package com.example.footballtracker.ui.players

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PlayersScreen(
    viewModel: PlayersViewModel
) {
    val players by viewModel.players.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(players) { player ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = player.name, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "Team: ${if (player.team == "A") "Fekete" else "Fehér"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(text = "Goals: ${player.goals}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}