package com.android.chat

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.android.chat.models.LogOutModel
import com.android.chat.ui.groups.GroupsActivity
import com.android.chat.ui.groups.GroupsActivityViewModel
import com.google.gson.Gson
import io.socket.client.IO
import java.text.SimpleDateFormat
import java.util.*

class MyService : Service() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
         super.onStartCommand(intent, flags, startId)

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        val _id = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE).getString("_id", "Empty")!!
        val socketIO = IO.socket("http://192.168.0.107:3000")
        val time = Date().time
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm").format(time)
        val logOutModel = LogOutModel(_id, sdf)
        socketIO.connect()
        socketIO.emit(Constants.LOG_OUT_EVENT, Gson().toJson(logOutModel))


    }
}