package com.dictaphone.android.ui.audiolist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dictaphone.android.databinding.ItemRecordingBinding
import com.dictaphone.android.data.model.SavedRecording
import com.dictaphone.android.service.AudioPlayer
import org.joda.time.Period
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.PeriodFormatterBuilder


/**
 * Adapter for the list of [SavedRecording].
 * The adapter also expects state of AudioPlayer in order to appropriately render play/stop button
 */
class AudioListAdapter : RecyclerView.Adapter<AudioListAdapter.RecordingItemViewHolder>() {
    private var itemList: List<SavedRecording> = emptyList()
    private var audioPlayerState: AudioPlayer.State = AudioPlayer.State.Idle
    private val durationFormatter = PeriodFormatterBuilder()
        .printZeroAlways().minimumPrintedDigits(2).appendMinutes()
        .appendSeparator(":")
        .printZeroAlways().minimumPrintedDigits(2).appendSeconds()
        .toFormatter()

    var onItemClick: (SavedRecording) -> Unit = {}

    /**
     * This method is called when Recordings list needs to be updated in the list
     * Additionally, it can be used if the AudioPlayer state has changed so it
     * can probably display Play/Pause button for the audios being played
     */
    fun updateData(
        audioList: List<SavedRecording>,
        playerState: AudioPlayer.State = AudioPlayer.State.Idle
    ) {
        itemList = audioList
        this.audioPlayerState = playerState
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RecordingItemViewHolder(
        ItemRecordingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount() = itemList.size

    override fun onBindViewHolder(holder: RecordingItemViewHolder, position: Int) {
        val recording = itemList[position]
        val currentAudioPlayerState = audioPlayerState

        holder.unplayedCircle.visibility = if (recording.isPlayed) View.GONE else View.VISIBLE
        holder.audioDuration.text = durationFormatter.print(
            Period.millis(recording.recordingLengthMs.toInt() / 1000)
                .normalizedStandard()
        )
        holder.itemLabel.text = DateTimeFormat.mediumDateTime().print(
            recording.recordingTime
        )

        if (!recording.fileExists) {
            holder.audioDuration.text = "This file has been deleted from your storage"
            holder.playButton.isEnabled = false
            holder.unplayedCircle.visibility = View.GONE
            holder.fileError.visibility = View.VISIBLE
            holder.playButton.setImageResource(android.R.drawable.stat_sys_warning)
            return
        }

        holder.playButton.isEnabled = true
        holder.fileError.visibility = View.GONE
        if (currentAudioPlayerState is AudioPlayer.State.PlayingRecording &&
            currentAudioPlayerState.recording?.id == recording.id
        ) {
            // This item is being played currently by AudioPlayer
            holder.playButton.isSelected = true
            holder.playButton.isActivated = true
            holder.playButton.setImageResource(android.R.drawable.ic_media_pause)
        } else if (currentAudioPlayerState is AudioPlayer.State.PausedRecording &&
            currentAudioPlayerState.recording?.id ==  recording.id
        ) {
            // This item was being played currently by AudioPlayer but is currently paused
            holder.playButton.isSelected = true
            holder.playButton.isActivated = false
            holder.playButton.setImageResource(android.R.drawable.ic_media_play)
        } else {
            // This item was is not being played by the audio player
            holder.playButton.setImageResource(android.R.drawable.ic_media_play)
            holder.playButton.isActivated = true
            holder.playButton.isSelected = false
        }

        holder.itemView.setOnClickListener {
            onItemClick(recording)
        }
    }

    class RecordingItemViewHolder(itemView: ItemRecordingBinding) : RecyclerView.ViewHolder(itemView.root) {
        val playButton = itemView.playPause
        val itemLabel: TextView = itemView.itemLabel
        val audioDuration: TextView = itemView.audioDuration
        val unplayedCircle: View = itemView.unplayedCircle
        val fileError = itemView.error
    }

}