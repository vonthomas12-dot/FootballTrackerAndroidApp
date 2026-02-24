package com.example.footballtracker.ui.matchsetup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footballtracker.connectiq.ConnectIQManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MatchSetupViewModel(
    private val connectIQManager: ConnectIQManager
) : ViewModel() {

    private val _teamA = MutableStateFlow<List<String>>(emptyList())
    val teamA: StateFlow<List<String>> = _teamA.asStateFlow()

    private val _teamB = MutableStateFlow<List<String>>(emptyList())
    val teamB: StateFlow<List<String>> = _teamB.asStateFlow()

    fun addPlayer(name: String, team: String) {
        if (team == "A") {
            _teamA.value = _teamA.value + name
        } else {
            _teamB.value = _teamB.value + name
        }
    }

    fun removePlayer(name: String, team: String) {
        if (team == "A") {
            _teamA.value = _teamA.value - name
        } else {
            _teamB.value = _teamB.value - name
        }
    }

    fun sendTeamsToWatch() {
        viewModelScope.launch(Dispatchers.IO) {
            connectIQManager.sendTeams(
                teamA = _teamA.value,
                teamB = _teamB.value
            )
        }
    }
}