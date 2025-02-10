package com.neungi.moyeo.views.plan.scheduleviewmodel

import ScheduleReceive
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.neungi.data.entity.AddEvent
import com.neungi.data.entity.PathReceive
import com.neungi.data.entity.ServerReceive
import com.neungi.data.entity.ServerEvent
import com.neungi.domain.model.*
import com.neungi.moyeo.views.plan.adapter.LocalDateTimeAdapter
import okhttp3.*
import okio.ByteString
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

class WebSocketManager @Inject constructor() {

    var tripId:Int = 0
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private var isConnected = false

    // 각 이벤트에 대한 콜백
    var onServerEventReceived: ((ServerReceive) -> Unit)? = null
    var onRouteEventReceived: ((PathReceive) -> Unit)? = null
    var onScheduleEventReceived: ((ScheduleReceive) -> Unit)? = null
    var onAddEventReceived: ((AddEvent) -> Unit)? = null
    private val webSocketListener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            isConnected = true
            Timber.tag("WebSocket").d("onOpen: Connected")
            val event = ServerEvent("MOVE123", tripId, Operation("START", 0, 0), 111231)
            sendMessage(event)
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
//                    Timber.d("Success")
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
                        val scheduleReceive = gson.fromJson(text, ScheduleReceive::class.java)
//                        Timber.d(scheduleReceive.toString())
                        onScheduleEventReceived?.invoke(scheduleReceive)
                    }
                    jsonObject.has("paths") -> {
                        val routeReceive = gson.fromJson(text, PathReceive::class.java)
//                        Timber.d(routeReceive.toString())
                        onRouteEventReceived?.invoke(routeReceive)
                    }
                    jsonObject.has("action") -> {
                        val add = gson.fromJson(text, AddEvent::class.java)
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
        Timber.tag("WebSocket").d("close")
        webSocket?.close(1000, "Closing WebSocket")
    }
}
