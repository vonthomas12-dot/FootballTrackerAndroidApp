package com.example.footballtracker.data.repository

import com.example.footballtracker.data.local.dao.MatchDao
import com.example.footballtracker.data.local.entity.*
import kotlinx.coroutines.flow.Flow

class MatchRepository(
    private val matchDao: MatchDao
) {

    suspend fun saveMatch(match: MatchEntity, teamAPlayerNames: List<String>, teamBPlayerNames: List<String>) {
        val matchId = matchDao.insertMatch(match)
        
        val matchPlayers = mutableListOf<MatchPlayerEntity>()
        
        teamAPlayerNames.forEach { name ->
            val existingPlayer = matchDao.getPlayerByName(name)
            val playerId = existingPlayer?.id ?: matchDao.insertPlayer(PlayerEntity(name = name))
            
            matchPlayers.add(MatchPlayerEntity(matchId = matchId, playerId = playerId, team = "A"))
        }

        teamBPlayerNames.forEach { name ->
            val existingPlayer = matchDao.getPlayerByName(name)
            val playerId = existingPlayer?.id ?: matchDao.insertPlayer(PlayerEntity(name = name))
            
            matchPlayers.add(MatchPlayerEntity(matchId = matchId, playerId = playerId, team = "B"))
        }
        
        matchDao.insertMatchPlayers(matchPlayers)
    }

    suspend fun addPlayer(player: PlayerEntity) {
        matchDao.insertPlayer(player)
    }

    suspend fun deleteMatch(match: MatchEntity) {
        matchDao.deleteMatch(match)
    }

    fun getAllPlayers(): Flow<List<PlayerEntity>> {
        return matchDao.getAllPlayers()
    }

    fun getAllMatchesWithPlayers(): Flow<List<MatchWithPlayersAndTeam>> {
        return matchDao.getMatchesWithPlayers()
    }
}
