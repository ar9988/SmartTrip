package com.neungi.moyeo.views.aiplanning.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neungi.domain.model.Festival
import com.neungi.domain.model.Place
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
        listOf(
            Festival("강화도왕방마을얼음축제",
                "https://tong.visitkorea.or.kr/cms/resource/75/2938675_image2_1.jpg",
                "인천광역시 강화군 중앙로787번길 8-1 관리소매점",
                "20241225",
                "20250303"),
            Festival("계양 빛축제",
                "https://tong.visitkorea.or.kr/cms/resource/07/3399307_image2_1.jpg",
                "인천광역시 계양구 경명대로 지하1089 (계산동)",
                "20240923",
                "20250228"),
        ))
    val recommendFestivals = _recommendFestivals.asStateFlow()

    private val _placeSearchResult = MutableStateFlow<List<Place>>(listOf(Place("만장굴","동굴"),Place("경복궁","궁궐"),Place("불국사","절")))
    val placeSearchResult = _placeSearchResult.asStateFlow()



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
                    currentList
                }
            }
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

    override fun onClickPopBackToDestiination() {
        viewModelScope.launch {
            _aiPlanningUiEvent.emit(AiPlanningUiEvent.PopBackToDestiination)
        }
        _searchUiState.update { it.copy( searchTextState = EmptyState.EMPTY) }
    }


}