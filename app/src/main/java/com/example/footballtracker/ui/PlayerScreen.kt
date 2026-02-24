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
    onAddPlayerToTeam: (String, String) -> Unit,
    onAddPlayerToDb: (String) -> Unit
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

        Text("Select Team for current match")

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
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = {
                    if (playerName.isNotBlank()) {
                        onAddPlayerToTeam(playerName, selectedTeam)
                        playerName = ""
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Add to Match")
            }
            
            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (playerName.isNotBlank()) {
                        onAddPlayerToDb(playerName)
                        playerName = ""
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Add Player")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Fekete (Team A)", style = MaterialTheme.typography.titleMedium)
        teamA.forEach {
            Text(it, modifier = Modifier.padding(4.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Fehér (Team B)", style = MaterialTheme.typography.titleMedium)
        teamB.forEach {
            Text(it, modifier = Modifier.padding(4.dp))
        }
    }
}
