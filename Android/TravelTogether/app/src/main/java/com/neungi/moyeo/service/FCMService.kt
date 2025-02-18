package com.neungi.moyeo.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.neungi.domain.model.Notification
import com.neungi.domain.usecase.SaveNotificationUseCase
import com.neungi.moyeo.MoyeoApplication

import com.neungi.moyeo.R
import com.neungi.moyeo.views.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

class FCMService: FirebaseMessagingService (){

    private val saveNotificationUseCase: SaveNotificationUseCase by lazy {
        (application as MoyeoApplication).saveNotificationUseCase
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // 데이터 메시지 처리
        var tripId: String? = null
        if (remoteMessage.data.isNotEmpty()) {
            Timber.d("FCM Message data payload: ${remoteMessage.data}")
            tripId = remoteMessage.data["tripId"]
            Timber.d("Received tripId: $tripId")
        }

        // 알림 메시지 처리
        remoteMessage.notification?.let {
            Timber.d("FCM Message Notification Body: ${it}")
            createNotification(it.title, it.body, tripId)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("FCM New token: $token")
        sendTokenToServer(token)
    }

    private fun createNotification(title: String?, body: String?, tripId: String? = null) {
        val channelId = "default_notification_channel_id"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        Timber.d("title: $title, tripId: $tripId")

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

        // 알림 클릭 시 실행될 Intent 생성
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            // 여행 상세 화면으로 이동하기 위한 액션 설정
            action = "OPEN_TRIP_DETAIL"
            // tripId가 있는 경우 extra 데이터로 추가
            tripId?.let {
                putExtra("TRIP_ID", it)
            }
        }

        // PendingIntent 생성
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent) // 알림 클릭 시 실행될 PendingIntent 설정

        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notificationBuilder.build()
        )

        title?.let { nonNullTitle ->
            body?.let { nonNullBody ->
                CoroutineScope(Dispatchers.IO).launch {
                    // Notification 모델에 tripId 필드가 있다면 아래와 같이 저장
                    // 없다면 Notification 모델 클래스 수정 필요
                    saveNotificationUseCase.saveNotifiacation(
                        Notification.create(
                            title = nonNullTitle,
                            body = nonNullBody
                        )
                    )
                    NotificationEventBus.emitNotificationReceived()
                }
            }
        }
    }

    private fun sendTokenToServer(token: String) {
    }
}

object NotificationEventBus {
    private val _notificationReceived = MutableSharedFlow<Unit>()
    val notificationReceived = _notificationReceived.asSharedFlow()

    suspend fun emitNotificationReceived() {
        _notificationReceived.emit(Unit)
    }
}