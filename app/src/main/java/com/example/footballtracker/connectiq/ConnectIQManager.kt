package com.example.footballtracker.connectiq

import android.content.Context
import android.util.Log
import com.garmin.android.connectiq.ConnectIQ
import com.garmin.android.connectiq.IQApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConnectIQManager(
    private val context: Context
) {

    private val connectIQ =
        ConnectIQ.getInstance(context, ConnectIQ.IQConnectType.TETHERED)

    private val appId = "3ac30dc4-ec32-42dd-994f-2bf038aec404"
    private val iqApp = IQApp(appId)

    init {
        connectIQ.initialize(context, true, object : ConnectIQ.ConnectIQListener {
            override fun onSdkReady() {
                Log.d("ConnectIQ", "SDK Ready")
            }

            override fun onInitializeError(status: ConnectIQ.IQSdkErrorStatus?) {
                Log.e("ConnectIQ", "Init Error: $status")
            }

            override fun onSdkShutDown() {
                Log.d("ConnectIQ", "SDK Shut Down")
            }
        })
    }

    fun sendTeams(
        teamA: List<String>,
        teamB: List<String>
    ) {

        CoroutineScope(Dispatchers.IO).launch {
            val device = connectIQ.knownDevices.firstOrNull() ?: return@launch

            val message = mapOf(
                "teamA" to teamA,
                "teamB" to teamB
            )

            connectIQ.sendMessage(
                device,
                iqApp,
                message
            ) { _, _, status ->
                Log.d("ConnectIQ", "Send status: $status")
            }
        }
    }

    fun shutdown() {
        connectIQ.shutdown(context)
    }
}