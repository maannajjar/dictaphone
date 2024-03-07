package com.dictaphone.android.data.persistence

import android.app.Application
import androidx.room.Room
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Application-scoped component which provides access to local persistence
 */
@Singleton
class PersistenceService @Inject constructor(application: Application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "dictaphone-database"
    ).build()

    fun recordingsDao() = db.recordingsDao()

}