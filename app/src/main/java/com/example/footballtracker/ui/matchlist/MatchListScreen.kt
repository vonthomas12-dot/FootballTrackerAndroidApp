package com.example.footballtracker.ui.matchlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.footballtracker.data.local.entity.MatchWithPlayers

@Composable
fun MatchListScreen(
    viewModel: MatchListViewModel,
    onMatchClick: (Long) -> Unit = {}
) {
    // Collect Flow from ViewModel
    val matches by viewModel.matches.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(matches) { matchWithPlayers ->
            val match = matchWithPlayers.match

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .wrapContentHeight()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "${match.teamAName} vs ${match.teamBName}")
                    Text(text = "Score: ${match.teamAScore} - ${match.teamBScore}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Players: " +
                                matchWithPlayers.players.joinToString { it.name }
                    )
                }
            }
        }
    }
}