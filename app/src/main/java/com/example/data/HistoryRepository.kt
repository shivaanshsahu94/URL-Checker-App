package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class HistoryRepository(private val historyDao: HistoryDao) {
    // Audit: explicitly push flow to Dispatchers.IO
    val allHistory: Flow<List<HistoryItem>> = historyDao.getAllHistory()
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)

    suspend fun insert(originalUrl: String, resolvedUrl: String) {
        // Audit: explicitly push DB write to Dispatchers.IO
        withContext(Dispatchers.IO) {
            val item = HistoryItem(originalUrl = originalUrl, resolvedUrl = resolvedUrl)
            historyDao.insertHistory(item)
        }
    }

    suspend fun deleteById(id: Int) {
        // Audit: explicitly push DB write to Dispatchers.IO
        withContext(Dispatchers.IO) {
            historyDao.deleteHistoryById(id)
        }
    }

    suspend fun clearAll() {
        // Audit: explicitly push DB write to Dispatchers.IO
        withContext(Dispatchers.IO) {
            historyDao.clearAllHistory()
        }
    }

    suspend fun deleteOlderThan(days: Int) {
        // Audit: explicitly push DB write to Dispatchers.IO
        withContext(Dispatchers.IO) {
            val cutoff = System.currentTimeMillis() - (days * 24L * 60 * 60 * 1000)
            historyDao.deleteOldHistory(cutoff)
        }
    }
}
