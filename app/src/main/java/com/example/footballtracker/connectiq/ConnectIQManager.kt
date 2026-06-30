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
        ConnectIQ.getInstance(context, ConnectIQ.IQConnectType.WIRELESS)

    private val appId = "3ac30dc4-ec32-42dd-994f-2bf038aec404"
    private val iqApp = IQApp(appId)

    private var onMessageReceived: ((Any?) -> Unit)? = null

    init {
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

                for (device in devices) {
                    // Listen for connect/disconnect. The app-event registration is
                    // lost when the watch disconnects (BT drop, watch sleep, phone app
                    // backgrounded). On a long match this happened silently, so the
                    // result message sent at Save had no listener. Re-register the app
                    // events every time the device (re)connects.
                    connectIQ.registerForDeviceEvents(device) { dev, status ->
                        Log.d("ConnectIQ", "Device ${dev?.friendlyName} status: $status")
                        if (status == IQDevice.IQDeviceStatus.CONNECTED) {
                            registerForAppEvents(dev)
                        }
                    }

                    // If it is already connected right now, register immediately
                    // (we may have missed the initial CONNECTED transition above).
                    try {
                        if (connectIQ.getDeviceStatus(device) == IQDevice.IQDeviceStatus.CONNECTED) {
                            registerForAppEvents(device)
                        }
                    } catch (e: Exception) {
                        Log.e("ConnectIQ", "Error querying device status", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("ConnectIQ", "Error registering for messages", e)
            }
        }
    }

    private fun registerForAppEvents(device: IQDevice?) {
        if (device == null) return
        try {
            // registerForAppEvents replaces any previous listener for this
            // device/app pair, so calling it again on reconnect is safe.
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
            Log.d("ConnectIQ", "Registered app events for ${device.friendlyName}")
        } catch (e: Exception) {
            Log.e("ConnectIQ", "Error registering app events", e)
        }
    }

    fun setOnMessageReceivedListener(listener: (Any?) -> Unit) {
        onMessageReceived = listener
    }

    fun sendTeams(
        matchId: Long,
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
                    "matchId" to matchId,
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
