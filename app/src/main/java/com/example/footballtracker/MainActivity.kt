package com.example.footballtracker

import android.R.attr.onClick
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.footballtracker.ui.PlayerScreen
import com.example.footballtracker.ui.theme.FootballTrackerTheme
import com.garmin.android.connectiq.ConnectIQ
import com.garmin.android.connectiq.ConnectIQ.ConnectIQListener
import com.garmin.android.connectiq.ConnectIQ.IQSdkErrorStatus
import com.garmin.android.connectiq.IQApp
import com.garmin.android.connectiq.IQDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FootballTrackerTheme {
                FootballApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun FootballApp() {
    val teamA = remember { mutableStateListOf<String>() }
    val teamB = remember { mutableStateListOf<String>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Football Manager") }
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            item {
                ConnectToFootballWatchApp()

                Spacer(modifier = Modifier.height(16.dp))

                PlayerScreen(
                    teamA = teamA,
                    teamB = teamB,
                    onAddPlayer = { name, team ->
                        if (team == "A") {
                            teamA.add(name)
                        } else {
                            teamB.add(name)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                SendMessageButton(teamA, teamB)
            }
        }
    }
}

@Composable
fun SendMessageButton(
    teamA: List<String>,
    teamB: List<String>,
    modifier: Modifier = Modifier
    .fillMaxSize()
    .wrapContentSize(Alignment.Center)
) {
    var showPreview by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Button(onClick = { showPreview = true }) {
            if (showPreview) {
                SendMessage(teamA, teamB)
                showPreview = false
            }
            Text(stringResource(R.string.sendMessage))
        }
    }
}

private lateinit var connectIQ: ConnectIQ
private var isSending = true

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun SendMessage(
    teamA: List<String>,
    teamB: List<String>
) {

    val devices = connectIQ.knownDevices;

    val appId = "3ac30dc4-ec32-42dd-994f-2bf038aec404"
    val iqApp = IQApp(appId);
    val testNum = Random.nextInt(0, 100);
    val message = mapOf(
        "teamA" to teamA,
        "teamB" to teamB
    )

    /*connectIQ.sendMessage(devices[0], iqApp, message, object : ConnectIQ.IQSendMessageListener {
        override fun onMessageStatus(
            device: IQDevice?,
            iqApp: IQApp?,
            status: ConnectIQ.IQMessageStatus?
        ) {
            Log.d("MainActivity", "Send status: $status")
        }
    })*/

    isSending = true

    CoroutineScope(Dispatchers.IO).launch {
        connectIQ.sendMessage(devices[0], iqApp, message,
            object : ConnectIQ.IQSendMessageListener {
                override fun onMessageStatus(
                    device: IQDevice?,
                    iqApp: IQApp?,
                    status: ConnectIQ.IQMessageStatus?
                ) {
                    runOnUiThread {
                        isSending = false
                        Log.d("MainActivity", "Send status: $status")
                    }
                }

                private fun runOnUiThread(function: () -> Int) {}
            }
        )
    }
}

@Composable
fun ConnectToFootballWatchApp() {
    val context = LocalContext.current
    connectIQ = ConnectIQ.getInstance(
        context,
        ConnectIQ.IQConnectType.TETHERED
    )

    connectIQ.initialize(context, true, object : ConnectIQListener {
        // Called when the SDK has been successfully initialized
        override fun onSdkReady() {
            // Do any post initialization setup.
            Log.d("MainActivity", "Sdk ready")
        }

        override fun onInitializeError(p0: IQSdkErrorStatus?) {
            Log.d("MainActivity", "Sdk error")
        }

        // Called when the SDK has been shut down
        override fun onSdkShutDown() {
            // Take care of any post shutdown requirements
        }

        // Called when initialization fails.
        public fun onInitializationError(status: IQSdkErrorStatus?) {
            // A failure has occurred during initialization. Inspect
            // the IQSdkErrorStatus value for more information regarding
            // the failure.
        }
    })
}