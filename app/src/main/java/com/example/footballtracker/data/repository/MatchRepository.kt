package com.example.footballtracker.data.repository

import com.example.footballtracker.data.local.dao.MatchDao
import com.example.footballtracker.data.local.entity.*
import com.example.footballtracker.data.remote.EventDto
import com.example.footballtracker.data.remote.MatchApi
import com.example.footballtracker.data.remote.MatchUploadDto
import com.example.footballtracker.data.remote.PlayerUploadDto
import com.example.footballtracker.BuildConfig
import android.util.Log
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
        .client(
            OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("x-api-key", BuildConfig.API_KEY)
                        .build()
                    chain.proceed(request)
                }
                .build()
        )
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()

    private val matchApi = retrofit.create(MatchApi::class.java)

    suspend fun saveMatch(match: MatchEntity, teamAPlayerNames: List<String>, teamBPlayerNames: List<String>): Long {
        val matchId = matchDao.insertMatch(match)

        val allNames = (teamAPlayerNames + teamBPlayerNames).distinct()

        // Single query to find which players already exist
        val existingPlayers = matchDao.getPlayersByNames(allNames).associateBy { it.name }

        // Insert only the new ones in bulk
        val newPlayers = allNames
            .filter { it !in existingPlayers }
            .map { PlayerEntity(name = it) }
        val newIds = if (newPlayers.isNotEmpty()) matchDao.insertPlayers(newPlayers) else emptyList()

        // Build name -> id map combining existing and newly inserted
        val nameToId = existingPlayers.mapValues { it.value.id }.toMutableMap()
        newPlayers.forEachIndexed { index, player -> nameToId[player.name] = newIds[index] }

        val matchPlayers = (teamAPlayerNames.map { MatchPlayerEntity(matchId, nameToId.getValue(it), "A") } +
                            teamBPlayerNames.map { MatchPlayerEntity(matchId, nameToId.getValue(it), "B") })

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
            Log.d("MatchRepository", "Uploading match DTO: ${json.encodeToString(MatchUploadDto.serializer(), dto)}")
            val response = matchApi.uploadMatch(dto)
            if (response.isSuccessful) {
                // Update local database status on success
                matchDao.updateMatchUploadStatus(matchWithPlayers.match.matchId, true)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Upload failed with code: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchEvent(date: String): Result<EventDto> {
        return try {
            val response = matchApi.getEvent(date)
            if (response.isSuccessful) {
                Result.success(response.body() ?: EventDto())
            } else {
                Result.failure(Exception("Fetch failed with code: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addPlayer(player: PlayerEntity) {
        matchDao.insertPlayer(player)
    }

    suspend fun saveEventPlayers(names: List<String>) {
        val existingNames = matchDao.getPlayersByNames(names).map { it.name }.toSet()
        val newPlayers = names.filter { it !in existingNames }.map { PlayerEntity(name = it) }
        if (newPlayers.isNotEmpty()) matchDao.insertPlayers(newPlayers)
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
