package com.neungi.moyeo.views.plan.scheduleviewmodel.websocket

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import javax.inject.Inject

class WebSocketManager {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private var isConnected = false
    private val _events = MutableLiveData<ServerReceive>() // 서버에서 수신한 이벤트
    val events: LiveData<ServerReceive> get() = _events

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            isConnected = true
            println("WebSocket Connected")
            Log.d("websocket", "onOpen: Connected")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            println("Received raw message: $text")
            try {
                println("Received JSON: $text")

                // JSON을 ServerEvent 객체로 변환
                val gson = Gson()
                val ServerReceive = gson.fromJson(text, ServerReceive::class.java)

                // 변환된 데이터 출력
                println("Parsed ServerEvent -> tripId: ${ServerReceive.tripId}, timestamp: ${ServerReceive.timestamp}")
                println("Operation -> action: ${ServerReceive.operation.action}, " +
                        "schedule_id: ${ServerReceive.operation.scheduleId}, " +
                        "fromPosition: ${ServerReceive.operation.fromPosition}, " +
                        "toPosition: ${ServerReceive.operation.toPosition}")

                _events.postValue(ServerReceive)
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
                println("Sent message: $message")
            } catch (e: Exception) {
                e.printStackTrace()
                println("Failed to send message: ${e.message}")
            }
        } else {
            println("WebSocket is not connected. Message not sent.")
        }
    }

    fun close() {
        webSocket?.close(1000, "Closing WebSocket")
        isConnected = false
    }
}