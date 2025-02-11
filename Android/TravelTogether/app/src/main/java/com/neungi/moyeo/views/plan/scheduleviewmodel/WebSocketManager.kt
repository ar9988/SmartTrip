package com.neungi.moyeo.views.plan.scheduleviewmodel

import ScheduleReceive
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.neungi.data.entity.ManipulationEvent
import com.neungi.data.entity.PathReceive
import com.neungi.data.entity.ServerReceive
import com.neungi.moyeo.views.plan.adapter.LocalDateTimeAdapter
import kotlinx.coroutines.CompletableDeferred
import okhttp3.*
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

class WebSocketManager @Inject constructor() {

    var tripId:Int = 0
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    var isConnected = false
    // 각 이벤트에 대한 콜백
    var onServerEventReceived: ((ServerReceive) -> Unit)? = null
    var onRouteEventReceived: ((PathReceive) -> Unit)? = null
    var onScheduleEventReceived: ((ScheduleReceive) -> Unit)? = null
    var onAddEventReceived: ((ManipulationEvent) -> Unit)? = null
    private val connectionDeferred = CompletableDeferred<Unit>() // Deferred object to track connection status.

    private val webSocketListener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            Timber.tag("WebSocket").d("onOpen: Connected")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            try {
                val gson = GsonBuilder()
                    .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
                    .setPrettyPrinting()
                    .serializeNulls()
                    .create()

                // 먼저 String으로 들어온 JSON이 유효한지 체크
                if (text == "SUCCESS") {
                    return
                }
                val jsonObject = JsonParser.parseString(text).asJsonObject

                when {

                    jsonObject.has("status") -> {
                        val serverReceive = gson.fromJson(text, ServerReceive::class.java)
//                        Timber.d(serverReceive.toString())
                        onServerEventReceived?.invoke(serverReceive)
                    }
                    jsonObject.has("title") -> {
                        isConnected = true
                        val scheduleReceive = gson.fromJson(text, ScheduleReceive::class.java)
                        Timber.d(scheduleReceive.toString())
                        onScheduleEventReceived?.invoke(scheduleReceive)
                    }
                    jsonObject.has("paths") -> {
                        val routeReceive = gson.fromJson(text, PathReceive::class.java)
//                        Timber.d(routeReceive.toString())
                        onRouteEventReceived?.invoke(routeReceive)
                    }
                    jsonObject.has("action") -> {
                        val add = gson.fromJson(text, ManipulationEvent::class.java)
                        onAddEventReceived?.invoke(add)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse message: $text")
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

    suspend fun sendMessage(message: Any) {
        if (!isConnected) {
            // Suspend until the WebSocket is connected
            connectionDeferred.await()
        }

        if (isConnected) {
            val gson = Gson()
            val jsonMessage = gson.toJson(message)
            Timber.d("Sending message: $jsonMessage")
            webSocket?.send(jsonMessage)
        } else {
            Timber.d("WebSocket is not connected.")
        }
    }

    fun close() {
        Timber.tag("WebSocket").d("close")
        webSocket?.close(1000, "Closing WebSocket")
    }
}
