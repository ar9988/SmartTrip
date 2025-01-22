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
class ScheduleViewModel @Inject constructor(

) : ViewModel() {
    private val _schedules : MutableStateFlow<List<Schedule>> = MutableStateFlow(emptyList())
    val schedules: StateFlow<List<Schedule>> = _schedules

    fun updateschedules(newschedules: List<Schedule>) {
        _schedules.value = newschedules
    }

    fun addSchedule(newSchedule: Schedule) {
        // 기존 리스트에 새로운 Schedule을 추가하고 새로운 리스트로 설정
        _schedules.value = _schedules.value + newSchedule
    }

    fun removeSchedule(ScheduleId: Int) {
        _schedules.value = _schedules.value.filter { it.id != ScheduleId }
    }

//    fun loadScheduleData() {
//        viewModelScope.launch {
//            updateschedules(exampleschedules)  // 리스트 업데이트
//        }
//    }
}

class Schedule {

    val id: Any = 1
}
