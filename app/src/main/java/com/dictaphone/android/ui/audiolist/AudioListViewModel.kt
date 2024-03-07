package com.dictaphone.android.ui.audiolist

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import com.dictaphone.android.data.RecordingsRepository
import com.dictaphone.android.data.model.SavedRecording
import com.dictaphone.android.service.AudioPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.kotlin.addTo
import javax.inject.Inject

/**
 * [ViewModel] which is responsible of providing the most up to date UI State for Audio List View
 * It mainly works by fetching all stored audio recordings and observing current state of [AudioPlayer]
 * The states of the aforementioned connects are then reconciled in order to provide up to date
 * view state
 */
@HiltViewModel
class AudioListViewModel @Inject constructor(
    recordingsRepository: RecordingsRepository,
    private val audioPlayer: AudioPlayer
) : ViewModel() {
    private val disposables = CompositeDisposable()


    init {
        // Observe [AudioPlayer] state in order to play next recording in the list when the AudioPlayer
        // notifies it finished playing current recording
        Observables.combineLatest(
            audioPlayer.currentState,
            recordingsRepository.fetchRecordings()
        ).subscribe { (audioPlayerState, storedRecordings) ->
                if (audioPlayerState is AudioPlayer.State.FinishedPlaying) {
                    audioPlayerState.recording?.let { playNextInQueue(it, storedRecordings) }
                }
            }.addTo(disposables)

    }

    // Observe the latest audio recordings from the database
    private val existingRecordingsList = recordingsRepository
        .fetchRecordings()
        .observeOn(AndroidSchedulers.mainThread())


    // Create observe to reconcile AudioPlayer state along with with existing recordings to
    // provide all relevant UI information needed for the Audio List View
    val uiState = Observables.combineLatest(existingRecordingsList, audioPlayer.currentState)
        .map { (myRecordingsList, playerState) ->
            UiState(
                recordingsList = myRecordingsList,
                audioPlayerState = playerState
            )
        }

    /**
     * This data class represent the UI state that is emitted by the view model which requests
     * the subscribing View to bind itself too
     */
    data class UiState(
        val recordingsList: List<SavedRecording> = emptyList(),
        val audioPlayerState: AudioPlayer.State = AudioPlayer.State.Idle
    )

    /**
     * This is called when AudioPlayer finishes playing current Recording so
     */
    private fun playNextInQueue(previous: SavedRecording, storedRecordings: List<SavedRecording>) {
        val currentIndex = storedRecordings.indexOfFirst {
            it.id == previous.id
        }
        if (currentIndex + 1 < storedRecordings.size) {
            Handler(Looper.getMainLooper()).post {
                audioPlayer.playRecordedAudio(
                    storedRecordings.drop(currentIndex+1).first { it.fileExists }
                )
            }
        }
    }

    // User triggered event to player a selected recording
    fun playRecording(savedRecording: SavedRecording) {
        audioPlayer.playRecordedAudio(savedRecording)
    }

    // User triggered event to pause playing
    fun pausePlaying() {
        audioPlayer.pausePlayback()
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
