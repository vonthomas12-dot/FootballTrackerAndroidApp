package com.example.footballtracker.ui.matchsetup

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

    fun removePlayer(player: PlayerEntity, team: String) {
        if (team == "A") {
            _teamA.value = _teamA.value - player
        } else {
            _teamB.value = _teamB.value - player
        }
    }

    fun saveMatchAndSendToWatch() {
        viewModelScope.launch(Dispatchers.IO) {
            val match = MatchEntity(
                teamAName = "Fekete",
                teamBName = "Fehér",
                teamAScore = 0,
                teamBScore = 0,
                timestamp = System.currentTimeMillis()
            )

            val teamAPlayerNames = _teamA.value.map { it.name }
            val teamBPlayerNames = _teamB.value.map { it.name }

            repository.saveMatch(match, teamAPlayerNames, teamBPlayerNames)

            connectIQManager.sendTeams(
                teamA = teamAPlayerNames,
                teamB = teamBPlayerNames
            )
        }
    }
}
