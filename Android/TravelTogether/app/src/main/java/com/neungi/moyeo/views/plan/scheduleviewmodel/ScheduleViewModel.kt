package com.neungi.moyeo.views.plan.scheduleviewmodel

import ScheduleReceive
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neungi.data.entity.ManipulationEvent
import com.neungi.data.entity.PathReceive
import com.neungi.data.entity.ScheduleEntity
import com.neungi.data.entity.ServerEvent
import com.neungi.data.entity.ServerReceive
import com.neungi.domain.model.*
import com.neungi.domain.usecase.GetScheduleUseCase
import com.neungi.moyeo.util.Section
import com.neungi.moyeo.util.convertToSections
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val getScheduleUseCase: GetScheduleUseCase,
    val webSocketManager: WebSocketManager
) : ViewModel() {

    private val serverUrl = "ws://43.202.51.112:8080/ws?tripId="
    lateinit var trip: Trip

    // 이벤트에 대한 LiveData를 ViewModel에서 관리
    private val _serverEvents = MutableLiveData<ServerReceive>()
    val serverEvents: LiveData<ServerReceive> get() = _serverEvents

    private val _pathEvents = MutableLiveData<PathReceive>()
    val pathEvent: LiveData<PathReceive> get() = _pathEvents

    private val _scheduleSections = MutableLiveData<List<Section>>()
    val scheduleSections: LiveData<List<Section>> get() = _scheduleSections


    private val _manipulationEvent = MutableLiveData<ManipulationEvent>()
    val manipulationEvent: LiveData<ManipulationEvent> get() = _manipulationEvent

    init {
        webSocketManager.onServerEventReceived = { event: ServerReceive ->
            _serverEvents.postValue(event)
        }

        webSocketManager.onRouteEventReceived = { pathReceive: PathReceive ->
            _pathEvents.postValue(pathReceive)
        }

        webSocketManager.onScheduleEventReceived = { scheduleReceive: ScheduleReceive ->
            val sections = convertToSections(scheduleReceive, trip)
            _scheduleSections.postValue(sections)
        }

        webSocketManager.onAddEventReceived = { manipulationEvent: ManipulationEvent ->
            _manipulationEvent.postValue(manipulationEvent)
        }
    }

    // WebSocket을 통한 메시지 전송
    fun sendMoveEvent(scheduleId: Int, newPosition: Int) {
        val event =
            ServerEvent("MOVE123", trip.id, Operation("MOVE", scheduleId, newPosition), 111231)
        viewModelScope.launch {
            webSocketManager.sendMessage(event)
        }
    }

    fun sendDeleteEvent(scheduleId: Int) {
        val event = ServerEvent("MOVE123", trip.id, Operation("DELETE", scheduleId, 0), 111231)
        viewModelScope.launch {
            webSocketManager.sendMessage(event)
        }
    }

    fun sendEditEvent(schedule: ScheduleEntity) {
        viewModelScope.launch {
            webSocketManager.sendMessage(
                ManipulationEvent(
                    action = "EDIT",
                    tripId = schedule.tripId,
                    dayId = schedule.day,
                    timeStamp = 0,
                    schedule = schedule
                )
            )
        }

    }

    fun sendAddEvent(schedule: ScheduleEntity) {
        Timber.d(schedule.toString())
        viewModelScope.launch {
            webSocketManager.sendMessage(
                ManipulationEvent(
                    action = "ADD",
                    tripId = schedule.tripId,
                    dayId = schedule.day,
                    timeStamp = 0,
                    schedule = schedule
                )
            )
        }
    }

//    fun startEvent() {
//        val event = ServerEvent("MOVE123", trip.id, Operation("START", 0, 0), 111231)
////        webSocketManager.sendMessage(event)
//    }


    fun startConnect() {
        webSocketManager.connect(serverUrl + trip.id)
        webSocketManager.tripId = trip.id
        Timber.d(serverUrl + trip.id)
    }


}
