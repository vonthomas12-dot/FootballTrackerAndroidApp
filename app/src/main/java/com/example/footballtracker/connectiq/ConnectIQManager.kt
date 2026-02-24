package com.example.footballtracker.connectiq

import android.content.Context
import android.util.Log
import com.garmin.android.connectiq.ConnectIQ
import com.garmin.android.connectiq.IQApp
import com.garmin.android.connectiq.IQDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ConnectIQManager(
    private val context: Context
) {

    private val connectIQ =
        ConnectIQ.getInstance(context, ConnectIQ.IQConnectType.TETHERED)

    private val appId = "3ac30dc4-ec32-42dd-994f-2bf038aec404"
    private val iqApp = IQApp(appId)

    private var onMessageReceived: ((Any?) -> Unit)? = null

    init {
        try {
            // Default Garmin Simulator port is 1234
            connectIQ.setAdbPort(7381)
        } catch (e: Exception) {
            Log.e("ConnectIQ", "Error setting ADB port", e)
        }
        connectIQ.initialize(context, true, object : ConnectIQ.ConnectIQListener {
            override fun onSdkReady() {
                Log.d("ConnectIQ", "SDK Ready")
                registerForMessages()
            }

            override fun onInitializeError(status: ConnectIQ.IQSdkErrorStatus?) {
                Log.e("ConnectIQ", "Init Error: $status")
            }

            override fun onSdkShutDown() {
                Log.d("ConnectIQ", "SDK Shut Down")
            }
        })
    }

    private fun registerForMessages() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Give the simulator a second to connect
                delay(2000)
                
                val devices = connectIQ.knownDevices
                if (devices.isNullOrEmpty()) {
                    Log.w("ConnectIQ", "No known devices found. Is the simulator running?")
                    return@launch
                }
                val device = devices.first()

                // Use registerForAppEvents to receive messages from the watch app
                connectIQ.registerForAppEvents(device, iqApp, object : ConnectIQ.IQApplicationEventListener {
                    override fun onMessageReceived(
                        device: IQDevice?,
                        app: IQApp?,
                        messages: MutableList<Any>?,
                        status: ConnectIQ.IQMessageStatus?
                    ) {
                        Log.d("ConnectIQ", "Message received: $messages, status: $status")
                        if (status == ConnectIQ.IQMessageStatus.SUCCESS && messages != null) {
                            for (message in messages) {
                                onMessageReceived?.invoke(message)
                            }
                        }
                    }
                })
            } catch (e: Exception) {
                Log.e("ConnectIQ", "Error registering for messages", e)
            }
        }
    }

    fun setOnMessageReceivedListener(listener: (Any?) -> Unit) {
        onMessageReceived = listener
    }

    fun sendTeams(
        teamA: List<String>,
        teamB: List<String>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val devices = connectIQ.knownDevices
                if (devices.isNullOrEmpty()) {
                    Log.w("ConnectIQ", "No devices found to send teams")
                    return@launch
                }
                val device = devices.first()

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
            } catch (e: Exception) {
                Log.e("ConnectIQ", "Error sending teams", e)
            }
        }
    }

    fun shutdown() {
        connectIQ.shutdown(context)
    }
}
