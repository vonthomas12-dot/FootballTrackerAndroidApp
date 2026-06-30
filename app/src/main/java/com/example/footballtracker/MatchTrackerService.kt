package com.example.footballtracker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.footballtracker.data.repository.MatchRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MatchTrackerService : Service() {

    companion object {
        // Low importance: silent persistent notification while match is running
        const val CHANNEL_ONGOING = "match_tracker_ongoing"
        // High importance: heads-up (pop-up) notification when result arrives
        const val CHANNEL_RESULT = "match_tracker_result"
        const val NOTIFICATION_ONGOING = 1
        const val NOTIFICATION_RESULT = 2
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ONGOING, buildOngoingNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ONGOING, buildOngoingNotification())
        }

        val app = application as FootballTrackerApplication
        app.connectIQManager.setOnMessageReceivedListener { message ->
            handleMessage(message, app.repository)
        }

        return START_NOT_STICKY
    }

    private fun handleMessage(message: Any?, repository: MatchRepository) {
        val data = message as? Map<*, *> ?: return
        val matchId = (data["matchId"] as? Number)?.toLong() ?: return
        val scoreA = (data["scoreA"] as? Number)?.toInt() ?: 0
        val scoreB = (data["scoreB"] as? Number)?.toInt() ?: 0

        val playersWithStats = mutableListOf<Triple<String, Int, Int>>()
        listOf(data["teamA"], data["teamB"]).forEach { teamRaw ->
            (teamRaw as? List<*>)?.forEach { playerObj ->
                val m = playerObj as? Map<*, *> ?: return@forEach
                val name = m["name"] as? String ?: return@forEach
                val goals = (m["goals"] as? Number)?.toInt() ?: 0
                val assists = (m["assists"] as? Number)?.toInt() ?: 0
                playersWithStats.add(Triple(name, goals, assists))
            }
        }

        scope.launch {
            try {
                repository.updateMatchResult(matchId, scoreA, scoreB, playersWithStats)
                showResultNotification(
                    title = "Meccs vége!",
                    text = "Fekete $scoreA – $scoreB Fehér · Eredmény elmentve"
                )
            } catch (e: Exception) {
                showResultNotification(
                    title = "Football Tracker",
                    text = "Hiba az eredmény mentésekor"
                )
            } finally {
                stopSelf()
            }
        }
    }

    private fun showResultNotification(title: String, text: String) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // Cancel the silent ongoing notification first
        manager.cancel(NOTIFICATION_ONGOING)
        // Post the heads-up result notification on the high-importance channel
        manager.notify(NOTIFICATION_RESULT, buildResultNotification(title, text))
    }

    private fun buildOngoingNotification(): Notification {
        val pendingIntent = openAppIntent()
        return NotificationCompat.Builder(this, CHANNEL_ONGOING)
            .setContentTitle("Football Tracker")
            .setContentText("Meccs folyamatban...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun buildResultNotification(title: String, text: String): Notification {
        val pendingIntent = openAppIntent()
        return NotificationCompat.Builder(this, CHANNEL_RESULT)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            // Heads-up requires priority HIGH and a sound/vibration on the channel
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun openAppIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        // Silent channel for the "match running" foreground notification
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ONGOING, "Aktív meccs", NotificationManager.IMPORTANCE_LOW)
                .apply { description = "Jelzi hogy fut a meccs követés" }
        )

        // High importance channel for the result — this enables heads-up pop-up
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_RESULT, "Meccs eredmény", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Értesítés amikor az eredmény megérkezik az óráról" }
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
