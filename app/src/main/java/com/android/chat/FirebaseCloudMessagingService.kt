package com.android.chat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.android.chat.ui.groups.GroupsActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class FirebaseCloudMessagingService: FirebaseMessagingService() {

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        Log.e("FirebaseCloudMessagingService", p0.messageId!!)
        val intent = Intent(this, GroupsActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val chanel = NotificationChannel("123","Messages", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(chanel)
            val notificationCompat = NotificationCompat.Builder(this,"123")
            notificationCompat.setSmallIcon(R.mipmap.ic_launcher)
            notificationCompat.setStyle(NotificationCompat.BigTextStyle())
            notificationCompat.setLargeIcon(
                BitmapFactory.decodeResource(
                    Resources.getSystem(),
                R.mipmap.ic_launcher
            ))
            notificationCompat.setContentText(p0.notification!!.body)
            notificationCompat.setDefaults(NotificationCompat.DEFAULT_SOUND)
            notificationCompat.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            notificationCompat.setContentTitle(p0.notification!!.title)
            notificationCompat.setContentIntent(pendingIntent)
            notificationManager.notify(123, notificationCompat.build())
        }else{
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationCompat = NotificationCompat.Builder(this)
            notificationCompat.setSmallIcon(R.mipmap.ic_launcher)
            notificationCompat.setContentText(p0.notification!!.body)
            notificationCompat.setContentTitle(p0.notification!!.title)
            notificationCompat.setStyle(NotificationCompat.BigTextStyle().bigText(p0.notification!!.body))
            notificationCompat.setDefaults(NotificationCompat.DEFAULT_SOUND)
            notificationCompat.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            notificationCompat.setLargeIcon(BitmapFactory.decodeResource(Resources.getSystem(),
                R.mipmap.ic_launcher
            ))
            notificationCompat.setContentIntent(pendingIntent)
            notificationManager.notify(123, notificationCompat.build())

        }
    }

}