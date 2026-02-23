package com.example.footballtracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun PlayerScreen(
    teamA: List<String>,
    teamB: List<String>,
    onAddPlayer: (String, String) -> Unit
) {
    var playerName by remember { mutableStateOf("") }
    var selectedTeam by remember { mutableStateOf("A") }

    Column {

        OutlinedTextField(
            value = playerName,
            onValueChange = { playerName = it },
            label = { Text("Player Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Select Team")

        Row(verticalAlignment = Alignment.CenterVertically) {

            RadioButton(
                selected = selectedTeam == "A",
                onClick = { selectedTeam = "A" }
            )
            Text("Fekete")

            Spacer(modifier = Modifier.width(16.dp))

            RadioButton(
                selected = selectedTeam == "B",
                onClick = { selectedTeam = "B" }
            )
            Text("Fehér")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (playerName.isNotBlank()) {
                    onAddPlayer(playerName, selectedTeam)
                    playerName = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Player")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Fekete", style = MaterialTheme.typography.titleMedium)
        teamA.forEach {
            Text(it, modifier = Modifier.padding(4.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Fehér", style = MaterialTheme.typography.titleMedium)
        teamB.forEach {
            Text(it, modifier = Modifier.padding(4.dp))
        }
    }
}