package com.example.footballtracker

import android.app.Application
import com.example.footballtracker.connectiq.ConnectIQManager
import com.example.footballtracker.data.local.database.AppDatabase
import com.example.footballtracker.data.repository.MatchRepository

class FootballTrackerApplication : Application() {
    val database by lazy { AppDatabase.build(this) }
    val connectIQManager by lazy { ConnectIQManager(this) }
    val repository by lazy { MatchRepository(database.matchDao()) }
}
