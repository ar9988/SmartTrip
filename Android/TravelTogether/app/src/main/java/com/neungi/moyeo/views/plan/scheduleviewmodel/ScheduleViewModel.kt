package com.neungi.moyeo.views.plan.scheduleviewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.neungi.domain.model.Operation
import com.neungi.domain.model.Place
import com.neungi.domain.model.ServerEvent
import com.neungi.domain.usecase.GetScheduleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val getScheduleUseCase : GetScheduleUseCase
) : ViewModel() {
    val serverUrl = "ws://43.202.51.112:8080/ws?tripId=1"
    var tripId = 0

    private val _searchResults = MutableLiveData<Place>()
    val searchResut: LiveData<Place> get() = _searchResults
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