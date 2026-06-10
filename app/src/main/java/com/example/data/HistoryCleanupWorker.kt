package com.example.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class HistoryCleanupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            val settings = SettingsManager(applicationContext)
            if (settings.saveHistory) {
                val db = AppDatabase.getDatabase(applicationContext)
                val repository = HistoryRepository(db.historyDao())
                val days = settings.autoDeleteDays
                if (days > 0) {
                    repository.deleteOlderThan(days)
                }
            }
            Result.success()
        } catch (_: Exception) {
            Result.failure()
        }
    }
}
