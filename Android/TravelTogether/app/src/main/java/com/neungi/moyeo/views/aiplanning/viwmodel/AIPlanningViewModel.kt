package com.neungi.moyeo.views.aiplanning.viwmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.geometry.LatLng
import com.neungi.moyeo.util.MarkerData
import com.neungi.moyeo.views.album.viewmodel.AlbumUiEvent
import com.neungi.moyeo.views.album.viewmodel.AlbumUiState
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

) : ViewModel() {


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
                currentList - location
            } else {
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




}