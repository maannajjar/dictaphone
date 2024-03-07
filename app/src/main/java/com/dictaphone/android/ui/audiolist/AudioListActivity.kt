package com.dictaphone.android.ui.audiolist

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dictaphone.android.databinding.ActivityAudioListBinding
import com.dictaphone.android.ui.recorder.AudioRecorderActivity
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo

/**
 * Launcher activity (View) which displays list of all existing recording made by the user.
 * It binds [AudioListViewModel] state to update he UI
 */
@AndroidEntryPoint
class AudioListActivity : AppCompatActivity() {

    // Get a reference to the ViewModel scoped to its Activity
    private val viewModel: AudioListViewModel by viewModels()
    private lateinit var viewBinding: ActivityAudioListBinding

    private val disposables = CompositeDisposable()
    private val audioListAdapter = AudioListAdapter().apply {
        onItemClick = { recording ->
            viewModel.playRecording(recording)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityAudioListBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.newRecording.setOnClickListener {
            startActivity(Intent(this, AudioRecorderActivity::class.java))
            viewModel.pausePlaying()
        }

        viewBinding.recordingsList.adapter = audioListAdapter

        // Subscribe to our view model for an UI updates event
        viewModel.uiState.subscribe {
            bindUiState(it)
        }.addTo(disposables)
    }

    /**
     * This method ensures the UI is rendered up to date with UiState provided by the view model
     */
    private fun bindUiState(viewState: AudioListViewModel.UiState) {
        audioListAdapter.updateData(viewState.recordingsList, viewState.audioPlayerState)
        if (viewState.recordingsList.isEmpty()) {
            viewBinding.emptyView.visibility = View.VISIBLE
        } else {
            viewBinding.emptyView.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }
}