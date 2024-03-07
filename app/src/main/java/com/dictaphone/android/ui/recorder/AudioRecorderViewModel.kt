package com.dictaphone.android.ui.recorder

import androidx.lifecycle.ViewModel
import com.dictaphone.android.data.RecordingsRepository
import com.dictaphone.android.service.AudioRecorder
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject

/**
 * [ViewModel] which is responsible of managing the Ui State for Audio Recording View
 * It mainly works by reacting to user event and pass them down to core [AudioRecorder] service
 * It also ensures the UI state is up to date to reflect the current state of [AudioRecorder]
 */
@HiltViewModel
class AudioRecorderViewModel @Inject constructor(
    private val audioRecorder: AudioRecorder,
    private val recordingsRepository: RecordingsRepository
) : ViewModel() {

    private val dismissEvent = BehaviorSubject.createDefault(false)
    val recorderUiSubject = Observables.combineLatest(
        audioRecorder.recorderState,
        dismissEvent
    ).map { (recorderState, isDismissed) ->
        UiState(
            isRecording = recorderState is AudioRecorder.RecorderState.Recording,
            previewAvailable = recorderState is AudioRecorder.RecorderState.Idle && recorderState.recordingComplete,
            isPlayingPreview = recorderState is AudioRecorder.RecorderState.Playing,
            isDismissed = isDismissed
        )
    }

    data class UiState(
        val isRecording: Boolean = false,
        val previewAvailable: Boolean = false,
        val isPlayingPreview: Boolean = false,
        val isDismissed: Boolean = false
    )

    fun doRecord() {
        audioRecorder.startRecording()
    }

    fun stopRecord() {
        audioRecorder.stopRecording()
    }

    fun playPreview() {
        audioRecorder.playRecordingPreview()
    }

    fun stopPreview() {
        audioRecorder.stopPlayingRecordingPreview()
    }

    fun saveRecording() {
        stopPreview()
        val newRecording = audioRecorder.saveRecording() ?: return
        recordingsRepository.insertRecording(
            newRecording
        )
        dismissView()
    }

    fun dismissView() {
        dismissEvent.onNext(true)
    }
}