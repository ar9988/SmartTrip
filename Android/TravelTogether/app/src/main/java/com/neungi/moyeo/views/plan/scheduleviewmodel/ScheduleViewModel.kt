package com.neungi.moyeo.views.plan.scheduleviewmodel

import ScheduleReceive
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.neungi.data.entity.AddEvent
import com.neungi.data.entity.PathReceive
import com.neungi.data.entity.ScheduleEntity
import com.neungi.data.entity.ServerEvent
import com.neungi.data.entity.ServerReceive
import com.neungi.domain.model.*
import com.neungi.domain.usecase.GetScheduleUseCase
import com.neungi.moyeo.util.Section
import com.neungi.moyeo.util.convertToSections
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val getScheduleUseCase: GetScheduleUseCase,
    private val webSocketManager: WebSocketManager
) : ViewModel() {

    private val serverUrl = "ws://43.202.51.112:8080/ws?tripId="
    lateinit var trip : Trip

    // 이벤트에 대한 LiveData를 ViewModel에서 관리
    private val _serverEvents = MutableLiveData<ServerReceive>()
    val serverEvents: LiveData<ServerReceive> get() = _serverEvents

    private val _pathEvents = MutableLiveData<PathReceive>()
    val pathEvent: LiveData<PathReceive> get() = _pathEvents

    private val _scheduleSections = MutableLiveData<List<Section>>()
    val scheduleSections: LiveData<List<Section>> get() = _scheduleSections


    private val _addEvent = MutableLiveData<AddEvent>()
    val addEvent: LiveData<AddEvent> get() = _addEvent

    init {
        webSocketManager.onServerEventReceived = { event :ServerReceive->
            _serverEvents.postValue(event)
        }

        webSocketManager.onRouteEventReceived = { pathReceive :PathReceive->
            _pathEvents.postValue(pathReceive)
        }

        webSocketManager.onScheduleEventReceived = { scheduleReceive:ScheduleReceive ->
            val sections = convertToSections(scheduleReceive,trip)
            _scheduleSections.postValue(sections)
        }

        webSocketManager.onAddEventReceived = { addEvent:AddEvent ->
            _addEvent.postValue(addEvent)
        }
    }

    // WebSocket을 통한 메시지 전송
    fun sendMoveEvent(scheduleId: Int, newPosition: Int) {
        val event = ServerEvent("MOVE123", trip.id, Operation("MOVE", scheduleId, newPosition), 111231)
        webSocketManager.sendMessage(event)
    }

    fun sendDeleteEvent(scheduleId: Int) {
        val event = ServerEvent("MOVE123", trip.id, Operation("DELETE", scheduleId, 0), 111231)
        webSocketManager.sendMessage(event)
    }


    fun sendAddEvent(schedule: ScheduleEntity) {
        webSocketManager.sendMessage(AddEvent(
            tripId = schedule.tripId,
            dayId = schedule.day,
            schedule = schedule
        ))
    }

    fun startEvent() {
        val event = ServerEvent("MOVE123", trip.id, Operation("START", 0, 0), 111231)
//        webSocketManager.sendMessage(event)
    }


    // WebSocket 종료
    fun closeWebSocket() {
        webSocketManager.close()
    }

    fun startConnect() {

        webSocketManager.connect(serverUrl+trip.id)
        webSocketManager.tripId = trip.id
        startEvent()
        Timber.d(serverUrl+trip.id)
    }


}
