package com.example.footballtracker.data.repository

import com.example.footballtracker.data.local.dao.MatchDao
import com.example.footballtracker.data.local.entity.*
import com.example.footballtracker.data.remote.MatchApi
import com.example.footballtracker.data.remote.MatchUploadDto
import com.example.footballtracker.data.remote.PlayerUploadDto
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class MatchRepository(
    private val matchDao: MatchDao
) {

    private val json = Json { ignoreUnknownKeys = true }
    private val contentType = "application/json".toMediaType()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(MatchApi.BASE_URL)
        .client(OkHttpClient.Builder().build())
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()

    private val matchApi = retrofit.create(MatchApi::class.java)

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
        playersWithStats: List<Triple<String, Int, Int>> // name, goals, assists
    ) {
        matchDao.updateMatchScore(matchId, scoreA, scoreB)

        playersWithStats.forEach { (name, goalsInMatch, assistsInMatch) ->
            val player = matchDao.getPlayerByName(name)
            if (player != null) {
                matchDao.updateMatchPlayerGoals(matchId, player.id, goalsInMatch)
                matchDao.updateMatchPlayerAssists(matchId, player.id, assistsInMatch)
                val allTimeGoals = matchDao.sumGoalsForPlayer(player.id)
                val allTimeAssists = matchDao.sumAssistsForPlayer(player.id)
                matchDao.updatePlayer(player.copy(goals = allTimeGoals, assists = allTimeAssists))
            }
        }
    }

    suspend fun uploadMatch(matchWithPlayers: MatchWithPlayersAndTeam): Result<Unit> {
        return try {
            val dto = MatchUploadDto(
                teamAName = matchWithPlayers.match.teamAName,
                teamBName = matchWithPlayers.match.teamBName,
                teamAScore = matchWithPlayers.match.teamAScore,
                teamBScore = matchWithPlayers.match.teamBScore,
                timestamp = matchWithPlayers.match.timestamp,
                players = matchWithPlayers.matchPlayers.map {
                    PlayerUploadDto(
                        name = it.player.name,
                        goals = it.matchPlayer.goals,
                        assists = it.matchPlayer.assists,
                        team = it.matchPlayer.team
                    )
                }
            )
            val response = matchApi.uploadMatch(dto)
            if (response.isSuccessful) {
                // Update local database status on success
                matchDao.updateMatchUploadStatus(matchWithPlayers.match.matchId, true)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Upload failed with code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addPlayer(player: PlayerEntity) {
        matchDao.insertPlayer(player)
    }

    suspend fun deleteMatch(match: MatchEntity) {
        val matchPlayers = matchDao.getMatchPlayersForMatch(match.matchId)
        matchDao.deleteMatch(match)
        matchPlayers.forEach { matchPlayer ->
            val player = matchDao.getPlayerById(matchPlayer.playerId)
            if (player != null) {
                val allTimeGoals = matchDao.sumGoalsForPlayer(player.id)
                val allTimeAssists = matchDao.sumAssistsForPlayer(player.id)
                matchDao.updatePlayer(player.copy(goals = allTimeGoals, assists = allTimeAssists))
            }
        }
    }

    fun getAllPlayers(): Flow<List<PlayerEntity>> {
        return matchDao.getAllPlayers()
    }

    fun getAllMatchesWithPlayers(): Flow<List<MatchWithPlayersAndTeam>> {
        return matchDao.getMatchesWithPlayers()
    }
}
