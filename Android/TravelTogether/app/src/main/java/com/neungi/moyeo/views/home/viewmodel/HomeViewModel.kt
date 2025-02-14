package com.neungi.moyeo.views.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.ApiStatus
import com.neungi.domain.model.Festival
import com.neungi.domain.model.Notification
import com.neungi.domain.model.Place
import com.neungi.domain.model.Trip
import com.neungi.domain.usecase.GetOverviewUseCase
import com.neungi.domain.usecase.GetRecommendFestivalUseCase
import com.neungi.domain.usecase.GetRecommendPlaceUseCase
import com.neungi.domain.usecase.GetTripUseCase
import com.neungi.domain.usecase.PlaceFollowUseCase
import com.neungi.domain.usecase.SaveNotificationUseCase
import com.neungi.moyeo.util.CommonUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRecommendFestivalUseCase: GetRecommendFestivalUseCase,
    private val getOverviewUseCase: GetOverviewUseCase,
    private val saveNotificationUseCase: SaveNotificationUseCase,
    private val getRecommendPlaceUseCase: GetRecommendPlaceUseCase,
    private val placeFollowUseCase: PlaceFollowUseCase,
    private val getTripUseCase: GetTripUseCase
) : ViewModel(),onHomeClickListener {

    private val _homeUiState = MutableStateFlow<HomeUiState>(HomeUiState())
    val homeUiState = _homeUiState.asStateFlow()

    private val _homeUiEvent = MutableSharedFlow<HomeUiEvent>()
    val homeUiEvent = _homeUiEvent.asSharedFlow()

    private val _homeScheduleCardTrip = MutableStateFlow<Trip?>(
        null
    )
    val homeScheduleCardTrip = _homeScheduleCardTrip.asStateFlow()

    private val _recommendPlaces = MutableStateFlow<List<Place>>(emptyList())
    val recommendPlace = _recommendPlaces.asStateFlow()

    private val _placeState =
        MutableStateFlow<ApiResult<List<Place>>>(ApiResult.success(emptyList()))
    val placeState = _placeState.asStateFlow()

    private val _dialogPlace = MutableStateFlow<Place?>(null)
    val dialogPlace = _dialogPlace.asStateFlow()

    private val _recommendFestivals = MutableStateFlow<List<Festival>>(emptyList())
    val recommendFestivals = _recommendFestivals.asStateFlow()


    private val _dialogFestival = MutableStateFlow<Festival?>(null)
    val dialogFestival = _dialogFestival.asStateFlow()

    private val _notificationHistory = MutableStateFlow<List<Notification>>(emptyList())
    val notificationHistory = _notificationHistory.asStateFlow()

    val hasNotification = notificationHistory.map { it.isNotEmpty() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false
        )

    private val _randomSelectedRegion = MutableStateFlow<Pair<String, String>>(Pair("", ""))
    val randomSelectedRegion = _randomSelectedRegion.asStateFlow()

    private val regionCode = mapOf(
        "서울" to "1",
        "인천" to "2",
        "대전" to "3",
        "대구" to "4",
        "광주" to "5",
        "부산" to "6",
        "울산" to "7",
        "세종" to "8",
        "제주도" to "39",
        "경기도" to "31",
        "강원도" to "32",
        "충청북도" to "33",
        "충청남도" to "34",
        "경상북도" to "35",
        "경상남도" to "36",
        "전라북도" to "37",
        "전라남도" to "38"
    )


//    private val _recommendFestivals = MutableStateFlow<List<Festival>>(emptyList())
//    val recommendFestivals = _recommendFestivals.asStateFlow()

//    private val _placeState =
//        MutableStateFlow<ApiResult<List<Festival>>>(ApiResult.success(emptyList()))
//    val festivalState = _festivalState.asStateFlow()

    init{
        getRecommendPlace()
        getFestivalList()
        getLatestTrip()
        getNotification()
    }

    fun getLatestTrip(){
        viewModelScope.launch {
            getTripUseCase.getLatestTrip().collectLatest { result ->
                Timber.d("${result.data}")
                when (result.status) {
                    ApiStatus.SUCCESS -> {
                        _homeScheduleCardTrip.value = result.data
                    }
                    ApiStatus.ERROR -> _homeScheduleCardTrip.value = null
                    ApiStatus.FAIL -> _homeScheduleCardTrip.value = null
                    ApiStatus.LOADING -> _homeScheduleCardTrip.value = null
                }

            }
        }

    }

    fun getFestivalList() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val endDate = today.plusDays(30)
            Timber.d("getFestivalList started")
            getRecommendFestivalUseCase(
                CommonUtils.convertToYYYYMMDDwithHyphen(today),
                CommonUtils.convertToYYYYMMDDwithHyphen(endDate),
                "-1"
            ).collectLatest { result ->
                Timber.d("Festival result: $result")
                _homeUiState.update { currentState ->
                    when (result.status) {
                        ApiStatus.SUCCESS -> {
                            _recommendFestivals.value = result.data?: emptyList()
                            currentState.copy(
                                festivals = result.data ?: emptyList(),
                                isLoading = false
                            )
                        }
                        ApiStatus.LOADING -> currentState.copy(isLoading = true)
                        else -> currentState.copy(
                            error = "축제 정보를 불러오는데 실패했습니다",
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun selectFestival(festival:Festival){
        viewModelScope.launch {
            val result = getOverviewUseCase(festival.contentId)
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



    //추천 지역 정보
    fun getRecommendPlace() {
        _randomSelectedRegion.update {
            regionCode.entries.random().let {
                Pair(it.key, it.value)
            }
        }
        viewModelScope.launch {
            Timber.d("RandomRegionNumber${randomSelectedRegion.value.second}")
            getRecommendPlaceUseCase(randomSelectedRegion.value.second).collectLatest { result ->
                _homeUiState.update { currentState ->
                    when (result.status) {
                        ApiStatus.SUCCESS -> {
                            val places = result.data ?: emptyList()
                            Timber.d("받은 일정! ${places}")
                            _recommendPlaces.value = places
                            currentState.copy(
                                places = places,
                                isLoading = false
                            )

                        }
                        ApiStatus.LOADING -> currentState.copy(isLoading = true)
                        else -> currentState.copy(
                            error = "장소 정보를 불러오는데 실패했습니다",
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun selectPlace(place:Place){
        viewModelScope.launch {
            val result = getOverviewUseCase(place.contentId)
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
            val newFestival = Place(
                placeName = place.placeName,
                imageUrl = place.imageUrl,
                address = place.address,
                contentId = place.contentId,
                overView = overView,
                isFollowed = place.isFollowed,
                lat = null,
                lng = null
            )
            _dialogPlace.update {
                newFestival
            }
            _homeUiEvent.emit(HomeUiEvent.ShowPlaceDialog)
        }
    }

    override fun onClickFollow(contentId:String){
        viewModelScope.launch {
            placeFollowUseCase(contentId).collect(){result->
                when(result.status){
                    ApiStatus.SUCCESS -> {
                        if (result.data == true) {
                            // Dialog 업데이트
                            _dialogPlace.update { currentPlace ->
                                currentPlace?.copy(isFollowed = !currentPlace.isFollowed)
                            }

                            // RecommendPlaces 업데이트
                            _homeUiState.update { currentState ->
                                val updatedPlaces = currentState.places.map { place ->
                                    if (place.contentId == contentId) {
                                        place.copy(isFollowed = !place.isFollowed)
                                    } else {
                                        place
                                    }
                                }
                                currentState.copy(places = updatedPlaces)
                            }
                        }
                    }
                    ApiStatus.ERROR ->{}
                    ApiStatus.FAIL -> {}
                    ApiStatus.LOADING -> {}
                }
            }
        }
    }



    //Notification내역 가져오기
    fun getNotification(){
        viewModelScope.launch {
            saveNotificationUseCase.getNotification().collect { notificationList ->
                Timber.d("notification : $notificationList")
                _notificationHistory.update { notificationList }
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

    override fun onClickTripCard() {
        viewModelScope.launch {
            _homeUiEvent.emit(HomeUiEvent.GoToPlanDetail)
        }
    }








}