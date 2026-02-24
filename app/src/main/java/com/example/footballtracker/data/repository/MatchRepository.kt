package com.example.footballtracker.data.repository

import com.example.footballtracker.data.local.dao.MatchDao
import com.example.footballtracker.data.local.entity.*
import kotlinx.coroutines.flow.Flow

class MatchRepository(
    private val matchDao: MatchDao
) {

    suspend fun saveMatch(match: MatchEntity, teamAPlayerNames: List<String>, teamBPlayerNames: List<String>): Long {
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
        return matchId
    }

    suspend fun updateMatchResult(
        matchId: Long, 
        scoreA: Int, 
        scoreB: Int, 
        playersWithGoals: List<Pair<String, Int>>
    ) {
        // Update match scores
        matchDao.updateMatchScore(matchId, scoreA, scoreB)

        // Update each player's goals for this match and their total goals
        playersWithGoals.forEach { (name, goalsInMatch) ->
            val player = matchDao.getPlayerByName(name)
            if (player != null) {
                // Update match-specific goals in the junction table
                matchDao.updateMatchPlayerGoals(matchId, player.id, goalsInMatch)

                // Update total player goals in the players table
                val updatedPlayer = player.copy(goals = player.goals + goalsInMatch)
                matchDao.updatePlayer(updatedPlayer)
            }
        }
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
