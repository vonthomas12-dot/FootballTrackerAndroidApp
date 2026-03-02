package com.example.footballtracker.ui.players

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footballtracker.data.local.entity.PlayerEntity
import com.example.footballtracker.data.repository.MatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class SortOrder { GOALS, NAME }

class PlayersViewModel(
    private val repository: MatchRepository
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(SortOrder.GOALS)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    val players: StateFlow<List<PlayerEntity>> = repository.getAllPlayers()
        .combine(_sortOrder) { list, order ->
            when (order) {
                SortOrder.NAME -> list.sortedBy { it.name.lowercase() }
                SortOrder.GOALS -> list.sortedByDescending { it.goals }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }
}