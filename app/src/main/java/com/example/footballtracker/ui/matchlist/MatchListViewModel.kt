package com.example.footballtracker.ui.matchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footballtracker.data.local.entity.MatchEntity
import com.example.footballtracker.data.local.entity.MatchWithPlayersAndTeam
import com.example.footballtracker.data.repository.MatchRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MatchListViewModel(
    private val repository: MatchRepository
) : ViewModel() {

    val matches = repository.getAllMatchesWithPlayers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uploadResult = MutableSharedFlow<Result<Unit>>()
    val uploadResult: SharedFlow<Result<Unit>> = _uploadResult.asSharedFlow()

    fun deleteMatch(match: MatchEntity) {
        viewModelScope.launch {
            repository.deleteMatch(match)
        }
    }

    fun uploadMatch(matchWithPlayers: MatchWithPlayersAndTeam) {
        viewModelScope.launch {
            val result = repository.uploadMatch(matchWithPlayers)
            _uploadResult.emit(result)
        }
    }
}
