package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_items")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val originalUrl: String,
    val resolvedUrl: String,
    val timestamp: Long = System.currentTimeMillis()
)
