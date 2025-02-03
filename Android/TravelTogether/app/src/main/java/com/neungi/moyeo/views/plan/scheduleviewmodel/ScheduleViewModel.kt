package com.neungi.moyeo.views.plan.scheduleviewmodel

import androidx.lifecycle.ViewModel
import com.neungi.domain.model.Operation
import com.neungi.domain.model.ServerEvent
import com.neungi.moyeo.views.plan.scheduleviewmodel.websocket.WebSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    //
) : ViewModel() {
    val serverUrl = "ws://43.202.51.112:8080/ws?tripId=1"
    var tripId = 0

    var webSocketManager : WebSocketManager // hilt에서 주입받을 수 있도록 변경

    fun onItemMoved(scheduleId: Int, positionPath: Int) {

        // 서버로 이동 이벤트 전송 (예시)
        sendMoveEventToServer(scheduleId, positionPath)
    }

    private fun sendMoveEventToServer(scheduleId: Int, newPosition: Int) {
        // WebSocketManager 등을 사용해 서버로 이벤트 전송
        webSocketManager.sendMessage(ServerEvent("MOVE123",tripId, Operation("MOVE", scheduleId, newPosition), 111231))
    }

    init {

        webSocketManager = WebSocketManager()
        webSocketManager.connect(serverUrl)
//        webSocketManager.sendMessage("Hello, WebSocket!")

    }
}