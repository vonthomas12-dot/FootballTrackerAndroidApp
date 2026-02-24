package com.example.footballtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.footballtracker.connectiq.ConnectIQManager
import com.example.footballtracker.ui.navigation.NavGraph
import com.example.footballtracker.ui.theme.FootballTrackerTheme

class MainActivity : ComponentActivity() {
    private lateinit var connectIQManager: ConnectIQManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        connectIQManager = ConnectIQManager(this)

        setContent {
            FootballTrackerTheme {
                NavGraph(connectIQManager)
            }
        }
    }
}
