package com.dictaphone.android.service

import android.app.Application
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import com.dictaphone.android.data.model.SavedRecording
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date
import javax.inject.Inject


/**
 * Basic implementation which provides the functionality to capture audio input from user
 * It maintains its own state allowing other components to subscribe to it for UI/Business logic
 */
class AudioRecorder @Inject constructor(
    val application: Application,
    val audioPlayer: AudioPlayer
) {

    private var recorder: MediaRecorder? = null
    private val fileName = "${application.externalCacheDir?.absolutePath}/temp.3gp"
    private var player: MediaPlayer? = null

    sealed class RecorderState {
        data class Idle(val recordingComplete: Boolean = false) : RecorderState()
        data object Recording : RecorderState()
        data object Playing : RecorderState()
    }

    private val recorderStateSubject = BehaviorSubject.createDefault<RecorderState>(RecorderState.Idle())
    val recorderState: Observable<RecorderState> = recorderStateSubject


    /**
     * This is invoked to start capture user audio recording
     * It will also stop any existing playback of previous recording preview (if any)
     * If the AudioRecorder is already recording an audio, this method will be no-op
     */
    fun startRecording() {
        if (recorderStateSubject.value is RecorderState.Playing) {
            stopPlayingRecordingPreview()
        }
        if (recorderStateSubject.value !is RecorderState.Idle) {
            return
        }
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                //TODO: Handle error
            }
            start()
        }
        recorderStateSubject.onNext(RecorderState.Recording)
    }

    /**
     * This is invoked to stop capture user audio recording
     * It will also stop any existing playback of previous recording preview (if any)
     * If the AudioRecorder is not recording an audio, this method will be no-op
     */
    fun stopRecording() {
        if (recorderStateSubject.value !is RecorderState.Recording) {
            return
        }

        recorder?.apply {
            stop()
            release()
        }
        recorderStateSubject.onNext(RecorderState.Idle(recordingComplete = true))
        recorder = null
    }

    /**
     * This plays a preview of the audio which was just recorded using this AudioRecorder
     * If the AudioRecorder was in IDLE state or didn't finsh recording, it will be no-op
     */
    fun playRecordingPreview() {
        val currentState = recorderStateSubject.value
        if (currentState !is RecorderState.Idle || !currentState.recordingComplete) {
            return
        }

        player = MediaPlayer().apply {
            try {
                setDataSource(fileName)
                prepare()
                start()
                setOnCompletionListener {
                    recorderStateSubject.onNext(RecorderState.Idle(true))
                }
                recorderStateSubject.onNext(RecorderState.Playing)
            } catch (e: IOException) {
                // TODO: handle errors
            }
        }

    }

    /**
     * This will stop playing audio preview of the recording which was just captured by the [AudioRecorder]
     * If the AudioRecorder wasn't actually playing a preview, it will be no-op
     */
    fun stopPlayingRecordingPreview() {
        if (recorderStateSubject.value !is RecorderState.Playing) {
            return
        }
        player?.release()
        player = null
        recorderStateSubject.onNext(RecorderState.Idle(true))
    }


    /**
     * This will save the audio file of the recording which was just made by the user
     * If there was no recording ever mode, it will be no-op
     * returns the full path of the recorded audio file
     */
    fun saveRecording(): SavedRecording? {
        val currentState = recorderStateSubject.value
        if ((currentState !is RecorderState.Idle || !currentState
                .recordingComplete) && currentState !is RecorderState.Playing
        ) {
            return null
        }
        val uri = Uri.parse(fileName)
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(application, uri)
        val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val millSecond = durationStr!!.toInt()

        val target = "${application.dataDir.absolutePath}/${System.currentTimeMillis()}.3gp"
        copy(File(fileName), File(target))
        return SavedRecording(
            Date().time,
            millSecond.toLong()*1000L,
            target,
            false
        )
    }

    /**
     * convenient method to copy audio file
     */
    @Throws(IOException::class)
    private fun copy(src: File?, dst: File?) {
        FileInputStream(src).use { `in` ->
            FileOutputStream(dst).use { out ->
                // Transfer bytes from in to out
                val buf = ByteArray(1024)
                var len: Int
                while (`in`.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
            }
        }
    }


}