package com.example.footballtracker.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class MatchWithPlayers(
    @Embedded val match: MatchEntity,
    @Relation(
        parentColumn = "matchId",
        entityColumn = "matchOwnerId"
    )
    val players: List<PlayerEntity>
)