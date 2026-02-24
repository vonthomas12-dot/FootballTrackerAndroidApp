package com.example.footballtracker.ui.matchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footballtracker.data.repository.MatchRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class MatchListViewModel(
    repository: MatchRepository
) : ViewModel() {

    val matches = repository.getAllMatches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}