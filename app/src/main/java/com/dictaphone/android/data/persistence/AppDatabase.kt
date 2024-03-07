package com.dictaphone.android.data.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dictaphone.android.data.model.SavedRecording

/**
 * Room-specific components used to set up PersistenceService implementation
 */
@Database(entities = [SavedRecording::class], version = 1)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun recordingsDao(): RecordingsDao
}
