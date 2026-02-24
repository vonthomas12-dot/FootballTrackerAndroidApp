package com.example.footballtracker.ui.matchlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.footballtracker.data.local.entity.MatchEntity
import com.example.footballtracker.data.local.entity.MatchWithPlayersAndTeam
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MatchListScreen(
    viewModel: MatchListViewModel,
    onMatchClick: (Long) -> Unit = {}
) {
    // Collect Flow from ViewModel
    val matches by viewModel.matches.collectAsState()

    var matchToDelete by remember { mutableStateOf<MatchEntity?>(null) }

    if (matchToDelete != null) {
        AlertDialog(
            onDismissRequest = { matchToDelete = null },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this match? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        matchToDelete?.let { viewModel.deleteMatch(it) }
                        matchToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { matchToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    val dateFormatter = remember { SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(matches) { matchWithPlayers ->
            val match = matchWithPlayers.match
            val formattedDate = dateFormatter.format(Date(match.timestamp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .wrapContentHeight()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(end = 40.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${match.teamAName} vs ${match.teamBName}",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(text = "Score: ${match.teamAScore} - ${match.teamBScore}")
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val teamAPlayers = matchWithPlayers.matchPlayers.filter { it.matchPlayer.team == "A" }
                        val teamBPlayers = matchWithPlayers.matchPlayers.filter { it.matchPlayer.team == "B" }

                        Text(text = "Fekete (A):", style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = teamAPlayers.joinToString { it.player.name },
                            style = MaterialTheme.typography.bodySmall
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(text = "Fehér (B):", style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = teamBPlayers.joinToString { it.player.name },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    IconButton(
                        onClick = { matchToDelete = match },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Match",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
