package com.neungi.moyeo.views.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class TripViewModel @Inject constructor(

): ViewModel(){
//    private val _trips :MutableStateFlow<List<TripEntity>> = MutableStateFlow(emptyList())
//    val trips:StateFlow<List<TripEntity>> = _trips
//
//    fun updateTrips(newTrips: List<TripEntity>) {
//        _trips.value = newTrips
//    }
//
//    fun addTrip(newTrip: TripEntity) {
//        // 기존 리스트에 새로운 Trip을 추가하고 새로운 리스트로 설정
//        _trips.value = _trips.value + newTrip
//    }
//
//    fun removeTrip(tripId: Int) {
//        _trips.value = _trips.value.filter { it.id != tripId }
//    }
//
//    fun loadTripData() {
//        viewModelScope.launch {
//            // 예시로 TripEntity 리스트를 생성
//            val exampleTrips = listOf(
//                TripEntity(
//                    id = 1, title = "Trip to Bali", startDate = Date(),
//                    endDate = TODO(),
//                    thumbnail = TODO(),
//                    memberCount = TODO(),
//                    status = TODO(),
//                    createdAt = TODO()
//                ),
//                TripEntity(
//                    id = 2, title = "Trip to Paris", startDate = Date(),
//                    endDate = TODO(),
//                    thumbnail = TODO(),
//                    memberCount = TODO(),
//                    status = TODO(),
//                    createdAt = TODO()
//                )
//            )
//            updateTrips(exampleTrips)  // 리스트 업데이트
//        }
//    }
}