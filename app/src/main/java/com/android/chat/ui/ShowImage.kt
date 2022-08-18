package com.android.chat.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.lifecycle.ViewModelProviders
import com.android.chat.Constants
import com.android.chat.databinding.ActivityShowImageBinding
import com.android.chat.ui.chat.ChatActivityViewModel
import com.squareup.picasso.Picasso
import io.socket.client.IO

class ShowImage : AppCompatActivity() {


    lateinit var customViewModel: ChatActivityViewModel
    private val socketIO = IO.socket(Constants.SOCKETIO_URL)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityShowImageBinding = ActivityShowImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url = intent.getStringExtra("ImageUrl")

        customViewModel = ViewModelProviders.of(this)[ChatActivityViewModel::class.java]
        customViewModel.connectSocket(socketIO)

        Picasso.with(this).load(url).into(binding.imgView)

        binding.chatItemBack.setOnClickListener {
            finish()
        }


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