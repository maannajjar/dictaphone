package com.dictaphone.android.ui.recorder

import android.Manifest
import android.animation.Animator
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.dictaphone.android.R
import com.dictaphone.android.databinding.ActivityAudioRecorderBinding
import com.dictaphone.android.extensions.dp
import com.dictaphone.android.extensions.revealAnimateWhenReady
import com.dictaphone.android.extensions.revealAnimation
import com.dictaphone.android.extensions.reverseRevealAnimation
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo


private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

/**
 * Audio Recorder (View) which enables user to record and save audio files into the app.
 * It binds [AudioRecorderViewModel] state to update he UI and trigger user events
 */
@AndroidEntryPoint
class AudioRecorderActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityAudioRecorderBinding
    private var permissionToRecordAccepted = false

    private val viewModel: AudioRecorderViewModel by viewModels()
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityAudioRecorderBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Used for circular reveal animation
        overridePendingTransition(R.anim.noanim, R.anim.noanim);

        // Request recording permission
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_RECORD_AUDIO_PERMISSION
        )

        viewBinding.back.setOnClickListener { viewModel.dismissView() }
        viewBinding.save.setOnClickListener { viewModel.saveRecording() }

        viewModel.recorderUiSubject.subscribe { state ->
            handleUiStateChange(state)
        }.addTo(disposables)

        revealAnimateWhenReady()
    }

    private fun handleUiStateChange(state: AudioRecorderViewModel.UiState) {

        if (state.isDismissed) {
            dismiss()
            return
        }
        viewBinding.preview.setOnClickListener {
            if (state.isPlayingPreview) {
                viewModel.stopPreview()
            } else {
                viewModel.playPreview()
            }
        }
        if (state.isPlayingPreview) {
            viewBinding.preview.setImageResource(R.drawable.stop)
        } else {
            viewBinding.preview.setImageResource(android.R.drawable.ic_media_play)
        }

        if (state.previewAvailable || state.isPlayingPreview) {
            viewBinding.preview.visibility = View.VISIBLE
            viewBinding.previewLabel.visibility = View.VISIBLE
            viewBinding.save.visibility = View.VISIBLE
            viewBinding.saveLabel.visibility = View.VISIBLE
        } else {
            viewBinding.preview.visibility = View.GONE
            viewBinding.save.visibility = View.GONE
            viewBinding.previewLabel.visibility = View.GONE
            viewBinding.saveLabel.visibility = View.GONE


        }

        if (state.isRecording) {
            viewBinding.animationView.visibility = View.VISIBLE
            viewBinding.instructions.visibility = View.GONE
            viewBinding.record.setImageResource(R.drawable.stop)
        } else {
            viewBinding.animationView.visibility = View.GONE
            viewBinding.instructions.visibility = View.VISIBLE
            viewBinding.record.setImageResource(R.drawable.mic)
        }
        if (state.previewAvailable) {
            viewBinding.instructions.text =
                "Tap Play to preview your recording\n" +
                        "Tap Record button to " +
                        "re-record\n" + "Tape Save to save the recording"
        } else if (state.isPlayingPreview) {
            viewBinding.instructions.text = "Playing preview"
        } else {
            viewBinding.instructions.text = "Tap the record button to start recording"
        }

        viewBinding.record.setOnClickListener {
            if (state.isRecording) {
                viewModel.stopRecord()
            } else {
                viewModel.doRecord()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    private fun dismiss() {
        reverseRevealAnimation()
    }

    override fun onBackPressed() {
        dismiss()
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted =
            if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            } else {
                false
            }
        if (!permissionToRecordAccepted) finish()
    }

}