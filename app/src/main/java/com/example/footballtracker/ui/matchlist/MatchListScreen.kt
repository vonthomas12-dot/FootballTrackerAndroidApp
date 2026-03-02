package com.example.footballtracker.ui.matchlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.footballtracker.data.local.entity.MatchEntity
import com.example.footballtracker.data.local.entity.MatchWithPlayersAndTeam
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchListScreen(
    viewModel: MatchListViewModel,
    onMatchClick: (Long) -> Unit = {}
) {
    val matches by viewModel.matches.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uploadResult.collect { result ->
            if (result.isSuccess) {
                snackbarHostState.showSnackbar("Match uploaded successfully!")
            } else {
                snackbarHostState.showSnackbar("Upload failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    var matchToDelete by remember { mutableStateOf<MatchEntity?>(null) }

    if (matchToDelete != null) {
        AlertDialog(
            onDismissRequest = { matchToDelete = null },
            title = { Text("Delete match?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        matchToDelete?.let { viewModel.deleteMatch(it) }
                        matchToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(onClick = { matchToDelete = null }) { Text("Cancel") }
            }
        )
    }

    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Matches", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (matches.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No matches yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(matches) { matchWithPlayers ->
                    MatchCard(
                        matchWithPlayers = matchWithPlayers,
                        dateFormatter = dateFormatter,
                        onDelete = { matchToDelete = matchWithPlayers.match },
                        onUpload = { viewModel.uploadMatch(matchWithPlayers) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchCard(
    matchWithPlayers: MatchWithPlayersAndTeam,
    dateFormatter: SimpleDateFormat,
    onDelete: () -> Unit,
    onUpload: () -> Unit
) {
    val match = matchWithPlayers.match
    val teamAPlayers = matchWithPlayers.matchPlayers.filter { it.matchPlayer.team == "A" }
        .sortedByDescending { it.matchPlayer.goals }
    val teamBPlayers = matchWithPlayers.matchPlayers.filter { it.matchPlayer.team == "B" }
        .sortedByDescending { it.matchPlayer.goals }
    val formattedDate = dateFormatter.format(Date(match.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Date + actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                // Upload button
                IconButton(
                    onClick = onUpload,
                    enabled = !match.isUploaded,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (match.isUploaded) Icons.Default.CloudDone else Icons.Default.Upload,
                        contentDescription = if (match.isUploaded) "Uploaded" else "Upload",
                        tint = if (match.isUploaded) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Score row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = match.teamAName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${match.teamAScore}  –  ${match.teamBScore}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = match.teamBName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // Players side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    teamAPlayers.forEach { mp ->
                        PlayerChip(name = mp.player.name, goals = mp.matchPlayer.goals)
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    teamBPlayers.forEach { mp ->
                        PlayerChip(name = mp.player.name, goals = mp.matchPlayer.goals)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerChip(name: String, goals: Int) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f)
            )
            if (goals > 0) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "⚽ $goals",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
