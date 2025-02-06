package com.neungi.moyeo.views.plan.scheduleviewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.neungi.data.entity.RouteReceive
import com.neungi.data.entity.ServerReceive
import com.neungi.data.entity.ScheduleReceive
import com.neungi.domain.model.*
import okhttp3.*
import okio.ByteString
import timber.log.Timber
import javax.inject.Inject

class WebSocketManager @Inject constructor() {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private var isConnected = false

    // 각 이벤트에 대한 콜백
    var onServerEventReceived: ((ServerReceive) -> Unit)? = null
    var onRouteEventReceived: ((RouteReceive) -> Unit)? = null
    var onScheduleEventReceived: ((ScheduleReceive) -> Unit)? = null

    private val webSocketListener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            isConnected = true
            Log.d("WebSocket", "onOpen: Connected")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            try {
                val gson = Gson()
                val jsonObject = gson.fromJson(text, JsonObject::class.java)

                // 서버 이벤트
                if (jsonObject.has("status")) {
                    val serverReceive = gson.fromJson(text, ServerReceive::class.java)
                    onServerEventReceived?.invoke(serverReceive)
                }
                // 일정 정보
                else if (jsonObject.has("title")) {
                    val scheduleReceive = gson.fromJson(text, ScheduleReceive::class.java)
                    onScheduleEventReceived?.invoke(scheduleReceive)
                }
                // 여행 경로 정보
                else if (jsonObject.has("tripId")) {
                    val routeReceive = gson.fromJson(text, RouteReceive::class.java)
                    onRouteEventReceived?.invoke(routeReceive)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("WebSocket", "Failed to parse message: $text")
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            isConnected = false
            Timber.e(t, "WebSocket Error: ${t.message}")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            isConnected = false
            Log.d("WebSocket", "onClosed: Code=$code, Reason=$reason")
        }
    }

    fun connect(url: String) {
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, webSocketListener)
    }

    fun sendMessage(message: Any) {
        if (isConnected) {
            val gson = Gson()
            val jsonMessage = gson.toJson(message)
            webSocket?.send(jsonMessage)
        } else {
            Timber.d("WebSocket is not connected.")
        }
    }

    fun close() {
        webSocket?.close(1000, "Closing WebSocket")
    }
}
