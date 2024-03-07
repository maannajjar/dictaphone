package com.dictaphone.android.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Models Recording already saved by user
 */
@Entity(
    tableName = "recording",
)
data class SavedRecording(
    val recordingTime: Long,
    val recordingLengthMs: Long,
    val recordingFilePath: String,
    val isPlayed: Boolean
) {
    @PrimaryKey(autoGenerate = true) var id: Int = 0
    // This is only computed runtime
    @Ignore var fileExists: Boolean = true
}