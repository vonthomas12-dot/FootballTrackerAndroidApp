package com.example.footballtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.footballtracker.connectiq.ConnectIQManager
import com.example.footballtracker.data.local.database.AppDatabase
import com.example.footballtracker.data.repository.MatchRepository
import com.example.footballtracker.ui.navigation.NavGraph
import com.example.footballtracker.ui.theme.FootballTrackerTheme

class MainActivity : ComponentActivity() {
    private lateinit var connectIQManager: ConnectIQManager
    private lateinit var repository: MatchRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        connectIQManager = ConnectIQManager(this)
        
        val database = AppDatabase.build(this)
        repository = MatchRepository(database.matchDao())

        setContent {
            FootballTrackerTheme {
                NavGraph(connectIQManager, repository)
            }
        }
    }
}
