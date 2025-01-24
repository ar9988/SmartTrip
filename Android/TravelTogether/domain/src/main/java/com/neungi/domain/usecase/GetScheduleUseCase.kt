package com.neungi.domain.usecase

import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Schedule
import com.neungi.domain.repository.ScheduleRepository
import javax.inject.Inject

class GetScheduleUseCase @Inject constructor(
    private val scheduleRepository : ScheduleRepository
) {
    suspend fun getSchedules(tripId:Int) : ApiResult<List<Schedule>>{
        return scheduleRepository.getSchedulesById(tripId)
    }

    suspend fun rescheduleEventPosition(sId:Int,from: Int,to:Int) : ApiResult<Boolean>{
        return scheduleRepository.updateSchedule()
    }

    suspend fun removeSchedule(schedule: Int) : ApiResult<Void>{
        return scheduleRepository.deleteSchedule(schedule)
    }
}