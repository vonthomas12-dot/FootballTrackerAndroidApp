package com.example.footballtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.footballtracker.ui.navigation.NavGraph
import com.example.footballtracker.ui.theme.FootballTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FootballTrackerTheme {
                NavGraph()
            }
        }
    }
}
