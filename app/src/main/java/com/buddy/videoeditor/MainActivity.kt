package com.buddy.videoeditor

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import android.widget.TextView
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var player: ExoPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var tvTimecode: TextView
    private lateinit var btnPlayPause: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var userSeeking = false
    private var clipSelected = false

    private val pickVideo = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { loadVideo(it) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        seekBar = findViewById(R.id.seekBar)
        tvTimecode = findViewById(R.id.tvTimecode)
        btnPlayPause = findViewById(R.id.btnPlayPause)

        player = ExoPlayer.Builder(this).build()
        findViewById<PlayerView>(R.id.playerView).player = player

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                btnPlayPause.text = if (isPlaying) "⏸" else "▶"
            }
        })

        findViewById<TextView>(R.id.toolMedia).setOnClickListener { pickVideo.launch("video/*") }
        btnPlayPause.setOnClickListener {
            if (player.isPlaying) player.pause() else player.play()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) player.seekTo(progress.toLong())
            }
            override fun onStartTrackingTouch(sb: SeekBar?) { userSeeking = true }
            override fun onStopTrackingTouch(sb: SeekBar?) { userSeeking = false }
        })

        findViewById<FrameLayout>(R.id.clipVideoWrapper).setOnClickListener {
            clipSelected = !clipSelected
            it.setBackgroundColor(
                if (clipSelected) resources.getColor(R.color.accent, theme) else android.graphics.Color.TRANSPARENT
            )
        }

        startProgressUpdates()
    }

    private fun startProgressUpdates() {
        handler.post(object : Runnable {
            override fun run() {
                if (player.duration > 0) {
                    seekBar.max = player.duration.toInt()
                    if (!userSeeking) seekBar.progress = player.currentPosition.toInt()
                    tvTimecode.text = "${format(player.currentPosition)} / ${format(player.duration)}"
                }
                handler.postDelayed(this, 250)
            }
        })
    }

    private fun format(ms: Long): String {
        val totalSec = TimeUnit.MILLISECONDS.toSeconds(ms)
        return String.format("%02d:%02d", totalSec / 60, totalSec % 60)
    }

    private fun loadVideo(uri: Uri) {
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }
}
