package com.buddy.videoeditor

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.ui.PlayerView

class MainActivity : AppCompatActivity() {
    private lateinit var player: ExoPlayer

    private val pickVideo = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { loadVideo(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        player = ExoPlayer.Builder(this).build()
        findViewById<PlayerView>(R.id.playerView).player = player

        findViewById<android.widget.TextView>(R.id.toolMedia).setOnClickListener {
            pickVideo.launch("video/*")
        }

        findViewById<android.widget.TextView>(R.id.btnPlayPause).setOnClickListener {
            if (player.isPlaying) player.pause() else player.play()
        }
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
