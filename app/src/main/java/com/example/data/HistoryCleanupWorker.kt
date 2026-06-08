package com.example.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters

class HistoryCleanupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val channelId = "history_cleanup_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "History Cleanup", NotificationManager.IMPORTANCE_LOW)
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Cleaning up old history")
            .setSmallIcon(android.R.drawable.ic_menu_delete)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return ForegroundInfo(101, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE)
        } else {
            return ForegroundInfo(101, notification)
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val settings = SettingsManager(applicationContext)
            if (settings.saveHistory) {
                val db = AppDatabase.getDatabase(applicationContext)
                val repository = HistoryRepository(db.historyDao())
                val days = settings.autoDeleteDays
                repository.deleteOlderThan(days)
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
