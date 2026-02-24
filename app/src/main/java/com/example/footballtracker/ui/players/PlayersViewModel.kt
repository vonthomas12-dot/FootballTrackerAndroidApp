package com.example.footballtracker.ui.players

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footballtracker.data.local.entity.PlayerEntity
import com.example.footballtracker.data.repository.MatchRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class PlayersViewModel(
    private val repository: MatchRepository
) : ViewModel() {

    val players: StateFlow<List<PlayerEntity>> = repository.getAllPlayers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}