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

    init {
        connectIQManager.setOnMessageReceivedListener { message ->
            val data = message as? Map<*, *>
            val matchId = (data?.get("matchId") as? Number)?.toLong()
            if (matchId != null) {
                val scoreA = (data.get("scoreA") as? Number)?.toInt() ?: 0
                val scoreB = (data.get("scoreB") as? Number)?.toInt() ?: 0

                val playersWithStats = mutableListOf<Triple<String, Int, Int>>()

                // Parse teamA players
                (data.get("teamA") as? List<*>)?.forEach { playerObj ->
                    val playerMap = playerObj as? Map<*, *>
                    val name = playerMap?.get("name") as? String
                    val goals = (playerMap?.get("goals") as? Number)?.toInt() ?: 0
                    val assists = (playerMap?.get("assists") as? Number)?.toInt() ?: 0
                    if (name != null) playersWithStats.add(Triple(name, goals, assists))
                }

                // Parse teamB players
                (data.get("teamB") as? List<*>)?.forEach { playerObj ->
                    val playerMap = playerObj as? Map<*, *>
                    val name = playerMap?.get("name") as? String
                    val goals = (playerMap?.get("goals") as? Number)?.toInt() ?: 0
                    val assists = (playerMap?.get("assists") as? Number)?.toInt() ?: 0
                    if (name != null) playersWithStats.add(Triple(name, goals, assists))
                }

                updateMatch(matchId, scoreA, scoreB, playersWithStats)
            }
        }
    }

    fun addPlayersToTeam(players: List<PlayerEntity>, team: String) {
        if (team == "A") {
            _teamA.value = (_teamA.value + players).distinctBy { it.id }
        } else {
            _teamB.value = (_teamB.value + players).distinctBy { it.id }
        }
    }

    fun removePlayerFromTeam(player: PlayerEntity, team: String) {
        if (team == "A") {
            _teamA.value = _teamA.value.filter { it.id != player.id }
        } else {
            _teamB.value = _teamB.value.filter { it.id != player.id }
        }
    }

    fun addPlayerToDb(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addPlayer(PlayerEntity(name = name))
        }
    }

    private fun updateMatch(matchId: Long, scoreA: Int, scoreB: Int, playersWithStats: List<Triple<String, Int, Int>>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMatchResult(matchId, scoreA, scoreB, playersWithStats)
        }
    }

    private suspend fun createMatch(scoreA: Int = 0, scoreB: Int = 0): Long {
        val match = MatchEntity(
            teamAName = "Fekete",
            teamBName = "Fehér",
            teamAScore = scoreA,
            teamBScore = scoreB,
            timestamp = System.currentTimeMillis()
        )

        val teamAPlayerNames = _teamA.value.map { it.name }
        val teamBPlayerNames = _teamB.value.map { it.name }

        return repository.saveMatch(match, teamAPlayerNames, teamBPlayerNames)
    }

    fun saveMatchAndSendToWatch() {
        viewModelScope.launch(Dispatchers.IO) {
            // Initially save as 0-0 when starting/sending to watch
            val matchId = createMatch(0, 0)

            val teamAPlayerNames = _teamA.value.map { it.name }
            val teamBPlayerNames = _teamB.value.map { it.name }

            connectIQManager.sendTeams(
                matchId = matchId,
                teamA = teamAPlayerNames,
                teamB = teamBPlayerNames
            )
        }
    }
}
