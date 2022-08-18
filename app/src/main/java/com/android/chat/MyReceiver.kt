package com.android.chat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class MyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        Toast.makeText(context, "hdhdhhdhd", Toast.LENGTH_SHORT).show()
        Log.e("onReceive","jddddddddddddd")
     }
}