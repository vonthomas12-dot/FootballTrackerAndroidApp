package com.example.footballtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true)
    val matchId: Long = 0,
    val teamAName: String,
    val teamBName: String,
    val teamAScore: Int,
    val teamBScore: Int,
    val timestamp: Long,
    val isUploaded: Boolean = false
)
