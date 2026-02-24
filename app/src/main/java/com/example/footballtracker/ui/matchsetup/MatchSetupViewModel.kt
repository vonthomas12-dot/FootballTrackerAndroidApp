package com.example.footballtracker.ui.matchsetup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footballtracker.connectiq.ConnectIQManager
import com.example.footballtracker.data.local.entity.MatchEntity
import com.example.footballtracker.data.local.entity.PlayerEntity
import com.example.footballtracker.data.repository.MatchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MatchSetupViewModel(
    private val connectIQManager: ConnectIQManager,
    private val repository: MatchRepository
) : ViewModel() {

    private val _teamA = MutableStateFlow<List<PlayerEntity>>(emptyList())
    val teamA: StateFlow<List<PlayerEntity>> = _teamA.asStateFlow()

    private val _teamB = MutableStateFlow<List<PlayerEntity>>(emptyList())
    val teamB: StateFlow<List<PlayerEntity>> = _teamB.asStateFlow()

    val allPlayers: StateFlow<List<PlayerEntity>> = repository.getAllPlayers()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        connectIQManager.setOnMessageReceivedListener { message ->
            // Parse message data if it's a Map (Dictionary from Monkey C)
            val data = message as? Map<*, *>

            val scoreA = (data?.get("scoreA") as? Number)?.toInt() ?: 0
            val scoreB = (data?.get("scoreB") as? Number)?.toInt() ?: 0
            val teamA = (data?.get("teamA") as? StateFlow<*>)
            val teamB = (data?.get("teamB") as? StateFlow<*>)

            saveMatch(scoreA, scoreB)
        }
    }

    fun addPlayersToTeam(players: List<PlayerEntity>, team: String) {
        if (team == "A") {
            _teamA.value = (_teamA.value + players).distinctBy { it.id }
        } else {
            _teamB.value = (_teamB.value + players).distinctBy { it.id }
        }
    }

    fun addPlayerToDb(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addPlayer(PlayerEntity(name = name))
        }
    }

    private fun saveMatch(scoreA: Int = 0, scoreB: Int = 0) {
        viewModelScope.launch(Dispatchers.IO) {
            val match = MatchEntity(
                teamAName = "Fekete",
                teamBName = "Fehér",
                teamAScore = scoreA,
                teamBScore = scoreB,
                timestamp = System.currentTimeMillis()
            )

            val teamAPlayerNames = _teamA.value.map { it.name }
            val teamBPlayerNames = _teamB.value.map { it.name }

            repository.saveMatch(match, teamAPlayerNames, teamBPlayerNames)
        }
    }

    fun saveMatchAndSendToWatch() {
        viewModelScope.launch(Dispatchers.IO) {
            // Initially save as 0-0 when starting/sending to watch
            saveMatch(0, 0)

            val teamAPlayerNames = _teamA.value.map { it.name }
            val teamBPlayerNames = _teamB.value.map { it.name }

            connectIQManager.sendTeams(
                teamA = teamAPlayerNames,
                teamB = teamBPlayerNames
            )
        }
    }
}
