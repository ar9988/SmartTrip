package com.neungi.moyeo.views.plan.scheduleviewmodel

import androidx.lifecycle.ViewModel
import com.neungi.moyeo.views.plan.scheduleviewmodel.websocket.Operation
import com.neungi.moyeo.views.plan.scheduleviewmodel.websocket.ServerEvent
import com.neungi.moyeo.views.plan.scheduleviewmodel.websocket.WebSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    //
) : ViewModel() {
    val serverUrl = "ws://192.168.100.63:8080/ws?tripId=1"
    var tripId = 0

    var webSocketManager : WebSocketManager // hilt에서 주입받을 수 있도록 변경

    fun onItemMoved(fromPosition: Int, toPosition: Int) {

        // 서버로 이동 이벤트 전송 (예시)
        sendMoveEventToServer(fromPosition, toPosition)
    }

    private fun sendMoveEventToServer(fromPosition: Int, toPosition: Int) {
        println("Server notified of move: $fromPosition -> $toPosition")
        // WebSocketManager 등을 사용해 서버로 이벤트 전송
        webSocketManager.sendMessage(ServerEvent(tripId, Operation(0, 0, fromPosition, toPosition), "timestamp"))
    }

    init {

        webSocketManager = WebSocketManager()
        webSocketManager.connect(serverUrl)
//        webSocketManager.sendMessage("Hello, WebSocket!")

    }
}