package com.neungi.moyeo.views.aiplanning.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neungi.domain.model.AiPlanningRequest
import com.neungi.domain.model.ApiStatus
import com.neungi.domain.model.Festival
import com.neungi.domain.model.LocationCategory
import com.neungi.domain.model.Place
import com.neungi.domain.model.Preferences
import com.neungi.domain.model.ThemeItem
import com.neungi.domain.usecase.GetFollowedPlacesUseCase
import com.neungi.domain.usecase.GetOverviewUseCase
import com.neungi.domain.usecase.GetRecommendFestivalUseCase
import com.neungi.domain.usecase.GetUserInfoUseCase
import com.neungi.domain.usecase.RqeuestAiPlanningUseCase
import com.neungi.moyeo.R
import com.neungi.moyeo.util.CommonUtils
import com.neungi.moyeo.util.EmptyState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
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
    private val getOverviewUseCase:GetOverviewUseCase,
    private val requestAiPlanningUseCase: RqeuestAiPlanningUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val regionMapper: RegionMapper
) : ViewModel(),OnAIPlanningClickListener {


    private val regions = mapOf(
        "특별" to R.array.local_special,
        "경기" to R.array.local_gyeonggi,
        "강원" to R.array.local_gangwon,
        "경북" to R.array.local_gyeongbuk,
        "경남" to R.array.local_gyeongnam,
        "전북" to R.array.local_jeonbuk,
        "전남" to R.array.local_jeonnam,
        "충북" to R.array.local_chungbuk,
    )


    private val _aiDestinatiionUiState = MutableStateFlow<AIPlanningUiState>(AIPlanningUiState())
    val aiDestinatiionUiState = _aiDestinatiionUiState.asStateFlow()



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

    private val _locationCategoryState = MutableStateFlow<List<LocationCategory>>(emptyList())
    val locationCategoryState = _locationCategoryState.asStateFlow()

    private val _selectedLocalTab = MutableStateFlow<String>("특별")
    val selectedLocalTab = _selectedLocalTab.asStateFlow()

    private val _selectedLocations = MutableStateFlow<List<String>>(emptyList())
    val selectedLocations = _selectedLocations.asStateFlow()

    private val _selectedPlaces = MutableStateFlow<List<String>>(emptyList())
    val selectedPlaces = _selectedPlaces.asStateFlow()

    private val _recommendFestivals = MutableStateFlow<List<Festival>>(
        emptyList()
    )
    val recommendFestivals = _recommendFestivals.asStateFlow()

    private val _dialogSelectedFestival = MutableStateFlow<Festival?>(null)
    val dialogFestival = _dialogSelectedFestival.asStateFlow()

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

    fun selectLocalTab(categoryName:String){
        _locationCategoryState.update { currentCategories ->
            currentCategories.map { category ->
                category.copy(isSelected = category.name == categoryName)
            }
        }
        _selectedLocalTab.update {
            categoryName
        }
    }

    fun setCategories(categories: List<String>) {
        val locationCategories = categories.mapIndexed { index, name ->
            LocationCategory(name = name, isSelected = index == 0)
        }
        Timber.d(locationCategories.toString())
        _locationCategoryState.update { locationCategories }
    }

    fun getRegionCategoryId(category: String):Int{
        return regions[category]?:1
    }

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
        Timber.d("updateFestival!!"+regionMapper.getRegionCode(firstLocation))
        viewModelScope.launch {
            val resultFlow = getRecommendFestivalUseCase(CommonUtils.convertToYYYYMMDDwithHyphen(startDate.value), CommonUtils.convertToYYYYMMDDwithHyphen(endDate.value), regionMapper.getRegionCode(firstLocation))

            resultFlow.collectLatest { result ->
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
    }

    //추천 축제 선택시 dialog
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
            _dialogSelectedFestival.update {
                newFestival
            }
            _aiPlanningUiEvent.emit(AiPlanningUiEvent.ShowFestivalDialog)
        }


    }
    
    //AiPlannig 요청
    fun requestAiPlanning(){
        viewModelScope.launch {
            _aiPlanningUiEvent.emit(AiPlanningUiEvent.RequestPlanning)
            getUserId().first(){userId ->
                val requestBody = AiPlanningRequest(
                    userId = userId!!,
                    startDate = CommonUtils.convertToyyyyMMdd(_startDate.value),
                    startTime = _startTime.value,
                    endDate =  CommonUtils.convertToyyyyMMdd(_endDate.value),
                    endTime = _endTime.value,
                    destination = selectedLocations.value,
                    preferences = Preferences(
                        places = selectedPlaces.value,
                        theme = selectedThemeList.value
                    )
                )
                val response = requestAiPlanningUseCase(requestBody)
                val status = response.status
                val data = response.data
                clearDatas()
                when (status == ApiStatus.SUCCESS && data != null) {
                    true -> {
                        Timber.d("성공?")
                    }

                    else -> {
                        Timber.d("실패 "+response.toString())
                    }
                }
                true

            }
        }

    }



    private fun clearDatas() {
        _calendarSelectState.value = 0
        _startDate.value = null
        _endDate.value = null
        _startTime.value  = "오전 10:00"
        _endTime.value = "오후 5:00"
        _selectedLocations.value = emptyList()
        _selectedPlaces.value = emptyList()
        _recommendFestivals.value = emptyList()
        _dialogSelectedFestival.value = null
        _selectedTheme.value = emptyList()
    }

    fun getUserId(): Flow<String?> = flow {
        val id = getUserInfoUseCase.getUserId().first()
        emit(id)
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




    fun clearState(){
        viewModelScope.launch {
            _aiDestinatiionUiState.update { AIPlanningUiState() }
            _calendarSelectState.update { 0 }
            _startDate.update { null }
            _endDate.update { null }
            _startTime.update { DEFAULT_START_TIME }
            _endTime.update { DEFAULT_END_TIME }
            _uiState.update { RegionUiState() }
            _selectedLocalTab.update { DEFAULT_SELECTED_LOCAL_TAB }
            _selectedLocations.update { emptyList() }
            _selectedPlaces.update { emptyList() }
            _recommendFestivals.update { emptyList() }
            _dialogSelectedFestival.update { null }
            _selectedTheme.update { emptyList() }
        }
    }

    companion object {
        private val DEFAULT_START_TIME = "오전 10:00"
        private val DEFAULT_END_TIME = "오후 5:00"
        private val DEFAULT_SELECTED_LOCAL_TAB = "특별"
    }


}