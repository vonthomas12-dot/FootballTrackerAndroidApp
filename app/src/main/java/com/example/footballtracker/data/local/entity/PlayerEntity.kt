package com.example.footballtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val matchOwnerId: Long = 0, // reference to MatchEntity.matchId, 0 if not assigned
    val name: String,
    val team: String, // "A" or "B"
    val goals: Int = 0
)