package com.neungi.moyeo.views.plan.tripviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.neungi.domain.model.ApiStatus
import com.neungi.domain.model.Trip
import com.neungi.domain.usecase.GetInviteUseCase
import com.neungi.domain.usecase.GetTripUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TripViewModel @Inject constructor(
    private val getTripUseCase: GetTripUseCase,
    private val getInviteUseCase: GetInviteUseCase
): ViewModel(){

    private val _tripUiState = MutableStateFlow<TripUiState>(TripUiState())
    val tripUiState = _tripUiState.asStateFlow()

    private val _tripUiEvent = MutableSharedFlow<TripUiEvent>()
    val tripUiEvent = _tripUiEvent.asSharedFlow()

    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips = _trips.asStateFlow()

    lateinit var trip:Trip

    private val _selectedTrip = MutableStateFlow<Trip?>(null)
    val selectedTrip = _selectedTrip.asStateFlow()

    fun initTrip(trip: Trip) {
        _selectedTrip.value = trip
    }

    private fun makeRequestBody(token: String): RequestBody {
        val metadata = mapOf(
            "token" to token
        )
        val json = Gson().toJson(metadata)
        return json.toRequestBody("application/json".toMediaTypeOrNull())
    }

    fun requestInvite(token: String) {
        viewModelScope.launch {
            val response = getInviteUseCase.inviteAccept(makeRequestBody(token))
            when (response.status) {
                ApiStatus.SUCCESS -> {
                    Timber.d("Data: ${response.data}")
                    val message = response.data?.first ?: ""
                    val tripId = response.data?.second ?: -1
                    _tripUiEvent.emit(TripUiEvent.TripInviteSuccess(message, tripId))
                }

                else -> {
                    val message = response.data?.first ?: ""
                    _tripUiEvent.emit(TripUiEvent.TripInviteFail(message))
                }
            }
        }
    }

    fun deleteTrip(trip: Trip){

    }

    fun getTrips(userId :String){
        viewModelScope.launch {
            val result = getTripUseCase.getTrips(userId)
            Timber.d(result.toString())
            when (result.status) {
                ApiStatus.SUCCESS -> {
                    // result.data가 null이 아니고 List<Trip>으로 캐스팅 가능한지 확인
                    val tripList = result.data as? List<Trip>
                    if (tripList != null) {
                        _trips.value = result.data!!
                    } else {
                        _trips.value = emptyList() // data가 null이거나 List<Trip>이 아닌 경우 처리
                    }
                }
                ApiStatus.ERROR -> {
                    // ApiResult.Status가 ERROR일 때 처리
                    // 예: UI 상태 변경 또는 에러 메시지 표시
                    // 예시: _tripUiState.value = TripUiState(errorMessage = result.message)
                }
                ApiStatus.FAIL -> {
                    // ApiResult.Status가 FAIL일 때 처리
                    // 예시: _tripUiState.value = TripUiState(failed = true)
                }
                ApiStatus.LOADING -> {
                    // ApiResult.Status가 LOADING일 때 처리
                    // 예시: _tripUiState.value = TripUiState(loading = true)
                }
            }
        }
    }

    fun getTrip(tripId: Int) {
        _trips.value.forEach { nowTrip ->
            if (nowTrip.id == tripId) {
                _selectedTrip.value = nowTrip
                Timber.d("Selected Trip: ${_selectedTrip.value}")
            }
        }
    }
}