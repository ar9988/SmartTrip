package com.neungi.moyeo.views.plan.scheduleviewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neungi.data.entity.ManipulationEvent
import com.neungi.data.entity.Member
import com.neungi.data.entity.PathReceive
import com.neungi.data.entity.ScheduleEntity
import com.neungi.data.entity.ScheduleReceive
import com.neungi.data.entity.ServerEvent
import com.neungi.data.entity.ServerReceive
import com.neungi.domain.model.*
import com.neungi.domain.usecase.GetInviteUseCase
import com.neungi.domain.usecase.GetScheduleUseCase
import com.neungi.domain.usecase.GetUserInfoUseCase
import com.neungi.moyeo.util.Section
import com.neungi.moyeo.util.convertToSections
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getInviteUseCase: GetInviteUseCase,
    private val webSocketManager: WebSocketManager
) : ViewModel(), OnScheduleClickListener {

    private val _scheduleUiEvent = MutableSharedFlow<ScheduleUiEvent>()
    val scheduleUiEvent = _scheduleUiEvent.asSharedFlow()

    private val _userName = MutableStateFlow<String?>(null)
    val userName = _userName.asStateFlow()

    private val _userId = MutableStateFlow<String?>(null)
    val userId = _userId.asStateFlow()

    private val serverUrl = "ws://43.202.51.112:8080/ws?tripId="

    private val _selectedTrip = MutableStateFlow<Trip?>(null)
    val selectedTrip = _selectedTrip.asStateFlow()

    // 이벤트에 대한 LiveData를 ViewModel에서 관리
    private val _serverEvents = MutableLiveData<ServerReceive>()
    val serverEvents: LiveData<ServerReceive> get() = _serverEvents

    private val _pathEvents = MutableLiveData<PathReceive>()
    val pathEvent: LiveData<PathReceive> get() = _pathEvents

    private val _scheduleSections = MutableLiveData<List<Section>>()
    val scheduleSections: LiveData<List<Section>> get() = _scheduleSections

    private val _manipulationEvent = MutableLiveData<ScheduleData>()
    val manipulationEvent: LiveData<ScheduleData> get() = _manipulationEvent

    private val _editEvent = MutableLiveData<ManipulationEvent>()
    val editEvent : LiveData<ManipulationEvent> get() = _editEvent


    private val _memberList = MutableLiveData<List<Member>>()
    val memberList: LiveData<List<Member>> get() = _memberList


    init {
        viewModelScope.launch {
            _userName.value = fetchUserName().first()
        }
    }


    override fun onClickGoToInvite() {
        viewModelScope.launch {
            _scheduleUiEvent.emit(ScheduleUiEvent.GoToScheduleInvite)
        }
    }

    override fun onClickInvite(tripId: Int) {
        viewModelScope.launch {
            val response = getInviteUseCase.invite(tripId)
            when (response.status) {
                ApiStatus.SUCCESS -> {
                    _scheduleUiEvent.emit(ScheduleUiEvent.ScheduleInvite(
                        _userName.value ?: "",
                        _selectedTrip.value?.title ?: "",
                        response.data ?: ""
                    ))
                }

                else -> {}
            }
        }
    }

    private fun fetchUserName(): Flow<String?> = flow {
        val name = getUserInfoUseCase.getUserName().first()
        emit(name)
    }

    private fun fetchUserId(): Flow<String?> = flow {
        val id = getUserInfoUseCase.getUserId().first()
        emit(id)
    }

    private fun initWebSocket() {
        webSocketManager.onServerEventReceived = { event: ServerReceive ->
            _serverEvents.postValue(event)
        }

        webSocketManager.onRouteEventReceived = { pathReceive: PathReceive ->
            _pathEvents.postValue(pathReceive)
        }

        webSocketManager.onScheduleEventReceived = { scheduleReceive: ScheduleReceive ->
            val trip = _selectedTrip.value
            trip?.let {
                Timber.d("Receive: $scheduleReceive")
                val sections = convertToSections(scheduleReceive, trip)
                _scheduleSections.postValue(sections)
            }
        }

        webSocketManager.onMemberEventReceived = { memberList: List<Member> ->
            _memberList.postValue(memberList)
        }

        webSocketManager.onAddEventReceived = { addEvent: ScheduleData ->
            _manipulationEvent.postValue(addEvent)
        }

        webSocketManager.onEditEventReceived = {
            _editEvent.postValue(it)
        }
        viewModelScope.launch {
            _userId.value = fetchUserId().first()
        }
    }

    fun initTrip(trip: Trip) {
        _selectedTrip.value = trip
        Timber.d("Selected: ${_selectedTrip.value}")
        initWebSocket()
    }

    fun fetchTrip(tripId: String) {
        viewModelScope.launch {
            // val response = getScheduleUseCase.
        }
    }

    // WebSocket을 통한 메시지 전송
    fun sendMoveEvent(scheduleId: Int, newPosition: Int) {
        viewModelScope.launch {
            val trip = _selectedTrip.value
            trip?.let {
                val event = ServerEvent(
                    "MOVE123",
                    trip.id,
                    Operation("MOVE", scheduleId, newPosition),
                    111231
                )
                webSocketManager.sendMessage(event)
            }
        }
    }

    fun sendDeleteEvent(scheduleId: Int) {
        val trip = _selectedTrip.value
        trip?.let {
            val event = ServerEvent("MOVE123", trip.id, Operation("DELETE", scheduleId, 0), 111231)
            webSocketManager.sendMessage(event)
        }
    }

    fun sendEditEvent(schedule: ScheduleEntity) {
        webSocketManager.sendMessage(
            ManipulationEvent(
                action = "EDIT",
                tripId = schedule.tripId,
                dayOrder = schedule.day,
                timeStamp = 0,
                schedule = schedule
            )
        )

    }

    fun sendAddEvent(schedule: ScheduleEntity) {
        Timber.d(schedule.toString())
        webSocketManager.sendMessage(
            ManipulationEvent(
                action = "ADD",
                tripId = schedule.tripId,
                dayOrder = schedule.day,
                timeStamp = 0,
                schedule = schedule
            )
        )

    }

    fun startConnect() {
        val trip = _selectedTrip.value
        viewModelScope.launch {
            getUserInfoUseCase.getUserId().collect{
                if (it != null) {
                    webSocketManager.id = it
                    trip?.let {
                        webSocketManager.connect(serverUrl + trip.id)
                        webSocketManager.tripId = trip.id
                    }
                }
            }
        }

    }

}
