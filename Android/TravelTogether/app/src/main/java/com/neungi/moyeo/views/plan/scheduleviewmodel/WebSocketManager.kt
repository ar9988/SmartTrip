package com.neungi.moyeo.views.plan.scheduleviewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.neungi.domain.model.AddRequest
import com.neungi.domain.model.Route
import com.neungi.domain.model.RouteReceive
import com.neungi.domain.model.ServerEvent
import com.neungi.domain.model.ServerReceive
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import timber.log.Timber

class WebSocketManager {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private var isConnected = false
    private val _events = MutableLiveData<ServerReceive>() // 서버에서 수신한 이벤트
    val events: LiveData<ServerReceive> get() = _events
    private val _routes = MutableLiveData<RouteReceive>()
    val routes: LiveData<RouteReceive> get() = _routes
    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            isConnected = true
            println("WebSocket Connected")
            Log.d("websocket", "onOpen: Connected")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            try {
                // JSON을 먼저 일반적인 형태로 받음
                val gson = Gson()

                // 메시지에 `status` 필드가 있는지 확인하여 구분
                val jsonObject = gson.fromJson(text, JsonObject::class.java)

                // `status` 필드가 있으면, 상태 메시지로 처리
                if (jsonObject.has("status")) {
                    val serverReceive = gson.fromJson(text, ServerReceive::class.java)
                    println("Operation details -> action: ${serverReceive.operation.action}, " +
                            "schedule_id: ${serverReceive.operation.scheduleId}, " +
                            "newPosition: ${serverReceive.operation.positionPath}")
                    _events.postValue(serverReceive)
                }
                // `tripId` 필드가 있으면, 여행 정보 메시지로 처리
                else if (jsonObject.has("tripId")) {
                    val info = gson.fromJson(text, RouteReceive::class.java)
                    println("Received trip details: tripId: ${info.tripId}")
                    info.routes.forEach { route ->
                        println("Public Transport Duration: ${route.publicTransport.duration}")
                        println("Personal Vehicle Duration: ${route.personalVehicle.duration}")
                    }
                    _routes.postValue(info)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Failed to parse JSON: $text")
            }
        }



        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            super.onMessage(webSocket, bytes)
            println("Received binary message: ${bytes.hex()}")
            // Handle received binary message here
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)
            println("WebSocket Closing: Code=$code, Reason=$reason")
            webSocket.close(1000, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            isConnected = false
            println("WebSocket Closed: Code=$code, Reason=$reason")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            isConnected = false
            Timber.d(t)
            Timber.d(response.toString())
            println("WebSocket Error: ${t.message}")
        }
    }

    fun connect(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()

        webSocket = client.newWebSocket(request, webSocketListener)
    }

    fun sendMessage(serverEvent: ServerEvent) {
        if (isConnected) {
            try {
                // ServerEvent 객체를 JSON 문자열로 변환
                val gson = Gson()
                val message = gson.toJson(serverEvent)

                // WebSocket으로 메시지 전송
                webSocket?.send(message)
                Timber.d("Sent message: $message")
            } catch (e: Exception) {
                e.printStackTrace()
                Timber.d("Failed to send message: ${e.message}")
            }
        } else {
            Timber.d("WebSocket is not connected. Message not sent.")
        }
    }

    fun sendMessage(addRequest: AddRequest) {
        if (isConnected) {
            try {
                // ServerEvent 객체를 JSON 문자열로 변환
                val gson = Gson()
                val message = gson.toJson(addRequest)

                // WebSocket으로 메시지 전송
                webSocket?.send(message)
                Timber.d("Sent message: $message")
            } catch (e: Exception) {
                e.printStackTrace()
                Timber.d("Failed to send message: ${e.message}")
            }
        } else {
            Timber.d("WebSocket is not connected. Message not sent.")
        }
    }

    fun close() {
        webSocket?.close(1000, "Closing WebSocket")
        isConnected = false
    }
}