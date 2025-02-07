package com.neungi.moyeo.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

import com.neungi.moyeo.R
import timber.log.Timber

class FCMService: FirebaseMessagingService(){
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // 데이터 메시지 처리
        remoteMessage.data.isNotEmpty().let {
            Timber.d("FCM Message data payload: ${remoteMessage.data}")
        }

        // 알림 메시지 처리
        remoteMessage.notification?.let {
            Timber.d("FCM Message Notification Body: ${it.body}")
            createNotification(it.title, it.body)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d( "FCM New token: $token")
        sendTokenToServer(token)
    }

    private fun createNotification(title: String?, body: String?) {
        val channelId = "default_notification_channel_id"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "기본 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "기본 알림 채널"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notificationBuilder.build()
        )
    }

    private fun sendTokenToServer(token: String) {
        // 서버에 토큰 전송 구현
    }
}