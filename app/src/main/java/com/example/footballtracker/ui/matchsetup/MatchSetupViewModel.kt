package com.example.footballtracker.ui.matchsetup

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.footballtracker.FootballTrackerApplication
import com.example.footballtracker.MatchTrackerService
import com.example.footballtracker.data.local.entity.MatchEntity
import com.example.footballtracker.data.local.entity.PlayerEntity
import com.example.footballtracker.data.remote.EventDto
import com.example.footballtracker.data.remote.EventPlayerDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MatchSetupViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val app = application as FootballTrackerApplication
    private val connectIQManager = app.connectIQManager
    private val repository = app.repository

    private val _teamA = MutableStateFlow<List<PlayerEntity>>(emptyList())
    val teamA: StateFlow<List<PlayerEntity>> = _teamA.asStateFlow()

    private val _teamB = MutableStateFlow<List<PlayerEntity>>(emptyList())
    val teamB: StateFlow<List<PlayerEntity>> = _teamB.asStateFlow()

    val allPlayers: StateFlow<List<PlayerEntity>> = repository.getAllPlayers()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedDateMillis = MutableStateFlow(todayMillis())
    val selectedDateMillis: StateFlow<Long> = _selectedDateMillis.asStateFlow()

    private val _eventPlayers = MutableStateFlow<List<EventPlayerDto>>(emptyList())
    val eventPlayers: StateFlow<List<EventPlayerDto>> = _eventPlayers.asStateFlow()

    private val _fetchError = MutableStateFlow<String?>(null)
    val fetchError: StateFlow<String?> = _fetchError.asStateFlow()

    private val _isFetching = MutableStateFlow(false)
    val isFetching: StateFlow<Boolean> = _isFetching.asStateFlow()

    fun setSelectedDate(millis: Long) {
        _selectedDateMillis.value = millis
    }

    fun fetchEvent() {
        viewModelScope.launch(Dispatchers.IO) {
            _isFetching.value = true
            _fetchError.value = null
            val dateStr = formatDate(_selectedDateMillis.value)
            val result = repository.fetchEvent(dateStr)
            if (result.isSuccess) {
                val event = result.getOrDefault(EventDto())
                _eventPlayers.value = event.players
                repository.saveEventPlayers(event.players.map { it.name })
            } else {
                _fetchError.value = result.exceptionOrNull()?.message
                _eventPlayers.value = emptyList()
            }
            _isFetching.value = false
        }
    }

    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        fun todayMillis(): Long {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

        fun formatDate(millis: Long): String = dateFormat.format(millis)
    }

    fun addPlayersToTeam(players: List<PlayerEntity>, team: String) {
        if (team == "A") {
            _teamA.value = (_teamA.value + players).distinctBy { it.id }
        } else {
            _teamB.value = (_teamB.value + players).distinctBy { it.id }
        }
    }

    fun addEventPlayersToTeam(players: List<PlayerEntity>, team: String) {
        if (team == "A") {
            _teamA.value = (_teamA.value + players).distinctBy { it.name }
        } else {
            _teamB.value = (_teamB.value + players).distinctBy { it.name }
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
            val matchId = createMatch(0, 0)
            val teamAPlayerNames = _teamA.value.map { it.name }
            val teamBPlayerNames = _teamB.value.map { it.name }

            connectIQManager.sendTeams(
                matchId = matchId,
                teamA = teamAPlayerNames,
                teamB = teamBPlayerNames
            )
        }
        // Start the foreground service so it can receive the result even
        // if the user closes the app before the match ends.
        getApplication<Application>().startForegroundService(
            Intent(getApplication(), MatchTrackerService::class.java)
        )
    }
}
