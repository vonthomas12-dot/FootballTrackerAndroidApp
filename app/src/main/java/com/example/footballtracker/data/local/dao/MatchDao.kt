package com.example.footballtracker.data.local.dao

import androidx.room.*
import com.example.footballtracker.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {

    // -------------------
    // MATCHES
    // -------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity): Long

    @Query("SELECT * FROM matches ORDER BY timestamp DESC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Delete
    suspend fun deleteMatch(match: MatchEntity)

    @Query("UPDATE matches SET teamAScore = :scoreA, teamBScore = :scoreB WHERE matchId = :matchId")
    suspend fun updateMatchScore(matchId: Long, scoreA: Int, scoreB: Int)

    // -------------------
    // PLAYERS
    // -------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity): Long

    @Update
    suspend fun updatePlayer(player: PlayerEntity)

    @Query("SELECT * FROM players")
    fun getAllPlayers(): Flow<List<PlayerEntity>>
    
    @Query("SELECT * FROM players WHERE name = :name LIMIT 1")
    suspend fun getPlayerByName(name: String): PlayerEntity?

    // -------------------
    // MATCH PLAYERS (JUNCTION)
    // -------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchPlayers(matchPlayers: List<MatchPlayerEntity>)

    @Query("UPDATE match_players SET goals = :goals WHERE matchId = :matchId AND playerId = :playerId")
    suspend fun updateMatchPlayerGoals(matchId: Long, playerId: Long, goals: Int)

    // -------------------
    // RELATIONS
    // -------------------

    @Transaction
    @Query("SELECT * FROM matches ORDER BY timestamp DESC")
    fun getMatchesWithPlayers(): Flow<List<MatchWithPlayersAndTeam>>
}
