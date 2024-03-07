package com.dictaphone.android.service

import android.media.MediaPlayer
import com.dictaphone.android.data.RecordingsRepository
import com.dictaphone.android.data.model.SavedRecording
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.io.IOException
import javax.inject.Inject

/**
 * Basic implementation which provides the functionality to play existing audio recordings
 * It maintains its own state allowing other components to subscribe to it for UI/Business logic
 */
class AudioPlayer @Inject constructor(
    private val recordingsRepository: RecordingsRepository
) {

    private var player: MediaPlayer? = null

    sealed class State(val recording: SavedRecording?) {
        data object Idle : State(null)
        class PlayingRecording(recording: SavedRecording) : State(recording)
        class PausedRecording(recording: SavedRecording) : State(recording)
        class FinishedPlaying(recording: SavedRecording) : State(recording)
    }

    // Subject which enables the player to emit latest state
    private val currentStateSubject = BehaviorSubject.createDefault<State>(State.Idle)

    // Observable for use by other components subscribing to the player state
    val currentState: Observable<State> = currentStateSubject

    /**
     * Invoking this will cause the AudioPlayer to play the requested [SavedRecording]
     * If the the requested recording is already being played, this will pause the player instead
     * If the the requested recording was the last thing played but paused, this will resume the player instead
     * Otherwise, it will play the new recording (and stop any existing playback for other recording, if any)
     */
    fun playRecordedAudio(savedRecording: SavedRecording) {
        val state = currentStateSubject.value

        // It's already playing a recording
        if (state is State.PlayingRecording) {
            if (state.recording?.id == savedRecording.id) {
                // Pause the existing recording since it's the same one requested
                pausePlayback()
                return
            } else {
                // Otherwise, stop current playback
                stopPlaying()
            }
        } else if (state is State.PausedRecording && state.recording?.id == savedRecording.id) {
            // Resume playing of previously paused playback of the same recording
            resumePlaying()
            return
        }
        recordingsRepository.updateRecording(savedRecording.copy(isPlayed = true).apply { id = savedRecording.id })
        // Start playing the recording since this is a new recording requested
        player = MediaPlayer().apply {
            try {
                setDataSource(savedRecording.recordingFilePath)
                prepare()
                seekTo(0)
                start()
                setOnCompletionListener {
                    if (player == it) {
                        player?.release()
                        player = null
                        currentStateSubject.onNext(State.FinishedPlaying(savedRecording))
                    }
                }
                currentStateSubject.onNext(State.PlayingRecording(savedRecording))
            } catch (e: IOException) {
                // TODO: Handle and emit errors
            }
        }

    }

    /**
     * This pauses playback for the current recording being played
     */
    fun pausePlayback() {
        // If the current state is not playing, this has no-op
        val state = currentStateSubject.value as? State.PlayingRecording ?: return

        player?.pause()

        currentStateSubject.onNext(
            State.PausedRecording(
                state.recording!!
            )
        )
    }

    /**
     * Resumes playing of whatever last recording was being played
     * If no recording was being played before, it will be no-op
     */
    private fun resumePlaying() {
        // If the current state is not paused, this has no-op
        val state = currentStateSubject.value as? State.PausedRecording ?: return

        player?.start()
        currentStateSubject.onNext(
            State.PlayingRecording(
                state.recording!!
            )
        )
    }

    /**
     * Private method to stop MediaPlayer playback, this doesn't affect the AudioPlayer state
     */
    private fun stopPlaying() {
        // Stop playing the playback preview
        player?.release()
        player = null
    }

}