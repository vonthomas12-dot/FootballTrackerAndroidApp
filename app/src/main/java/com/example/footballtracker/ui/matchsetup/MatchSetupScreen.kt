package com.example.footballtracker.ui.matchsetup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
 import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.footballtracker.data.local.entity.PlayerEntity
import com.example.footballtracker.data.remote.EventPlayerDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchSetupScreen(
    viewModel: MatchSetupViewModel
) {
    val teamA by viewModel.teamA.collectAsState()
    val teamB by viewModel.teamB.collectAsState()
    val allPlayers by viewModel.allPlayers.collectAsState()
    val isFetching by viewModel.isFetching.collectAsState()
    val selectedDateMillis by viewModel.selectedDateMillis.collectAsState()
    val eventPlayers by viewModel.eventPlayers.collectAsState()
    val fetchError by viewModel.fetchError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(fetchError) {
        if (fetchError != null) {
            snackbarHostState.showSnackbar("Fetch failed: $fetchError")
        }
    }

    val availablePlayers = remember(allPlayers, teamA, teamB) {
        allPlayers.filter { player ->
            teamA.none { it.id == player.id } && teamB.none { it.id == player.id }
        }
    }

    var selectedPlayers by remember { mutableStateOf<List<PlayerEntity>>(emptyList()) }
    var playerPickerExpanded by remember { mutableStateOf(false) }

    // Date picker dialog state
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateMillis
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.setSelectedDate(millis)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Event players dropdown state
    var eventPickerExpanded by remember { mutableStateOf(false) }
    var selectedEventPlayers by remember(eventPlayers) { mutableStateOf<List<EventPlayerDto>>(emptyList()) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Match Setup") })
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.saveMatchAndSendToWatch() },
                        enabled = teamA.isNotEmpty() && teamB.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.SportsSoccer, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save & Send to Watch", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {

            // --- Event fetch section ---
            item {
                Text(
                    "Fetch Event Players",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Date picker row
                OutlinedTextField(
                    value = MatchSetupViewModel.formatDate(selectedDateMillis),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Event Date") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Pick date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { viewModel.fetchEvent() },
                    enabled = !isFetching,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isFetching) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.CloudDownload, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fetch Players from Server")
                }

                // Event players dropdown — only shown after a successful fetch
                if (eventPlayers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    ExposedDropdownMenuBox(
                        expanded = eventPickerExpanded,
                        onExpandedChange = { eventPickerExpanded = !eventPickerExpanded }
                    ) {
                        OutlinedTextField(
                            value = if (selectedEventPlayers.isEmpty()) ""
                                    else selectedEventPlayers.joinToString { it.name },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Event Players (${eventPlayers.size})") },
                            placeholder = { Text("Select from event…") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = eventPickerExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = eventPickerExpanded,
                            onDismissRequest = { eventPickerExpanded = false }
                        ) {
                            eventPlayers.forEach { ep ->
                                val teamLabel = when (ep.team) {
                                    "A" -> " (Fekete)"
                                    "B" -> " (Fehér)"
                                    else -> ""
                                }
                                DropdownMenuItem(
                                    text = { Text(ep.name + teamLabel) },
                                    onClick = {
                                        selectedEventPlayers =
                                            if (selectedEventPlayers.any { it.name == ep.name })
                                                selectedEventPlayers.filter { it.name != ep.name }
                                            else
                                                selectedEventPlayers + ep
                                    },
                                    leadingIcon = {
                                        Checkbox(
                                            checked = selectedEventPlayers.any { it.name == ep.name },
                                            onCheckedChange = null
                                        )
                                    }
                                )
                            }
                        }
                    }

                    if (selectedEventPlayers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val toAdd = selectedEventPlayers.map {
                                        PlayerEntity(name = it.name)
                                    }
                                    viewModel.addEventPlayersToTeam(toAdd, "A")
                                    selectedEventPlayers = emptyList()
                                    eventPickerExpanded = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) { Text("→ Fekete", maxLines = 1) }
                            Button(
                                onClick = {
                                    val toAdd = selectedEventPlayers.map {
                                        PlayerEntity(name = it.name)
                                    }
                                    viewModel.addEventPlayersToTeam(toAdd, "B")
                                    selectedEventPlayers = emptyList()
                                    eventPickerExpanded = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                )
                            ) { Text("→ Fehér", maxLines = 1) }
                        }
                    }
                }
            }

            // --- Player picker (local DB) ---
            item {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Add Players to a Team",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = playerPickerExpanded,
                    onExpandedChange = { playerPickerExpanded = !playerPickerExpanded }
                ) {
                    OutlinedTextField(
                        value = if (selectedPlayers.isEmpty()) ""
                                else selectedPlayers.joinToString { it.name },
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Select players…") },
                        label = { Text("Players") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = playerPickerExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = playerPickerExpanded,
                        onDismissRequest = { playerPickerExpanded = false }
                    ) {
                        if (availablePlayers.isEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "All players are already assigned",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = {},
                                enabled = false
                            )
                        } else {
                            availablePlayers.forEach { player ->
                                DropdownMenuItem(
                                    text = { Text(player.name) },
                                    onClick = {
                                        selectedPlayers = if (selectedPlayers.contains(player))
                                            selectedPlayers - player
                                        else
                                            selectedPlayers + player
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
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (selectedPlayers.isNotEmpty()) {
                                viewModel.addPlayersToTeam(selectedPlayers, "A")
                                selectedPlayers = emptyList()
                                playerPickerExpanded = false
                            }
                        },
                        enabled = selectedPlayers.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("→ Fekete", maxLines = 1)
                    }
                    Button(
                        onClick = {
                            if (selectedPlayers.isNotEmpty()) {
                                viewModel.addPlayersToTeam(selectedPlayers, "B")
                                selectedPlayers = emptyList()
                                playerPickerExpanded = false
                            }
                        },
                        enabled = selectedPlayers.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text("→ Fehér", maxLines = 1)
                    }
                }
            }

            // --- Teams side by side ---
            item {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TeamColumn(
                        title = "Fekete",
                        players = teamA,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        onContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        onRemove = { viewModel.removePlayerFromTeam(it, "A") },
                        modifier = Modifier.weight(1f)
                    )
                    TeamColumn(
                        title = "Fehér",
                        players = teamB,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        onContentColor = Color.White,
                        onRemove = { viewModel.removePlayerFromTeam(it, "B") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamColumn(
    title: String,
    players: List<PlayerEntity>,
    containerColor: androidx.compose.ui.graphics.Color,
    onContentColor: androidx.compose.ui.graphics.Color,
    onRemove: (PlayerEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // Header with player count badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = onContentColor,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = onContentColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${players.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = onContentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (players.isEmpty()) {
                Text(
                    text = "No players yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = onContentColor.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )
            } else {
                players.forEach { player ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Text(
                            text = player.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = onContentColor,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onRemove(player) },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove ${player.name}",
                                tint = onContentColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
