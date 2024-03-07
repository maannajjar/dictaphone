package com.dictaphone.android.data

import com.dictaphone.android.data.model.SavedRecording
import com.dictaphone.android.data.persistence.PersistenceService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository of [SavedRecording].
 * Data layer component which provides access to user saved recordings
 */
@Singleton
class RecordingsRepository @Inject constructor(
    private val persistenceService: PersistenceService
) {

    /**
     * Returns an [Observable] emitting a set of [SavedRecording].
     * The Observable will  emit the results found in the database, any changes to the database
     * will be automatically emitted
     */
    fun fetchRecordings(): Observable<List<SavedRecording>> {
        return persistenceService.recordingsDao()
            .fetchRecordings().map {
                it.forEach { it.fileExists = File(it.recordingFilePath).exists() }
                it
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    }

    fun insertRecording(savedRecording: SavedRecording) {
        persistenceService.recordingsDao()
            .insertRecording(savedRecording)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()

    }

    fun updateRecording(savedRecording: SavedRecording) {
        persistenceService.recordingsDao()
            .updateRecording(savedRecording)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }
}