package com.example.footballtracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "match_players",
    primaryKeys = ["matchId", "playerId"],
    foreignKeys = [
        ForeignKey(
            entity = MatchEntity::class,
            parentColumns = ["matchId"],
            childColumns = ["matchId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("matchId"), Index("playerId")]
)
data class MatchPlayerEntity(
    val matchId: Long,
    val playerId: Long,
    val team: String, // "A" or "B"
    val goals: Int = 0,
    val assists: Int = 0
)
