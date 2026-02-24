package com.example.footballtracker.ui.matchsetup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.footballtracker.data.local.entity.PlayerEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchSetupScreen(
    viewModel: MatchSetupViewModel
) {
    val teamA by viewModel.teamA.collectAsState()
    val teamB by viewModel.teamB.collectAsState()
    val allPlayers by viewModel.allPlayers.collectAsState()

    val availablePlayers = remember(allPlayers, teamA, teamB) {
        allPlayers.filter { player ->
            teamA.none { it.id == player.id } && teamB.none { it.id == player.id }
        }
    }

    var selectedTeam by remember { mutableStateOf("A") }
    var selectedPlayers by remember { mutableStateOf<List<PlayerEntity>>(emptyList()) }
    var expanded by remember { mutableStateOf(false) }
    var newPlayerName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Match Setup") })
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            item {
                Text("Create New Player", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newPlayerName,
                        onValueChange = { newPlayerName = it },
                        label = { Text("Player Name") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (newPlayerName.isNotBlank()) {
                            viewModel.addPlayerToDb(newPlayerName)
                            newPlayerName = ""
                        }
                    }) {
                        Text("Add")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(24.dp))

                // Multi-select Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedPlayers.joinToString { it.name },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Players") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        availablePlayers.forEach { player ->
                            DropdownMenuItem(
                                text = { Text(player.name) },
                                onClick = {
                                    selectedPlayers = if (selectedPlayers.contains(player)) {
                                        selectedPlayers - player
                                    } else {
                                        selectedPlayers + player
                                    }
                                },
                                leadingIcon = {
                                    Checkbox(
                                        checked = selectedPlayers.contains(player),
                                        onCheckedChange = null
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Select Team", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedTeam == "A",
                        onClick = { selectedTeam = "A" }
                    )
                    Text("Fekete (A)")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = selectedTeam == "B",
                        onClick = { selectedTeam = "B" }
                    )
                    Text("Fehér (B)")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (selectedPlayers.isNotEmpty()) {
                            viewModel.addPlayersToTeam(selectedPlayers, selectedTeam)
                            selectedPlayers = emptyList()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Selected to Team")
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(24.dp))

                Text("Fekete (Team A)", style = MaterialTheme.typography.titleMedium)
                teamA.forEach { player ->
                    Text(player.name, modifier = Modifier.padding(4.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Fehér (Team B)", style = MaterialTheme.typography.titleMedium)
                teamB.forEach { player ->
                    Text(player.name, modifier = Modifier.padding(4.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.saveMatchAndSendToWatch() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Match & Send to Watch")
                }
            }
        }
    }
}
