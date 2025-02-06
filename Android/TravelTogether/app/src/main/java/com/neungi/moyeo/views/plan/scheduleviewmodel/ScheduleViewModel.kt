package com.neungi.moyeo.views.plan.scheduleviewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.neungi.data.entity.RouteReceive
import com.neungi.data.entity.ServerEvent
import com.neungi.data.entity.ServerReceive
import com.neungi.domain.model.*
import com.neungi.domain.usecase.GetScheduleUseCase
import com.neungi.moyeo.util.Section
import com.neungi.moyeo.util.convertToSections
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val getScheduleUseCase: GetScheduleUseCase,
    private val webSocketManager: WebSocketManager
) : ViewModel() {

    val serverUrl = "ws://43.202.51.112:8080/ws?tripId="
    var tripId = 0

    // 이벤트에 대한 LiveData를 ViewModel에서 관리
    private val _serverEvents = MutableLiveData<ServerReceive>()
    val serverEvents: LiveData<ServerReceive> get() = _serverEvents

    private val _routeEvents = MutableLiveData<RouteReceive>()
    val routeEvents: LiveData<RouteReceive> get() = _routeEvents

    private val _scheduleSections = MutableLiveData<List<Section>>()
    val scheduleSections: LiveData<List<Section>> get() = _scheduleSections

    init {
        // WebSocket 연결
        webSocketManager.connect(serverUrl+tripId)

        // WebSocket으로부터 받은 이벤트를 ViewModel에 전달
        webSocketManager.onServerEventReceived = { event ->
            _serverEvents.postValue(event)
        }

        webSocketManager.onRouteEventReceived = { routeReceive ->
            _routeEvents.postValue(routeReceive)
        }

        webSocketManager.onScheduleEventReceived = { scheduleReceive ->
            val sections = convertToSections(scheduleReceive)
            _scheduleSections.postValue(sections)
        }
    }

    // WebSocket을 통한 메시지 전송
    fun sendMoveEvent(scheduleId: Int, newPosition: Int) {
        val event = ServerEvent("MOVE123", tripId, Operation("MOVE", scheduleId, newPosition), 111231)
        webSocketManager.sendMessage(event)
    }

    fun sendDeleteEvent(scheduleId: Int) {
        val event = ServerEvent("MOVE123", tripId, Operation("DELETE", scheduleId, 0), 111231)
        webSocketManager.sendMessage(event)
    }

    // WebSocket 종료
    fun closeWebSocket() {
        webSocketManager.close()
    }
}
