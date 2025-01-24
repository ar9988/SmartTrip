package com.neungi.moyeo.views.plan.tripviewmodel

import androidx.lifecycle.ViewModel
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

}