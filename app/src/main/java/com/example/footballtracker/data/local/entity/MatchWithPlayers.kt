package com.example.footballtracker.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class MatchWithPlayers(
    @Embedded val match: MatchEntity,
    @Relation(
        entity = PlayerEntity::class,
        parentColumn = "matchId",
        entityColumn = "id",
        associateBy = Junction(
            value = MatchPlayerEntity::class,
            parentColumn = "matchId",
            entityColumn = "playerId"
        )
    )
    val players: List<PlayerEntity>
)

data class MatchWithPlayersAndTeam(
    @Embedded val match: MatchEntity,
    @Relation(
        entity = MatchPlayerEntity::class,
        parentColumn = "matchId",
        entityColumn = "matchId"
    )
    val matchPlayers: List<MatchPlayerWithPlayer>
)

data class MatchPlayerWithPlayer(
    @Embedded val matchPlayer: MatchPlayerEntity,
    @Relation(
        parentColumn = "playerId",
        entityColumn = "id"
    )
    val player: PlayerEntity
)
