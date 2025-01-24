package com.neungi.moyeo.views.plan.tripviewmodel

import androidx.lifecycle.ViewModel
import com.neungi.moyeo.util.MarkerData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class TripViewModel @Inject constructor(


): ViewModel(){

    private val _tripUiState = MutableStateFlow<TripUiState>(TripUiState())
    val tripUiState = _tripUiState.asStateFlow()


    private val _tripUiEvent = MutableSharedFlow<TripUiEvent>()
    val tripUiEvent = _tripUiEvent.asSharedFlow()

    private val _trips = MutableStateFlow<List<TripData>>(emptyList())
    val trips = _trips.asStateFlow()

    init {
        val newTrips = mutableListOf<TripData>()
        newTrips.add(TripData(0,"제주도","1231-111","3명"))
        newTrips.add(TripData(1,"강원","1231-111","4명"))
        newTrips.add(TripData(2,"서울","1231-111","2명"))
        newTrips.add(TripData(3,"충청","1231-111","5명"))
        newTrips.add(TripData(4,"평창","1231-111","3명"))
        _trips.value = newTrips.toList()
    }
}