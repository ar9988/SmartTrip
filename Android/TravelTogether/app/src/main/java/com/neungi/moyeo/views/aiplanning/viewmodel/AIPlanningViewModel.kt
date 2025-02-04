package com.neungi.moyeo.views.aiplanning.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.ApiStatus
import com.neungi.domain.model.Festival
import com.neungi.domain.model.Place
import com.neungi.domain.model.ThemeItem
import com.neungi.domain.usecase.GetFestivalOverview
import com.neungi.domain.usecase.GetRecommendFestivalUseCase
import com.neungi.moyeo.util.CommonUtils
import com.neungi.moyeo.util.EmptyState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
@HiltViewModel
class AIPlanningViewModel @Inject constructor(
    private val getRecommendFestivalUseCase: GetRecommendFestivalUseCase,
    private val GetFestivalOverview:GetFestivalOverview,
    private val regionMapper: RegionMapper
) : ViewModel(),OnAIPlanningClickListener {



    private val _aiDestinatiionUiState = MutableStateFlow<AIPlanningUiState>(AIPlanningUiState())
    val aiDestinatiionUiState = _aiDestinatiionUiState.asStateFlow()

    private val _searchUiState = MutableStateFlow<SearchUiState>(SearchUiState())
    val searchUiState = _searchUiState.asStateFlow()

    private val _aiPlanningUiEvent = MutableSharedFlow<AiPlanningUiEvent>()
    val aiPlanningUiEvent = _aiPlanningUiEvent.asSharedFlow()

    private val _calendarSelectState = MutableStateFlow<Int>(0)
    val calendarSelectState = _calendarSelectState.asStateFlow()

    private val _startDate = MutableStateFlow<LocalDate?>(null)
    val startDate = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<LocalDate?>(null)
    val endDate = _endDate.asStateFlow()

    private val _startTime = MutableStateFlow("오전 10:00")
    val startTime = _startTime.asStateFlow()

    private val _endTime = MutableStateFlow("오후 5:00")
    val endTime = _endTime.asStateFlow()

    private val _uiState = MutableStateFlow(RegionUiState())
    val uiState = _uiState.asStateFlow()

    private val _selectedLocations = MutableStateFlow<List<String>>(emptyList())
    val selectedLocations = _selectedLocations.asStateFlow()

    private val _selectedPlaces = MutableStateFlow<List<String>>(listOf("섭지코지","한라산"))
    val selectedPlaces = _selectedPlaces.asStateFlow()

    private val _recommendFestivals = MutableStateFlow<List<Festival>>(
        emptyList()
    )
    val recommendFestivals = _recommendFestivals.asStateFlow()

    private val _dialogSelectedFestival = MutableStateFlow<Festival?>(null)
    val dialogFestival = _dialogSelectedFestival.asStateFlow()

    private val _placeSearchResult = MutableStateFlow<List<Place>>(listOf(Place("만장굴","동굴"),Place("경복궁","궁궐"),Place("불국사","절")))
    val placeSearchResult = _placeSearchResult.asStateFlow()

    private val _themeList = MutableStateFlow<List<ThemeItem>>(emptyList())
    val themeList = _themeList.asStateFlow()

    private val _selectedTheme = MutableStateFlow<List<String>>(emptyList())
    val selectedThemeList = _selectedTheme.asStateFlow()





    /*
    SelectPeriodFragment
    달력에서 선택한 년월일 포매팅
     */

    val formattedStartDate = startDate.map { date ->
        date?.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
    }.stateIn(
    viewModelScope,
    SharingStarted.Eagerly,
    ""
    )

    val formattedEndDate = endDate.map { date ->
        date?.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ""
    )



    fun updateSelectedDateRange(startDate: LocalDate?, endDate: LocalDate?) {
        viewModelScope.launch {
            _startDate.value = startDate
            _endDate.value = endDate
            Timber.d(_startDate.value.toString())
            if(_startDate.value!=null&&_endDate.value==null){
                _calendarSelectState.value = 1
            }else if(_startDate.value!=null&&_endDate.value!=null){
                _calendarSelectState.value = 2
            }else{
                _calendarSelectState.value = 0

            }
        }
    }
    /*
    SelectPeriodFragment
    선택한 시간 업데이트
     */
    fun updateTime(isStartTime: Boolean, hour: Int, minute: Int) {
        val amPm = if (hour < 12) "오전" else "오후"
        val displayHour = if (hour > 12) hour - 12 else hour
        val timeString = "$amPm ${displayHour}:${String.format("%02d", minute)}"

        if (isStartTime) _startTime.value = timeString
        else _endTime.value = timeString
    }


    /*
    selectLocation
    목적지 선택 토글
     */

    fun toggleLocationSelection(location: String) {
        _selectedLocations.update { currentList ->
            if (currentList.contains(location)) {
                if(currentList.size==1){
                    _aiDestinatiionUiState.update { it.copy( destinationSelectState = EmptyState.EMPTY) }
                    _selectedPlaces.update{ emptyList() }
                }
                currentList - location
            } else {
                _aiDestinatiionUiState.update { it.copy( destinationSelectState = EmptyState.NONE) }
                if (currentList.size < 3) {
                    currentList + location
                } else {
                    viewModelScope.launch {
                        _aiPlanningUiEvent.emit(AiPlanningUiEvent.LimitToast)
                    }
                    currentList
                }
            }
        }
    }


    fun isLocationSelected(location: String): Boolean {
        return selectedLocations.value.contains(location)
    }

    fun clearSelectedLocations() {
        _selectedLocations.value = emptyList()
    }

    /*
    AiDestination
    선택된 관광지(place) 토글
     */

    fun togglePlaceSelection(place: String) {
        _selectedPlaces.update { currentList ->
            if (currentList.contains(place)) {
                currentList - place
            } else {
                if (currentList.size < 3) {
                    currentList + place
                } else {
                    viewModelScope.launch {
                        _aiPlanningUiEvent.emit(AiPlanningUiEvent.LimitToast)
                    }
                    currentList
                }
            }
        }
    }



    //축제 api통신 연결
    fun updateFestivalsByLocation(firstLocation: String) {
        Timber.d("updateFestival!!")
        viewModelScope.launch {
            val result = getRecommendFestivalUseCase(CommonUtils.convertToYYYYMMDD(startDate.value), CommonUtils.convertToYYYYMMDD(endDate.value), regionMapper.getRegionCode(firstLocation))
            Timber.d("${result}")
            when (result.status) {
                ApiStatus.SUCCESS -> {
                    result.data?.let { festivals ->
                        _recommendFestivals.value = festivals.toList()
                    }
                }
                ApiStatus.ERROR -> {
                    Timber.e(result.message)
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

    //추천 축제 선택시 dialog
    fun selectFestival(festival:Festival){
        viewModelScope.launch {
            val result = GetFestivalOverview(festival.contentId)
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
            _dialogSelectedFestival.update {
                newFestival
            }
            _aiPlanningUiEvent.emit(AiPlanningUiEvent.ShowFestivalDialog)
        }


    }

    /*
    AiPlaceSearch
    검색창 텍스트 변경시
     */
    fun onSearchTextChanged(text:String?){
        Timber.d(text.toString())
        if(text.isNullOrBlank()){
            _searchUiState.update { it.copy( searchTextState = EmptyState.EMPTY) }
        }else{
            _searchUiState.update { it.copy( searchTextState = EmptyState.NONE) }
        }
    }



    /*
    UIClickListener
     */
    override fun onClickGoToSelectLocal(){
        viewModelScope.launch {
            _aiPlanningUiEvent.emit(AiPlanningUiEvent.GoToSelectLocal)
        }
    }
    override fun onClickGoToDestination(){
        viewModelScope.launch {
            _aiPlanningUiEvent.emit(AiPlanningUiEvent.GoToDestination)
        }
    }

    override fun onClickGoToSearchPlace() {
        viewModelScope.launch {
            _aiPlanningUiEvent.emit(AiPlanningUiEvent.GoToSearchPlace)
        }
    }

    override fun onClickGoToTheme() {
        viewModelScope.launch {
            _aiPlanningUiEvent.emit(AiPlanningUiEvent.GoToTheme)
        }
    }

    override fun onClickPopBackToDestiination() {
        viewModelScope.launch {
            _aiPlanningUiEvent.emit(AiPlanningUiEvent.PopBackToDestination)
        }
        _searchUiState.update { it.copy( searchTextState = EmptyState.EMPTY) }
    }

    // selectTheme
    fun setThemes(themes: List<ThemeItem>) {
        _themeList.value = themes
    }
    fun toggleThemeSelection(theme: String) {
        _selectedTheme.update { currentList ->
            if (currentList.contains(theme)) {
                currentList - theme
            } else {
                _aiDestinatiionUiState.update { it.copy( destinationSelectState = EmptyState.NONE) }
                if (currentList.size < 3) {
                    currentList + theme
                } else {
                    viewModelScope.launch {
                        _aiPlanningUiEvent.emit(AiPlanningUiEvent.LimitToast)
                    }
                    currentList
                }
            }
        }
    }


}