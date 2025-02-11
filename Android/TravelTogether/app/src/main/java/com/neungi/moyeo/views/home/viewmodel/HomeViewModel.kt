package com.neungi.moyeo.views.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neungi.domain.model.ApiStatus
import com.neungi.domain.model.Festival
import com.neungi.domain.model.Notification
import com.neungi.domain.model.Trip
import com.neungi.domain.usecase.GetFestivalOverview
import com.neungi.domain.usecase.GetRecommendFestivalUseCase
import com.neungi.domain.usecase.SaveNotificationUseCase
import com.neungi.moyeo.util.CommonUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRecommendFestivalUseCase: GetRecommendFestivalUseCase,
    private val getFestivalOverview: GetFestivalOverview,
    private val saveNotificationUseCase: SaveNotificationUseCase
) : ViewModel(),onHomeClickListener {

    private val _homeUiState = MutableStateFlow<HomeUiState>(HomeUiState())
    val homeUiState = _homeUiState.asStateFlow()

    private val _homeUiEvent = MutableSharedFlow<HomeUiEvent>()
    val homeUiEvent = _homeUiEvent.asSharedFlow()

    private val _homeScheduleCardTrip = MutableStateFlow<Trip?>(
        null
    )
    val homeScheduleCardTrip = _homeScheduleCardTrip.asStateFlow()

    private val _recommendFestivals = MutableStateFlow<List<Festival>>(emptyList())
    val recommendFestivals = _recommendFestivals.asStateFlow()

    private val _dialogFestival = MutableStateFlow<Festival?>(null)
    val dialogFestival = _dialogFestival.asStateFlow()

    private val _notificationHistory = MutableStateFlow<List<Notification>>(emptyList())
    val notificationHistory = _notificationHistory.asStateFlow()

    init{
        getFestivalList()
    }

    fun getFestivalList(){
        viewModelScope.launch {
            val today = LocalDate.now()
            val endDate = today.plusDays(30)
            Timber.d("?!?!?!??!!??")
            val result = getRecommendFestivalUseCase(CommonUtils.convertToYYYYMMDDwithHyphen(today), CommonUtils.convertToYYYYMMDDwithHyphen(endDate), "-1")
            Timber.d("홈 추천 축제 : ${result}")
            when (result.status) {
                ApiStatus.SUCCESS -> {
                    result.data?.let { festivals ->
                        _recommendFestivals.value = festivals.toList()
                    }
                }
                ApiStatus.ERROR -> {
                    Timber.e("errormsg : "+result.message)
                    _recommendFestivals.value = emptyList()
                }
                ApiStatus.FAIL -> {
                    _recommendFestivals.value = emptyList()
                }
                ApiStatus.LOADING -> {
                    // 로딩 상태 처리가 필요한 경우
                }
            }

        }

    }

    fun selectFestival(festival:Festival){
        viewModelScope.launch {
            val result = getFestivalOverview(festival.contentId)
            Timber.d("${result}")
            val overView : String = when (result.status) {
                ApiStatus.SUCCESS -> {
                    result.data?:"정보가 없습니다"
                }
                ApiStatus.ERROR -> {
                    "정보가 없습니다"
                }
                ApiStatus.FAIL -> {
                    "정보가 없습니다"
                }
                ApiStatus.LOADING -> {
                    ""
                }
            }
            val newFestival = Festival(
                title = festival.title,
                imageUrl = festival.imageUrl,
                address = festival.address,
                startDate = festival.startDate,
                endDate = festival.endDate,
                overView = overView,
                contentId = festival.contentId

            )
            _dialogFestival.update {
                newFestival
            }
            _homeUiEvent.emit(HomeUiEvent.ShowFestivalDialog)
        }


    }
    //Notification내역 가져오기
    fun getNotification(){
        viewModelScope.launch {
            saveNotificationUseCase.getNotifiaacion().first() {notificationLsit ->
                _notificationHistory.update { notificationLsit }
                true
            }
        }
    }

    fun deleteNotification(id:String){
        viewModelScope.launch {
            saveNotificationUseCase.deleteNotification(id)
            getNotification()
        }
    }

    override fun onClickToNotifcation() {
        viewModelScope.launch {
            _homeUiEvent.emit(HomeUiEvent.GoToNotification)
        }
    }

    override fun onClickNotificationClear(){
        viewModelScope.launch {
            saveNotificationUseCase.clearNotification()
            getNotification()
        }

    }


}