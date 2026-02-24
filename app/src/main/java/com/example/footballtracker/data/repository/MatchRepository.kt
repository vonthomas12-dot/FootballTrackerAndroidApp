package com.example.footballtracker.data.repository

import com.example.footballtracker.data.local.dao.MatchDao
import com.example.footballtracker.data.local.entity.MatchEntity
import com.example.footballtracker.data.local.entity.PlayerEntity
import com.example.footballtracker.data.local.entity.MatchWithPlayers
import kotlinx.coroutines.flow.Flow

class MatchRepository(
    private val matchDao: MatchDao
) {

    // -------------------
    // Insert a match with its players
    // -------------------
    suspend fun saveMatch(match: MatchEntity, players: List<PlayerEntity>) {
        val matchId = matchDao.insertMatch(match)
        val playersWithMatchId = players.map { it.copy(matchOwnerId = matchId) }
        matchDao.insertPlayers(playersWithMatchId)
    }

    suspend fun addPlayer(player: PlayerEntity) {
        matchDao.insertPlayer(player)
    }

    fun getAllPlayers(): Flow<List<PlayerEntity>> {
        return matchDao.getAllPlayers()
    }

    // -------------------
    // Observe all matches with their players
    // -------------------
    fun getAllMatches(): Flow<List<MatchWithPlayers>> {
        return matchDao.getMatchesWithPlayers()
    }

    // -------------------
    // Optional: observe just match entities
    // -------------------
    fun getMatchesOnly() = matchDao.getAllMatches()
}