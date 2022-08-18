package com.android.chat.ui

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.android.chat.Constants
import com.android.chat.databinding.ActivityPlayVideoBinding
import com.android.chat.ui.chat.ChatActivityViewModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import io.socket.client.IO

class PlayVideo : AppCompatActivity() {

    lateinit var url :String
    private val socketIO = IO.socket(Constants.SOCKETIO_URL)
    lateinit var player:ExoPlayer
    lateinit var playerView: PlayerView
    lateinit var customViewModel: ChatActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityPlayVideoBinding = ActivityPlayVideoBinding.inflate(layoutInflater)
            setContentView(binding.root)

        customViewModel = ViewModelProviders.of(this)[ChatActivityViewModel::class.java]
        customViewModel.connectSocket(socketIO)

        url = intent.getStringExtra("videoUrl")!!

        binding.chatItemBack.setOnClickListener {
            finish()
        }

        val trackSelector = DefaultTrackSelector(this).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }
        val currentUser = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE).getString("_id", "Empty")!!

        playerView = binding.playerView
        player = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build()
        playerView.player = player
        player.addMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.play()

        player.addListener(object :Player.Listener{
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)

                runOnUiThread {
                    binding.progressLoadingVideo.visibility = View.GONE
                }

            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                player.prepare()
                player.play()
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()

        player.stop()
        player.release()
    }

    override fun onResume() {
        super.onResume()
        val sh = getSharedPreferences("onlineState", Context.MODE_PRIVATE).edit()
        sh.putBoolean("onlineState", false)
        sh.apply()
        val currentUser = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE).getString("_id", "Empty")!!
        customViewModel.joinChat(currentUser)
    }

    override fun onPause() {
        super.onPause()
        val sh = getSharedPreferences("onlineState", Context.MODE_PRIVATE).edit()
        sh.putBoolean("onlineState", true)
        sh.apply()
    }

    override fun onStop() {
        super.onStop()

        Handler().postDelayed({
            val sh = getSharedPreferences("onlineState", Context.MODE_PRIVATE).getBoolean("onlineState", true)
            if (sh){
                val currentUser = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE).getString("_id", "Empty")!!
                customViewModel.MethodLogOutMutableLiveData(currentUser)
            }

        }, 7000)
    }

}
