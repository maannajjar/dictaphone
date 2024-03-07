package com.dictaphone.android.data.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dictaphone.android.data.model.SavedRecording
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * An interface which describes a DAO responsible for searching for recordings in local persistence layer
 */
@Dao
interface RecordingsDao {
    @Query("SELECT * FROM recording ORDER BY recordingTime DESC")
    fun fetchRecordings(): Observable<List<SavedRecording>>

    @Insert
    fun insertRecording(savedRecording: SavedRecording) : Single<Unit>

    @Update
    fun updateRecording(savedRecording: SavedRecording) : Single<Unit>

}