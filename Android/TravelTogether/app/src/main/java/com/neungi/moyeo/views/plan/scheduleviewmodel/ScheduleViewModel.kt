package com.neungi.moyeo.views.plan.scheduleviewmodel

import ScheduleReceive
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.neungi.data.entity.PathReceive
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

    val serverUrl = "ws://43.202.51.112:8080/ws?tripId="
    lateinit var trip : Trip

    // 이벤트에 대한 LiveData를 ViewModel에서 관리
    private val _serverEvents = MutableLiveData<ServerReceive>()
    val serverEvents: LiveData<ServerReceive> get() = _serverEvents

    private val _pathEvents = MutableLiveData<Path>()
    val pathEvent: LiveData<Path> get() = _pathEvents

    private val _scheduleSections = MutableLiveData<List<Section>>()
    val scheduleSections: LiveData<List<Section>> get() = _scheduleSections

    private val pathQueue = ArrayDeque<Path>()  // 경로를 처리할 큐
    private var isProcessingPath = false  // 경로 처리 중 여부

    init {
//        webSocketManager.tripId = trip.id
        // WebSocket으로부터 받은 이벤트를 ViewModel에 전달
        webSocketManager.onServerEventReceived = { event :ServerReceive->
            _serverEvents.postValue(event)
        }

        webSocketManager.onRouteEventReceived = { pathReceive :PathReceive->
            pathReceive.paths.forEach {
//                Timber.d(it.totalTime.toString())
                addPathToQueue(it)
//                _pathEvents.postValue(it)
            }
        }

        webSocketManager.onScheduleEventReceived = { scheduleReceive:ScheduleReceive ->
            val sections = convertToSections(scheduleReceive,trip)
            _scheduleSections.postValue(sections)
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

    fun startEvent() {
        val event = ServerEvent("MOVE123", trip.id, Operation("START", 0, 0), 111231)
        webSocketManager.sendMessage(event)
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

    // 경로를 큐에 추가하고 처리
    private fun addPathToQueue(path: Path) {
        pathQueue.add(path)  // 큐에 경로 추가
        processNextPath()  // 큐에 경로가 추가될 때마다 처리 시작
    }

    // 큐에 있는 경로를 하나씩 처리
    private fun processNextPath() {
        if (isProcessingPath || pathQueue.isEmpty()) {
            return  // 경로를 처리 중이거나 큐가 비어있으면 리턴
        }

        val path = pathQueue.removeFirst()  // 큐에서 첫 번째 경로를 꺼냄
        isProcessingPath = true  // 경로 처리 중

        Timber.d("Processing path with scheduleId: ${path.sourceScheduleId}")

        // 경로를 처리하는 로직 (예: 맵에 그리기 등)
        _pathEvents.postValue(path)  // 경로 이벤트 업데이트

        // 경로 처리가 끝난 후, 처리 플래그를 리셋하고 다음 경로를 처리
        isProcessingPath = false
        processNextPath()  // 다음 경로를 처리
    }

}
